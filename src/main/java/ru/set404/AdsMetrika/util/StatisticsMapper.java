package ru.set404.AdsMetrika.util;


import ru.set404.AdsMetrika.services.network.ads.NetworkStatEntity;
import ru.set404.AdsMetrika.services.network.cpa.AdcomboStatsEntity;
import ru.set404.AdsMetrika.dto.StatDTO;

import java.util.Map;

public class StatisticsMapper {
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

}
