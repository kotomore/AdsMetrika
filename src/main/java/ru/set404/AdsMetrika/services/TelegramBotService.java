package ru.set404.AdsMetrika.services;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.set404.AdsMetrika.dto.TableDTO;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.scheduled.telegram.TelegramBot;

import java.time.LocalDate;

@Service
public class TelegramBotService {
    private final TelegramBot telegramBot;
    private final SettingsService settingsService;
    protected Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    public TelegramBotService(TelegramBot telegramBot, SettingsService settingsService) {
        this.telegramBot = telegramBot;
        this.settingsService = settingsService;
    }

    public void sendTelegramMessage(User user, TableDTO combinedStats) {
        String telegramId = settingsService.userSettings(user).getTelegramUsername();
        String text = "*Stats by " + LocalDate.now().minusDays(1) + "*\n\n" +
                "*Total clicks: *" + combinedStats.getTotalClicks() + "\n" +
                "*Total spend: *$" + (int) combinedStats.getTotalSpend() + "\n" +
                "*Total revenue: *$" + (int) combinedStats.getTotalRevenue() + "\n" +
                "\n---------\n\n" +
                "*Profit: *$" + (int) combinedStats.getTotalProfit() + "\n" +
                "*ROI: *" + combinedStats.getTotalROI() + "%";

        sendMessage(telegramId, text);
    }

    public void sendMessage(String chatId, String text) {
        try {
            SendMessage message = new SendMessage(chatId, text);
            message.enableMarkdown(true);
            telegramBot.execute(message);
        } catch (TelegramApiException e) {
            logger.debug(e.getMessage());

        }
    }
}
