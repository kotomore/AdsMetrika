package ru.set404.AdsMetrika.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import ru.set404.AdsMetrika.dto.*;
import ru.set404.AdsMetrika.models.Stat;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.security.UserDetails;
import ru.set404.AdsMetrika.services.CredentialsService;
import ru.set404.AdsMetrika.services.NetworksService;
import ru.set404.AdsMetrika.services.OffersService;
import ru.set404.AdsMetrika.services.StatsService;
import ru.set404.AdsMetrika.network.Network;
import ru.set404.AdsMetrika.util.OfferListDTOValidator;
import ru.set404.AdsMetrika.util.StatisticsUtilities;

import java.io.IOException;
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
    private final OfferListDTOValidator offerListDTOValidator;

    @Autowired
    public UserController(NetworksService networksService, OffersService offersService, StatsService statsService,
                          CredentialsService credentialsService, OfferListDTOValidator offerListDTOValidator) {
        this.networksService = networksService;
        this.offersService = offersService;
        this.statsService = statsService;
        this.credentialsService = credentialsService;
        this.offerListDTOValidator = offerListDTOValidator;
    }

    @GetMapping("/statistics")
    public String index(@RequestParam(value = "ds", required = false) LocalDate dateStart,
                        @RequestParam(value = "de", required = false) LocalDate dateEnd, Model model) {
        User currentUser = getUser();
        List<TableDTO> tableStats = new ArrayList<>();
        if (dateStart != null) {
            try {
                tableStats = getTableStats(dateStart, dateEnd);
            } catch (Exception e) {
                return "redirect:/statistics?error";
            }
        }
        final int COUNT_DAYS_IN_CHART = 7;
        List<Stat> oldStats = statsService.getStatsList(currentUser, LocalDate.now().minusDays(COUNT_DAYS_IN_CHART));
        List<ChartDTO> chartStats = StatisticsUtilities.convertToChartDTOList(oldStats);

        putCredentialsInModel(model);
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("statistics", tableStats);
        model.addAttribute("combinedStats", StatisticsUtilities.combineTableDTO(tableStats));

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
    public String report(@RequestParam(value = "type", required = false) String type, Model model) {
        String headerText = "Choose a date";
        TableDTO combinedStats = new TableDTO();
        if (type != null && type.equals("month")) {
            LocalDate firstDay = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate lastDay = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
            try {
                List<TableDTO> tableStats = getTableStats(firstDay, lastDay);
                combinedStats = StatisticsUtilities.combineTableDTO(tableStats);
            } catch (Exception e) {
                return "redirect:/report?error";
            }

            headerText = firstDay + " - " + lastDay;
        } else if (type != null) {
            LocalDate firstDay = LocalDate.now().minusDays(1);
            LocalDate lastDay = LocalDate.now().minusDays(1);
            try {
                List<TableDTO> tableStats = getTableStats(firstDay, lastDay);
                combinedStats = StatisticsUtilities.convertForSingleTable(tableStats);
            } catch (Exception e) {
                return "redirect:/report?error";
            }
            headerText = firstDay.toString();
        }
        putCredentialsInModel(model);
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

        try {
            if (dateStart != null && dateEnd != null) {
                campaignStats = networksService.getCampaignStats(network, dateStart, dateEnd);
                headerText = dateStart + " - " + dateEnd;
            }
        } catch (Exception e) {
            return "redirect:/campaigns?error";
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

    @PostMapping("/credentials/save")
    public String saveCredentials(@ModelAttribute("credentialsADCOMBO") CredentialsDTO credentialsDTO) {
        if (credentialsDTO.getNetworkName() != Network.ADCOMBO &&
                (credentialsDTO.getUsername().isEmpty()))
            credentialsService.deleteById(credentialsDTO.getId());
        else
            credentialsService.saveCredentialsDTO(credentialsDTO, getUser());
        return "redirect:/statistics";
    }

    private static User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetails) authentication.getPrincipal()).user();
    }

    private List<TableDTO> getTableStats(LocalDate dateStart, LocalDate dateEnd)
            throws IOException {

        Set<Network> userNetworks = credentialsService.userNetworks(getUser());

        List<TableDTO> tableStats = new ArrayList<>();
        for (Network network : userNetworks) {
            List<StatDTO> currentNetworkStat;
            if (getUser().getRole().equals("ROLE_GUEST"))
                currentNetworkStat = networksService.getNetworkStatisticsListMock();
            else
                currentNetworkStat = networksService.getNetworkStatisticsList(network, dateStart, dateEnd);
            tableStats.add(new TableDTO(currentNetworkStat, network));
            if (dateStart.equals(dateEnd))
                statsService.saveStatDTOList(currentNetworkStat, getUser(), network, dateStart);
        }
        return tableStats;
    }

    private void putCredentialsInModel(Model model) {
        Map<Network, CredentialsDTO> credentials = credentialsService.getUserCredentialsList(getUser()).stream()
                .collect(Collectors.toMap(CredentialsDTO::getNetworkName, Function.identity()));
        credentials.putIfAbsent(Network.ADCOMBO, new CredentialsDTO());
        credentials.putIfAbsent(Network.EXO, new CredentialsDTO());
        credentials.putIfAbsent(Network.TF, new CredentialsDTO());

        model.addAttribute("credentialsADCOMBO", credentials.get(Network.ADCOMBO));
        model.addAttribute("credentialsEXO", credentials.get(Network.EXO));
        model.addAttribute("credentialsTF", credentials.get(Network.TF));
        model.addAttribute("username", getUser().getUsername());
    }
}
