package ru.set404.AdsMetrika.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.set404.AdsMetrika.models.Stat;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.network.Network;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface StatsRepository extends JpaRepository<Stat, Integer> {
    @Query("select s.id from Stat s where s.owner = ?1 and s.networkName = ?2 and s.createdDate = ?3")
    Optional<Integer> findSimilar(User user, Network network, LocalDate createdDate);

    List<Stat> findAllByOwnerAndCreatedDateAfter(User user, LocalDate dateStart);
}
