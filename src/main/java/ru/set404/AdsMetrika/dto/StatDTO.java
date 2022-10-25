package ru.set404.AdsMetrika.dto;


import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class StatDTO {

    private int campaignId;
    private String campaignName;
    private int clicks;
    private double spend;
    private double holdCost;
    private int approveCount;
    private double revenue;

    public StatDTO() {
    }

    public StatDTO(int campaignId, String campaignName, int clicks, double spend, double holdCost, int approveCount, double revenue) {
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.clicks = clicks;
        this.spend = spend;
        this.holdCost = holdCost;
        this.approveCount = approveCount;
        this.revenue = revenue;
    }
    public double getProfit() {
        return revenue - spend;
    }

    public int getROI() {
        return (int) (((revenue - spend) / spend) * 100);
    }
}
