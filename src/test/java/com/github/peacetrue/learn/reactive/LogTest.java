package com.github.peacetrue.learn.reactive;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.blockhound.BlockHound;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Hooks;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.time.Duration;
import java.util.concurrent.CountDownLatch;

/**
 * @author : xiayx
 * @since : 2020-12-21 06:34
 **/
@Slf4j
public class LogTest {

    @Test
    void basic() {
        Mono.just(1)
                .publishOn(Schedulers.newSingle("log1"))
                .subscribeOn(Schedulers.single())
                .subscribe(i -> log.info("i in mono: {}", i));
    }

    @Test
    void log01() {
        log.info("对比同步和异步日志输出");
        Mono.just(1)//异步代码
                .subscribe(i -> log.info("i in mono: {}", i));
        log.info("i in main: {}", 1);//同步代码
        //2020-12-21 14:12:06,560 [Test worker] INFO  c.g.p.learn.reactive.MonoTest.lambda$basic$0 - i in mono: 1
        //2020-12-21 14:12:06,563 [Test worker] INFO  c.g.p.learn.reactive.MonoTest.basic - i in main: 1
    }

    @Test
    void log02() {
        log.info("对比同步和异步日志输出");
        Mono.fromCallable(() -> {
            Thread.sleep(1000);
            return 1;
        })
                .subscribe(i -> log.info("i in mono: {}", i));
        log.info("i in main: {}", 1);
        //2020-12-21 14:54:04,683 [Test worker] INFO  c.g.p.learn.reactive.MonoTest.lambda$log02$2 - i in mono: 1
        //2020-12-21 14:54:04,686 [Test worker] INFO  c.g.p.learn.reactive.MonoTest.log02 - i in main: 1
    }

    @Test
    void viewFlowFromLog() {
        log.info("从日志看调用流程");
        Flux.range(1, 2)
                .log("test-1")
                .log("test-2")
                .subscribe()
        ;
        //2020-12-21 15:18:34,023 [Test worker] INFO  test-1.info - | onSubscribe([Synchronous Fuseable] FluxRange.RangeSubscription)
        //2020-12-21 15:18:34,025 [Test worker] INFO  test-2.info - | onSubscribe([Fuseable] FluxPeekFuseable.PeekFuseableSubscriber)
        //2020-12-21 15:18:34,026 [Test worker] INFO  test-2.info - | request(unbounded)
        //2020-12-21 15:18:34,026 [Test worker] INFO  test-1.info - | request(unbounded)
        //2020-12-21 15:18:34,026 [Test worker] INFO  test-1.info - | onNext(1)
        //2020-12-21 15:18:34,027 [Test worker] INFO  test-2.info - | onNext(1)
        //2020-12-21 15:18:34,027 [Test worker] INFO  test-1.info - | onNext(2)
        //2020-12-21 15:18:34,027 [Test worker] INFO  test-2.info - | onNext(2)
        //2020-12-21 15:18:34,028 [Test worker] INFO  test-1.info - | onComplete()
        //2020-12-21 15:18:34,028 [Test worker] INFO  test-2.info - | onComplete()
    }

    @Test
    void onOperatorDebug() {
        Hooks.onOperatorDebug();
        Flux.range(0, 5)
                .single() // <-- Aha!
                .subscribeOn(Schedulers.parallel())
                .block();
    }

    @Test
    void diedLock() {
        BlockHound.install();

        Flux.range(0, Runtime.getRuntime().availableProcessors() * 2)
                .subscribeOn(Schedulers.parallel())
                .map(i -> {
                    CountDownLatch latch = new CountDownLatch(1);

                    Mono.delay(Duration.ofMillis(i * 100))
                            .subscribe(it -> latch.countDown());

                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }

                    return i;
                })
                .blockLast();
    }


}
