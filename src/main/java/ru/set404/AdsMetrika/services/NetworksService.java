package ru.set404.AdsMetrika.services;

import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.dto.StatDTO;
import ru.set404.AdsMetrika.network.Network;
import ru.set404.AdsMetrika.network.ads.ExoClick;
import ru.set404.AdsMetrika.network.ads.NetworkStats;
import ru.set404.AdsMetrika.network.ads.AffiliateNetwork;
import ru.set404.AdsMetrika.network.ads.TrafficFactory;
import ru.set404.AdsMetrika.network.cpa.Adcombo;
import ru.set404.AdsMetrika.network.cpa.AdcomboStats;
import ru.set404.AdsMetrika.util.StatisticsUtilities;

import java.io.IOException;
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

    public List<StatDTO> getNetworkStatisticsListMock() {
        List<StatDTO> statDTOS = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 15; i++) {
            statDTOS.add(new StatDTO(random.nextInt(1000),
                    "Offer or Campaign " + i,
                    random.nextInt(1000),
                    random.nextDouble(100),
                    random.nextDouble(100),
                    random.nextInt(10),
                    random.nextDouble(100)));
        }
        return statDTOS;
    }

    public List<StatDTO> getNetworkStatisticsList(Network network, LocalDate dateStart,
                                                  LocalDate dateEnd) throws IOException {

        AffiliateNetwork affiliateNetwork = null;
        switch (network) {
            case TF -> affiliateNetwork = trafficFactory;
            case EXO -> affiliateNetwork = exoClick;
        }

        Map<Integer, AdcomboStats> adcomboStatsMap = adCombo.getNetworkStatMap(network,
                dateStart.minusDays(1), dateEnd);

        List<StatDTO> statsEntities = new ArrayList<>();

        for (int offerId : adcomboStatsMap.keySet()) {
            if (adcomboStatsMap.containsKey(offerId)) {
                assert affiliateNetwork != null;
                NetworkStats stat = affiliateNetwork.getNetworkStatEntity(adcomboStatsMap.get(offerId)
                        .getCampaigns(), dateStart, dateEnd);
                statsEntities.add(StatisticsUtilities.createStatsDTO(offerId, stat, adcomboStatsMap));
            }
        }
        return statsEntities;
    }

    public List<StatDTO> getCampaignStats(Network network, LocalDate dateStart,
                                          LocalDate dateEnd) throws IOException {
        AffiliateNetwork affiliateNetwork = null;
        switch (network) {
            case TF -> affiliateNetwork = trafficFactory;
            case EXO -> affiliateNetwork = exoClick;
        }

        assert affiliateNetwork != null;
        Map<Integer, NetworkStats> networkStatsMap = affiliateNetwork
                .getCampaignStatsMap(dateStart, dateEnd);
        Map<Integer, AdcomboStats> adcomboStatsMap = adCombo.getCampaignStatMap(network, dateStart, dateEnd);

        List<StatDTO> stats = new ArrayList<>();
        for (int campaignId : networkStatsMap.keySet()) {
            if (networkStatsMap.containsKey(campaignId) && adcomboStatsMap.containsKey(campaignId))
                stats.add(StatisticsUtilities.createStatsDTO(campaignId, networkStatsMap, adcomboStatsMap));
        }
        return stats;
    }
}
