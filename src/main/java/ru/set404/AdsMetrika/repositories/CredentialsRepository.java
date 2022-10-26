package ru.set404.AdsMetrika.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.services.network.Network;

import java.util.List;

@Repository
public interface CredentialsRepository extends JpaRepository<Credentials, Integer> {
    List<Credentials> findByOwnerAndNetworkName(User user, String networkName);

    List<Credentials> findByOwner(User user);
    Credentials findCredentialsByOwnerAndNetworkName(User user, Network network);
}
