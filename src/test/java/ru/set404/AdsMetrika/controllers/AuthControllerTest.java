package ru.set404.AdsMetrika.controllers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.repositories.UsersRepository;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {

    @Autowired
    @MockBean
    private UsersRepository repository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Test
    public void registration_new_user_right_credentials() throws Exception {
        User user = getNewUser();
        mockMvc.perform(
                        post("/registration")
                                .param("username", user.getUsername())
                                .param("password", user.getPassword())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf())
                )
                .andExpect(status().is(200))
                .andExpect(model().attribute("success", true));

    }

    @Test
    public void registration_new_user_wrong_credentials() throws Exception {
        User user = getNewUser();
        user.setPassword(null);
        mockMvc.perform(
                        post("/registration")
                                .param("username", user.getUsername())
                                .param("password", user.getPassword())
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf())
                )
                .andExpect(status().is(200))
                .andExpect(model().attribute("hasError", true));
    }

    @Test
    public void login_new_user_right_credentials() throws Exception {
        User user = getNewUser();
        String password = user.getPassword();
        user.setPassword(passwordEncoder.encode(password));
        when(repository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        mockMvc.perform(
                        post("/process_login")
                                .param("username", user.getUsername())
                                .param("password", password)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf())
                )
                .andExpect(status().is(302))
                .andExpect(redirectedUrl("/statistics"));
    }

    @Test
    public void login_new_user_bad_credentials() throws Exception {
        User user = getNewUser();
        String password = user.getPassword();
        user.setPassword(passwordEncoder.encode(password));
        when(repository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        mockMvc.perform(
                        post("/process_login")
                                .param("username", "Some Name")
                                .param("password", password)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf())
                )
                .andExpect(status().is(302))
                .andExpect(redirectedUrl("/?error"));
    }

    @Test
    public void login_with_admin_should_be_redirected_to_admin_page() throws Exception {
        User user = getNewUser();
        String password = user.getPassword();
        user.setRole("ROLE_ADMIN");
        user.setPassword(passwordEncoder.encode(password));
        when(repository.findByUsername(user.getUsername())).thenReturn(Optional.of(user));
        mockMvc.perform(
                        post("/process_login")
                                .param("username", user.getUsername())
                                .param("password", password)
                                .contentType(MediaType.APPLICATION_JSON)
                                .with(csrf())
                )
                .andExpect(status().is(302))
                .andExpect(redirectedUrl("/admin"));
    }

    private User getNewUser() {
        User user = new User();
        user.setUsername("User");
        user.setPassword("Password");
        user.setRole("ROLE_USER");
        return user;
    }

}
