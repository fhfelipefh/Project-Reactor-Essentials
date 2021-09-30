package com.fhfelipefh.reactor;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class OperatorsTest {

    @Test
    public void subscribeOnSingle() {
        Flux<Integer> flux = Flux.range(1, 4)
                .map(i -> {
                    log.info("Map 1 - on Thread: {}", i, Thread.currentThread().getName());
                    return i;
                }).subscribeOn(Schedulers.single()) // 1 thread
                .map(i -> {
                    log.info("Map 2 - on Thread: {}", i, Thread.currentThread().getName());
                    return i;
                });

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void publishOnSimple() {
        Flux<Integer> flux = Flux.range(1, 4)
                .map(i -> {
                    log.info("Map 1 - on Thread: {}", i, Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.boundedElastic()) // More peoples, more threads
                .map(i -> {
                    log.info("Map 2 - on Thread: {}", i, Thread.currentThread().getName());
                    return i;
                });
        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void multipleSubscribeOn() {
        Flux<Integer> flux = Flux.range(1, 4)
                .publishOn(Schedulers.single())
                .map(i -> {
                    log.info("Map 1 - on Thread: {}", i, Thread.currentThread().getName());
                    return i;
                })
                .publishOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 2 - on Thread: {}", i, Thread.currentThread().getName());
                    return i;
                });
        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void publishAndSubscribeOnSimple() {
        Flux<Integer> flux = Flux.range(1, 4)
                .publishOn(Schedulers.single())
                .map(i -> {
                    log.info("Map 1 - on Thread: {}", i, Thread.currentThread().getName());
                    return i;
                })
                .subscribeOn(Schedulers.boundedElastic())
                .map(i -> {
                    log.info("Map 2 - on Thread: {}", i, Thread.currentThread().getName());
                    return i;
                });
        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext(1, 2, 3, 4)
                .verifyComplete();
    }

    @Test
    public void subscribeOnIO() throws Exception {
        Mono<List<String>> list = Mono.fromCallable(() -> Files.readAllLines(Paths.get("./textfile.txt")))
                .log()
                .subscribeOn(Schedulers.boundedElastic());

        list.subscribe(s -> log.info("{}", s));
        Thread.sleep(3000);
        StepVerifier.create(list)
                .expectSubscription()
                .thenConsumeWhile(l -> {
                    Assertions.assertFalse(l.isEmpty());
                    log.info("Size {}", l.size());
                    return true;
                }).verifyComplete();
    }

    @Test
    public void switchIfEmptyOperator() {
        Flux<Object> flux = emptyFlux()
                .switchIfEmpty(Flux.just("No more empty"))
                .log();

        StepVerifier.create(flux)
                .expectSubscription()
                .expectNext("No more empty")
                .expectComplete()
                .verify();
    }

    @Test
    public void deferOperation() {
        Mono<Long> defer = Mono.defer(() -> Mono.just(System.currentTimeMillis()));
        defer.subscribe(s -> log.info("{}", s));

        AtomicLong atomicLong = new AtomicLong(0);
        defer.subscribe(atomicLong::set);
        Assertions.assertTrue(atomicLong.get() > 0);

    }

    private Flux<Object> emptyFlux() {
        return Flux.empty();
    }

    @Test
    public void concatOperator() {
        Flux<String> f1 = Flux.just("a", "b");
        Flux<String> f2 = Flux.just("c", "d");
        Flux<String> log = Flux.concat(f1, f2).log();
        StepVerifier.create(log)
                .expectSubscription()
                .expectNext("a", "b", "c", "d")
                .expectComplete()
                .verify();
    }

    @Test
    public void concatWithOperator() {
        Flux<String> f1 = Flux.just("a", "b");
        Flux<String> f2 = Flux.just("c", "d");

        Flux<String> log = f1.concatWith(f2).log();

        StepVerifier.create(log)
                .expectSubscription()
                .expectNext("a", "b", "c", "d")
                .expectComplete()
                .verify();
    }

    @Test
    public void combineLatestOperator() {
        Flux<String> f1 = Flux.just("a", "b");
        Flux<String> f2 = Flux.just("c", "d");
        Flux<String> log = Flux.combineLatest(f1, f2, (fx1, fx2) -> fx1.toUpperCase() + fx2.toUpperCase()).log();
        StepVerifier.create(log)
                .expectSubscription()
                .expectNext("BC","BD")
                .expectComplete()
                .verify();
    }

    

}
