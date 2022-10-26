package ru.set404.AdsMetrika.util;


import ru.set404.AdsMetrika.dto.ChartDTO;
import ru.set404.AdsMetrika.dto.TableDTO;
import ru.set404.AdsMetrika.models.Stat;
import ru.set404.AdsMetrika.services.network.Network;
import ru.set404.AdsMetrika.services.network.ads.NetworkStatEntity;
import ru.set404.AdsMetrika.services.network.cpa.AdcomboStatsEntity;
import ru.set404.AdsMetrika.dto.StatDTO;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class StatisticsUtilities {
    public static StatDTO createStatsDTO(int offerId, Map<Integer, NetworkStatEntity> networkStats,
                                         Map<Integer, AdcomboStatsEntity> adcomboStats) {
        return new StatDTO(
                adcomboStats.get(offerId).getCampaignId(),
                adcomboStats.get(offerId).getCampaignName(),
                networkStats.get(offerId).getClicks(),
                networkStats.get(offerId).getCost(),
                adcomboStats.get(offerId).getHoldCost(),
                adcomboStats.get(offerId).getConfirmedCount(),
                adcomboStats.get(offerId).getCost()
        );
    }

    public static StatDTO stackStatDTO(StatDTO statDTO1, StatDTO statDTO2) {
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

    public static TableDTO getCombinedStats(List<TableDTO> tableStats) {
        Map<Integer, StatDTO> combinedStats = new HashMap<>();
        for (TableDTO table : tableStats) {
            for (StatDTO statDTO : table.getCurrentStats()) {
                combinedStats.computeIfPresent(statDTO.getCampaignId(),
                        (key, val) -> StatisticsUtilities.stackStatDTO(val, statDTO));
                combinedStats.putIfAbsent(statDTO.getCampaignId(), statDTO);
            }
        }
        return new TableDTO(combinedStats.values().stream().toList(), null);
    }

    public static TableDTO getOneList(List<TableDTO> tableStats) {
        List<StatDTO> statDTOS = new ArrayList<>();
        for (TableDTO table : tableStats) {
            statDTOS.add(new StatDTO());
            statDTOS.add(new StatDTO());
            statDTOS.add(new StatDTO(null, table.getNetwork().getFullName(),
                    null, null, null, null, null));
            statDTOS.addAll(table.getCurrentStats());
        }
        statDTOS.remove(0);
        statDTOS.remove(0);
        return new TableDTO(statDTOS, null);
    }

    public static ChartDTO getTotalChartStats(List<Stat> oldUserStats) {
        double totalSpend = oldUserStats.stream().mapToDouble(Stat::getSpend).sum();
        double totalRevenue = oldUserStats.stream().mapToDouble(Stat::getRevenue).sum();

        return new ChartDTO(null, totalSpend, totalRevenue);
    }

    public static List<ChartDTO> getChartStats(List<Stat> userStats) {
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
