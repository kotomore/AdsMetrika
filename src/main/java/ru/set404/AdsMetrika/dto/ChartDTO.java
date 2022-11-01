package ru.set404.AdsMetrika.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class ChartDTO {

    private LocalDate createdDate;
    private double spend;
    private double revenue;

    public ChartDTO(LocalDate createdDate, double spend, double revenue) {
        this.createdDate = createdDate;
        this.spend = spend;
        this.revenue = revenue;
    }

    public ChartDTO(double spend, double revenue) {
        this.spend = spend;
        this.revenue = revenue;
    }
}
