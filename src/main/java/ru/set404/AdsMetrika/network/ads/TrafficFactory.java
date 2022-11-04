package ru.set404.AdsMetrika.network.ads;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.network.Network;
import ru.set404.AdsMetrika.repositories.CredentialsRepository;
import ru.set404.AdsMetrika.security.UserDetails;

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
public class TrafficFactory implements AffiliateNetwork {

    private String apiToken;
    private String password;
    private final CredentialsRepository credentialsRepository;
    private final ObjectMapper objectMapper;

    @Autowired
    public TrafficFactory(CredentialsRepository credentialsRepository, ObjectMapper objectMapper) {
        this.credentialsRepository = credentialsRepository;
        this.objectMapper = objectMapper;
    }

    private List<Integer> getCampaignList() {
        authorization();
        String url = "https://main.trafficfactory.biz/webservices/" + apiToken + "/campaigns.json";
        List<Integer> campaigns = new ArrayList<>();
        JsonNode json;
        try {
            json = objectMapper.readTree(parseNetwork(url).body());
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Couldn't get statistics from traffic factory. Try later");
        }
        for (JsonNode campaign : json.get("campaigns")) {
            campaigns.add(campaign.get("id").asInt());
        }
        return campaigns;
    }

    public Map<Integer, NetworkStats> getCampaignStatsMap(LocalDate dateStart, LocalDate dateEnd) {
        authorization();
        Map<Integer, NetworkStats> campaignStats = new HashMap<>();
        Map<Integer, Future<NetworkStats>> campaignStatsFuture = new HashMap<>();


        try {
            List<Integer> campaigns = getCampaignList();
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

    public NetworkStats getNetworkStatEntity(List<Integer> campaigns, LocalDate dateStart, LocalDate dateEnd) {
        authorization();
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
                    .data("status", "active")
                    .ignoreContentType(true)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException("Couldn't get statistics from traffic factory. Try later");
        }
        return response;
    }

    private void authorization() {
        if (apiToken == null || password == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Credentials credentials = credentialsRepository.
                    findCredentialsByOwnerAndNetworkName(((UserDetails) authentication.getPrincipal())
                            .user(), Network.TF).orElseThrow(() ->
                            new RuntimeException("Couldn't connect to Traffic Factory. Check API"));
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
