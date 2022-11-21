package ru.set404.AdsMetrika.scheduled.telegram;

import org.telegram.telegrambots.meta.api.objects.Chat;
import org.telegram.telegrambots.meta.api.objects.User;
import org.telegram.telegrambots.meta.bots.AbsSender;

/**
 * Команда "Старт"
 */
public class StartCommand extends ServiceCommand {

    public StartCommand(String identifier, String description) {
        super(identifier, description);
    }

    @Override
    public void execute(AbsSender absSender, User user, Chat chat, String[] strings) {
        String userName = (user.getUserName() != null) ? user.getUserName() :
                String.format("%s %s", user.getLastName(), user.getFirstName());

        sendAnswer(absSender, chat.getId(), this.getCommandIdentifier(), userName,
                ("""
                        Copy: `%s`\s

                        --------------
                        Add this to *AdsMetrika - User - Settings - Telegram numbers* field
                        https://adsmetrika.ru/settings""").formatted(chat.getId()));

        sendAnswer(absSender, chat.getId(), this.getCommandIdentifier(), userName,
                "You will receive statistics every day at *11:00*");
    }
}