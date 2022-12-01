package ru.set404.AdsMetrika.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;
import ru.set404.AdsMetrika.dto.StatDTO;
import ru.set404.AdsMetrika.dto.TableDTO;
import ru.set404.AdsMetrika.models.Credentials;
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
@SessionScope
public class NetworksService {
    private final ExoClick exoClick;
    private final TrafficFactory trafficFactory;
    private final Adcombo adCombo;
    private final CredentialsService credentialsService;

    @Autowired
    public NetworksService(ExoClick exoClick, TrafficFactory trafficFactory,
                           Adcombo adCombo, CredentialsService credentialsService) {
        this.exoClick = exoClick;
        this.trafficFactory = trafficFactory;
        this.adCombo = adCombo;
        this.credentialsService = credentialsService;
    }

    public List<TableDTO> getNetworkStatisticsListMock(User user) {
        List<TableDTO> tableDTOS = new ArrayList<>();
        Set<Network> userNetworks = credentialsService.userNetworks(user);
        for (Network userNetwork : userNetworks) {
            tableDTOS.add(new TableDTO(getCampaignsStatisticsListMock(userNetwork), userNetwork));
        }
        return tableDTOS;
    }

    public List<StatDTO> getCampaignsStatisticsListMock(Network userNetwork) {
        List<StatDTO> statDTOS = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < 15; i++) {
            statDTOS.add(new StatDTO(i,
                    userNetwork.getFullName() + " Campaign or Offer " + i,
                    random.nextInt(1000),
                    random.nextDouble(100),
                    random.nextDouble(100),
                    random.nextInt(10),
                    random.nextDouble(100)));
        }
        return statDTOS;
    }

    ////////////////////////////////////////////////
    //StatDTO contains campaignId = adcombo offer id
    ////////////////////////////////////////////////
    @Cacheable(value = "network_stats")
    public List<TableDTO> getOfferStats(User user, LocalDate dateStart, LocalDate dateEnd) {

        Set<Network> userNetworks = credentialsService.userNetworks(user);
        List<TableDTO> tableStats = new ArrayList<>();

        for (Network network : userNetworks) {
            AffiliateNetwork affiliateNetwork = null;
            switch (network) {
                case TF -> affiliateNetwork = trafficFactory;
                case EXO -> affiliateNetwork = exoClick;
            }

            Map<Network, Credentials> userCredentials = credentialsService.getUserCredentials(user);

            Map<Integer, AdcomboStats> adcomboStatsMap = adCombo.getNetworkStatMap(userCredentials.get(Network.ADCOMBO),
                    network, dateStart.minusDays(1), dateEnd);

            List<StatDTO> statsEntities = new ArrayList<>();

            for (int offerId : adcomboStatsMap.keySet()) {
                if (adcomboStatsMap.containsKey(offerId)) {
                    assert affiliateNetwork != null;
                    NetworkStats stat = affiliateNetwork.getCombinedStatsByOfferCampaigns(userCredentials.get(network),
                            adcomboStatsMap.get(offerId).getCampaigns(), dateStart, dateEnd);
                    statsEntities.add(StatisticsUtilities.createStatsDTO(offerId, stat, adcomboStatsMap));
                }
            }
            tableStats.add(new TableDTO(statsEntities, network));
        }
        return tableStats;
    }

    ////////////////////////////////////////////////
    //StatDTO contains campaignId = network campaign id
    ////////////////////////////////////////////////
    @Cacheable("campaign_stats")
    public List<StatDTO> getCampaignStats(User user, Network network, LocalDate dateStart, LocalDate dateEnd) {

        AffiliateNetwork affiliateNetwork = null;
        switch (network) {
            case TF -> affiliateNetwork = trafficFactory;
            case EXO -> affiliateNetwork = exoClick;
        }

        assert affiliateNetwork != null;
        Map<Network, Credentials> userCredentials = credentialsService.getUserCredentials(user);

        Map<Integer, NetworkStats> networkStatsMap = affiliateNetwork.getCampaignsStats(userCredentials.get(network),
                dateStart, dateEnd);
        Map<Integer, AdcomboStats> adcomboStatsMap = adCombo.getCampaignStatMap(userCredentials.get(Network.ADCOMBO),
                network, dateStart, dateEnd);

        List<StatDTO> stats = new ArrayList<>();
        for (int campaignId : networkStatsMap.keySet()) {
            if (networkStatsMap.containsKey(campaignId) && adcomboStatsMap.containsKey(campaignId))
                stats.add(StatisticsUtilities.createStatsDTO(campaignId, networkStatsMap, adcomboStatsMap));
        }
        return stats;
    }
}
