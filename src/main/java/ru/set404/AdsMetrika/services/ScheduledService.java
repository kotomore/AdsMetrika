package ru.set404.AdsMetrika.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.set404.AdsMetrika.dto.TableDTO;
import ru.set404.AdsMetrika.models.User;
import ru.set404.AdsMetrika.scheduled.googlesheets.SpreadSheet;
import ru.set404.AdsMetrika.scheduled.telegram.TelegramBot;
import ru.set404.AdsMetrika.util.StatisticsUtilities;

import java.time.LocalDate;

@Service
public class ScheduledService {
    private final SpreadSheet spreadSheet;
    private final TelegramBot telegramBot;

    @Autowired
    public ScheduledService(SpreadSheet spreadSheet, TelegramBot telegramBot) {
        this.spreadSheet = spreadSheet;
        this.telegramBot = telegramBot;
    }

    public void writeSpreadSheetTable(User user, TableDTO combinedStats, LocalDate date) {
        spreadSheet.writeTable(user, StatisticsUtilities.convertTableDTOToObject(combinedStats), date);
    }

    public void sendTelegramMessage(User user, TableDTO combinedStats) {
        String text = "*Stats by " + LocalDate.now().minusDays(1) + "*\n\n" +
                "*Total clicks: *" + combinedStats.getTotalClicks() + "\n" +
                "*Total spend: *$" + (int) combinedStats.getTotalSpend() + "\n" +
                "*Total revenue: *$" + (int) combinedStats.getTotalRevenue() + "\n" +
                "\n---------\n\n" +
                "*Profit: *$" + (int) combinedStats.getTotalProfit() + "\n" +
                "*ROI: *" + combinedStats.getTotalROI() + "%";

        telegramBot.setAnswer(Long.parseLong(user.getSettings().getTelegramUsername()), text);
    }
}
