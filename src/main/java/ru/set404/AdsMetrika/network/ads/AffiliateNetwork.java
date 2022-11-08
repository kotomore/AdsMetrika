package ru.set404.AdsMetrika.network.ads;

import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public interface AffiliateNetwork {
    Map<Integer, NetworkStats> getCampaignStatsMap(LocalDate dateStart, LocalDate dateEnd);

    NetworkStats getNetworkStatsByOfferCampaigns(List<Integer> campaigns, LocalDate dateStart, LocalDate dateEnd);
}
