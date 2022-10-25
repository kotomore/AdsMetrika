package ru.set404.AdsMetrika.services;

import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.dto.StatDTO;
import ru.set404.AdsMetrika.models.Offer;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.repositories.OffersRepository;
import ru.set404.AdsMetrika.services.network.Network;
import ru.set404.AdsMetrika.services.network.ads.ExoClick;
import ru.set404.AdsMetrika.services.network.ads.NetworkStatEntity;
import ru.set404.AdsMetrika.services.network.ads.NetworkStats;
import ru.set404.AdsMetrika.services.network.ads.TrafficFactory;
import ru.set404.AdsMetrika.services.network.cpa.Adcombo;
import ru.set404.AdsMetrika.services.network.cpa.AdcomboStatsEntity;
import ru.set404.AdsMetrika.util.StatisticsUtilities;

import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class OffersService {
    private final ExoClick exoClick;
    private final TrafficFactory trafficFactory;
    private final Adcombo adCombo;
    private final OffersRepository offersRepository;

    public OffersService(ExoClick exoClick, TrafficFactory trafficFactory, Adcombo adCombo, OffersRepository offersRepository) {
        this.exoClick = exoClick;
        this.trafficFactory = trafficFactory;
        this.adCombo = adCombo;
        this.offersRepository = offersRepository;
    }

    public List<Offer> getUserOffersList(User user) {
        return offersRepository.findByOwner(user);
    }

    public List<StatDTO> getNetworkStatisticsList(List<Offer> userOffers, Network network, LocalDate dateStart,
                                                   LocalDate dateEnd) throws IOException, InterruptedException {

        Map<Integer, String> userOffersFilteredByNetwork = userOffers.stream()
                .filter(offer -> offer.getNetworkName() == network)
                .collect(Collectors.toMap(Offer::getAdcomboNumber, Offer::getGroupName));

        NetworkStats networkStats = null;
        switch (network) {
            case TF -> networkStats = trafficFactory;
            case EXO -> networkStats = exoClick;
        }

        assert networkStats != null;
        Map<Integer, NetworkStatEntity> networkStatsMap = networkStats.getStat(userOffersFilteredByNetwork,
                dateStart, dateEnd);

        Map<Integer, AdcomboStatsEntity> adcomboStatsMap = adCombo.getStat(network,
                dateStart.minusDays(1), dateEnd);

        List<StatDTO> statsEntities = new ArrayList<>();

        for (int offerId : userOffersFilteredByNetwork.keySet()) {
            if (networkStatsMap.containsKey(offerId) && adcomboStatsMap.containsKey(offerId))
                statsEntities.add(StatisticsUtilities.createStatsDTO(offerId, networkStatsMap, adcomboStatsMap));
        }
        return statsEntities;
    }
}
