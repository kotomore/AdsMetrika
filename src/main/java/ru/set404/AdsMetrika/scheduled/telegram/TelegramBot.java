package ru.set404.AdsMetrika.scheduled.telegram;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.BotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.starter.SpringWebhookBot;

@Getter
@Setter
@Component
public class TelegramBot extends SpringWebhookBot {
    @Value("${telegram.webhook-path}")
    private String botPath;
    @Value("${telegram.bot-name}")
    private String botUsername;
    @Value("${telegram.bot-token}")
    private String botToken;
    protected Log logger = LogFactory.getLog(this.getClass());

    @Autowired
    public TelegramBot(SetWebhook setWebhook) {
        super(setWebhook);
    }

    @Override
    public BotApiMethod<?> onWebhookUpdateReceived(Update update) {
        try {
            return answerMessage(update.getMessage());
        } catch (IllegalArgumentException e) {
            logger.debug(e.getMessage());
        }
        return null;
    }

    public BotApiMethod<?> answerMessage(Message message) {
        String chatId = message.getChatId().toString();

        String inputText = message.getText();
        if (inputText.equals("/start")) {
            return getStartMessage(chatId);
        }
        return null;
    }

    private SendMessage getStartMessage(String chatId) {
        String answer = """
                Copy: `%s`\s

                --------------
                Add this to *AdsMetrika - User - Settings - Telegram numbers* field
                https://adsmetrika.ru/settings""".formatted(chatId);

        SendMessage sendMessage = new SendMessage(chatId, answer);
        sendMessage.enableMarkdown(true);
        return sendMessage;
    }
}
