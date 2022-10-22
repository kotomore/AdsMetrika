package ru.set404.AdsMetrika.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.set404.AdsMetrika.models.Offer;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.services.network.Network;

import java.util.List;

@Repository
public interface OffersRepository extends JpaRepository<Offer, Integer> {
    List<Offer> findByOwnerAndNetworkName(User user, Network network);

}
