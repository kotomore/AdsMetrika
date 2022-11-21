package ru.set404.AdsMetrika.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.set404.AdsMetrika.models.Credentials;
import ru.set404.AdsMetrika.models.Settings;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.network.Network;

import java.util.List;
import java.util.Optional;

@Repository
public interface SettingsRepository extends JpaRepository<Settings, Integer> {
    Optional<Settings> findSettingsByOwner(User user);
}
