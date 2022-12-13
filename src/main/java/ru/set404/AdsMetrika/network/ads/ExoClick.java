package ru.set404.AdsMetrika.network.ads;

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
public class ExoClick implements AffiliateNetwork {
    private final ObjectMapper objectMapper;
    protected Log logger = LogFactory.getLog(this.getClass());
    private static String authToken = null;

    @Autowired
    public ExoClick(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    private boolean authorization(Credentials credentials) {

        logger.debug("ExoClick authorization...");
        String apiToken = credentials.getUsername();
        String jsonBody = """
                {
                "api_token": "%s"
                }
                """.formatted(apiToken);
        Connection.Response response;
        try {
            response = Jsoup
                    .connect("https://api.exoclick.com/v2/login")
                    .method(Connection.Method.POST)
                    .ignoreContentType(true)
                    .header("Content-Type", "application/json")
                    .requestBody(jsonBody)
                    .execute();
            authToken = objectMapper.readTree(response.body()).get("token").asText();
            return response.statusCode() == 200;
        } catch (IOException e) {
            logger.info(e.getMessage());
            throw new RuntimeException("Couldn't connect to ExoClick. Check API");
        }

    }

    ///////////////////////////////////////
    //return all campaigns stats by network Map<campaign_id, network_stats>
    ///////////////////////////////////////
    public Map<Integer, NetworkStats> getCampaignsStats(Credentials credentials, LocalDate dateStart, LocalDate dateEnd) {
        if (!authorization(credentials))
            throw new RuntimeException("Couldn't get statistics from ExoClick. Check API");
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
            logger.info(e.getMessage());
            throw new RuntimeException("Couldn't get statistics from ExoClick. Check API");
        }
        return stat;
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
}

