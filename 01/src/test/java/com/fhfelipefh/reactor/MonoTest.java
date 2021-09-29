package com.fhfelipefh.reactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

/**
 * Reactive Streams
 * 1. Asynchronous
 * 2. Non-Blocking
 * 3. Backpressure
 * Publisher <- (subscribe) Subscriber
 * Subscription is created
 * Publisher (onSubscribe with the subscription) -> Subscriber
 * Subscription <- (request N) Subscriber
 * Publisher -> (onNext) Subscriber
 * until:
 * 1. publisher sends all the objects requested.
 * 2. publisher sends all the objects it has. (onComplete) subscriber and subscriptions will be canceled.
 * 3. there is on error. (onError) -> subscriber and subscriptions will be canceled.
 */

@Slf4j
public class MonoTest {

    @Test
    public void monoSubscriber() {
        String name = "felipe";
        Mono<String> mono = Mono.just(name).log();
        StepVerifier.create(mono) // StepVerifier
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumer() {
        String name = "felipe";
        Mono<String> mono = Mono.just(name).log();
        mono.subscribe(s -> log.info(s)); // pega valor de dentro do publisher
        StepVerifier.create(mono)
                .expectNext(name)
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerError() {
        String name = "felipe";
        Mono<String> mono = Mono.just(name).map(s -> {
            throw new RuntimeException("Testing mono with error");
        });
        mono.subscribe(s -> log.info(s), s -> log.error("Something bad")); // pega valor de dentro do publisher
        // mono.subscribe(s -> log.info(s),Throwable::printStackTrace); print errors
        StepVerifier.create(mono)
                .expectError(RuntimeException.class)
                .verify();
    }

    @Test
    public void monoSubscriberConsumerComplete() {
        String name = "felipe";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);
        mono.subscribe(s -> log.info(s), Throwable::printStackTrace, () -> log.info("FINISHED!")); // pega valor de dentro do publisher
        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();
    }

    @Test
    public void monoSubscriberConsumerSubscription() {
        String name = "felipe";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase);

        mono.subscribe(s -> log.info(s), Throwable::printStackTrace,
                () -> log.info("FINISHED!")
                , subscription -> subscription.request(2));

        /*
        StepVerifier.create(mono)
                .expectNext(name.toUpperCase())
                .verifyComplete();*/
    }

    @Test
    public void monoDoOnMethods() {
        String name = "felipe";
        Mono<String> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(longNumber -> log.info("Request received..."))
                .doOnNext(s -> log.info("Value is gere. Executing: doOnNext{}: ", s))
                .doOnSuccess(s -> log.info("doOnSuccess executed"));

        mono.subscribe(s -> log.info(s), Throwable::printStackTrace,
                () -> log.info("FINISHED!"));
    }

    @Test
    public void monoDoOnMethods2() {
        String name = "felipe";
        Mono<Object> mono = Mono.just(name)
                .log()
                .map(String::toUpperCase)
                .doOnSubscribe(subscription -> log.info("Subscribed"))
                .doOnRequest(longNumber -> log.info("Request received..."))
                .flatMap(s -> Mono.empty())//clean lines, not be executed
                .doOnNext(s -> log.info("Value is gere. Executing: doOnNext{}: ", s))
                .doOnSuccess(s -> log.info("doOnSuccess executed"));
    }


    @Test
    public void monoDoOnError() {
        Mono<Object> error = Mono.error(new IllegalArgumentException("->ERROR - ERROR - ERROR<-"))
                .doOnError(message -> log.info("Error message: {}:", message.getMessage()))
                .doOnNext(s -> log.info("Executing do doOnNext")) // Não será executado em seguida
                .log();

        StepVerifier.create(error).expectError(IllegalArgumentException.class).verify();

    }

    @Test
    public void monoDoOnErrorResume() {
        String name = "test";
        Mono<Object> error = Mono.error(new IllegalArgumentException("->ERROR - ERROR - ERROR<-"))
                .doOnError(message -> MonoTest.log.error("Error message: {}:", message.getMessage()))
                .onErrorResume(s -> {
                    log.info("Inside on error");
                    return Mono.just(name);
                })
                .log();

        StepVerifier.create(error).expectNext(name).verifyComplete();
    }

    @Test
    public void monoDoOnErrorReturn() {
        String name = "test";
        Mono<Object> error = Mono.error(new IllegalArgumentException("->ERROR - ERROR - ERROR<-"))
                .doOnError(message -> MonoTest.log.error("Error message: {}:", message.getMessage()))
                .onErrorReturn("EMPTY")
                .onErrorResume(s -> {
                    log.info("Inside on error");
                    return Mono.just(name);
                })
                .log();

        StepVerifier.create(error).expectNext("EMPTY").verifyComplete();
    }

}
