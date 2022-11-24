package ru.set404.AdsMetrika;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.telegram.telegrambots.meta.api.methods.updates.SetWebhook;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import ru.set404.AdsMetrika.scheduled.telegram.TelegramBot;

@SpringBootApplication
@EnableCaching
@EnableScheduling
public class AdsMetrikaApplication {
    @Value("${telegram.webhook-path}")
    private String webhookPath;
    @Value("${telegram.bot-name}")
    private String botName;
    @Value("${telegram.bot-token}")
    private String botToken;

    public static void main(String[] args) {
        SpringApplication.run(AdsMetrikaApplication.class, args);
    }

    @Bean
    public ObjectMapper objectMapper() {
        return new ObjectMapper();
    }

    @Bean
    public ModelMapper modelMapper() {
        return new ModelMapper();
    }

    @Bean
    public CacheManager cacheManager() {
        return new ConcurrentMapCacheManager("credentials", "networks",
                "settings", "stats", "network_stats");
    }

    @Bean
    public SetWebhook setWebhookInstance() {
        return SetWebhook.builder().url(webhookPath).build();
    }

    @Bean
    public TelegramBot springWebhookBot(SetWebhook setWebhook) throws TelegramApiException {
        return new TelegramBot(setWebhook).setup(webhookPath, botName, botToken);
    }
}
