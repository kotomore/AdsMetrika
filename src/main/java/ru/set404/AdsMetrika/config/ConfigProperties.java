package ru.set404.AdsMetrika.config;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:appconfig.properties")
@Getter
public class ConfigProperties {
    @Value("${parser.deep-scan}")
    Integer MinScanCount;
    @Value("${parser.default-timezone}")
    String defaultTimeZone;
    @Value("${parser.default-offset}")
    String defaultOffset;
    @Value("${parser.network.min-cost}")
    Integer minCost;

}