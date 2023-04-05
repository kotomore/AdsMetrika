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
import ru.set404.AdsMetrika.config.ConfigProperties;
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

    private final ObjectMapper objectMapper;
    private final ConfigProperties config;
    protected Log logger = LogFactory.getLog(this.getClass());


    @Autowired
    public TrafficFactory(ObjectMapper objectMapper, ConfigProperties config) {
        this.objectMapper = objectMapper;
        this.config = config;
    }

    private List<Integer> getCampaignList(Credentials credentials) {
        String url = "https://main.trafficfactory.biz/webservices/" + credentials.getUsername() + "/campaigns.json";
        List<Integer> campaigns = new ArrayList<>();
        JsonNode json;
        try {
            json = objectMapper.readTree(parseNetwork(url, credentials).body());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Couldn't get statistics from traffic factory. Try later");
        }
        for (JsonNode campaign : json.get("campaigns")) {
            campaigns.add(campaign.get("id").asInt());
        }
        return campaigns;
    }

    private Map<Integer, NetworkStats> getStats(List<Integer> campaigns, Credentials credentials, LocalDate dateStart, LocalDate dateEnd) {
        Map<Integer, NetworkStats> campaignStats = new HashMap<>();

        try {
            if (campaigns == null)
                campaigns = getCampaignList(credentials);

            ExecutorService executor = Executors.newFixedThreadPool(9);
            CompletionService<NetworkStats> completionService = new ExecutorCompletionService<>(executor);

            for (Integer campaign : campaigns) {
                if (campaign > 0) {
                    String url = "https://main.trafficfactory.biz/webservices/" + credentials.getUsername() + "/stats/campaign/" +
                            campaign + "/" + dateStart + "/" + dateEnd + ".json";
                    completionService.submit(() -> {
                        int deliveries = 0;
                        double total = 0;
                        JsonNode json = objectMapper.readTree(parseNetwork(url, credentials).body()).get("stats");
                        for (JsonNode node : json) {
                            deliveries += node.get("deliveries").asInt();
                            total += node.get("total").asDouble();
                        }
                        return new NetworkStats(deliveries, total);
                    });
                }
            }

            for (Integer campaign : campaigns) {
                try {
                    Future<NetworkStats> future = completionService.take();
                    NetworkStats stats = future.get();
                    campaignStats.put(campaign, stats);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    logger.error(e.getMessage());
                    throw new RuntimeException("Something wrong. Try again");
                } catch (ExecutionException e) {
                    logger.error(e.getMessage());
                    throw new RuntimeException("Couldn't get statistics from traffic factory. Try later");
                }
            }

            executor.shutdown();
            if (!executor.awaitTermination(1, TimeUnit.MINUTES)) {
                logger.error("Parse Traffic Factory timed out error");
                executor.shutdownNow();
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
            throw new RuntimeException("An error occurred. Try later");
        }

        return campaignStats;
    }



    ///////////////////////////////////////
    //return all campaigns stats by network Map<campaign_id, campaigns_stats>
    ///////////////////////////////////////
    public Map<Integer, NetworkStats> getCampaignsStats(Credentials credentials, LocalDate dateStart, LocalDate dateEnd) {
        return getStats(null, credentials, dateStart, dateEnd);
    }


    ///////////////////////////////////////
    //return all offers stats by network Map<offer_id, offers_stats>
    ///////////////////////////////////////
    @Override
    public Map<Integer, NetworkStats> getOfferCombinedStats(Credentials credentials, Map<Integer, AdcomboStats> adcomboStatsMap, LocalDate dateStart, LocalDate dateEnd) {
        List<Integer> campaigns = adcomboStatsMap.values().stream()
                .flatMap(c -> c.getCampaigns().stream()).toList();
        Map<Integer, NetworkStats> campaignStats = getStats(campaigns, credentials, dateStart, dateEnd);

        Map<Integer, NetworkStats> result = new HashMap<>();
        for (Map.Entry<Integer, AdcomboStats> entry : adcomboStatsMap.entrySet()) {
            int clicks = 0;
            double cost = 0;
            for (Integer campaign : entry.getValue().getCampaigns()) {
                if (campaignStats.containsKey(campaign)) {
                    clicks += campaignStats.get(campaign).getClicks();
                    cost += campaignStats.get(campaign).getCost();
                }
            }
            if (cost > config.getMinCost())
                result.put(entry.getKey(), new NetworkStats(clicks, cost));
        }
        return result;
    }

    private Connection.Response parseNetwork(String url, Credentials credentials) {

        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String securityToken = sha1(timestamp + url + credentials.getPassword());

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
            logger.error(e.getMessage());
            throw new RuntimeException("Couldn't get statistics from traffic factory. Try later");
        }
        return response;
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
