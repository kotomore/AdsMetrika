package ru.set404.AdsMetrika.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.network.Network;

import java.util.List;
import java.util.Optional;

@Repository
public interface CredentialsRepository extends JpaRepository<Credentials, Integer> {
    List<Credentials> findByOwner(User user);
    Optional<Credentials> findCredentialsByOwnerAndNetworkName(User user, Network network);
}
