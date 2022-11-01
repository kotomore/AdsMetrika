package ru.set404.AdsMetrika.util;


import ru.set404.AdsMetrika.dto.ChartDTO;
import ru.set404.AdsMetrika.dto.TableDTO;
import ru.set404.AdsMetrika.models.Stat;
import ru.set404.AdsMetrika.network.Network;
import ru.set404.AdsMetrika.network.ads.NetworkStats;
import ru.set404.AdsMetrika.network.cpa.AdcomboStats;
import ru.set404.AdsMetrika.dto.StatDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsUtilities {
    public static StatDTO createStatsDTO(int offerId, Map<Integer, NetworkStats> networkStats,
                                         Map<Integer, AdcomboStats> adcomboStats) {
        return new StatDTO(
                adcomboStats.get(offerId).getOfferId(),
                adcomboStats.get(offerId).getOfferName(),
                networkStats.get(offerId).getClicks(),
                networkStats.get(offerId).getCost(),
                adcomboStats.get(offerId).getHoldCost(),
                adcomboStats.get(offerId).getConfirmedCount(),
                adcomboStats.get(offerId).getCost()
        );
    }

    public static StatDTO createStatsDTO(int offerId, NetworkStats networkStats,
                                         Map<Integer, AdcomboStats> adcomboStats) {
        return new StatDTO(
                adcomboStats.get(offerId).getOfferId(),
                adcomboStats.get(offerId).getOfferName(),
                networkStats.getClicks(),
                networkStats.getCost(),
                adcomboStats.get(offerId).getHoldCost(),
                adcomboStats.get(offerId).getConfirmedCount(),
                adcomboStats.get(offerId).getCost()
        );
    }

    public static StatDTO sumStatDTO(StatDTO statDTO1, StatDTO statDTO2) {
        return new StatDTO(
                statDTO1.getCampaignId(),
                statDTO1.getCampaignName(),
                statDTO1.getClicks() + statDTO2.getClicks(),
                statDTO1.getSpend() + statDTO2.getSpend(),
                statDTO1.getHoldCost() + statDTO2.getHoldCost(),
                statDTO1.getApproveCount() + statDTO2.getApproveCount(),
                statDTO1.getRevenue() + statDTO2.getRevenue()
        );
    }

    public static TableDTO combineTableDTO(List<TableDTO> tableStats) {
        Map<Integer, StatDTO> combinedStats = new HashMap<>();
        for (TableDTO table : tableStats) {
            for (StatDTO statDTO : table.getCurrentStats()) {
                combinedStats.computeIfPresent(statDTO.getCampaignId(),
                        (key, val) -> StatisticsUtilities.sumStatDTO(val, statDTO));
                combinedStats.putIfAbsent(statDTO.getCampaignId(), statDTO);
            }
        }
        return new TableDTO(combinedStats.values().stream().toList());
    }

    public static TableDTO convertForSingleTable(List<TableDTO> tableStats) {
        List<StatDTO> statDTOS = new ArrayList<>();
        for (TableDTO table : tableStats) {
            //For empty columns in table
            statDTOS.add(new StatDTO());
            statDTOS.add(new StatDTO());
            statDTOS.add(new StatDTO(table.getNetwork().getFullName()));
            statDTOS.addAll(table.getCurrentStats());
        }
        //For empty columns in table
        statDTOS.remove(0);
        statDTOS.remove(0);
        return new TableDTO(statDTOS);
    }

    public static ChartDTO getTotalChartDTO(List<Stat> oldStats) {
        double totalSpend = oldStats.stream().mapToDouble(Stat::getSpend).sum();
        double totalRevenue = oldStats.stream().mapToDouble(Stat::getRevenue).sum();
        return new ChartDTO(totalSpend, totalRevenue);
    }

    public static List<ChartDTO> convertToChartDTOList(List<Stat> userStats) {
        Map<LocalDate, Double> groupedSpend = userStats.stream()
                .collect(Collectors.groupingBy(Stat::getCreatedDate, Collectors.summingDouble(Stat::getSpend)));
        Map<LocalDate, Double> groupedRevenue = userStats.stream()
                .collect(Collectors.groupingBy(Stat::getCreatedDate, Collectors.summingDouble(Stat::getRevenue)));

        List<ChartDTO> chartStats = new ArrayList<>();
        for (LocalDate date : groupedSpend.keySet()) {
            chartStats.add(new ChartDTO(date, groupedSpend.get(date), groupedRevenue.get(date)));
        }
        return chartStats;
    }

    public static Map<Network, Double> getTotalChartSpend(List<Stat> userStats) {
        return userStats.stream()
                .collect(Collectors.groupingBy(Stat::getNetworkName, Collectors.summingDouble(Stat::getSpend)));
    }

}
