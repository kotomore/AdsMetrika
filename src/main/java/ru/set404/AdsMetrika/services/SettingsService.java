package ru.set404.AdsMetrika.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.models.Settings;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.repositories.SettingsRepository;

@Service
public class SettingsService {
    private final SettingsRepository settingsRepository;
    @Autowired
    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    public Settings userSettings(User user) {
        return settingsRepository.findSettingsByOwner(user).orElse(new Settings());
    }

    public void update(Settings settings, User user) {
        settings.setOwner(user);
        settingsRepository.save(settings);
    }
}
