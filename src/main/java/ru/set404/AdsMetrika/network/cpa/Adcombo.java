package ru.set404.AdsMetrika.network.cpa;

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
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class Adcombo {
    protected Log logger = LogFactory.getLog(this.getClass());
    private final CredentialsRepository credentialsRepository;
    private final ObjectMapper objectMapper;
    private String apiKey;

    @Autowired
    public Adcombo(CredentialsRepository credentialsRepository, ObjectMapper objectMapper) {
        this.credentialsRepository = credentialsRepository;
        this.objectMapper = objectMapper;
    }

    public Map<Integer, AdcomboStats> getNetworkStatMap(Network network, LocalDate dateStart, LocalDate dateEnd) throws IOException {
        authorization();
        String url = "https://api.adcombo.com/stats/data/?api_key=%s" +
                "&ts=%s&te=%s" +
                "&group_by=offer_id" +
                "&group_by=subacc_4" +
                "&tz_offset=%s&cols=uniq_traffic&cols=orders_confirmed" +
                "&cols=user_orders_confirmed_income&cols=user_total_hold_income" +
                "&utm_source=%s";

        Map<Integer, AdcomboStats> stats = new HashMap<>();
        List<Integer> campaignsToCheck = new ArrayList<>();
        for (JsonNode campaign : parseNetwork(url, network, dateStart, dateEnd)) {
            if (campaign.get("uniq_traffic").asInt(-1) > 10) {
                AdcomboStats adcomboStat = new AdcomboStats(
                        campaign.get("group_by").asInt(),
                        campaign.get("key_for_groupping").asText(),
                        campaign.get("user_total_hold_income").asDouble(),
                        campaign.get("orders_confirmed").asInt(),
                        campaign.get("user_orders_confirmed_income").asDouble());
                List<Integer> campaigns = new ArrayList<>();
                for (JsonNode group : campaign.get("sub_groups")) {
                    if (!campaignsToCheck.contains(group.get("group_by").asInt())) {
                        campaigns.add(group.get("group_by").asInt());
                        campaignsToCheck.add(group.get("group_by").asInt());
                    }
                }
                adcomboStat.setCampaigns(campaigns);
                stats.put(campaign.get("group_by").asInt(), adcomboStat);
            }
        }
        logger.debug("Adcombo stats received");
        return stats;
    }

    public Map<Integer, AdcomboStats> getCampaignStatMap(Network network, LocalDate dateStart, LocalDate dateEnd) throws IOException {
        authorization();
        String url = "https://api.adcombo.com/stats/data/?api_key=%s" +
                "&ts=%s&te=%s" +
                "&group_by=subacc_4&group_by=offer_id&tz_offset=%s" +
                "&cols=orders_confirmed&cols=uniq_traffic" +
                "&cols=user_orders_confirmed_income&cols=user_total_hold_income&utm_source=%s";

        Map<Integer, AdcomboStats> stats = new HashMap<>();
        for (JsonNode campaign : parseNetwork(url, network, dateStart.minusDays(1), dateEnd)) {
            if (campaign.get("uniq_traffic").asInt(-1) > 10) {
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


    private JsonNode parseNetwork(String url, Network network, LocalDate dateStart, LocalDate dateEnd) throws IOException {
        String timeZone = "+03:00";
        String tzOffset = "180";
        int time = 0;
        if (network == Network.EXO) {
            tzOffset = "-240";
            time = -25200;
        }

        long timeStart = dateStart.plusDays(1).toEpochSecond(LocalTime.MIN, ZoneOffset.of(timeZone)) - time;
        long timeEnd = dateEnd.plusDays(1).toEpochSecond(LocalTime.MIN, ZoneOffset.of(timeZone)) - 60 - time;
        Connection.Response response = Jsoup
                .connect(String.format(url, apiKey, timeStart, timeEnd, tzOffset, network.getName()))
                .method(Connection.Method.GET)
                .ignoreContentType(true)
                .maxBodySize(0)
                .execute();

        return objectMapper.readTree(response.body()).get("data").elements().next().get("rows");
    }

    private void authorization() {
        if (apiKey == null) {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Credentials credentials = credentialsRepository.
                    findCredentialsByOwnerAndNetworkName(((UserDetails) authentication.getPrincipal())
                            .user(), Network.ADCOMBO).orElseThrow(() -> new BadCredentialsException("Api token not found"));
            this.apiKey = credentials.getUsername();
        }
    }
}
