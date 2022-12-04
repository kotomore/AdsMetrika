package ru.set404.AdsMetrika.network.ads;

import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.network.cpa.AdcomboStats;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public interface AffiliateNetwork {
    Map<Integer, NetworkStats> getCampaignsStats(Credentials credentials, LocalDate dateStart, LocalDate dateEnd);
    default Map<Integer, NetworkStats> getOfferCombinedStats(Credentials credentials, Map<Integer, AdcomboStats> adcomboStatsMap, LocalDate dateStart, LocalDate dateEnd) {
        Map<Integer, NetworkStats> allCampaigns = getCampaignsStats(credentials, dateStart, dateEnd);
        Map<Integer, NetworkStats> result = new HashMap<>();

        for (Map.Entry<Integer, AdcomboStats> entry : adcomboStatsMap.entrySet()) {
            int clicks = 0;
            double cost = 0;
            for (Integer campaign : entry.getValue().getCampaigns()) {
                if (allCampaigns.containsKey(campaign)) {
                    clicks += allCampaigns.get(campaign).getClicks();
                    cost += allCampaigns.get(campaign).getCost();
                }
            }
            if (cost > 0)
                result.put(entry.getKey(), new NetworkStats(clicks, cost));
        }
        return result;
    }
}
