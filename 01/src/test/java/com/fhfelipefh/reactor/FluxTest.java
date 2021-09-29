package com.fhfelipefh.reactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.util.ArrayList;
import java.util.List;


// 1 or more elements
@Slf4j
public class FluxTest {

    @Test
    public void fluxSubscriber() {
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
            private int requestCount = 2;

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

}
