package ru.set404.AdsMetrika.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.set404.AdsMetrika.models.Settings;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.repositories.SettingsRepository;

import java.util.List;

@Service
public class SettingsService {
    private final SettingsRepository settingsRepository;
    @Autowired
    public SettingsService(SettingsRepository settingsRepository) {
        this.settingsRepository = settingsRepository;
    }

    @Cacheable("settings")
    public Settings userSettings(User user) {
        return settingsRepository.findSettingsByOwner(user).orElse(new Settings());
    }

    @Transactional
    @CacheEvict(cacheNames = "settings", allEntries = true)
    public void update(Settings settings, User user) {
        settings.setOwner(user);

        if (!settings.getAdcomboId().isEmpty())
            settings.setAdcomboId(settings.getAdcomboId().trim());

        if (settings.getSpreadSheetId().isEmpty()) {
            settings.setSpreadSheetEnabled(false);
            settings.setSpreadSheetScheduleEnabled(false);
        }

        if (settings.getTelegramUsername().isEmpty()) {
            settings.setTelegramEnabled(false);
        }

        settingsRepository.save(settings);
    }
    public List<Settings> findSettingsWithScheduledTask() {
        return settingsRepository.findAllEnabledTasks();
    }
}
