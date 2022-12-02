package ru.set404.AdsMetrika.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import ru.set404.AdsMetrika.dto.StatDTO;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.network.Network;
import ru.set404.AdsMetrika.network.ads.ExoClick;
import ru.set404.AdsMetrika.network.ads.TrafficFactory;
import ru.set404.AdsMetrika.network.ads.TrafficStars;
import ru.set404.AdsMetrika.network.cpa.Adcombo;

import java.time.LocalDate;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@Import(NetworksServiceTestContextConfiguration.class)
public class NetworksServiceTest {
    @Autowired
    private TrafficFactory trafficFactory;
    @Autowired
    private ExoClick exoClick;
    @Autowired
    private Adcombo adcombo;
    @Autowired
    private TrafficStars trafficStars;
    @MockBean
    private CredentialsService credentialsService;

    @Test
    public void getNetworkStatisticsListTest() {
        User user = new User();
        user.setRole("ROLE_GUEST");
        Credentials credentialsAdcombo = new Credentials();
        credentialsAdcombo.setNetworkName(Network.ADCOMBO);
        credentialsAdcombo.setUsername("credentials");
        credentialsAdcombo.setOwner(user);
        Credentials credentialsTF = new Credentials();
        credentialsTF.setNetworkName(Network.TF);
        credentialsTF.setUsername("credentials");
        credentialsTF.setPassword("password");
        credentialsTF.setOwner(user);

        user.setCredentials(List.of(credentialsAdcombo, credentialsTF));

        when(credentialsService.userNetworks(user)).thenReturn(Set.of(Network.TF));

        NetworksService networksService = new NetworksService(exoClick, trafficFactory, trafficStars, adcombo, credentialsService);
        assertEquals(2, networksService.getOfferStats(user, LocalDate.now(), LocalDate.now()).get(0).getCurrentStats().size());
        assertTrue(networksService.getOfferStats(user, LocalDate.now(), LocalDate.now()).get(0).getCurrentStats().contains(
                new StatDTO(12345, "TestName", 100, 100.0, 100.0, 10, 100.0)));
    }

    @Test
    public void getCampaignStatsTest() {
        User user = new User();
        user.setRole("ROLE_GUEST");
        Credentials credentialsAdcombo = new Credentials();
        credentialsAdcombo.setNetworkName(Network.ADCOMBO);
        credentialsAdcombo.setUsername("credentials");
        Credentials credentialsTF = new Credentials();
        credentialsTF.setNetworkName(Network.TF);
        credentialsTF.setUsername("credentials");
        credentialsTF.setPassword("password");
        user.setCredentials(List.of(credentialsAdcombo, credentialsTF));


        NetworksService networksService = new NetworksService(exoClick, trafficFactory, trafficStars, adcombo, credentialsService);
        assertEquals(2, networksService.getCampaignStats(user, Network.TF, LocalDate.now(), LocalDate.now()).size());
        assertTrue(networksService.getCampaignStats(user, Network.TF, LocalDate.now(), LocalDate.now()).contains(
                new StatDTO(12345, "TestName", 100, 100.0, 100.0, 10, 100.0)));
    }

}
