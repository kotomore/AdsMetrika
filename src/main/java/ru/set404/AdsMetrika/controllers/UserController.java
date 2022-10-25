package ru.set404.AdsMetrika.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.set404.AdsMetrika.dto.ChartDTO;
import ru.set404.AdsMetrika.dto.StatDTO;
import ru.set404.AdsMetrika.dto.TableDTO;
import ru.set404.AdsMetrika.models.Offer;
import ru.set404.AdsMetrika.models.Stat;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.security.UserDetails;
import ru.set404.AdsMetrika.services.OffersService;
import ru.set404.AdsMetrika.services.StatsService;
import ru.set404.AdsMetrika.services.network.Network;
import ru.set404.AdsMetrika.util.StatisticsUtilities;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/statistics")
public class UserController {
    private final OffersService offersService;
    private final StatsService statsService;

    @Autowired
    public UserController(OffersService offersService, StatsService statsService) {
        this.offersService = offersService;
        this.statsService = statsService;
    }

    @GetMapping
    public String index(@RequestParam(value = "ds", required = false,
            defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate dateStart,
                        @RequestParam(value = "de", required = false,
                                defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate dateEnd, Model model)
            throws IOException, InterruptedException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = ((UserDetails) authentication.getPrincipal()).user();

        List<Offer> userOffers = offersService.getUserOffersList(currentUser);
        Set<Network> userNetworks = userOffers.stream().map(Offer::getNetworkName).collect(Collectors.toSet());

        List<TableDTO> tableStats = new ArrayList<>();

        for (Network network : userNetworks) {
            List<StatDTO> currentNetworkStat = offersService.getNetworkStatisticsList(userOffers,
                    network, dateStart, dateEnd);
            tableStats.add(new TableDTO(currentNetworkStat, network));
            if (dateStart.equals(dateEnd))
                statsService.saveStatDTO(currentNetworkStat, currentUser, network, dateStart);
        }

        List<Stat> oldStats = statsService.getStatsList(currentUser, LocalDate.now().minusDays(30));
        List<ChartDTO> chartStats = StatisticsUtilities.getChartStats(oldStats);


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
}
