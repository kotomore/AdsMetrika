package ru.set404.AdsMetrika.util;


import ru.set404.AdsMetrika.services.network.adsnetworks.NetworkStatEntity;
import ru.set404.AdsMetrika.services.network.cpanetworks.AdcomboStatsEntity;
import ru.set404.AdsMetrika.to.StatsEntity;

import java.util.Map;

public class StatisticsMapper {
    public static StatsEntity map(int offerId, Map<Integer, NetworkStatEntity> networkStats,
                                  Map<Integer, AdcomboStatsEntity> adcomboStats) {
        return new StatsEntity(
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
