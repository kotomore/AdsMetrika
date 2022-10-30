package ru.set404.AdsMetrika.services.network.ads;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.stereotype.Component;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDate;
import java.util.*;

@Component
public class TrafficFactoryAPI {

    private String apiToken;
    private  List<Integer> getCampaignList() throws IOException {
        String url = "https://main.trafficfactory.biz/webservices/" + apiToken + "/campaigns.json";
        List<Integer> campaigns = new ArrayList<>();
        JsonNode json = new ObjectMapper().readTree(getStat(url).body());
        for (JsonNode campaign : json.get("campaigns")) {
            campaigns.add(campaign.get("id").asInt());
        }
        return campaigns;
    }

    public Map<Integer, NetworkStatEntity> getCampaignStats(LocalDate dateStart, LocalDate dateEnd) throws IOException {
        String url = "";
        List<Integer> campaigns = getCampaignList();
        Map<Integer, NetworkStatEntity> campaignStats = new HashMap<>();
        for (Integer campaign : campaigns) {
            url = "https://main.trafficfactory.biz/webservices/" + apiToken + "/stats/campaign/" +
                    campaign + "/" + dateStart + "/" + dateEnd + ".json";
            //JsonNode json = new ObjectMapper().readTree(getStat(url).body()).get("stats").get(String.valueOf(dateStart));
            JsonNode json = new ObjectMapper().readTree(getStat(url).body()).get("stats");
            int deliveries = 0;
            double total = 0;
            for (JsonNode node : json) {
                deliveries += node.get("deliveries").asInt();
                total += node.get("total").asDouble();
            }
            campaignStats.put(campaign, new NetworkStatEntity(deliveries, total));
        }
        return campaignStats;
    }

    private Connection.Response getStat(String url) throws IOException {

        String timestamp = String.valueOf(System.currentTimeMillis() / 1000);
        apiToken = "api token";
        String password = "api password";

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
