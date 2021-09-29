package com.fhfelipefh.reactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

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
        Flux<Integer> stringFlux = Flux.range(1,5).log();

        stringFlux.subscribe(i -> log.info("Number: ",i));
        System.out.println("-------------------");
        StepVerifier.create(stringFlux).expectNext(1,2,3,4,5).verifyComplete();
    }

    


}
