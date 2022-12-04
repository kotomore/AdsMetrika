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
import ru.set404.AdsMetrika.network.cpa.AdcomboStats;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.*;

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

    ///////////////////////////////////////
    //return all campaigns stats by network Map<campaign_id, network_stats>
    ///////////////////////////////////////
    public Map<Integer, NetworkStats> getCampaignsStats(Credentials credentials, LocalDate dateStart, LocalDate dateEnd) {
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
                NetworkStats stats = campaignStatsFuture.get(campaign).get();
                if (stats.getCost() > 0)
                    campaignStats.put(campaign, stats);
            }
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    logger.error("Parse Traffic Factory timed out error");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                logger.info(e.getMessage());
                throw new RuntimeException("Something wrong. Try again");

            }

        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new RuntimeException("Couldn't get statistics from traffic factory. Try later");
        }

        return campaignStats;
    }

    ///////////////////////////////////////
    //return combined stats by list of campaigns
    ///////////////////////////////////////
    public Map<Integer, NetworkStats> getOfferCombinedStats(Credentials credentials, Map<Integer, AdcomboStats> adcomboStatsMap, LocalDate dateStart, LocalDate dateEnd) {
        authorization(credentials);
        Map<Integer, NetworkStats> result = new HashMap<>();

        Future<JsonNode> networkStatJson;
        ExecutorService executor = Executors.newFixedThreadPool(9);
        Map<Integer, List<Future<JsonNode>>> combinedStatsFuture = new HashMap<>();

        try {
            //cycle for all adcombo offers
            for (AdcomboStats adcomboStats : adcomboStatsMap.values()) {
                List<Future<JsonNode>> statFuture = new ArrayList<>();
                //cycle for all offer campaigns
                for (Integer campaign : adcomboStats.getCampaigns()) {
                    if (campaign > 0) {
                        networkStatJson = executor.submit(() ->
                        {
                            String url = "https://main.trafficfactory.biz/webservices/" + apiToken + "/stats/campaign/" +
                                    campaign + "/" + dateStart + "/" + dateEnd + ".json";
                            return objectMapper.readTree(parseNetwork(url).body()).get("stats");
                        });
                        statFuture.add(networkStatJson);
                    }
                }
                combinedStatsFuture.put(adcomboStats.getOfferId(), statFuture);
            }

            for (Map.Entry<Integer, List<Future<JsonNode>>> entry : combinedStatsFuture.entrySet()) {
                int deliveries = 0;
                double total = 0;
                for (Future<JsonNode> networkStat : entry.getValue()) {
                    for (JsonNode node : networkStat.get()) {
                        deliveries += node.get("deliveries").asInt();
                        total += node.get("total").asDouble();
                    }
                }
                if (total > 0)
                    result.put(entry.getKey(), new NetworkStats(deliveries, total));
            }

            executor.shutdown();

            try {
                if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                    logger.error("Parse Traffic Factory timed out error");
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                logger.info(e.getMessage());
                executor.shutdownNow();
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new RuntimeException("Couldn't get statistics from traffic factory. Try later");
        }

        return result;
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
            logger.info(e.getMessage());
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
