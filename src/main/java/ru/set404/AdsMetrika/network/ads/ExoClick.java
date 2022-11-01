package ru.set404.AdsMetrika.network.ads;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.network.Network;
import ru.set404.AdsMetrika.repositories.CredentialsRepository;
import ru.set404.AdsMetrika.security.UserDetails;

import java.io.IOException;
import java.rmi.AccessException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class ExoClick implements AffiliateNetwork {

    private final CredentialsRepository credentialsRepository;
    private final ObjectMapper objectMapper;
    protected Log logger = LogFactory.getLog(this.getClass());

    private static String authToken = null;

    @Autowired
    public ExoClick(CredentialsRepository credentialsRepository, ObjectMapper objectMapper) throws IOException {
        this.credentialsRepository = credentialsRepository;
        this.objectMapper = objectMapper;
    }


    private List<Integer> campaignList() throws IOException {
        authorization();
        List<Integer> campaigns = new ArrayList<>();
        String url = "https://api.exoclick.com/v2/campaigns?status=1";
        for (JsonNode node : objectMapper.readTree(parseNetwork(url).body()).get("result")) {
            campaigns.add(node.get("id").asInt());
        }
        return campaigns;
    }

    private void authorization() throws IOException {
        if (authToken == null) {
            logger.debug("ExoClick authorization...");
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Credentials credentials = credentialsRepository.
                    findCredentialsByOwnerAndNetworkName(((UserDetails) authentication.getPrincipal())
                            .user(), Network.EXO)
                    .orElseThrow(() -> new BadCredentialsException("ExoClick credentials doesn`t exist"));

            String apiToken = credentials.getUsername();

            String jsonBody = """
                    {
                    "api_token": "%s"
                    }
                    """.formatted(apiToken);

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

    public Map<Integer, NetworkStats> getCampaignStatsMap(LocalDate dateStart, LocalDate dateEnd) {
        try {
            authorization();
        } catch (IOException e) {
            throw new RuntimeException("Could not connect to ExoClick" + e.getMessage());
        }

        Map<Integer, NetworkStats> stat = new HashMap<>();

        ExecutorService pool = Executors.newFixedThreadPool(6);
        try {
            for (Integer campaign : campaignList()) {
                pool.execute(() ->
                {
                    try {
                        stat.put(campaign, getNetworkStatEntity(campaignList(), dateStart, dateEnd));
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        pool.shutdown();
        try {
            if (!pool.awaitTermination(1, TimeUnit.MINUTES)) {
                logger.debug("Parse ExoClick timed out error");
            }
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        return stat;
    }

    public NetworkStats getNetworkStatEntity(List<Integer> campaigns, LocalDate dateStart, LocalDate dateEnd)
            throws IOException {

        authorization();
        String url = "";
        int clicks = 0;
        double cost = 0;

        for (Integer campaign : campaigns) {
            url = "https://api.exoclick.com/v2/statistics/a/campaign?campaignid=" + campaign
                    + "&date-to=" + dateStart
                    + "&date-from=" + dateEnd
                    + "&include=totals&detailed=false";
            JsonNode node = objectMapper.readTree(parseNetwork(url).body());
            if (node.hasNonNull("resultTotal")) {
                JsonNode resultTotal = node.get("resultTotal");
                clicks += resultTotal.get("clicks").asInt();
                cost += resultTotal.get("cost").asDouble();
            }
        }
        return new NetworkStats(clicks, cost);
    }

    private Connection.Response parseNetwork(String url) {
        Connection.Response response;
        try {
            response = Jsoup
                    .connect(url)
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .header("Accept", "application/json")
                    .header("Authorization", "Bearer " + authToken)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return response;
    }

}

