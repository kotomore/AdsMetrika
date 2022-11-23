package ru.set404.AdsMetrika.network.ads;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;
import ru.set404.AdsMetrika.models.Credentials;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

@Component
@SessionScope
public class TrafficFactory implements AffiliateNetwork {

    private String apiToken;
    private String password;
    private final ObjectMapper objectMapper;
    protected Log logger = LogFactory.getLog(this.getClass());


    @Autowired
    public TrafficFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private List<Integer> getCampaignList(Credentials credentials) {
        authorization(credentials);
        String url = "https://main.trafficfactory.biz/webservices/" + apiToken + "/campaigns.json";
        List<Integer> campaigns = new ArrayList<>();
        JsonNode json;
        try {
            json = objectMapper.readTree(parseNetwork(url).body());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Couldn't get statistics from traffic factory. Try later");
        }
        for (JsonNode campaign : json.get("campaigns")) {
            if (campaign.get("daily_spent").asDouble(0) > 0)
                campaigns.add(campaign.get("id").asInt());
        }
        return campaigns;
    }

    public Map<Integer, NetworkStats> getCampaignStatsMap(Credentials credentials, LocalDate dateStart, LocalDate dateEnd) {
        authorization(credentials);
        Map<Integer, NetworkStats> campaignStats = new HashMap<>();
        Map<Integer, Future<NetworkStats>> campaignStatsFuture = new HashMap<>();

        try {
            List<Integer> campaigns = getCampaignList(credentials);
            ExecutorService executor = Executors.newFixedThreadPool(9);
            Future<NetworkStats> networkStats;
            for (Integer campaign : campaigns) {
                if (campaign > 0) {
                    networkStats = executor.submit(() -> {
                        String url = "https://main.trafficfactory.biz/webservices/" + apiToken + "/stats/campaign/" +
                                campaign + "/" + dateStart + "/" + dateEnd + ".json";
                        int deliveries = 0;
                        double total = 0;
                        JsonNode json = objectMapper.readTree(parseNetwork(url).body()).get("stats");
                        for (JsonNode node : json) {
                            deliveries += node.get("deliveries").asInt();
                            total += node.get("total").asDouble();
                        }
                        return new NetworkStats(deliveries, total);

                    });
                    campaignStatsFuture.put(campaign, networkStats);
                }
            }
            for (Integer campaign : campaignStatsFuture.keySet()) {
                campaignStats.put(campaign, campaignStatsFuture.get(campaign).get());
            }
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    logger.error("Parse Traffic Factory timed out error");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                throw new RuntimeException("Something wrong. Try again");

            }

        } catch (Exception e) {
            throw new RuntimeException("Couldn't get statistics from traffic factory. Try later");
        }

        return campaignStats;
    }

    public NetworkStats getNetworkStatsByOfferCampaigns(Credentials credentials, List<Integer> campaigns, LocalDate dateStart, LocalDate dateEnd) {
        authorization(credentials);
        int deliveries = 0;
        double total = 0;

        try {
            ExecutorService executor = Executors.newFixedThreadPool(9);
            Future<JsonNode> networkStatJson;
            List<Future<JsonNode>> networkStatsFuture = new ArrayList<>();
            for (Integer campaign : campaigns) {
                if (campaign > 0) {
                    networkStatJson = executor.submit(() ->
                    {
                        String url = "https://main.trafficfactory.biz/webservices/" + apiToken + "/stats/campaign/" +
                                campaign + "/" + dateStart + "/" + dateEnd + ".json";
                        return objectMapper.readTree(parseNetwork(url).body()).get("stats");
                    });
                    networkStatsFuture.add(networkStatJson);
                }
            }

            for (Future<JsonNode> networkStat : networkStatsFuture) {
                for (JsonNode node : networkStat.get()) {
                    deliveries += node.get("deliveries").asInt();
                    total += node.get("total").asDouble();
                }
            }

            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    logger.error("Parse Traffic Factory timed out error");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
        } catch (Exception e) {
            throw new RuntimeException("Couldn't get statistics from traffic factory. Try later");
        }

        return new NetworkStats(deliveries, total);
    }

    private Connection.Response parseNetwork(String url) {

        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String securityToken = sha1(timestamp + url + password);

        Connection.Response response;
        try {
            response = Jsoup
                    .connect(url)
                    .method(Connection.Method.POST)
                    .data("timestamp", timestamp)
                    .data("security_token", securityToken.toLowerCase())
                    //Filter by campaign status
                    //.data("status", "active")
                    .ignoreContentType(true)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't get statistics from traffic factory. Try later");
        }
        return response;
    }

    private void authorization(Credentials credentials) {
        if (apiToken == null || password == null) {
            this.apiToken = credentials.getUsername();
            this.password = credentials.getPassword();
        }
    }

    private String sha1(String input) {
        String sha1 = null;
        try {
            MessageDigest msdDigest = MessageDigest.getInstance("SHA-1");
            msdDigest.update(input.getBytes(StandardCharsets.UTF_8), 0, input.length());
            sha1 = DatatypeConverter.printHexBinary(msdDigest.digest());
        } catch (NoSuchAlgorithmException ignored) {
        }
        return sha1;
    }
}
