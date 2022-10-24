package ru.set404.AdsMetrika.controllers;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import ru.set404.AdsMetrika.dto.DashboardStatDTO;
import ru.set404.AdsMetrika.dto.StatDTO;
import ru.set404.AdsMetrika.models.Offer;
import ru.set404.AdsMetrika.models.Stat;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.security.UserDetails;
import ru.set404.AdsMetrika.services.OffersService;
import ru.set404.AdsMetrika.services.StatsService;
import ru.set404.AdsMetrika.services.network.Network;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/statistics")
public class UserController {
    private final OffersService offersService;
    private final StatsService statsService;
    private final ModelMapper modelMapper;

    @Autowired
    public UserController(OffersService offersService, StatsService statsService, ModelMapper modelMapper) {
        this.offersService = offersService;
        this.statsService = statsService;
        this.modelMapper = modelMapper;
    }

    @GetMapping
    public String index(@RequestParam(value = "ds", required = false,
            defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate dateStart,
                        @RequestParam(value = "de", required = false,
                                defaultValue = "#{T(java.time.LocalDate).now()}") LocalDate dateEnd, Model model)
            throws IOException, InterruptedException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User currentUser = ((UserDetails) authentication.getPrincipal()).user();

        List<Stat> stats = statsService.getStatsList(currentUser, LocalDate.now().minusDays(30));
        Map<Network, List<StatDTO>> currentStatistics = getAndSaveStatistics(currentUser, dateStart, dateEnd);
        double totalSpend = currentStatistics.values().stream()
                .mapToDouble(a -> a.stream().mapToDouble(StatDTO::getCost).sum()).sum();
        double totalRevenue = currentStatistics.values().stream()
                .mapToDouble(a -> a.stream().mapToDouble(StatDTO::getApproveCost).sum()).sum();
        int totalOrders = currentStatistics.values().stream()
                .mapToInt(a -> a.stream().mapToInt(StatDTO::getApproveCount).sum()).sum();
        int totalClicks = currentStatistics.values().stream()
                .mapToInt(a -> a.stream().mapToInt(StatDTO::getClicks).sum()).sum();

        List<DashboardStatDTO> dashboardStats = getDashboardStats(stats);

        List<Double> costs = dashboardStats.stream().map(DashboardStatDTO::getSpend).toList();
        List<Double> revenue = dashboardStats.stream().map(DashboardStatDTO::getRevenue).toList();
        List<LocalDate> dates = dashboardStats.stream().map(DashboardStatDTO::getCreatedDate).toList();

        model.addAttribute("monthlyTotal", getTotalDashboardStats(stats));
        model.addAttribute("totalSpendByNetwork", getTotalSpendByNetwork(stats));

        model.addAttribute("costs", costs);
        model.addAttribute("revenue", revenue);
        model.addAttribute("dates", dates);

        model.addAttribute("totalClicks", totalClicks);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalSpend", totalSpend);
        model.addAttribute("totalRevenue", totalRevenue);

        model.addAttribute("username", currentUser.getUsername());
        model.addAttribute("dashboard", dashboardStats);
        model.addAttribute("currentDate", LocalDate.now());
        model.addAttribute("statistics", currentStatistics);

        return "user/index";
    }

    @GetMapping("/13")
    public String dss(){
        return "auth/index";
    }
    public void saveStatisticListAsStat(List<StatDTO> statDTOList, User user, Network network, LocalDate date) {
        for (StatDTO statDTO : statDTOList) {
            Stat stat = modelMapper.map(statDTO, Stat.class);
            stat.setNetworkName(network);
            stat.setOwner(user);
            stat.setCreatedDate(date);
            statsService.save(stat, user, date, stat.getCampaignId());
        }
    }

    public Map<Network, List<StatDTO>> getAndSaveStatistics(User currentUser, LocalDate dateStart, LocalDate dateEnd)
            throws IOException, InterruptedException {
        List<Offer> userOffers = offersService.getUserOffersList(currentUser);
        Set<Network> networks = userOffers.stream().map(Offer::getNetworkName).collect(Collectors.toSet());

        Map<Network, List<StatDTO>> statsMap = new HashMap<>();
        for (Network network : networks) {
            List<StatDTO> networkStatisticsList = offersService.getNetworkStatisticsList(userOffers,
                    network, dateStart, dateEnd);
            statsMap.put(network, networkStatisticsList);
            if (dateStart.equals(dateEnd))
                saveStatisticListAsStat(networkStatisticsList, currentUser, network, dateStart);
        }
        return statsMap;
    }

    public DashboardStatDTO getTotalDashboardStats(List<Stat> userStats) {
        double totalSpend = userStats.stream().mapToDouble(Stat::getCost).sum();
        double totalRevenue = userStats.stream().mapToDouble(Stat::getApproveCost).sum();
        return new DashboardStatDTO(null, totalSpend, totalRevenue);
    }

    public Map<Network, Double> getTotalSpendByNetwork(List<Stat> userStats) {
        return userStats.stream()
                .collect(Collectors.groupingBy(Stat::getNetworkName, Collectors.summingDouble(Stat::getCost)));
    }
    public List<DashboardStatDTO> getDashboardStats(List<Stat> userStats) {
        Map<LocalDate, Double> groupedCost = userStats.stream()
                .collect(Collectors.groupingBy(Stat::getCreatedDate, Collectors.summingDouble(Stat::getCost)));
        Map<LocalDate, Double> groupedApproveCost = userStats.stream()
                .collect(Collectors.groupingBy(Stat::getCreatedDate, Collectors.summingDouble(Stat::getApproveCost)));

        List<DashboardStatDTO> dashboardStats = new ArrayList<>();
        for (LocalDate date : groupedCost.keySet()) {
            dashboardStats.add(new DashboardStatDTO(date, groupedCost.get(date), groupedApproveCost.get(date)));
        }
        return dashboardStats;
    }

}
