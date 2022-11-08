package ru.set404.AdsMetrika.services;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.network.Network;
import ru.set404.AdsMetrika.repositories.CredentialsRepository;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@RunWith(SpringRunner.class)
@SpringBootTest
public class CredentialsServiceTest {
    @MockBean
    private CredentialsRepository credentialsRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    public void userNetworksTest(){
        User user = new User();
        user.setUsername("User");
        user.setPassword(passwordEncoder.encode("password"));
        user.setRole("ROLE_USER");

        Credentials credentials = new Credentials();
        credentials.setOwner(user);
        credentials.setUsername("Username");
        credentials.setPassword("Password");
        credentials.setNetworkName(Network.TF);
        List<Credentials> credentialsList = new ArrayList<>();
        credentialsList.add(credentials);

        credentials = new Credentials();
        credentials.setOwner(user);
        credentials.setUsername("Username2");
        credentials.setPassword("Password2");
        credentials.setNetworkName(Network.EXO);
        credentialsList.add(credentials);

        when(credentialsRepository.findByOwner(user)).thenReturn(new ArrayList<>(credentialsList));
        CredentialsService credentialsService = new CredentialsService(credentialsRepository, new ModelMapper());

        assertEquals(2, credentialsService.userNetworks(user).size());
        assertTrue(credentialsService.userNetworks(user).containsAll(List.of(Network.TF, Network.EXO)));
    }


}
