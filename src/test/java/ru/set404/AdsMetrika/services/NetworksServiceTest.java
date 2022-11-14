package ru.set404.AdsMetrika.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import ru.set404.AdsMetrika.dto.StatDTO;
import ru.set404.AdsMetrika.network.Network;
import ru.set404.AdsMetrika.network.ads.ExoClick;
import ru.set404.AdsMetrika.network.ads.TrafficFactory;
import ru.set404.AdsMetrika.network.cpa.Adcombo;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@RunWith(SpringRunner.class)
@Import(NetworksServiceTestContextConfiguration.class)
public class NetworksServiceTest {
    @Autowired
    private TrafficFactory trafficFactory;
    @Autowired
    private ExoClick exoClick;
    @Autowired
    private Adcombo adcombo;

    @Test
    public void getNetworkStatisticsListTest() {
        NetworksService networksService = new NetworksService(exoClick, trafficFactory, adcombo);
        assertEquals(2, networksService.getNetworkStatisticsList(Network.TF, LocalDate.now(), LocalDate.now()).size());
        assertTrue(networksService.getNetworkStatisticsList(Network.TF, LocalDate.now(), LocalDate.now()).contains(
                new StatDTO(12345, "TestName", 100, 100.0, 100.0, 10, 100.0)));
    }

    @Test
    public void getCampaignStatsTest() {
        NetworksService networksService = new NetworksService(exoClick, trafficFactory, adcombo);
        assertEquals(2, networksService.getCampaignStats(Network.TF, LocalDate.now(), LocalDate.now()).size());
        assertTrue(networksService.getNetworkStatisticsList(Network.TF, LocalDate.now(), LocalDate.now()).contains(
                new StatDTO(12345, "TestName", 100, 100.0, 100.0, 10, 100.0)));
    }

}
