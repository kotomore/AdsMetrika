package ru.set404.AdsMetrika.network.cpa;

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
import ru.set404.AdsMetrika.network.Network;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@SessionScope
public class Adcombo {
    protected Log logger = LogFactory.getLog(this.getClass());
    private final ObjectMapper objectMapper;
    private String apiKey;
    private final ConfigProperties config;

    @Autowired
    public Adcombo(ObjectMapper objectMapper, ConfigProperties config) {
        this.objectMapper = objectMapper;
        this.config = config;
    }

    public Map<Integer, AdcomboStats> getNetworkStatMap(Credentials credentials, Network network, LocalDate dateStart, LocalDate dateEnd) {
        authorization(credentials);
        String url = "https://api.adcombo.com/stats/data/?api_key=%s" +
                "&ts=%s&te=%s" +
                "&group_by=offer_id" +
                "&group_by=subacc_4" +
                "&tz_offset=%s&cols=uniq_traffic&cols=orders_confirmed" +
                "&cols=user_orders_confirmed_income&cols=user_total_hold_income" +
                "&utm_source=%s";

        Map<Integer, AdcomboStats> stats = new HashMap<>();
        List<Integer> campaignsToCheck = new ArrayList<>();
        Map<String, Integer> offerNameMap = new HashMap<>();

        for (JsonNode campaign : parseNetwork(url, network, dateStart, dateEnd)) {
            if (campaign.get("uniq_traffic").asInt(-1) > config.getMinScanCount()) {
                String offerName = campaign.get("key_for_groupping").asText();
                AdcomboStats adcomboStat;
                String substring = offerName.substring(offerName.length() - 2);
                if (!offerNameMap.containsKey(substring)) {
                    adcomboStat = new AdcomboStats(
                            campaign.get("group_by").asInt(),
                            offerName,
                            campaign.get("user_total_hold_income").asDouble(),
                            campaign.get("orders_confirmed").asInt(),
                            campaign.get("user_orders_confirmed_income").asDouble());
                    offerNameMap.put(substring, campaign.get("group_by").asInt());
                    List<Integer> campaigns = new ArrayList<>();
                    for (JsonNode group : campaign.get("sub_groups")) {
                        if (!campaignsToCheck.contains(group.get("group_by").asInt())) {
                            campaigns.add(group.get("group_by").asInt());
                            campaignsToCheck.add(group.get("group_by").asInt());
                        }
                    }
                    adcomboStat.setCampaigns(campaigns);
                    stats.put(campaign.get("group_by").asInt(), adcomboStat);
                } else {
                    adcomboStat = stats.get(offerNameMap.get(substring));
                    adcomboStat.setCost(adcomboStat.getCost() + campaign.get("user_orders_confirmed_income").asDouble());
                    adcomboStat.setHoldCost(adcomboStat.getHoldCost() + campaign.get("user_total_hold_income").asDouble());
                    adcomboStat.setConfirmedCount(adcomboStat.getConfirmedCount() + campaign.get("orders_confirmed").asInt());
                    stats.put(offerNameMap.get(substring), adcomboStat);
                }
            }
        }
        logger.debug("Adcombo stats received");
        return stats;
    }

    public Map<Integer, AdcomboStats> getCampaignStatMap(Credentials credentials, Network network, LocalDate dateStart, LocalDate dateEnd) {
        authorization(credentials);
        String url = "https://api.adcombo.com/stats/data/?api_key=%s" +
                "&ts=%s&te=%s" +
                "&group_by=subacc_4&group_by=offer_id&tz_offset=%s" +
                "&cols=orders_confirmed&cols=uniq_traffic" +
                "&cols=user_orders_confirmed_income&cols=user_total_hold_income&utm_source=%s";

        Map<Integer, AdcomboStats> stats = new HashMap<>();
        for (JsonNode campaign : parseNetwork(url, network, dateStart.minusDays(1), dateEnd)) {
            if (campaign.get("uniq_traffic").asInt(-1) > config.getMinScanCount()) {
                stats.put(campaign.get("group_by").asInt(), new AdcomboStats(
                        campaign.get("group_by").asInt(),
                        campaign.get("sub_groups").elements().next().get("key_for_groupping").asText(),
                        campaign.get("user_total_hold_income").asDouble(),
                        campaign.get("orders_confirmed").asInt(),
                        campaign.get("user_orders_confirmed_income").asDouble())
                );
            }
        }
        return stats;
    }


    private JsonNode parseNetwork(String url, Network network, LocalDate dateStart, LocalDate dateEnd) {
        String timeZone = config.getDefaultTimeZone();
        String tzOffset = config.getDefaultOffset();
        int time = 0;
        if (network == Network.EXO) {
            tzOffset = "-240";
            time = -25200;
        }

        long timeStart = dateStart.plusDays(1).toEpochSecond(LocalTime.MIN, ZoneOffset.of(timeZone)) - time;
        long timeEnd = dateEnd.plusDays(1).toEpochSecond(LocalTime.MIN, ZoneOffset.of(timeZone)) - 60 - time;
        Connection.Response response;
        try {
            response = Jsoup
                    .connect(String.format(url, apiKey, timeStart, timeEnd, tzOffset, network.getName()))
                    .method(Connection.Method.GET)
                    .ignoreContentType(true)
                    .maxBodySize(0)
                    .execute();
        } catch (IOException e) {
            logger.info(e.getMessage());
            throw new RuntimeException("Couldn't access to adcombo. Check API");
        }
        JsonNode result;
        try {
            result = objectMapper.readTree(response.body()).get("data").elements().next().get("rows");
        } catch (JsonProcessingException e) {
            logger.info(e.getMessage());
            throw new RuntimeException("Couldn't get statistics from adcombo");
        }
        return result;
    }

    private void authorization(Credentials credentials) {
        if (apiKey == null) {
            this.apiKey = credentials.getUsername();
        }
    }
}
