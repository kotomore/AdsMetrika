package ru.set404.AdsMetrika.network.ads;

import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.models.Credentials;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
public interface AffiliateNetwork {
    Map<Integer, NetworkStats> getCampaignsStats(Credentials credentials, LocalDate dateStart, LocalDate dateEnd);

    NetworkStats getCombinedStatsByOfferCampaigns(Credentials credentials, List<Integer> campaigns, LocalDate dateStart,
                                                  LocalDate dateEnd);
}
