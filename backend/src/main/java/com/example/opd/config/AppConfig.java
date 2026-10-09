package com.example.opd.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;

@Configuration
public class AppConfig {

    /**
     * All "now" and "today" calculations use the server's local time zone.
     * Having the Clock as a bean keeps that rule in one place.
     */
    @Bean
    public Clock clock() {
        return Clock.systemDefaultZone();
    }
}
