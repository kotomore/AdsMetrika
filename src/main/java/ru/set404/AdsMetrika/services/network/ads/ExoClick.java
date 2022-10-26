package ru.set404.AdsMetrika.services.network.ads;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.repositories.CredentialsRepository;
import ru.set404.AdsMetrika.security.UserDetails;
import ru.set404.AdsMetrika.services.network.Network;

import java.io.IOException;
import java.rmi.AccessException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class ExoClick implements NetworkStats {

    private final CredentialsRepository credentialsRepository;
    private final ObjectMapper objectMapper;
    private static String authToken = null;

    @Autowired
    public ExoClick(CredentialsRepository credentialsRepository, ObjectMapper objectMapper) throws IOException {
        this.credentialsRepository = credentialsRepository;
        this.objectMapper = objectMapper;
    }

    private void connectToExoClick() throws IOException {
        if (authToken == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Credentials credentials = credentialsRepository.
                    findCredentialsByOwnerAndNetworkName(((UserDetails) authentication.getPrincipal())
                            .user(), Network.EXO)
                    .orElseThrow(() -> new BadCredentialsException("ExoClick credentials doesn`t exist"));

            String userName = credentials.getUsername();
            String password = credentials.getPassword();

            String jsonBody = """
                    {
                    "password": "%s",
                    "username": "%s"
                    }
                    """.formatted(password, userName);

            Connection.Response response = Jsoup
                    .connect("https://api.exoclick.com/v2/login")
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .header("Content-Type", "application/json")
                    .requestBody(jsonBody)
                    .execute();

            JsonNode token = objectMapper.readTree(response.body()).get("token");
            if (token.isNull())
                throw new AccessException("Could not connect to ExoClick");
            authToken = token.asText();
        }
    }

    public Map<Integer, NetworkStatEntity> getStat(Map<Integer, String> networkOffers, LocalDate dateStart,
                                                   LocalDate dateEnd) throws InterruptedException {
        try {
            connectToExoClick();
        } catch (IOException e) {
            throw new RuntimeException("Could not connect to ExoClick" + e.getMessage());
        }

        Map<Integer, NetworkStatEntity> stat = new HashMap<>();

        ExecutorService pool = Executors.newFixedThreadPool(6);
        for (Map.Entry<Integer, String> networkOffer : networkOffers.entrySet()) {
            pool.execute(() ->
                    stat.put(networkOffer.getKey(), parseNetwork(networkOffer.getValue(), dateStart, dateEnd)));
        }
        pool.shutdown();
        if (!pool.awaitTermination(1, TimeUnit.MINUTES)) {
            System.out.println("Parse ExoClick timed out error");
        }
        return stat;
    }

    private NetworkStatEntity parseNetwork(String group, LocalDate dateStart, LocalDate dateEnd) {
        System.out.println("Parse group - " + group);
        Connection.Response response;
        try {
            response = Jsoup
                    .connect("https://api.exoclick.com/v2/statistics/a/campaign?groupid="
                            + group
                            + "&date-to="
                            + dateStart + "&date-from="
                            + dateEnd
                            + "&include=totals&detailed=false")
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = null;
        try {
            node = mapper.readTree(response.body());
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        int clicks = 0;
        double cost = 0;

        if (node.hasNonNull("resultTotal")) {
            JsonNode resultTotal = node.get("resultTotal");
            clicks = resultTotal.get("clicks").asInt();
            cost = resultTotal.get("cost").asDouble();
        }
        return new NetworkStatEntity(clicks, cost);
    }

}

