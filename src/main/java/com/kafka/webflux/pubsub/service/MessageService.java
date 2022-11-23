package com.kafka.webflux.pubsub.service;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.publisher.Mono;
import reactor.core.publisher.Flux;

public interface MessageService {
    Mono<String> send(String key, Object value);
    Flux<ServerSentEvent<Object>> receive();
}