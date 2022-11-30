package ru.set404.AdsMetrika.scheduled;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.set404.AdsMetrika.config.SingletonFactoryForScheduling;
import ru.set404.AdsMetrika.dto.StatDTO;
import ru.set404.AdsMetrika.dto.TableDTO;
import ru.set404.AdsMetrika.models.Settings;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.network.Network;
import ru.set404.AdsMetrika.services.CredentialsService;
import ru.set404.AdsMetrika.services.SettingsService;
import ru.set404.AdsMetrika.services.TelegramBotService;
import ru.set404.AdsMetrika.util.StatisticsUtilities;

import javax.management.timer.Timer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Component
public class ScheduledTasks {
    private final SingletonFactoryForScheduling schedulingObjects;
    private final SettingsService settingsService;
    private final CredentialsService credentialsService;
    private final TelegramBotService telegramBotService;

    protected Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    public ScheduledTasks(SingletonFactoryForScheduling schedulingObjects, SettingsService settingsService,
                          CredentialsService credentialsService, TelegramBotService telegramBotService) {
        this.schedulingObjects = schedulingObjects;
        this.settingsService = settingsService;
        this.credentialsService = credentialsService;
        this.telegramBotService = telegramBotService;
    }


    @Scheduled(fixedRate = Timer.ONE_MINUTE * 10)
    @CacheEvict(value = {"network_stats", "campaign_stats"}, allEntries = true)
    public void clearCache() {
        logger.debug("Cache {network_stats, campaign_stats} cleared.");
    }

    @Scheduled(cron = "0 0 11 * * *", zone = "Europe/Moscow")
    public void scheduleTask()
    {
        logger.debug("Sheduled task execute");

        LocalDate date = LocalDate.now().minusDays(1);

        List<Settings> settingsList = settingsService.findSettingsWithScheduledTask();
        for (Settings settings : settingsList) {
            User user = settings.getOwner();
            List<TableDTO> tableStats = getStatsForTables(user, date, date);
            TableDTO combinedStats = StatisticsUtilities.combineTableDTO(tableStats);

            if (user.getSettings().isTelegramEnabled())
                telegramBotService.sendTelegramMessage(user, combinedStats);
            if (user.getSettings().isSpreadSheetScheduleEnabled())
                schedulingObjects.getScheduledServiceSingleton().writeSpreadSheetTable("", user, combinedStats, date);
        }
    }

    private List<TableDTO> getStatsForTables(User user, LocalDate dateStart, LocalDate dateEnd) {

        Set<Network> userNetworks = credentialsService.userNetworks(user);
        List<TableDTO> tableStats = new ArrayList<>();

        for (Network network : userNetworks) {
            List<StatDTO> currentNetworkStat;
            if (user.getRole().equals("ROLE_GUEST"))
                currentNetworkStat = schedulingObjects.getNetworksServiceSingleton().getNetworkStatisticsListMock(network);
            else
                currentNetworkStat = schedulingObjects.getNetworksServiceSingleton().getOfferStats(user,
                        network, dateStart, dateEnd);
            tableStats.add(new TableDTO(currentNetworkStat, network));
        }
        return tableStats;
    }
}
