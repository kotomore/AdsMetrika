package ru.set404.AdsMetrika.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.set404.AdsMetrika.models.Offer;
import ru.set404.AdsMetrika.models.User;

import java.util.List;

@Repository
public interface OffersRepository extends JpaRepository<Offer, Integer> {
    List<Offer> findByOwner(User user);

}
