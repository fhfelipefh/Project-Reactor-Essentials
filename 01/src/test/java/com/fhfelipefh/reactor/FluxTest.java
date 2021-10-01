package com.fhfelipefh.reactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.BaseSubscriber;
import reactor.core.publisher.ConnectableFlux;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;


// 1 or more elements
@Slf4j
public class FluxTest {

    @BeforeAll
    public static void setUp() {
        BlockHound.install();
    }

    @Test
    public void fluxSubscriber() throws InterruptedException {
        Flux<String> stringFlux = Flux.just("f", "e", "l", "i", "p", "e");
        StepVerifier.create(stringFlux).expectNext("f", "e", "l", "i", "p", "e").verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbers() {
        Flux<Integer> stringFlux = Flux.range(1, 5).log();

        stringFlux.subscribe(i -> log.info("Number: ", i));
        System.out.println("-------------------");
        StepVerifier.create(stringFlux).expectNext(1, 2, 3, 4, 5).verifyComplete();
    }

    @Test
    public void fluxSubscriberFromList() {
        List<Integer> list = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            list.add(i);
        }
        Flux<List<Integer>> stringFlux = Flux.just(list);
        System.out.println("-------------------");
        StepVerifier.create(stringFlux).expectNext(list).verifyComplete();
    }


    @Test
    public void fluxSubscriberNumbersError() {
        Flux<Integer> flux = Flux.range(1, 5)
                .log()
                .map(i -> {
                    if (i == 4) {
                        throw new IndexOutOfBoundsException("Index Error");
                    }
                    return i;
                });

        flux.subscribe(i -> log.info("Number: ", i), Throwable::printStackTrace, () -> log.info("DONE!")
                , subscription -> subscription.request(3));

        StepVerifier.create(flux)
                .expectNext(1, 2, 3)
                .expectError(IndexOutOfBoundsException.class)
                .verify();
    }

    @Test
    public void fluxSubscriberNumbersUglyBackpressure() {
        Flux<Integer> flux = Flux.range(1, 10)
                .log();

        flux.subscribe(new Subscriber<Integer>() { // 2 em 2 items
            private int count = 0;
            private Subscription subscription;
            private final int requestCount = 2;

            @Override
            public void onSubscribe(Subscription subscription) {
                this.subscription = subscription;
                subscription.request(2);
            }

            @Override
            public void onNext(Integer integer) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    subscription.request(requestCount);
                }
            }

            @Override
            public void onError(Throwable throwable) {

            }

            @Override
            public void onComplete() {

            }
        });
        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberNumbersNotSoUglyBackpressure() {
        Flux<Integer> flux = Flux.range(1, 10)
                .log();

        flux.subscribe(new BaseSubscriber<Integer>() {
            private int count = 0;
            private final int requestCount = 2;

            @Override
            protected void hookOnSubscribe(Subscription subscription) {
                request(requestCount);
            }

            @Override
            protected void hookOnNext(Integer value) {
                count++;
                if (count >= requestCount) {
                    count = 0;
                    request(requestCount);
                }
            }
        });
        StepVerifier.create(flux)
                .expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10)
                .verifyComplete();
    }

    @Test
    public void fluxSubscriberPrettyBackPressure() {
        Flux<Integer> stringFlux = Flux.range(1, 10)
                .log()
                .limitRate(3);

        stringFlux.subscribe(i -> log.info("Number: ", i));
        System.out.println("-------------------");
        StepVerifier.create(stringFlux).expectNext(1, 2, 3, 4, 5, 6, 7, 8, 9, 10).verifyComplete();
    }


    @Test
    public void fluxSubscriberIntervalOne() throws Exception {
        Flux<Long> interval = Flux.interval(Duration.ofMillis(100))
                .log();

        interval.subscribe(i -> log.info("Number: ", i));
        Thread.sleep(3000);
    }

    @Test
    public void fluxSubscriberIntervalTwo() throws Exception {
        StepVerifier.withVirtualTime(this::createInterval)
                .expectSubscription()
                .expectNoEvent(Duration.ofHours(23))
                .thenAwait(Duration.ofDays(2))
                .expectNext(0L)
                .expectNext(1L)
                .thenCancel()
                .verify();
    }

    private Flux<Long> createInterval() {
        return Flux.interval(Duration.ofDays(1)).log();
    }

    @Test
    public void connectableFlux() throws InterruptedException {
        ConnectableFlux<Integer> connectableFlux = Flux.range(1, 10)
                .log()
                .delayElements(Duration.ofMillis(100))
                .publish();

        connectableFlux.connect();

        StepVerifier.create(connectableFlux)
                .thenConsumeWhile(i -> i <= 5)
                .expectNext(6, 7, 8, 9, 10)
                .expectComplete()
                .verify();
    }

    @Test
    public void connectableFluxAutoConnect() throws InterruptedException {
        Flux<Integer> fluxAutoConnect = Flux.range(1, 5)
                .log()
                .delayElements(Duration.ofMillis(100))
                .publish()
                .autoConnect(2);


        StepVerifier.create(fluxAutoConnect)
                .then(fluxAutoConnect::subscribe)
                .expectNext(1, 2, 3, 4, 5)
                .expectComplete()
                .verify();
    }

}
