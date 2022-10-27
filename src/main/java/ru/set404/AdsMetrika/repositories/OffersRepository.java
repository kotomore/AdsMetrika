package ru.set404.AdsMetrika.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.set404.AdsMetrika.models.Offer;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.services.network.Network;

import javax.validation.constraints.NotEmpty;
import java.util.List;
import java.util.Optional;

@Repository
public interface OffersRepository extends JpaRepository<Offer, Integer> {
    List<Offer> findByOwner(User user);
    boolean existsOfferByOwnerAndId(User user, int id);

    @Transactional
    @Modifying
    @Query("delete from Offer o where o.id = ?1")
    void deleteById(int id);

    @Query("""
            select o.id from Offer o
            where o.owner = ?1 and o.adcomboNumber = ?2 and o.groupName = ?3 and o.networkName = ?4""")
    Optional<Integer> findIdByParameters(User owner, int adcomboNumber, @NotEmpty String groupName, Network networkName);
    @Query("""
            select (count(o) > 0) from Offer o
            where o.owner = ?1 and o.adcomboNumber = ?2 and o.groupName = ?3 and o.networkName = ?4""")
    boolean isExistsByParameters(User owner, int adcomboNumber, String groupName, Network networkName);

}
