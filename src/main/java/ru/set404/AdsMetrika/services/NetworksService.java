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
import ru.set404.AdsMetrika.network.ads.*;
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
    private final TrafficStars trafficStars;
    private final Adcombo adCombo;
    private final CredentialsService credentialsService;

    @Autowired
    public NetworksService(ExoClick exoClick, TrafficFactory trafficFactory,
                           TrafficStars trafficStars, Adcombo adCombo, CredentialsService credentialsService) {
        this.exoClick = exoClick;
        this.trafficFactory = trafficFactory;
        this.trafficStars = trafficStars;
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

        Map<Network, Credentials> userCredentials = credentialsService.getUserCredentials(user);

        Set<Network> userNetworks = credentialsService.userNetworks(user);
        List<TableDTO> tableStats = new ArrayList<>();

        for (Network network : userNetworks) {
            AffiliateNetwork affiliateNetwork = null;
            switch (network) {
                case TF -> affiliateNetwork = trafficFactory;
                case EXO -> affiliateNetwork = exoClick;
                case STARS -> affiliateNetwork = trafficStars;
            }

            Map<Integer, AdcomboStats> adcomboStatsMap = adCombo.getNetworkStatMap(userCredentials.get(Network.ADCOMBO),
                    network, dateStart.minusDays(1), dateEnd);

            List<StatDTO> statsEntities = new ArrayList<>();

            assert affiliateNetwork != null;
            Map<Integer, NetworkStats> networkStatsMap = affiliateNetwork.getOfferCombinedStats(userCredentials.get(network),
                        adcomboStatsMap, dateStart, dateEnd);

            for (int offerId : networkStatsMap.keySet()) {
                if (adcomboStatsMap.containsKey(offerId)) {
                    statsEntities.add(StatisticsUtilities.createStatsDTO(offerId, networkStatsMap, adcomboStatsMap));
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
            case STARS -> affiliateNetwork = trafficStars;
        }

        assert affiliateNetwork != null;
        Map<Network, Credentials> userCredentials = credentialsService.getUserCredentials(user);

        Map<Integer, NetworkStats> networkStatsMap = affiliateNetwork.getCampaignsStats(userCredentials.get(network),
                dateStart, dateEnd);
        Map<Integer, AdcomboStats> adcomboStatsMap = adCombo.getCampaignStatMap(userCredentials.get(Network.ADCOMBO),
                network, dateStart, dateEnd);

        List<StatDTO> stats = new ArrayList<>();
        for (int campaignId : networkStatsMap.keySet()) {
            if (adcomboStatsMap.containsKey(campaignId))
                stats.add(StatisticsUtilities.createStatsDTO(campaignId, networkStatsMap, adcomboStatsMap));
        }
        return stats;
    }
}
