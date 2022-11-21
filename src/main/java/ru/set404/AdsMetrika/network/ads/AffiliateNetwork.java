package ru.set404.AdsMetrika.network.ads;

import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.models.User;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public interface AffiliateNetwork {
    Map<Integer, NetworkStats> getCampaignStatsMap(User user, LocalDate dateStart, LocalDate dateEnd);

    NetworkStats getNetworkStatsByOfferCampaigns(User user, List<Integer> campaigns, LocalDate dateStart, LocalDate dateEnd);
}
