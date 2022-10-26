package ru.set404.AdsMetrika.dto;


import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class StatDTO {

    private Integer campaignId;
    private String campaignName;
    private Integer clicks;
    private Double spend;
    private Double holdCost;
    private Integer approveCount;
    private Double revenue;

    public StatDTO() {
    }

    public StatDTO(Integer campaignId, String campaignName, Integer clicks, Double spend, Double holdCost, Integer approveCount, Double revenue) {
        this.campaignId = campaignId;
        this.campaignName = campaignName;
        this.clicks = clicks;
        this.spend = spend;
        this.holdCost = holdCost;
        this.approveCount = approveCount;
        this.revenue = revenue;
    }

    public double getProfit() {
        if (revenue != null && spend != null)
            return revenue - spend;
        return 0;
    }

    public int getROI() {
        if (revenue != null && spend != null)
            return (int) (((revenue - spend) / spend) * 100);
        return 0;
    }
}
