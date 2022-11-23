package com.kafka.webflux.pubsub.service.impl;

import com.kafka.webflux.pubsub.service.KafkaService;
import lombok.RequiredArgsConstructor;
import org.apache.kafka.clients.producer.ProducerRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.core.publisher.Mono;
import org.springframework.stereotype.Service;
import org.springframework.http.codec.ServerSentEvent;
import reactor.core.Disposable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOffset;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.CountDownLatch;
import java.util.function.Consumer;

@Service
@RequiredArgsConstructor
public class KafkaServiceImpl implements KafkaService {

    private final KafkaSender<String, Object> kafkaSender;

    @Override
    public Mono<Boolean> send(String topic, String key, Object value) {
        return kafkaSender.createOutbound()
                .send(Mono.just(new ProducerRecord<>(topic, key, value)))  // 해당 topic으로 message 전송
                .then()
                .map(ret -> true)
                .onErrorResume(e -> {
                    System.out.println("Kafka send error");
                    return Mono.just(false);
                });
    }

    
    private final ReceiverOptions<String, Object> receiverOptions;
    private final Sinks.Many<Object> sinksMany;
    private Disposable disposable;

    @PostConstruct
    public void init() {    // Consumer를 열어놓음
        disposable = KafkaReceiver.create(receiverOptions).receive()
                .doOnNext(processReceivedData())
                .doOnError(e -> {
                    System.out.println("Kafka read error");
                    init();     // 에러 발생 시, consumer가 종료되고 재시작할 방법이 없기 때문에 error시 재시작
                })
                .subscribe();
    }
    
    @PreDestroy
    public void destroy() { 
        if (disposable != null) {
        }
    }
    private Consumer<ReceiverRecord<String, Object>> processReceivedData() {
        return r -> {
            System.out.println("Kafka Consuming");
            Object receivedData = r.value();
            if (receivedData != null) {
                sinksMany.emitNext(r.value(), Sinks.EmitFailureHandler.FAIL_FAST);   // data를 consuming할때마다 sink로 전송
            }
            r.receiverOffset().acknowledge();
        };
    }
}