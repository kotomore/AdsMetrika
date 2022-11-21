package ru.set404.AdsMetrika.services;

import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.dto.StatDTO;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.network.Network;
import ru.set404.AdsMetrika.network.ads.ExoClick;
import ru.set404.AdsMetrika.network.ads.NetworkStats;
import ru.set404.AdsMetrika.network.ads.AffiliateNetwork;
import ru.set404.AdsMetrika.network.ads.TrafficFactory;
import ru.set404.AdsMetrika.network.cpa.Adcombo;
import ru.set404.AdsMetrika.network.cpa.AdcomboStats;
import ru.set404.AdsMetrika.util.StatisticsUtilities;

import java.time.LocalDate;
import java.util.*;

@Service
public class NetworksService {
    private final ExoClick exoClick;
    private final TrafficFactory trafficFactory;
    private final Adcombo adCombo;

    public NetworksService(ExoClick exoClick, TrafficFactory trafficFactory,
                           Adcombo adCombo) {
        this.exoClick = exoClick;
        this.trafficFactory = trafficFactory;
        this.adCombo = adCombo;
    }

    public List<StatDTO> getNetworkStatisticsListMock(Network network) {
        List<StatDTO> statDTOS = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 15; i++) {
            statDTOS.add(new StatDTO(random.nextInt(1000),
                    network.getFullName() + " Campaign or Offer " + i,
                    random.nextInt(1000),
                    random.nextDouble(100),
                    random.nextDouble(100),
                    random.nextInt(10),
                    random.nextDouble(100)));
        }
        return statDTOS;
    }

    public List<StatDTO> getNetworkStatisticsList(User user, Network network, LocalDate dateStart,
                                                  LocalDate dateEnd) {
        AffiliateNetwork affiliateNetwork = null;
        switch (network) {
            case TF -> affiliateNetwork = trafficFactory;
            case EXO -> affiliateNetwork = exoClick;
        }

        Map<Integer, AdcomboStats> adcomboStatsMap = adCombo.getNetworkStatMap(user, network,
                dateStart.minusDays(1), dateEnd);

        List<StatDTO> statsEntities = new ArrayList<>();

        for (int offerId : adcomboStatsMap.keySet()) {
            if (adcomboStatsMap.containsKey(offerId)) {
                assert affiliateNetwork != null;
                NetworkStats stat = affiliateNetwork.getNetworkStatsByOfferCampaigns(user, adcomboStatsMap.get(offerId)
                        .getCampaigns(), dateStart, dateEnd);
                statsEntities.add(StatisticsUtilities.createStatsDTO(offerId, stat, adcomboStatsMap));
            }
        }
        return statsEntities;
    }

    public List<StatDTO> getCampaignStats(User user, Network network, LocalDate dateStart,
                                          LocalDate dateEnd) {
        AffiliateNetwork affiliateNetwork = null;
        switch (network) {
            case TF -> affiliateNetwork = trafficFactory;
            case EXO -> affiliateNetwork = exoClick;
        }

        assert affiliateNetwork != null;
        Map<Integer, NetworkStats> networkStatsMap = affiliateNetwork
                .getCampaignStatsMap(user, dateStart, dateEnd);
        Map<Integer, AdcomboStats> adcomboStatsMap = adCombo.getCampaignStatMap(user, network, dateStart, dateEnd);

        List<StatDTO> stats = new ArrayList<>();
        for (int campaignId : networkStatsMap.keySet()) {
            if (networkStatsMap.containsKey(campaignId) && adcomboStatsMap.containsKey(campaignId))
                stats.add(StatisticsUtilities.createStatsDTO(campaignId, networkStatsMap, adcomboStatsMap));
        }
        return stats;
    }
}
