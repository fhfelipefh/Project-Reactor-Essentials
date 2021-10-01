package com.fhfelipefh.reactor;

import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.test.StepVerifier;
import reactor.util.function.Tuple3;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
public class OperatorsTest {

    @BeforeAll
    public static void setUp() {
        BlockHound.install();
    }

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
                .expectNext("BC", "BD")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeOperator() {
        Flux<String> f1 = Flux.just("a", "b");
        Flux<String> f2 = Flux.just("c", "d").delayElements(Duration.ofMillis(50));

        Flux<String> fluxMerge = Flux.merge(f1, f2)
                .delayElements(Duration.ofMillis(200))
                .log();

        StepVerifier.create(fluxMerge)
                .expectSubscription()
                .expectNext("a", "b", "c", "d")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeWithOperator() {
        Flux<String> f1 = Flux.just("a", "b");
        Flux<String> f2 = Flux.just("c", "d").delayElements(Duration.ofMillis(50));

        Flux<String> fluxMerge = f1.mergeWith(f2)
                .delayElements(Duration.ofMillis(200))
                .log();

        StepVerifier.create(fluxMerge)
                .expectSubscription()
                .expectNext("a", "b", "c", "d")
                .expectComplete()
                .verify();
    }

    @Test
    public void mergeSequentialOperator() {
        Flux<String> f1 = Flux.just("a", "b");
        Flux<String> f2 = Flux.just("c", "d").delayElements(Duration.ofMillis(50));

        Flux<String> fluxMerge = Flux.mergeSequential(f1, f2, f1) //waiting all elements
                .log();

        StepVerifier.create(fluxMerge)
                .expectSubscription()
                .expectNext("a", "b", "c", "d", "a", "b")
                .expectComplete()
                .verify();
    }

    @Test
    public void concatDelayErrorOperator() {
        Flux<String> f1 = Flux.just("a", "b")
                .map(s -> {
                    if (s.equalsIgnoreCase("b")) {
                        throw new IllegalArgumentException("b exception");
                    }
                    return s;
                });
        Flux<String> f2 = Flux.just("c", "d");
        Flux<String> fluxMerge = Flux.concatDelayError(f1, f2).log();

        StepVerifier.create(fluxMerge)
                .expectSubscription()
                .expectNext("a", "c", "d")
                .expectError()
                .verify();
    }

    @Test
    public void mergeDelayErrorOperator() {
        Flux<String> f1 = Flux.just("a", "b")
                .map(s -> {
                    if (s.equalsIgnoreCase("b")) {
                        throw new IllegalArgumentException("b exception");
                    }
                    return s;
                });
        Flux<String> f2 = Flux.just("c", "d");
        Flux<String> fluxMerge = Flux.mergeDelayError(1, f1, f2).log();

        StepVerifier.create(fluxMerge)
                .expectSubscription()
                .expectNext("a", "c", "d")
                .expectError()
                .verify();
    }

    @Test
    public void flatMapOperator() {
        Flux<String> flux = Flux.just("a", "b");

        Flux<String> flatFlux = flux.flatMap(this::findByName).log();

        StepVerifier.create(flatFlux).expectSubscription()
                .expectNext("NA1", "NA2", "NB1", "NB2")
                .verifyComplete();

    }

    @Test
    public void flatMapSequentialOperator() {
        Flux<String> flux = Flux.just("a", "b");
        Flux<String> flatFlux = flux.flatMapSequential(this::findByName).log();

        StepVerifier.create(flatFlux).expectSubscription()
                .expectNext("NA1", "NA2", "NB1", "NB2")
                .verifyComplete();
    }

    @Test
    public void zipOperator() {
        Flux<String> title = Flux.just("Naruto", "One punch man");
        Flux<String> studio = Flux.just("Kyoto Animation", "Mad House");
        Flux<Integer> episodes = Flux.just(258, 12);

        Flux<Anime> animeFlux = Flux.zip(title, studio, episodes)
                .flatMap(tuple -> Flux.just(new Anime(tuple.getT1(), tuple.getT2(), tuple.getT3())));
        StepVerifier.create(animeFlux)
                .expectSubscription()
                .expectNext(new Anime("Naruto", "Kyoto Animation", 258), new Anime("One punch man", "Mad House", 12))
                .verifyComplete();
    }

    @AllArgsConstructor
    @Getter
    @ToString
    @EqualsAndHashCode
    class Anime {
        private String title;
        private String studio;
        private int episodes;
    }


    public Flux<String> findByName(String name) {
        return name.equals("a") ? Flux.just("NA1", "NA2") : Flux.just("NB1", "NB2");
    }


}
