package ru.set404.AdsMetrika.dto;

import lombok.Getter;
import ru.set404.AdsMetrika.services.network.Network;

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

    public int getTotalClicks() {
        return currentStats.stream().mapToInt(StatDTO::getClicks).sum();
    }

    public double getTotalSpend() {
        return currentStats.stream().mapToDouble(StatDTO::getSpend).sum();
    }

    public double getTotalHoldCost() {
        return currentStats.stream().mapToDouble(StatDTO::getHoldCost).sum();
    }

    public int getTotalApproveCount() {
        return currentStats.stream().mapToInt(StatDTO::getApproveCount).sum();
    }

    public double getTotalRevenue() {
        return currentStats.stream().mapToDouble(StatDTO::getRevenue).sum();
    }

    public double getTotalProfit() {
        return currentStats.stream().mapToDouble(StatDTO::getProfit).sum();
    }

    public double getTotalROI() {
        double spend = getTotalSpend();
        return (int) (((getTotalRevenue() - spend) / spend) * 100);
    }
}
