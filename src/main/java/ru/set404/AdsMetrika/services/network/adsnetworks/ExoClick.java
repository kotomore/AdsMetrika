package ru.set404.AdsMetrika.services.network.adsnetworks;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
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
public class ExoClick implements NetworkStats{

    private final CredentialsRepository credentialsRepository;
    private static String authToken = null;

    @Autowired
    public ExoClick(CredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
    }

    private void getAuthToken() throws IOException {

        if (authToken == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Credentials credentials = credentialsRepository.
                    findCredentialsByOwnerAndNetworkName(((UserDetails) authentication.getPrincipal()).user(),
                            Network.EXO);

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

            ObjectMapper mapper = new ObjectMapper();
            JsonNode token = mapper.readTree(response.body()).get("token");
            if (token.isNull())
                throw new AccessException("Could not connect to ExoClick");
            authToken = token.asText();
        }
    }

    public Map<Integer, NetworkStatEntity> getStat(Map<Integer, String> networkOffer, LocalDate dateStart,
                                                   LocalDate dateEnd)
            throws IOException, InterruptedException {
        getAuthToken();
        Map<Integer, NetworkStatEntity> stat = new HashMap<>();
        String token = authToken;

        ExecutorService service = Executors.newFixedThreadPool(6);
        for (Integer offerId : networkOffer.keySet()) {
            String groupId = networkOffer.get(offerId);
            service.execute(() -> {
                try {
                    stat.put(offerId, parse(groupId, token, dateStart, dateEnd));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        }
        service.shutdown();
        if (!service.awaitTermination(1, TimeUnit.MINUTES)) {
            System.out.println("Parse ExoClick timed out error");
        }
        return stat;
    }

    private NetworkStatEntity parse(String group, String token, LocalDate dateStart, LocalDate dateEnd) throws IOException {
        System.out.println("Parse group - " + group);
        Connection.Response response = Jsoup
                .connect("https://api.exoclick.com/v2/statistics/a/campaign?groupid="
                        + group
                        + "&date-to="
                        + dateStart + "&date-from="
                        + dateEnd.minusDays(1)
                        + "&include=totals&detailed=false")
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + token)
                .execute();

        ObjectMapper mapper = new ObjectMapper();
        JsonNode node = mapper.readTree(response.body());

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

