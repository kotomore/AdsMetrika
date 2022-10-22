package ru.set404.AdsMetrika.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.set404.AdsMetrika.models.Offer;
import ru.set404.AdsMetrika.repositories.OffersRepository;
import ru.set404.AdsMetrika.security.UserDetails;
import ru.set404.AdsMetrika.services.network.Network;
import ru.set404.AdsMetrika.services.network.adsnetworks.ExoClick;
import ru.set404.AdsMetrika.services.network.adsnetworks.NetworkStatEntity;
import ru.set404.AdsMetrika.services.network.adsnetworks.NetworkStats;
import ru.set404.AdsMetrika.services.network.adsnetworks.TrafficFactory;
import ru.set404.AdsMetrika.services.network.cpanetworks.Adcombo;
import ru.set404.AdsMetrika.services.network.cpanetworks.AdcomboStatsEntity;
import ru.set404.AdsMetrika.to.StatsEntity;
import ru.set404.AdsMetrika.util.StatisticsMapper;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/statistics")
public class UserController {
    private final OffersRepository offersRepository;
    private final ExoClick exoClick;
    private final TrafficFactory trafficFactory;
    private final Adcombo adCombo;

    @Autowired
    public UserController(OffersRepository offersRepository, ExoClick exoClick, TrafficFactory trafficFactory,
                          Adcombo adCombo) {
        this.offersRepository = offersRepository;
        this.exoClick = exoClick;
        this.trafficFactory = trafficFactory;
        this.adCombo = adCombo;
    }

    @GetMapping
    public String index(Model model) throws IOException, InterruptedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Offer> userOffers = offersRepository.
                findByOwner(((UserDetails) authentication.getPrincipal()).user());

        Set<Network> networks = userOffers.stream().map(Offer::getNetworkName).collect(Collectors.toSet());

        Map<Network, List<StatsEntity>> statistics = new HashMap<>();

        for (Network network : networks)
            statistics.put(network, getFullStatistics(userOffers, network));
        model.addAttribute("statistics", statistics);
        return "statistics";
    }

    public List<StatsEntity> getFullStatistics(List<Offer> userOffers, Network network)
            throws IOException, InterruptedException {

        Map<Integer, String> filterByNetworkUserOffers = userOffers.stream()
                .filter(offer -> offer.getNetworkName() == network)
                .collect(Collectors.toMap(Offer::getAdcomboNumber, Offer::getGroupName));

        NetworkStats networkStats = null;
        switch (network) {
            case TF -> networkStats = trafficFactory;
            case EXO -> networkStats = exoClick;
        }

        assert networkStats != null;
        Map<Integer, NetworkStatEntity> networkStatsMap = networkStats.getStat(filterByNetworkUserOffers,
                LocalDate.now().minusDays(1),
                LocalDate.now());

        Map<Integer, AdcomboStatsEntity> adcomboStatsMap = adCombo.getStat(network,
                LocalDate.now().minusDays(1),
                LocalDate.now());

        List<StatsEntity> statsEntities = new ArrayList<>();

        for (int offerId : filterByNetworkUserOffers.keySet()) {
            if (networkStatsMap.containsKey(offerId) && adcomboStatsMap.containsKey(offerId))
                statsEntities.add(StatisticsMapper.map(offerId, networkStatsMap, adcomboStatsMap));
        }
        return statsEntities;
    }
}
