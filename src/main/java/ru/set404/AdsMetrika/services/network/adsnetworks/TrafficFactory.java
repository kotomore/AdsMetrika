package ru.set404.AdsMetrika.services.network.adsnetworks;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Service
public class TrafficFactory implements NetworkStats {

    private final CredentialsRepository credentialsRepository;
    private static Connection.Response response = null;

    @Autowired
    public TrafficFactory(CredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
    }

    private void getAuth() throws IOException {
        if (response == null) {
            System.out.println("Authorization...");
            response = Jsoup
                    .connect("https://main.trafficfactory.biz/users/sign_in")
                    .execute();
            System.out.println("Authorization complete.");
            Document doc = response.parse();
            Element meta = doc.select("[name=\"signin[_csrf_token]\"]").first();
            assert meta != null;
            String token = meta.attr("value");

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Credentials credentials = credentialsRepository.
                    findCredentialsByOwnerAndNetworkName(((UserDetails) authentication.getPrincipal()).user(),
                            Network.TF);

            String email = credentials.getUsername();
            String password = credentials.getPassword();

            //Authorization, get cookies
            response = Jsoup
                    .connect("https://main.trafficfactory.biz/users/sign_in")
                    .method(Connection.Method.POST)
                    .data("signin[login]", email)
                    .data("signin[password]", password)
                    .data("signin[_csrf_token]", token)
                    .cookies(response.cookies())
                    .execute();
        }
    }

    public Map<Integer, NetworkStatEntity> getStat(Map<Integer, String> networkOffer, LocalDate dateStart,
                                                   LocalDate dateEnd)
            throws IOException, InterruptedException {

        getAuth();
        Map<Integer, NetworkStatEntity> stat = new HashMap<>();

        ExecutorService service = Executors.newFixedThreadPool(4);
        for (Integer offerId : networkOffer.keySet()) {

            Connection.Response responseLoop = response;
            String groupName = networkOffer.get(offerId);
            service.execute(() -> {
                try {
                    stat.put(offerId, parse(responseLoop, groupName, dateStart, dateEnd));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });

        }
        service.shutdown();
        if (!service.awaitTermination(1, TimeUnit.MINUTES)) {
            System.out.println("Parse Traffic Factory timed out error");
        }
        return stat;
    }

    private NetworkStatEntity parse(Connection.Response response, String offerId, LocalDate dateStart, LocalDate dateEnd)
            throws IOException {
        System.out.println("Parse offer - " + offerId);
        response = Jsoup
                .connect("https://main.trafficfactory.biz/stats/campaigns/"
                        + dateStart + "-00-00/"
                        + dateEnd.minusDays(1) + "-23-59?campaign_name="
                        + offerId)
                .cookies(response.cookies())
                .execute();

        Element elem = response.parse().select("[class=\"hg-admin-row hg-admin-row-total\"]").first();
        int clicks = 0;
        double cost = 0;
        if (elem != null) {
            clicks = Integer.parseInt(elem.getElementsByClass("hg-admin-list-td-deliveries")
                    .html()
                    .replace("&nbsp;", ""));

            cost = Double.parseDouble(elem.getElementsByClass("hg-admin-list-td-total")
                    .html()
                    .replace("&nbsp;", "")
                    .replace("$", "")
                    .replace(",", "."));
        } else {
            System.out.println("Offer id - " + offerId + " not found");
        }
        return new NetworkStatEntity(clicks, cost);
    }
}
