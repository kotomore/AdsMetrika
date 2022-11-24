package ru.set404.AdsMetrika.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.set404.AdsMetrika.scheduled.telegram.TelegramBot;

@RestController
public class TelegramWebhookController {
    private final TelegramBot telegramBot;

    @Autowired
    public TelegramWebhookController(TelegramBot telegramBot) {
        this.telegramBot = telegramBot;
    }

    @PostMapping("/telegram-webhook")
    public BotApiMethod<?> onUpdateReceived(@RequestBody Update update) {
        return telegramBot.onWebhookUpdateReceived(update);
    }
}

