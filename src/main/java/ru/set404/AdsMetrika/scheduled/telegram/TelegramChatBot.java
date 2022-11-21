package ru.set404.AdsMetrika.scheduled.telegram;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@Component
@PropertySource("classpath:application.properties")
public class TelegramChatBot {
    private Bot bot;

    @Value("${telegram.bot.name}")
    private String BOT_NAME;

    @Value("${telegram.bot.token}")
    private String BOT_TOKEN;

    private Bot getBot() {

        if (this.bot == null) {
            try {
                TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
                Bot bot = new Bot(BOT_NAME, BOT_TOKEN);
                botsApi.registerBot(bot);
                return bot;
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }
        return bot;
    }

    public void sendStatistics(long chatId, String text) {
        bot = getBot();
        bot.setAnswer(chatId, text);
    }
}
