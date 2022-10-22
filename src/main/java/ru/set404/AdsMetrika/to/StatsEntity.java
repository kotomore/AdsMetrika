package ru.set404.AdsMetrika.to;


import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class StatsEntity {

    private int campaignId;
    private String campaignName;
    private int clicks;
    private double cost;
    private double holdCost;
    private int approveCount;
    private double approveCost;

    public StatsEntity(int campaignId, String campaignName, int clicks, double cost, double holdCost, int approveCount, double approveCost) {
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.clicks = clicks;
        this.cost = cost;
        this.holdCost = holdCost;
        this.approveCount = approveCount;
        this.approveCost = approveCost;
    }
}
