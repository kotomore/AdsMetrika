package ru.set404.AdsMetrika.services.network.cpa;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.repositories.CredentialsRepository;
import ru.set404.AdsMetrika.security.UserDetails;
import ru.set404.AdsMetrika.services.network.Network;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

@Service
public class Adcombo {
    private final CredentialsRepository credentialsRepository;
    private final ObjectMapper objectMapper;
    public static Connection.Response responseConnection = null;

    @Autowired
    public Adcombo(CredentialsRepository credentialsRepository, ObjectMapper objectMapper) {
        this.credentialsRepository = credentialsRepository;

        this.objectMapper = objectMapper;
    }

    private void getAuthResponse() throws IOException {
        if (responseConnection == null) {
            System.out.println("Authorization...");

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Credentials credentials = credentialsRepository.
                    findCredentialsByOwnerAndNetworkName(((UserDetails) authentication.getPrincipal()).user(),
                            Network.ADCOMBO);

            String email = credentials.getUsername();
            String password = credentials.getPassword();

            Connection.Response response = Jsoup
                    .connect("https://my.adcombo.com/auth/login")
                    .method(Connection.Method.POST)
                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .execute();
            System.out.println("Cookies get");

            String token = response.cookies().get("X-CSRF-Token");
            String jsonBody = """
                    {
                    "email": "%s",
                    "password": "%s"
                    }
                    """.formatted(email, password);

            response = Jsoup
                    .connect("https://my.adcombo.com/auth/login")
                    .method(Connection.Method.POST)
                    .header("x-csrf-token", token)
                    .header("authority", "my.adcombo.com")
                    .header("method", "POST")
                    .header("path", "/auth/login")
                    .header("scheme", "https")
                    .header("accept", "application/json, text/plain, */*")
                    .header("accept-encoding", "gzip, deflate, br")
                    .header("accept-language", "en-US,en;q=0.9,ru-RU;q=0.8,ru;q=0.7")
                    .header("cache-control", "no-cache")
                    .header("content-length", "51")
                    .header("content-type", "application/json")
                    .header("origin", "https://my.adcombo.com")
                    .header("pragma", "no-cache")
                    .header("referer", "https://my.adcombo.com/login")
                    .header("sec-ch-ua", "\" Not A;Brand\";v=\"99\", \"Chromium\";v=\"101\", \"Google Chrome\";v=\"101\"")
                    .header("sec-ch-ua-mobile", "?0")
                    .header("sec-ch-ua-platform", "\"macOS\"")
                    .header("sec-fetch-dest", "empty")
                    .header("sec-fetch-mode", "cors")
                    .header("sec-fetch-site", "same-origin")
                    .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/101.0.4951.64 Safari/537.36")
                    .header("x-csrf-token", token)
                    .header("x-requested-with", "XMLHttpRequest")

                    .requestBody(jsonBody)
                    .cookies(response.cookies())
                    //.cookie("X-CSRF-Token", token)

                    .ignoreHttpErrors(true)
                    .ignoreContentType(true)
                    .execute();
            responseConnection = response;
        }
    }

    public Map<Integer, AdcomboStatsEntity> getStat(Network network, LocalDate dateStart, LocalDate dateEnd) throws IOException {

        getAuthResponse();

        String timeZone = "+03:00";

        if (network == Network.EXO) {
            timeZone = "-04:00";
        }
        long timeStart = dateStart.plusDays(1).toEpochSecond(LocalTime.MIN, ZoneOffset.of(timeZone));
        long timeEnd = dateEnd.plusDays(1).toEpochSecond(LocalTime.MIN, ZoneOffset.of(timeZone)) - 1;
        responseConnection = Jsoup
                .connect("https://my.adcombo.com/api/stats?page=1&count=100&order=desc&sorting=group_by" +
                        "&stat_type=pp_stat&ts=" + timeStart + "&te=" + timeEnd +
                        "&by_last_activity=false&percentage=false&normalize=false&comparing=false&group_by=offer_id" +
                        "&tz_offset=-10800&cols=uniq_traffic&cols=orders_confirmed&cols=orders_hold" +
                        "&cols=orders_rejected&cols=orders_trashed&cols=orders_total&cols=approve_total" +
                        "&cols=cr_uniq&cols=ctr_uniq&cols=user_orders_confirmed_income&cols=user_total_hold_income" +
                        "&cols=user_total_income&utm_source=" + network.getName() +
                        "&utm_source=-2&epc_factor=0&force=true")

                .method(Connection.Method.GET)
                .cookies(responseConnection.cookies())
                .ignoreContentType(true)
                .maxBodySize(0)
                .execute();

        JsonNode adcomboStat = objectMapper.readTree(responseConnection.body());

        Map<Integer, AdcomboStatsEntity> stats = new HashMap<>();

        if (adcomboStat.hasNonNull("objects")) {
            for (JsonNode campaign : adcomboStat.get("objects")) {
                stats.put(campaign.get("group_by").asInt(), new AdcomboStatsEntity(
                        campaign.get("group_by").asInt(),
                        campaign.get("key_for_groupping").asText(),
                        campaign.get("user_total_hold_income").asDouble(),
                        campaign.get("orders_confirmed").asInt(),
                        campaign.get("user_orders_confirmed_income").asDouble())
                );
            }
        }
        return stats;

    }
}
