package ru.set404.AdsMetrika.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.set404.AdsMetrika.models.Stat;
import ru.set404.AdsMetrika.models.User;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<Stat, Integer> {
    @Transactional
    @Modifying
    @Query("delete from Stat s where s.owner = ?1 and s.createdDate = ?2 and s.campaignId = ?3")
    void deleteSimilar(User user, LocalDate date, int campaignId);
    List<Stat> findAllByOwnerAndCreatedDateAfter(User user, LocalDate dateStart);
}
