package ru.set404.AdsMetrika.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.network.Network;
import ru.set404.AdsMetrika.network.ads.ExoClick;
import ru.set404.AdsMetrika.network.ads.NetworkStats;
import ru.set404.AdsMetrika.network.ads.TrafficFactory;
import ru.set404.AdsMetrika.network.cpa.Adcombo;
import ru.set404.AdsMetrika.network.cpa.AdcomboStats;
import ru.set404.AdsMetrika.repositories.CredentialsRepository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TestConfiguration
public class NetworksServiceTestContextConfiguration {

    @MockBean
    private final CredentialsRepository credentialsRepository;

    @Autowired
    public NetworksServiceTestContextConfiguration(CredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
    }

    @Bean
    public TrafficFactory trafficFactory () {
        return new TrafficFactory(credentialsRepository, new ObjectMapper()) {
            @Override
            public Map<Integer, NetworkStats> getCampaignStatsMap(User user, LocalDate dateStart, LocalDate dateEnd) {
                Map<Integer, NetworkStats> networkStatsMap = new HashMap<>();
                networkStatsMap.put(12345, getNetworkStats());
                networkStatsMap.put(54321, getNetworkStats());
                return networkStatsMap;
            }

            @Override
            public NetworkStats getNetworkStatsByOfferCampaigns(User user, List<Integer> campaigns, LocalDate dateStart, LocalDate dateEnd) {
                return getNetworkStats();
            }
        };
    }

    @Bean
    public ExoClick exoClick () {
        return new ExoClick(credentialsRepository, new ObjectMapper()) {
            @Override
            public Map<Integer, NetworkStats> getCampaignStatsMap(User user, LocalDate dateStart, LocalDate dateEnd) {
                Map<Integer, NetworkStats> networkStatsMap = new HashMap<>();
                networkStatsMap.put(12345, getNetworkStats());
                networkStatsMap.put(54321, getNetworkStats());
                return networkStatsMap;
            }

            @Override
            public NetworkStats getNetworkStatsByOfferCampaigns(User user, List<Integer> campaigns, LocalDate dateStart, LocalDate dateEnd) {
                return getNetworkStats();
            }
        };
    }

    @Bean
    public Adcombo adcombo() {
        return new Adcombo(credentialsRepository, new ObjectMapper()) {
            @Override
            public Map<Integer, AdcomboStats> getNetworkStatMap(User user, Network network, LocalDate dateStart, LocalDate dateEnd) {
                Map<Integer, AdcomboStats> adcomboStatsMap = new HashMap<>();
                adcomboStatsMap.put(12345, getAdcomboStats());
                adcomboStatsMap.put(54321, getAdcomboStats());
                return adcomboStatsMap;
            }

            @Override
            public Map<Integer, AdcomboStats> getCampaignStatMap(User user, Network network, LocalDate dateStart, LocalDate dateEnd) {
                Map<Integer, AdcomboStats> adcomboStatsMap = new HashMap<>();
                adcomboStatsMap.put(12345, getAdcomboStats());
                adcomboStatsMap.put(54321, getAdcomboStats());
                return adcomboStatsMap;
            }
        };
    }
    private NetworkStats getNetworkStats() {
        return new NetworkStats(100, 100);
    }

    private AdcomboStats getAdcomboStats() {
        return new AdcomboStats(12345, "TestName", 100.0, 10, 100.0);
    }
}
