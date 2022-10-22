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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
        model.addAttribute("exoOffers", getFullStatistics(Network.EXO));
        model.addAttribute("trafficFactoryOffers", getFullStatistics(Network.TF));

        return "statistics";
    }

    public List<StatsEntity> getFullStatistics(Network network) throws IOException, InterruptedException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<Offer> offers = offersRepository.
                findByOwnerAndNetworkName(((UserDetails) authentication.getPrincipal()).user(),
                        network);

        List<String> groupNames = offers.stream().map(Offer::getGroupName).toList();
        List<Integer> offerIds = offers.stream().map(Offer::getAdcomboNumber).toList();

        NetworkStats networkStats = null;
        switch (network) {
            case TF -> networkStats = trafficFactory;
            case EXO -> networkStats = exoClick;
        }

        assert networkStats != null;
        Map<Integer, NetworkStatEntity> exoClickStats = networkStats.getStat(groupNames,
                offerIds,
                LocalDate.now().minusDays(1),
                LocalDate.now());

        Map<Integer, AdcomboStatsEntity> adcomboStats = adCombo.getStat(network,
                LocalDate.now().minusDays(1),
                LocalDate.now());

        List<StatsEntity> statsEntities = new ArrayList<>();

        for (int offerId : offerIds) {
            statsEntities.add(StatisticsMapper.map(offerId, exoClickStats, adcomboStats));
        }
        return statsEntities;
    }
}
