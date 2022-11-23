package com.kafka.webflux.pubsub.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Sinks;

@Configuration
public class SseConfig {
    @Bean
    public Sinks.Many<Object> sinksMany() {
        return Sinks.many().multicast().directBestEffort();
    }
}