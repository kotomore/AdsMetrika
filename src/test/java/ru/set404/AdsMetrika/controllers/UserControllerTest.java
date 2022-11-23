package ru.set404.AdsMetrika.controllers;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.models.Settings;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.network.Network;
import ru.set404.AdsMetrika.repositories.CredentialsRepository;
import ru.set404.AdsMetrika.security.UserDetails;
import ru.set404.AdsMetrika.services.SettingsService;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.nullValue;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class UserControllerTest {
    @Autowired
    @MockBean
    private CredentialsRepository credentialsRepository;
    @MockBean
    private SettingsService settingsService;

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void test_statistics_page_with_guest_role_without_dates() throws Exception {
        User user = getNewUser();
        Settings settings = getSettings(user);
        user.setSettings(settings);
        user.setRole("ROLE_GUEST");
        UserDetails userDetails = new UserDetails(user);
        when(settingsService.userSettings(user)).thenReturn(settings);
        mockMvc.perform(get("/statistics")
                        .with(user(userDetails))
                )
                .andExpect(status().is(200))
                .andExpect(model().attribute("currentDate", LocalDate.now()))
                .andExpect(model().attribute("username", user.getUsername()))
                .andExpect(model().attribute("statistics", notNullValue()))
                .andExpect(model().hasNoErrors());
    }

    @Test
    public void test_statistics_page_with_guest_role_with_dates_and_credentials() throws Exception {
        User user = getNewUser();
        Settings settings = getSettings(user);
        user.setSettings(settings);
        List<Credentials> credentials = getCredentials(user);
        user.setCredentials(credentials);
        user.setRole("ROLE_GUEST");
        UserDetails userDetails = new UserDetails(user);
        when(settingsService.userSettings(user)).thenReturn(settings);
        when(credentialsRepository.findByOwner(user)).thenReturn(credentials);
        mockMvc.perform(get("/statistics?ds=2022-11-07&de=2022-11-08")
                        .with(user(userDetails))
                )
                .andExpect(status().is(200))
                .andExpect(model().attribute("currentDate", LocalDate.now()))
                .andExpect(model().attribute("username", user.getUsername()))
                .andExpect(model().attribute("combinedStats", notNullValue()))
                .andExpect(model().attribute("chartCosts", notNullValue()))
                .andExpect(model().attribute("chartTotal", notNullValue()))
                .andExpect(model().attribute("statistics", notNullValue()))
                .andExpect(model().hasNoErrors());
    }

    @Test
    public void test_report_page_with_guest_role_without_types() throws Exception {
        User user = getNewUser();
        user.setRole("ROLE_GUEST");
        UserDetails userDetails = new UserDetails(user);
        mockMvc.perform(get("/report")
                        .with(user(userDetails))
                )
                .andExpect(status().is(200))
                .andExpect(model().attribute("username", user.getUsername()))
                .andExpect(model().attribute("combinedStats", notNullValue()))
                .andExpect(model().hasNoErrors());
    }

    @Test
    public void test_report_page_with_guest_role_with_type_daily_and_credentials() throws Exception {
        User user = getNewUser();
        Settings settings = getSettings(user);
        user.setSettings(settings);
        List<Credentials> credentials = getCredentials(user);
        user.setCredentials(credentials);
        user.setRole("ROLE_GUEST");
        UserDetails userDetails = new UserDetails(user);

        when(settingsService.userSettings(user)).thenReturn(settings);
        when(credentialsRepository.findByOwner(user)).thenReturn(credentials);
        mockMvc.perform(get("/statistics?type=daily")
                        .with(user(userDetails))
                )
                .andExpect(status().is(200))
                .andExpect(model().attribute("username", user.getUsername()))
                .andExpect(model().attribute("combinedStats", notNullValue()))
                .andExpect(model().hasNoErrors());
    }

    @Test
    public void test_report_page_with_guest_role_with_type_monthly_and_credentials() throws Exception {
        User user = getNewUser();
        Settings settings = getSettings(user);
        user.setSettings(settings);
        List<Credentials> credentials = getCredentials(user);
        user.setCredentials(credentials);
        user.setRole("ROLE_GUEST");
        user.setSettings(getSettings(user));
        UserDetails userDetails = new UserDetails(user);

        when(settingsService.userSettings(user)).thenReturn(settings);
        when(credentialsRepository.findByOwner(user)).thenReturn(credentials);
        mockMvc.perform(get("/statistics?type=monthly")
                        .with(user(userDetails))
                )
                .andExpect(status().is(200))
                .andExpect(model().attribute("username", user.getUsername()))
                .andExpect(model().attribute("combinedStats", notNullValue()))
                .andExpect(model().attribute("error", notNullValue()))
                .andExpect(model().hasNoErrors());
    }

    @Test
    public void test_campaigns_page_with_guest_role_without_dates() throws Exception {
        User user = getNewUser();
        user.setRole("ROLE_GUEST");
        UserDetails userDetails = new UserDetails(user);
        mockMvc.perform(get("/campaigns")
                        .with(user(userDetails))
                )
                .andExpect(status().is(200))
                .andExpect(model().attribute("currentDate", LocalDate.now()))
                .andExpect(model().attribute("username", user.getUsername()))
                .andExpect(model().attribute("combinedStats", notNullValue()))
                .andExpect(model().hasNoErrors());
    }

    @Test
    public void test_campaigns_page_with_guest_role_with_network_TF_and_dates_and_credentials() throws Exception {
        User user = getNewUser();
        List<Credentials> credentials = getCredentials(user);
        user.setCredentials(credentials);
        user.setRole("ROLE_GUEST");
        UserDetails userDetails = new UserDetails(user);

        when(credentialsRepository.findByOwner(user)).thenReturn(credentials);
        mockMvc.perform(get("/campaigns?network=TF&ds=2022-11-07&de=2022-11-08")
                        .with(user(userDetails))
                )
                .andExpect(status().is(200))
                .andExpect(model().attribute("username", user.getUsername()))
                .andExpect(model().attribute("combinedStats", Matchers.iterableWithSize(15)))
                .andExpect(model().attribute("error", nullValue()))
                .andExpect(model().hasNoErrors());
    }

    @Test
    public void test_campaigns_page_with_guest_role_with_network_EXO_and_dates_and_credentials() throws Exception {
        User user = getNewUser();
        List<Credentials> credentials = getCredentials(user);
        user.setCredentials(credentials);
        user.setRole("ROLE_GUEST");
        UserDetails userDetails = new UserDetails(user);

        when(credentialsRepository.findByOwner(user)).thenReturn(credentials);
        mockMvc.perform(get("/campaigns?network=EXO&ds=2022-11-07&de=2022-11-08")
                        .with(user(userDetails))
                )
                .andExpect(status().is(200))
                .andExpect(model().attribute("username", user.getUsername()))
                .andExpect(model().attribute("combinedStats", Matchers.iterableWithSize(15)))
                .andExpect(model().attribute("error", nullValue()))
                .andExpect(model().hasNoErrors());
    }

    @Test
    public void test_credentials_save_with_guest_role_right_credentials() throws Exception {
        User user = getNewUser();
        user.setRole("ROLE_GUEST");
        UserDetails userDetails = new UserDetails(user);

        mockMvc.perform(post("/credentials/save")
                        .with(user(userDetails))
                        .param("username", "paramAPI")
                        .param("networkName", "EXO")
                        .with(csrf())
                )
                .andExpect(status().is(302))
                .andExpect(redirectedUrl("/statistics"))
                .andExpect(model().hasNoErrors());
    }

    @Test
    public void test_credentials_save_with_guest_role_bad_credentials() throws Exception {
        User user = getNewUser();
        user.setRole("ROLE_GUEST");
        UserDetails userDetails = new UserDetails(user);

        mockMvc.perform(post("/credentials/save")
                        .with(user(userDetails))
                        .param("username", "paramAPI")
                        .param("password", "")
                        .param("networkName", "TF")
                        .with(csrf())
                )
                .andExpect(status().is(302))
                .andExpect(redirectedUrl("/statistics?password_error"))
                .andExpect(model().hasNoErrors())
                .andDo(print());
    }

    private User getNewUser() {
        User user = new User();
        user.setUsername("User");
        user.setPassword("Password");
        user.setRole("ROLE_USER");
        return user;
    }

    private Settings getSettings(User user) {
        Settings settings = new Settings();
        settings.setAdcomboId("123");
        settings.setTelegramEnabled(false);
        settings.setSpreadSheetScheduleEnabled(false);
        settings.setSpreadSheetEnabled(false);
        settings.setSpreadSheetId("1231");
        settings.setOwner(user);
        settings.setTelegramUsername("123");
        return settings;
    }
    private List<Credentials> getCredentials(User user) {
        List<Credentials> credentialsList = new ArrayList<>();
        Credentials credentials = new Credentials();
        credentials.setNetworkName(Network.TF);
        credentials.setUsername("tf");
        credentials.setPassword("123");
        credentials.setOwner(user);
        credentialsList.add(credentials);

        credentials = new Credentials();
        credentials.setNetworkName(Network.EXO);
        credentials.setUsername("exo");
        credentials.setOwner(user);
        credentialsList.add(credentials);

        credentials = new Credentials();
        credentials.setNetworkName(Network.ADCOMBO);
        credentials.setUsername("adcombo");
        credentials.setOwner(user);
        credentialsList.add(credentials);

        return credentialsList;
    }
}
