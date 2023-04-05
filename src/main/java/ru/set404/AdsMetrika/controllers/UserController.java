package ru.set404.AdsMetrika.controllers;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.set404.AdsMetrika.dto.*;
import ru.set404.AdsMetrika.exceptions.OAuthCredentialEmptyException;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.models.Settings;
import ru.set404.AdsMetrika.models.Stat;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.security.UserDetails;
import ru.set404.AdsMetrika.services.*;
import ru.set404.AdsMetrika.network.Network;
import ru.set404.AdsMetrika.util.CredentialsValidator;
import ru.set404.AdsMetrika.util.SettingsValidator;
import ru.set404.AdsMetrika.util.StatisticsUtilities;

import javax.validation.Valid;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class UserController {
    private final NetworksService networksService;
    private final StatsService statsService;
    private final CredentialsService credentialsService;
    private final GoogleSpreadSheetService googleSpreadSheetService;
    private final SettingsService settingsService;
    private final CredentialsValidator credentialsValidator;
    private final SettingsValidator settingsValidator;
    protected Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    public UserController(NetworksService networksService, StatsService statsService, CredentialsService credentialsService,
                          GoogleSpreadSheetService googleSpreadSheetService, SettingsService settingsService, CredentialsValidator credentialsValidator,
                          SettingsValidator settingsValidator) {
        this.networksService = networksService;
        this.statsService = statsService;
        this.credentialsService = credentialsService;
        this.googleSpreadSheetService = googleSpreadSheetService;
        this.settingsService = settingsService;
        this.credentialsValidator = credentialsValidator;
        this.settingsValidator = settingsValidator;
    }

    @GetMapping("/statistics")
    public String index(@RequestParam(value = "ds", required = false) LocalDate dateStart,
                        @RequestParam(value = "de", required = false) LocalDate dateEnd, Model model) {

        User currentUser = getUser();
        List<TableDTO> tableStats = new ArrayList<>();
        List<Stat> oldStats = new ArrayList<>();
        List<ChartDTO> chartStats = new ArrayList<>();
        try {
            if (dateStart != null) {
                tableStats = getStatsForTables(currentUser, dateStart, dateEnd);
                model.addAttribute("success", "Success");
            }
            final int COUNT_DAYS_IN_CHART = 6;
            oldStats = statsService.getStatsList(currentUser, LocalDate.now().minusDays(COUNT_DAYS_IN_CHART));
            chartStats = StatisticsUtilities.convertToChartDTOList(oldStats);
        } catch (Exception e) {
            logger.error("method index exception with user - %s message - %s".formatted(getUser().getUsername(), e.getMessage()));
            model.addAttribute("error", e.getMessage());
        }

        TableDTO combinedStats = StatisticsUtilities.combineTableDTO(tableStats);

        List<String> favoriteOffers = List.of(settingsService.userSettings(currentUser).getAdcomboId().split(","));
        List<StatDTO> favoriteStatDTO = combinedStats.getCurrentStats().stream()
                .filter(statDTO -> favoriteOffers.contains(String.valueOf(statDTO.getCampaignId()))).toList();

        populateCredentialsWithModel(model);
        model.addAttribute("settings", settingsService.userSettings(currentUser));
        model.addAttribute("currentDate", LocalDate.now());

        //Favorite offers statistics
        model.addAttribute("favorite", favoriteStatDTO);

        //Table statistics
        model.addAttribute("statistics", tableStats);
        model.addAttribute("combinedStats", combinedStats);

        //column_line_chart
        model.addAttribute("chartTotal", StatisticsUtilities.getTotalChartDTO(oldStats));
        model.addAttribute("chartCosts", chartStats.stream().map(ChartDTO::getSpend).toList());
        model.addAttribute("chartRevenue", chartStats.stream().map(ChartDTO::getRevenue).toList());
        model.addAttribute("chartDates", chartStats.stream().map(ChartDTO::getCreatedDate).toList());

        //donut-chart
        model.addAttribute("totalSpendByNetwork", StatisticsUtilities.getTotalChartSpend(oldStats));
        return "user/statistics";
    }

    @GetMapping("/report")
    public String report(@RequestParam(value = "type", required = false) String type,
                         @RequestParam(value = "date", required = false) LocalDate date, Model model) {
        String headerText = "Choose a date";
        TableDTO combinedStats = new TableDTO();

        User currentUser = getUser();

        try {
            if (type != null && type.equals("month")) {
                LocalDate firstDay = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
                LocalDate lastDay = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
                List<TableDTO> tableStats = getStatsForTables(currentUser, firstDay, lastDay);
                combinedStats = StatisticsUtilities.combineTableDTO(tableStats);
                headerText = firstDay + " - " + lastDay;
                model.addAttribute("success", "Success");
            } else if (type != null && type.equals("daily") && date != null) {
                List<TableDTO> tableStats = getStatsForTables(currentUser, date, date);
                combinedStats = StatisticsUtilities.convertForSingleTable(tableStats);
                if (settingsService.userSettings(currentUser).isSpreadSheetEnabled()
                        && date.equals(LocalDate.now().minusDays(1))) {

                    googleSpreadSheetService.writeSpreadSheetTable("", currentUser, combinedStats, date);
                    model.addAttribute("success", "Success");
                }
                headerText = date.toString();
            }

        } catch (OAuthCredentialEmptyException e) {
            model.addAttribute("notAuthorized", e.getMessage());
        } catch (Exception e) {
            logger.error("method report exception with user - %s message - %s".formatted(getUser().getUsername(), e.getMessage()));
            model.addAttribute("error", e.getMessage());
        }

        populateCredentialsWithModel(model);
        model.addAttribute("settings", settingsService.userSettings(currentUser));
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("dates", headerText);
        model.addAttribute("combinedStats", combinedStats);
        return "user/report";
    }

    @GetMapping("/Callback")
    public String googleAuth(@RequestParam(value = "code") String code, Model model) {
        String headerText = "Choose a date";

        User currentUser = getUser();
        LocalDate date = LocalDate.now().minusDays(1);

        List<TableDTO> tableStats = getStatsForTables(currentUser, date, date);
        TableDTO combinedStats = StatisticsUtilities.convertForSingleTable(tableStats);

        populateCredentialsWithModel(model);
        model.addAttribute("settings", settingsService.userSettings(currentUser));
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("dates", headerText);
        model.addAttribute("combinedStats", combinedStats);
        try {
            googleSpreadSheetService.writeSpreadSheetTable(code, currentUser, combinedStats, date);
        } catch (OAuthCredentialEmptyException e) {
            model.addAttribute("error", "Permission denied");
        }
        if (!model.containsAttribute("error"))
            model.addAttribute("success", "success");
        return "user/report";
    }

    @GetMapping("/campaigns")
    public String campaigns(@RequestParam(value = "ds", required = false) LocalDate dateStart,
                            @RequestParam(value = "de", required = false) LocalDate dateEnd,
                            @RequestParam(value = "network", required = false) Network network,
                            Model model) {
        List<StatDTO> campaignStats = new ArrayList<>();
        String headerText = "Choose a date";

        User currentUser = getUser();

        try {
            if (dateStart != null && dateEnd != null) {
                if (currentUser.getRole().equals("ROLE_GUEST"))
                    campaignStats = networksService.getCampaignsStatisticsListMock(network);
                else
                    campaignStats = networksService.getCampaignStats(currentUser, network, dateStart, dateEnd);
                headerText = dateStart + " - " + dateEnd;
                model.addAttribute("success", "Success");
            }
        } catch (Exception e) {
            logger.error("method campaigns exception with user - %s message - %s".formatted(getUser().getUsername(), e.getMessage()));
            model.addAttribute("error", e.getMessage());
        }

        populateCredentialsWithModel(model);
        model.addAttribute("settings", settingsService.userSettings(currentUser));
        model.addAttribute("dates", headerText);
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("combinedStats", campaignStats);
        return "user/campaigns";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        populateCredentialsWithModel(model);
        model.addAttribute("settings", settingsService.userSettings(getUser()));

        return "user/settings";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@ModelAttribute("settings") @Valid Settings settings, BindingResult bindingResult) {
        settingsValidator.validate(settings, bindingResult);
        if (bindingResult.hasErrors()) {
            return "redirect:/settings?error";
        }
        settingsService.update(settings, getUser());
        return "redirect:/settings?success";
    }

    @PostMapping("/credentials/save")
    public String saveCredentials(@ModelAttribute("credentialsADCOMBO") @Valid Credentials credentials,
                                  BindingResult bindingResult) {
        if ((credentials.getUsername().isEmpty())) {
            if (credentials.getId() != 0)
                credentialsService.remove(credentials);
        } else {

            credentialsValidator.validate(credentials, bindingResult);
            if (bindingResult.hasErrors())
                return "redirect:/statistics?password_error";
            credentialsService.save(credentials, getUser());
        }
        return "redirect:/statistics";
    }

    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetails) authentication.getPrincipal()).user();
    }

    private List<TableDTO> getStatsForTables(User user, LocalDate dateStart, LocalDate dateEnd) {
            List<TableDTO> tableStats;
            if (user.getRole().equals("ROLE_GUEST"))
                tableStats = networksService.getNetworkStatisticsListMock(user);
            else
                tableStats = networksService.getOfferStats(user, dateStart, dateEnd);
            if (dateStart.equals(dateEnd))
                statsService.saveStatDTOList(tableStats, user, dateStart);
        return tableStats;
    }

    private void populateCredentialsWithModel(Model model) {
        Map<Network, CredentialsDTO> credentials = credentialsService.getUserCredentialsList(getUser()).stream()
                .collect(Collectors.toMap(CredentialsDTO::getNetworkName, Function.identity()));
        if (!credentials.containsKey(Network.ADCOMBO) || (credentials.containsKey(Network.ADCOMBO) &&
                credentials.size() < 2))
            model.addAttribute("checkAPI", "Add API for Adcombo and ad network");

        for (Network network : Network.values()) {
            model.addAttribute("credentials" + network.name().toUpperCase(),
                    credentials.getOrDefault(network, new CredentialsDTO()));
        }

        model.addAttribute("username", getUser().getUsername());
    }
}
