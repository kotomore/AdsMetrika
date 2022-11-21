package ru.set404.AdsMetrika.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
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
import ru.set404.AdsMetrika.util.OfferListDTOValidator;
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
    private final OffersService offersService;
    private final StatsService statsService;
    private final CredentialsService credentialsService;
    private final ScheduledService scheduledService;
    private final SettingsService settingsService;
    private final CredentialsValidator credentialsValidator;
    private final OfferListDTOValidator offerListDTOValidator;


    @Autowired
    public UserController(NetworksService networksService, OffersService offersService, StatsService statsService,
                          CredentialsService credentialsService, ScheduledService scheduledService, SettingsService settingsService, CredentialsValidator credentialsValidator, OfferListDTOValidator offerListDTOValidator) {
        this.networksService = networksService;
        this.offersService = offersService;
        this.statsService = statsService;
        this.credentialsService = credentialsService;
        this.scheduledService = scheduledService;
        this.settingsService = settingsService;
        this.credentialsValidator = credentialsValidator;
        this.offerListDTOValidator = offerListDTOValidator;
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
            }
            final int COUNT_DAYS_IN_CHART = 7;
            oldStats = statsService.getStatsList(currentUser, LocalDate.now().minusDays(COUNT_DAYS_IN_CHART));
            chartStats = StatisticsUtilities.convertToChartDTOList(oldStats);
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        TableDTO combinedStats = StatisticsUtilities.combineTableDTO(tableStats);

        putCredentialsInModel(model);
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("statistics", tableStats);
        model.addAttribute("combinedStats", combinedStats);

        //column_line_chart
        model.addAttribute("chartTotal", StatisticsUtilities.getTotalChartDTO(oldStats));
        model.addAttribute("chartCosts", chartStats.stream().map(ChartDTO::getSpend).toList());
        model.addAttribute("chartRevenue", chartStats.stream().map(ChartDTO::getRevenue).toList());
        model.addAttribute("chartDates", chartStats.stream().map(ChartDTO::getCreatedDate).toList());

        //donut-chart
        model.addAttribute("totalSpendByNetwork", StatisticsUtilities.getTotalChartSpend(oldStats));

        return "user/index";
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
            } else if (type != null && type.equals("daily") && date != null) {
                List<TableDTO> tableStats = getStatsForTables(currentUser, date, date);
                combinedStats = StatisticsUtilities.convertForSingleTable(tableStats);
                if (settingsService.userSettings(currentUser).isSpreadSheetEnabled()) {
                    scheduledService.writeSpreadSheetTable(currentUser, combinedStats, date);
                    model.addAttribute("success", "success");
                }
                headerText = date.toString();
            }

        } catch (OAuthCredentialEmptyException e) {
            model.addAttribute("notAuthorized", e.getMessage());
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        putCredentialsInModel(model);
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("dates", headerText);
        model.addAttribute("combinedStats", combinedStats);
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
                    campaignStats = networksService.getNetworkStatisticsListMock(network);
                else
                    campaignStats = networksService.getCampaignStats(currentUser, network, dateStart, dateEnd);
                headerText = dateStart + " - " + dateEnd;
            }
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
        }

        putCredentialsInModel(model);
        model.addAttribute("dates", headerText);
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("combinedStats", campaignStats);

        return "user/campaigns";
    }

    @GetMapping("/offers")
    public String offers(Model model) {
        OfferListDTO offerForm = new OfferListDTO();
        final int COUNT_EMPTY_FIELDS_FOR_OFFER = 7;
        for (int i = 1; i <= COUNT_EMPTY_FIELDS_FOR_OFFER; i++) {
            offerForm.addOffer(new OfferDTO());
        }

        putCredentialsInModel(model);
        model.addAttribute("blankForm", offerForm);
        model.addAttribute("form", offersService.getOfferListDTO(getUser()));
        return "user/offers";
    }

    @GetMapping("/settings")
    public String settings(Model model) {
        putCredentialsInModel(model);
        model.addAttribute("settings", settingsService.userSettings(getUser()));

        return "user/settings";
    }

    @GetMapping("/offers/{id}/delete")
    public String delete(@PathVariable("id") int id) {
        offersService.deleteById(getUser(), id);
        return "redirect:/offers?success";
    }

    @PostMapping("/offers/edit")
    public String editOffers(@ModelAttribute("form") OfferListDTO offerDTOS, BindingResult bindingResult) {
        offerListDTOValidator.validate(offerDTOS, bindingResult);
        if (bindingResult.hasErrors())
            return "redirect:/offers?error";

        offersService.saveOffersDTOList(offerDTOS, getUser());
        return "redirect:/offers?success";
    }

    @PostMapping("/offers/save")
    public String saveOffers(@ModelAttribute("blankForm") OfferListDTO offerDTOS, BindingResult bindingResult) {
        offerListDTOValidator.validate(offerDTOS, bindingResult);
        if (bindingResult.hasErrors())
            return "redirect:/offers?error";

        offersService.saveOffersDTOList(offerDTOS, getUser());
        return "redirect:/offers?success";
    }

    @PostMapping("/settings/update")
    public String updateSettings(@ModelAttribute("settings") @Valid Settings settings) {
        settingsService.update(settings, getUser());
        return "redirect:/statistics";
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

    @Scheduled(cron = "0 0 11 * * *", zone = "Europe/Moscow")
    public void scheduleTask() {
        LocalDate date = LocalDate.now().minusDays(1);

        List<Settings> settingsList = settingsService.findSettingsWithScheduledTask();
        for (Settings settings : settingsList) {
            User user = settings.getOwner();
            List<TableDTO> tableStats = getStatsForTables(user, date, date);
            TableDTO combinedStats = StatisticsUtilities.combineTableDTO(tableStats);

            if (user.getSettings().isTelegramEnabled())
                scheduledService.sendTelegramMessage(user, combinedStats);
            if (user.getSettings().isSpreadSheetScheduleEnabled())
                scheduledService.writeSpreadSheetTable(user, combinedStats, date);
        }
    }

    private User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetails) authentication.getPrincipal()).user();
    }

    private List<TableDTO> getStatsForTables(User user, LocalDate dateStart, LocalDate dateEnd) {

        Set<Network> userNetworks = credentialsService.userNetworks(user);
        List<TableDTO> tableStats = new ArrayList<>();

        for (Network network : userNetworks) {
            List<StatDTO> currentNetworkStat;
            if (user.getRole().equals("ROLE_GUEST"))
                currentNetworkStat = networksService.getNetworkStatisticsListMock(network);
            else
                currentNetworkStat = networksService.getNetworkStatisticsList(user, network, dateStart, dateEnd);
            tableStats.add(new TableDTO(currentNetworkStat, network));
            if (dateStart.equals(dateEnd))
                statsService.saveStatDTOList(currentNetworkStat, user, network, dateStart);
        }
        return tableStats;
    }

    private void putCredentialsInModel(Model model) {
        Map<Network, CredentialsDTO> credentials = credentialsService.getUserCredentialsList(getUser()).stream()
                .collect(Collectors.toMap(CredentialsDTO::getNetworkName, Function.identity()));
        if (!credentials.containsKey(Network.ADCOMBO) || (credentials.containsKey(Network.ADCOMBO) &&
                credentials.size() < 2))
            model.addAttribute("checkAPI", "Add API for Adcombo and ad network");

        credentials.putIfAbsent(Network.ADCOMBO, new CredentialsDTO());
        credentials.putIfAbsent(Network.EXO, new CredentialsDTO());
        credentials.putIfAbsent(Network.TF, new CredentialsDTO());

        model.addAttribute("credentialsADCOMBO", credentials.get(Network.ADCOMBO));
        model.addAttribute("credentialsEXO", credentials.get(Network.EXO));
        model.addAttribute("credentialsTF", credentials.get(Network.TF));
        model.addAttribute("username", getUser().getUsername());
    }
}
