package ru.set404.AdsMetrika.network.ads;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
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


    private List<Integer> getCampaignList() throws IOException {
        authorization();
        String url = "https://main.trafficfactory.biz/webservices/" + apiToken + "/campaigns.json";
        List<Integer> campaigns = new ArrayList<>();
        JsonNode json = objectMapper.readTree(parseNetwork(url).body());
        for (JsonNode campaign : json.get("campaigns")) {
            campaigns.add(campaign.get("id").asInt());
        }
        return campaigns;
    }

    public Map<Integer, NetworkStats> getCampaignStatsMap(LocalDate dateStart, LocalDate dateEnd) {
        authorization();
        List<Integer> campaigns = null;
        Map<Integer, NetworkStats> campaignStats = new HashMap<>();

        try {
            campaigns = getCampaignList();
            ExecutorService pool = Executors.newFixedThreadPool(6);
            for (Integer campaign : campaigns) {
                if (campaign > 0) {
                    JsonNode json = parseCampaigStat(dateStart, dateEnd, pool, campaign);
                    int deliveries = 0;
                    double total = 0;
                    for (JsonNode node : json) {
                        deliveries += node.get("deliveries").asInt();
                        total += node.get("total").asDouble();
                    }
                    campaignStats.put(campaign, new NetworkStats(deliveries, total));
                }
            }
        } catch (Exception ignore) {
        }

        return campaignStats;
    }

    public NetworkStats getNetworkStatEntity(List<Integer> campaigns, LocalDate dateStart, LocalDate dateEnd)
            throws IOException {
        authorization();
        int deliveries = 0;
        double total = 0;

        try {
            ExecutorService pool = Executors.newFixedThreadPool(6);
            for (Integer campaign : campaigns) {
                if (campaign > 0) {
                    JsonNode json = parseCampaigStat(dateStart, dateEnd, pool, campaign);
                    for (JsonNode node : json) {
                        deliveries += node.get("deliveries").asInt();
                        total += node.get("total").asDouble();
                    }
                }
            }
        } catch (Exception ignore) {
        }

        return new NetworkStats(deliveries, total);
    }

    private Connection.Response parseNetwork(String url) throws IOException {

        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String securityToken = sha1(timestamp + url + password);
        return Jsoup
                .connect(url)
                .method(Connection.Method.POST)
                .data("timestamp", timestamp)
                .data("security_token", securityToken.toLowerCase())
                .data("status", "active")
                .ignoreContentType(true)
                .execute();
    }

    private JsonNode parseCampaigStat(LocalDate dateStart, LocalDate dateEnd, ExecutorService pool, Integer campaign)
            throws JsonProcessingException, InterruptedException, java.util.concurrent.ExecutionException {
        String url = "https://main.trafficfactory.biz/webservices/" + apiToken + "/stats/campaign/" +
                campaign + "/" + dateStart + "/" + dateEnd + ".json";
        Future<String> response = pool.submit(() -> {
            try {
                return parseNetwork(url).body();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        return objectMapper.readTree(response.get()).get("stats");
    }

    private void authorization() {
        if (apiToken == null || password == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Credentials credentials = credentialsRepository.
                    findCredentialsByOwnerAndNetworkName(((UserDetails) authentication.getPrincipal())
                            .user(), Network.TF).orElseThrow(() -> new BadCredentialsException("Api token not found"));
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
