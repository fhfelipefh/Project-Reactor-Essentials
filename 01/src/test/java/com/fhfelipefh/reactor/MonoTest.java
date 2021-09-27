package com.fhfelipefh.reactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import reactor.test.StepVerifierOptions;

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
        StepVerifier.create(mono) // StepVerifier
                .expectNext(name)
                .verifyComplete();
    }


}
