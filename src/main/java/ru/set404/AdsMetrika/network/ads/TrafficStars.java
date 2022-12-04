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

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Component
@SessionScope
public class TrafficStars implements AffiliateNetwork {
    private final ObjectMapper objectMapper;
    protected Log logger = LogFactory.getLog(this.getClass());
    private static String authToken = null;

    @Autowired
    public TrafficStars(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private void authorization(Credentials credentials) {
        if (authToken == null) {

            String username = credentials.getUsername();
            String password = credentials.getPassword();

            String clientId = credentials.getClientId();
            String clientSecret = credentials.getSecret();

            Connection.Response response;
            JsonNode token;
            try {
                response = Jsoup
                        .connect("https://api.trafficstars.com/v1/auth/token")
                        .method(Connection.Method.POST)
                        .ignoreContentType(true)
                        .data("grant_type", "password")
                        .data("client_id", clientId)
                        .data("client_secret", clientSecret)
                        .data("username", username)
                        .data("password", password)
                        .execute();
            } catch (IOException e) {
                logger.info(e.getMessage());
                throw new RuntimeException("Couldn't connect to TrafficStars. Check API");
            }
            try {
                token = objectMapper.readTree(response.body()).get("access_token");
            } catch (JsonProcessingException e) {
                logger.info(e.getMessage());
                throw new RuntimeException("Couldn't get statistics from TrafficStars");
            }

            if (token.isNull())
                throw new RuntimeException("Couldn't connect to TrafficStars. Check API");
            authToken = token.asText();
        }
    }

    ///////////////////////////////////////
    //return all campaigns stats by network Map<campaign_id, network_stats>
    ///////////////////////////////////////
    public Map<Integer, NetworkStats> getCampaignsStats(Credentials credentials, LocalDate dateStart, LocalDate dateEnd) {
        if (authToken == null)
            authorization(credentials);
        Map<Integer, NetworkStats> stat = new HashMap<>();
        try {
            Connection.Response response = Jsoup
                    .connect("https://api.trafficstars.com/v1.1/advertiser/custom/report/by-campaign")
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .data("date_from", dateStart.toString())
                    .data("date_to", dateEnd.toString())
                    .header("Authorization", "Bearer " + authToken)
                    .execute();
            JsonNode jsonNode = objectMapper.readTree(response.body());
            for (JsonNode node : jsonNode) {
                stat.put(node.get("campaign_id").asInt(),
                        new NetworkStats(node.get("clicks").asInt(), node.get("amount").asDouble()));
            }
        } catch (Exception e) {
            logger.info(e.getMessage());
            throw new RuntimeException("Couldn't get statistics from TrafficStars. Check API");
        }
        return stat;
    }
}

