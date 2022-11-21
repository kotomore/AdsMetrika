package ru.set404.AdsMetrika.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.dto.TableDTO;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.scheduled.googlesheets.SpreadSheet;
import ru.set404.AdsMetrika.scheduled.telegram.TelegramChatBot;
import ru.set404.AdsMetrika.util.StatisticsUtilities;

import java.time.LocalDate;

@Service
public class ScheduledService {
    private final SpreadSheet spreadSheet;
    private final TelegramChatBot telegramChatBot;

    @Autowired
    public ScheduledService(SpreadSheet spreadSheet, TelegramChatBot telegramChatBot) {
        this.spreadSheet = spreadSheet;
        this.telegramChatBot = telegramChatBot;
    }

    public void writeSpreadSheetTable(User user, TableDTO combinedStats, LocalDate date) {
        spreadSheet.writeTable(user, StatisticsUtilities.convertTableDTOToObject(combinedStats), date);
    }

    public void sendTelegramMessage(User user, TableDTO combinedStats) {
        String text = "Stats by " + LocalDate.now().minusDays(1) + "\n\n" +
                "Total clicks:" + combinedStats.getTotalClicks() + "\n" +
                "Total spend:" + combinedStats.getTotalSpend() + "\n" +
                "Total revenue:" + combinedStats.getTotalRevenue() + "\n" +
                "\n---------\n\n" +
                "Profit:" + combinedStats.getTotalProfit() + "\n" +
                "ROI:" + combinedStats.getTotalROI();

        telegramChatBot.sendStatistics(Long.parseLong(user.getSettings().getTelegramUsername()), text);
    }
}
