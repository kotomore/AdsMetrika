package ru.set404.AdsMetrika.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.set404.AdsMetrika.dto.*;
import ru.set404.AdsMetrika.models.Offer;
import ru.set404.AdsMetrika.models.Stat;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.security.UserDetails;
import ru.set404.AdsMetrika.services.CredentialsService;
import ru.set404.AdsMetrika.services.NetworksService;
import ru.set404.AdsMetrika.services.OffersService;
import ru.set404.AdsMetrika.services.StatsService;
import ru.set404.AdsMetrika.services.network.Network;
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

    @Autowired
    public UserController(NetworksService networksService, OffersService offersService, StatsService statsService, CredentialsService credentialsService) {
        this.networksService = networksService;
        this.offersService = offersService;
        this.statsService = statsService;
        this.credentialsService = credentialsService;
    }

    @GetMapping("/statistics")
    public String index(@RequestParam(value = "ds", required = false,
            defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate dateStart,
                        @RequestParam(value = "de", required = false,
                                defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate dateEnd, Model model)
            throws IOException, InterruptedException {

        User currentUser = getUser();

        List<Offer> userOffers = offersService.getUserOffersList(currentUser);

        List<TableDTO> tableStats = getTableStats(dateStart, dateEnd, currentUser, userOffers);

        List<Stat> oldStats = statsService.getStatsList(currentUser, LocalDate.now().minusDays(30));
        List<ChartDTO> chartStats = StatisticsUtilities.getChartStats(oldStats);

        Map<Network, CredentialsDTO> credentials = getNetworkCredentialsDTOMap();
        model.addAttribute("credentialsADCOMBO", credentials.get(Network.ADCOMBO));
        model.addAttribute("credentialsEXO", credentials.get(Network.EXO));
        model.addAttribute("credentialsTF", credentials.get(Network.TF));


        model.addAttribute("username", currentUser.getUsername());
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("statistics", tableStats);
        model.addAttribute("combinedStats", StatisticsUtilities.getCombinedStats(tableStats));

        //column_line_chart
        model.addAttribute("chartTotal", StatisticsUtilities.getTotalChartStats(oldStats));
        model.addAttribute("chartCosts", chartStats.stream().map(ChartDTO::getSpend).toList());
        model.addAttribute("chartRevenue", chartStats.stream().map(ChartDTO::getRevenue).toList());
        model.addAttribute("chartDates", chartStats.stream().map(ChartDTO::getCreatedDate).toList());

        //donut-chart
        model.addAttribute("totalSpendByNetwork", StatisticsUtilities.getTotalChartSpend(oldStats));
        return "user/index";
    }

    @GetMapping("/report")
    public String report(@RequestParam(value = "type", required = false) String type, Model model) throws IOException, InterruptedException {
        User currentUser = getUser();
        List<Offer> userOffers = offersService.getUserOffersList(currentUser);

        String headerText;
        TableDTO combinedStats;
        if (type != null && type.equals("month")) {
            LocalDate firstDay = LocalDate.now().minusDays(1);
            LocalDate lastDay = LocalDate.now().minusDays(1);
            List<TableDTO> tableStats = getTableStats(firstDay, lastDay, currentUser, userOffers);
            combinedStats = StatisticsUtilities.getCombinedStats(tableStats);
            headerText = firstDay + " - " + lastDay;
        } else {
            LocalDate firstDay = LocalDate.now().minusMonths(1).with(TemporalAdjusters.firstDayOfMonth());
            LocalDate lastDay = LocalDate.now().minusMonths(1).with(TemporalAdjusters.lastDayOfMonth());
            List<TableDTO> tableStats = getTableStats(firstDay, lastDay, currentUser, userOffers);
            combinedStats = StatisticsUtilities.getOneList(tableStats);
            headerText = firstDay.toString();
        }


        Map<Network, CredentialsDTO> credentials = getNetworkCredentialsDTOMap();
        model.addAttribute("credentialsADCOMBO", credentials.get(Network.ADCOMBO));
        model.addAttribute("credentialsEXO", credentials.get(Network.EXO));
        model.addAttribute("credentialsTF", credentials.get(Network.TF));

        model.addAttribute("dates", headerText);
        model.addAttribute("username", currentUser.getUsername());
        model.addAttribute("combinedStats", combinedStats);

        return "user/report";
    }

    @GetMapping("/offers")
    public String offers(Model model) {
        OfferListDTO offerForm = new OfferListDTO();

        for (int i = 1; i <= 7; i++) {
            offerForm.addOffer(new OfferDTO());
        }

        Map<Network, CredentialsDTO> credentials = getNetworkCredentialsDTOMap();
        model.addAttribute("credentialsADCOMBO", credentials.get(Network.ADCOMBO));
        model.addAttribute("credentialsEXO", credentials.get(Network.EXO));
        model.addAttribute("credentialsTF", credentials.get(Network.TF));

        model.addAttribute("blankForm", offerForm);
        model.addAttribute("form", offersService.getOfferListDTO(getUser()));
        model.addAttribute("username", getUser().getUsername());
        return "user/offers";
    }

    @GetMapping("/offers/{id}/delete")
    public String delete(@PathVariable("id") int id) {
        offersService.deleteById(getUser(), id);
        return "redirect:/offers";
    }

    @PostMapping("/offers/edit")
    public String editOffers(@ModelAttribute("form") OfferListDTO offerDTOS) {
        offersService.saveOffersDTOList(offerDTOS, getUser());
        return "redirect:/offers";
    }

    @PostMapping("/offers/save")
    public String saveOffers(@ModelAttribute("blankForm") OfferListDTO offerDTOS) {
        offersService.saveOffersDTOList(offerDTOS, getUser());
        return "redirect:/offers";
    }

    @PostMapping("/credentials/save")
    public String saveCredentials(@ModelAttribute("credentialsADCOMBO") CredentialsDTO credentialsDTO) {
        if (credentialsDTO.getUsername().isEmpty() || credentialsDTO.getPassword().isEmpty())
            credentialsService.deleteById(credentialsDTO.getId());
        else
            credentialsService.saveCredentialsDTO(credentialsDTO, getUser());
        return "redirect:/statistics";
    }

    private static User getUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return ((UserDetails) authentication.getPrincipal()).user();
    }

    private List<TableDTO> getTableStats(LocalDate dateStart, LocalDate dateEnd, User currentUser, List<Offer> userOffers)
            throws IOException, InterruptedException {

        Set<Network> userNetworks = userOffers.stream().map(Offer::getNetworkName).collect(Collectors.toSet());

        List<TableDTO> tableStats = new ArrayList<>();
        for (Network network : userNetworks) {
            List<StatDTO> currentNetworkStat = networksService.getNetworkStatisticsList(userOffers,
                    network, dateStart, dateEnd);
            tableStats.add(new TableDTO(currentNetworkStat, network));
//            if (dateStart.equals(dateEnd))
//                statsService.saveStatDTOList(currentNetworkStat, currentUser, network, dateStart);
        }
        return tableStats;
    }

    private Map<Network, CredentialsDTO> getNetworkCredentialsDTOMap() {
        Map<Network, CredentialsDTO> credentials = credentialsService.getUserCredentialsList(getUser()).stream()
                .collect(Collectors.toMap(CredentialsDTO::getNetworkName, Function.identity()));
        credentials.putIfAbsent(Network.ADCOMBO, new CredentialsDTO());
        credentials.putIfAbsent(Network.EXO, new CredentialsDTO());
        credentials.putIfAbsent(Network.TF, new CredentialsDTO());
        return credentials;
    }
}
