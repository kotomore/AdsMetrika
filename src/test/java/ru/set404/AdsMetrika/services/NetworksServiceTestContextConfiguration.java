package ru.set404.AdsMetrika.services;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import ru.set404.AdsMetrika.config.ConfigProperties;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.network.Network;
import ru.set404.AdsMetrika.network.ads.ExoClick;
import ru.set404.AdsMetrika.network.ads.NetworkStats;
import ru.set404.AdsMetrika.network.ads.TrafficFactory;
import ru.set404.AdsMetrika.network.ads.TrafficStars;
import ru.set404.AdsMetrika.network.cpa.Adcombo;
import ru.set404.AdsMetrika.network.cpa.AdcomboStats;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@TestConfiguration
public class NetworksServiceTestContextConfiguration {
    @Bean
    public TrafficFactory trafficFactory () {
        return new TrafficFactory(new ObjectMapper(), new ConfigProperties()) {
            @Override
            public Map<Integer, NetworkStats> getCampaignsStats(Credentials credentials, LocalDate dateStart, LocalDate dateEnd) {
                Map<Integer, NetworkStats> networkStatsMap = new HashMap<>();
                networkStatsMap.put(12345, getNetworkStats());
                networkStatsMap.put(54321, getNetworkStats());
                return networkStatsMap;
            }
        };
    }

    @Bean
    public TrafficStars trafficStars () {
        return new TrafficStars(new ObjectMapper()) {
            @Override
            public Map<Integer, NetworkStats> getCampaignsStats(Credentials credentials, LocalDate dateStart, LocalDate dateEnd) {
                Map<Integer, NetworkStats> networkStatsMap = new HashMap<>();
                networkStatsMap.put(12345, getNetworkStats());
                networkStatsMap.put(54321, getNetworkStats());
                return networkStatsMap;
            }
        };
    }

    @Bean
    public ExoClick exoClick () {
        return new ExoClick(new ObjectMapper()) {
            @Override
            public Map<Integer, NetworkStats> getCampaignsStats(Credentials credentials, LocalDate dateStart, LocalDate dateEnd) {
                Map<Integer, NetworkStats> networkStatsMap = new HashMap<>();
                networkStatsMap.put(12345, getNetworkStats());
                networkStatsMap.put(54321, getNetworkStats());
                return networkStatsMap;
            }
        };
    }

    @Bean
    public Adcombo adcombo() {
        return new Adcombo(new ObjectMapper(), new ConfigProperties()) {
            @Override
            public Map<Integer, AdcomboStats> getNetworkStatMap(Credentials credentials, Network network, LocalDate dateStart, LocalDate dateEnd) {
                Map<Integer, AdcomboStats> adcomboStatsMap = new HashMap<>();
                adcomboStatsMap.put(12345, getAdcomboStatsWithCampaigns());
                adcomboStatsMap.put(54321, getAdcomboStatsWithCampaigns());
                return adcomboStatsMap;
            }

            @Override
            public Map<Integer, AdcomboStats> getCampaignStatMap(Credentials credentials, Network network, LocalDate dateStart, LocalDate dateEnd) {
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

    private AdcomboStats getAdcomboStatsWithCampaigns() {
        AdcomboStats adcomboStats = new AdcomboStats(12345, "TestName", 100.0, 10, 100.0);
        adcomboStats.setCampaigns(List.of(12345, 54321));
        return adcomboStats;
    }
}
