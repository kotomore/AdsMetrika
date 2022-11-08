package ru.set404.AdsMetrika.dto;


import lombok.Getter;
import lombok.Setter;

import java.util.Objects;


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

    public StatDTO(String campaignName) {
        this.campaignName = campaignName;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatDTO statDTO = (StatDTO) o;
        return Objects.equals(campaignId, statDTO.campaignId) && Objects.equals(campaignName, statDTO.campaignName) && Objects.equals(clicks, statDTO.clicks) && Objects.equals(spend, statDTO.spend) && Objects.equals(holdCost, statDTO.holdCost) && Objects.equals(approveCount, statDTO.approveCount) && Objects.equals(revenue, statDTO.revenue);
    }

    @Override
    public int hashCode() {
        return Objects.hash(campaignId, campaignName, clicks, spend, holdCost, approveCount, revenue);
    }
}
