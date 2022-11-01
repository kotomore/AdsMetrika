package ru.set404.AdsMetrika.network.cpa;


import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class AdcomboStats {

    private int offerId;
    private String offerName;
    double holdCost = 0;
    List<Integer> campaigns = new ArrayList<>();
    int confirmedCount = 0;
    double cost = 0;

    public AdcomboStats(int offerId, String offerName, double holdCost, int confirmedCount, double cost) {
        this.offerId = offerId;
        this.offerName = offerName;
        this.holdCost = holdCost;
        this.confirmedCount = confirmedCount;
        this.cost = cost;
    }
}
