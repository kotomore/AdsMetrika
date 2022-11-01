package ru.set404.AdsMetrika.dto;

import lombok.Getter;
import ru.set404.AdsMetrika.network.Network;

import java.util.List;

@Getter
public class TableDTO {

    Network network;
    List<StatDTO> currentStats;

    public TableDTO() {
    }

    public TableDTO(List<StatDTO> currentStats, Network network) {
        this.currentStats = currentStats;
        this.network = network;
    }

    public TableDTO(List<StatDTO> currentStats) {
        this.currentStats = currentStats;
    }

    public int getTotalClicks() {
        if (currentStats != null)
            return currentStats.stream().mapToInt(StatDTO::getClicks).sum();
        return 0;
    }

    public double getTotalSpend() {
        if (currentStats != null)
            return currentStats.stream().mapToDouble(StatDTO::getSpend).sum();
        return 0;
    }

    public double getTotalHoldCost() {
        if (currentStats != null)
            return currentStats.stream().mapToDouble(StatDTO::getHoldCost).sum();
        return 0;
    }

    public int getTotalApproveCount() {
        if (currentStats != null)
            return currentStats.stream().mapToInt(StatDTO::getApproveCount).sum();
        return 0;
    }

    public double getTotalRevenue() {
        if (currentStats != null)
            return currentStats.stream().mapToDouble(StatDTO::getRevenue).sum();
        return 0;
    }

    public double getTotalProfit() {
        if (currentStats != null)
            return currentStats.stream().mapToDouble(StatDTO::getProfit).sum();
        return 0;
    }

    public double getTotalROI() {
        double spend = getTotalSpend();
        return (int) (((getTotalRevenue() - spend) / spend) * 100);
    }
}
