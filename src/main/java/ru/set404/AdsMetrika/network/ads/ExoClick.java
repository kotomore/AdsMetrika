package ru.set404.AdsMetrika.network.ads;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.network.Network;
import ru.set404.AdsMetrika.repositories.CredentialsRepository;

import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;

@Component
@SessionScope
public class ExoClick implements AffiliateNetwork {

    private final CredentialsRepository credentialsRepository;
    private final ObjectMapper objectMapper;
    protected Log logger = LogFactory.getLog(this.getClass());
    private static String authToken = null;

    @Autowired
    public ExoClick(CredentialsRepository credentialsRepository, ObjectMapper objectMapper) {
        this.credentialsRepository = credentialsRepository;
        this.objectMapper = objectMapper;
    }

    private void authorization(User user) {
        if (authToken == null || !isAuth()) {
            logger.debug("ExoClick authorization...");
            Credentials credentials = credentialsRepository.
                    findCredentialsByOwnerAndNetworkName((user), Network.EXO)
                    .orElseThrow(() -> new RuntimeException("ExoClick API not found"));

            String apiToken = credentials.getUsername();

            String jsonBody = """
                    {
                    "api_token": "%s"
                    }
                    """.formatted(apiToken);
            Connection.Response response;
            JsonNode token;
            try {
                response = Jsoup
                        .connect("https://api.exoclick.com/v2/login")
                        .method(Connection.Method.POST)
                        .ignoreContentType(true)
                        .header("Content-Type", "application/json")
                        .requestBody(jsonBody)
                        .execute();
            } catch (IOException e) {
                throw new RuntimeException("Couldn't connect to ExoClick. Check API");
            }

            try {
                token = objectMapper.readTree(response.body()).get("token");
            } catch (JsonProcessingException e) {
                throw new RuntimeException("Couldn't get statistics from ExoClick");
            }

            if (token.isNull())
                throw new RuntimeException("Couldn't connect to ExoClick. Check API");
            authToken = token.asText();
        }
    }

    public Map<Integer, NetworkStats> getCampaignStatsMap(User user, LocalDate dateStart, LocalDate dateEnd) {
        authorization(user);
        Map<Integer, NetworkStats> stat = new HashMap<>();
        try {
            String url = "https://api.exoclick.com/v2/statistics/a/campaign"
                    + "?date-to=" + dateStart
                    + "&date-from=" + dateEnd;
            JsonNode jsonNode = objectMapper.readTree(parseNetwork(url).body());
            for (JsonNode node : jsonNode.get("result")) {
                stat.put(node.get("idcampaign").asInt(),
                        new NetworkStats(node.get("clicks").asInt(), node.get("cost").asDouble()));
            }
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Couldn't get statistics from ExoClick. Check API");
        }
        return stat;
    }

    public NetworkStats getNetworkStatsByOfferCampaigns(User user, List<Integer> campaigns, LocalDate dateStart, LocalDate dateEnd) {
        authorization(user);
        int clicks = 0;
        double cost = 0;

        List<Future<JsonNode>> statFuture = new ArrayList<>();
        Future<JsonNode> networkStatsJson;
        ExecutorService executor = Executors.newFixedThreadPool(9);

        try {
            for (Integer campaign : campaigns) {
                networkStatsJson = executor.submit(() -> {
                    String url = "https://api.exoclick.com/v2/statistics/a/campaign?campaignid=" + campaign
                            + "&date-to=" + dateStart
                            + "&date-from=" + dateEnd
                            + "&include=totals&detailed=false";
                    return objectMapper.readTree(parseNetwork(url).body());
                });
                statFuture.add(networkStatsJson);
            }
            for (Future<JsonNode> node : statFuture) {
                if (node.get().hasNonNull("resultTotal")) {
                    JsonNode resultTotal = node.get().get("resultTotal");
                    clicks += resultTotal.get("clicks").asInt();
                    cost += resultTotal.get("cost").asDouble();
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Couldn't get statistics from ExoClick. Check API");
        }
        executor.shutdown();
        try {
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                logger.error("Parse ExoClick timed out error");
                executor.shutdownNow();
            }
        } catch (InterruptedException e) {
            executor.shutdownNow();
        }
        return new NetworkStats(clicks, cost);
    }

    private Connection.Response parseNetwork(String url) throws IOException {
        Connection.Response response;
        response = Jsoup
                .connect(url)
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .header("Accept", "application/json")
                .header("Authorization", "Bearer " + authToken)
                .execute();
        return response;
    }

    private boolean isAuth() {
        try {
            Jsoup.connect("https://api.exoclick.com/v2/statistics/a/campaign")
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .execute();
        } catch (HttpStatusException e) {
            if (e.getStatusCode() == 401)
                return false;
        } catch (IOException e) {
            throw new RuntimeException("Couldn't connect to ExoClick. Check API");
        }

        return true;
    }
}

