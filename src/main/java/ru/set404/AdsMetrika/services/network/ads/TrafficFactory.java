package ru.set404.AdsMetrika.services.network.ads;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.repositories.CredentialsRepository;
import ru.set404.AdsMetrika.security.UserDetails;
import ru.set404.AdsMetrika.services.network.Network;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Component
public class TrafficFactory implements NetworkStats {

    protected Log logger = LogFactory.getLog(this.getClass());
    private final CredentialsRepository credentialsRepository;
    private Map<String, String> cookies;

    @Autowired
    public TrafficFactory(CredentialsRepository credentialsRepository) {
        this.credentialsRepository = credentialsRepository;
    }

    private void getAuth() throws IOException {
            logger.debug("Traffic Factory authorization...");
            Connection.Response response = Jsoup
                    .connect("https://main.trafficfactory.biz/users/sign_in")
                    .execute();
            logger.debug("Traffic Factory authorization complete.");
            Document doc = response.parse();
            Element meta = doc.select("[name=\"signin[_csrf_token]\"]").first();
            assert meta != null;
            String token = meta.attr("value");

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Credentials credentials = credentialsRepository.
                    findCredentialsByOwnerAndNetworkName(((UserDetails) authentication.getPrincipal())
                            .user(), Network.TF)
                    .orElseThrow(() -> new BadCredentialsException("Traffic Factory credentials doesn`t exist"));

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
            cookies = response.cookies();
    }

    public Map<Integer, NetworkStatEntity> getStat(Map<Integer, String> networkOffers, LocalDate dateStart,
                                                   LocalDate dateEnd)
            throws IOException, InterruptedException {

        getAuth();
        Map<Integer, NetworkStatEntity> stat = new HashMap<>();

        ExecutorService service = Executors.newFixedThreadPool(4);
        for (Map.Entry<Integer, String> networkOffer : networkOffers.entrySet()) {
            service.execute(() ->
                    stat.put(networkOffer.getKey(), parseNetwork(networkOffer.getValue(), dateStart, dateEnd)));

        }
        service.shutdown();
        if (!service.awaitTermination(1, TimeUnit.MINUTES)) {
            System.out.println("Parse Traffic Factory timed out error");
        }
        return stat;
    }

    private NetworkStatEntity parseNetwork(String offerId, LocalDate dateStart, LocalDate dateEnd) {
        logger.debug("Parse offer - " + offerId);
        Connection.Response response;
        try {
            response = Jsoup
                    .connect("https://main.trafficfactory.biz/stats/campaigns/"
                            + dateStart + "-00-00/"
                            + dateEnd + "-23-59?campaign_name="
                            + offerId)
                    .cookies(cookies)
                    .execute();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Element elem = null;
        try {
            elem = response.parse().select("[class=\"hg-admin-row hg-admin-row-total\"]").first();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
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
            logger.debug("Offer id - " + offerId + " not found");
        }
        return new NetworkStatEntity(clicks, cost);
    }
}
