package ru.set404.AdsMetrika.services.network.cpa;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdcomboStatsEntity {

    private int campaignId;
    private String campaignName;
    double holdCost = 0;
    int confirmedCount = 0;
    double cost = 0;

    public AdcomboStatsEntity(int campaignId, String campaignName, double holdCost, int confirmedCount, double cost) {
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.holdCost = holdCost;
        this.confirmedCount = confirmedCount;
        this.cost = cost;
    }
}
