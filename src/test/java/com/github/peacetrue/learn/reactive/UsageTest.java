package com.github.peacetrue.learn.reactive;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import reactor.util.context.Context;

import java.util.HashSet;
import java.util.Set;

/**
 * @author : xiayx
 * @since : 2020-12-26 10:26
 **/
@Slf4j
public class UsageTest {

    @Test
    void emptyBack() {
        log.info("当你走到 empty 的时候，如何回来");
        Mono.empty()
                .doOnNext(item -> Assertions.fail("这不会输出"))
                .switchIfEmpty(Mono.just(1))
                .doOnNext(item -> {
                    log.info("通过 switchIfEmpty 回来。这里会输出: {}", item);
                    Assertions.assertEquals(1, item);
                })
                .then()
                .doOnNext(item -> Assertions.fail("这不会输出"))
                .then(Mono.just(1))
                .doOnNext(item -> {
                    log.info("通过 then 回来。这里会输出: {}", item);
                    Assertions.assertEquals(1, item);
                })
                .subscribe();
    }

    @Test
    void propagateContext_deferContextual() {
        log.info("传播一些共享变量");
        Mono
                .deferContextual(contextView -> Mono.just(contextView.get("userId")))
                .log("getUserId")
                .contextWrite(Context.of("userId", 1))
                .log("setUserId")
                .subscribe();
        //2020-12-26 12:07:33,621 [Test worker] INFO  getUserId.info - onSubscribe([Synchronous Fuseable] Operators.ScalarSubscription)
        //2020-12-26 12:07:33,624 [Test worker] INFO  setUserId.info - | onSubscribe([Fuseable] FluxContextWrite.ContextWriteSubscriber)
        //2020-12-26 12:07:33,625 [Test worker] INFO  setUserId.info - | request(unbounded)
        //2020-12-26 12:07:33,626 [Test worker] INFO  getUserId.info - request(unbounded)
        //2020-12-26 12:07:33,626 [Test worker] INFO  getUserId.info - onNext(1)
        //2020-12-26 12:07:33,626 [Test worker] INFO  setUserId.info - | onNext(1)
        //2020-12-26 12:07:33,627 [Test worker] INFO  getUserId.info - onComplete()
        //2020-12-26 12:07:33,628 [Test worker] INFO  setUserId.info - | onComplete()
    }

    @Test
    void propagateContext_deferContextual_comparison() {
        log.info("传播一些共享变量，与非 Context 对比");
        Mono
                .just(1) //<1>
                .log("getUserId")
                .log("setUserId")
                .subscribe();
        //2020-12-26 12:09:23,011 [Test worker] INFO  getUserId.info - | onSubscribe([Synchronous Fuseable] Operators.ScalarSubscription)
        //2020-12-26 12:09:23,016 [Test worker] INFO  setUserId.info - | onSubscribe([Fuseable] FluxPeekFuseable.PeekFuseableSubscriber)
        //2020-12-26 12:09:23,017 [Test worker] INFO  setUserId.info - | request(unbounded)
        //2020-12-26 12:09:23,017 [Test worker] INFO  getUserId.info - | request(unbounded)
        //2020-12-26 12:09:23,018 [Test worker] INFO  getUserId.info - | onNext(1)
        //2020-12-26 12:09:23,018 [Test worker] INFO  setUserId.info - | onNext(1)
        //2020-12-26 12:09:23,019 [Test worker] INFO  getUserId.info - | onComplete()
        //2020-12-26 12:09:23,020 [Test worker] INFO  setUserId.info - | onComplete()
    }

    @Test
    void propagateContext_deferContextual_flatMap() {
        log.info("与非 Context 对比");
        Mono
                .deferContextual(contextView -> Mono.just(contextView.get("userId")))
                .log("getUserId")
                .flatMap(integer -> Mono.just(integer).log("flatMap"))
                .log("setUserId")
                .contextWrite(Context.of("userId", 1))
                .subscribe()
        ;
        //2020-12-26 21:35:47,414 [Test worker] INFO  setUserId.info - | onSubscribe([Fuseable] MonoFlatMap.FlatMapMain)
        //2020-12-26 21:35:47,417 [Test worker] INFO  setUserId.info - | request(unbounded)
        //2020-12-26 21:35:47,418 [Test worker] INFO  getUserId.info - | onSubscribe([Synchronous Fuseable] Operators.ScalarSubscription)
        //2020-12-26 21:35:47,419 [Test worker] INFO  getUserId.info - | request(unbounded)
        //2020-12-26 21:35:47,420 [Test worker] INFO  getUserId.info - | onNext(1)
        //2020-12-26 21:35:47,420 [Test worker] INFO  flatMap.info - | onSubscribe([Synchronous Fuseable] Operators.ScalarSubscription)
        //2020-12-26 21:35:47,420 [Test worker] INFO  flatMap.info - | request(unbounded)
        //2020-12-26 21:35:47,420 [Test worker] INFO  flatMap.info - | onNext(1)
        //2020-12-26 21:35:47,420 [Test worker] INFO  setUserId.info - | onNext(1)
        //2020-12-26 21:35:47,421 [Test worker] INFO  setUserId.info - | onComplete()
        //2020-12-26 21:35:47,421 [Test worker] INFO  flatMap.info - | onComplete()
        //2020-12-26 21:35:47,422 [Test worker] INFO  getUserId.info - | onComplete()
    }

    @Test
    void propagateContext_deferContextual_flatMap_comparison() {
        log.info("与非 Context 对比");
        Mono
                .just(1)
                .log("getUserId")
                .flatMap(integer -> Mono.just(integer).log("flatMap"))
                .log("setUserId")
                .subscribe();
        //2020-12-26 21:35:47,414 [Test worker] INFO  setUserId.info - | onSubscribe([Fuseable] MonoFlatMap.FlatMapMain)
        //2020-12-26 21:35:47,417 [Test worker] INFO  setUserId.info - | request(unbounded)
        //2020-12-26 21:35:47,418 [Test worker] INFO  getUserId.info - | onSubscribe([Synchronous Fuseable] Operators.ScalarSubscription)
        //2020-12-26 21:35:47,419 [Test worker] INFO  getUserId.info - | request(unbounded)
        //2020-12-26 21:35:47,420 [Test worker] INFO  getUserId.info - | onNext(1)
        //2020-12-26 21:35:47,420 [Test worker] INFO  flatMap.info - | onSubscribe([Synchronous Fuseable] Operators.ScalarSubscription)
        //2020-12-26 21:35:47,420 [Test worker] INFO  flatMap.info - | request(unbounded)
        //2020-12-26 21:35:47,420 [Test worker] INFO  flatMap.info - | onNext(1)
        //2020-12-26 21:35:47,420 [Test worker] INFO  setUserId.info - | onNext(1)
        //2020-12-26 21:35:47,421 [Test worker] INFO  setUserId.info - | onComplete()
        //2020-12-26 21:35:47,421 [Test worker] INFO  flatMap.info - | onComplete()
        //2020-12-26 21:35:47,422 [Test worker] INFO  getUserId.info - | onComplete()
    }

    @Test
    void propagateContext_deferContextual_flatMap_deferContextual() {
        log.info("在 flatMap 中传播一些共享变量");
        Mono
                .deferContextual(contextView -> Mono.just(contextView.get("userId")))
                .log("getUserId")
                .flatMap(o -> Mono.deferContextual(contextView -> Mono.just(contextView.get("userId"))).log("flatMap"))
                .contextWrite(Context.of("userId", 1))
                .log("setUserId")
                .subscribe();
        //2020-12-27 00:39:46,121 [Test worker] INFO  setUserId.info - | onSubscribe([Fuseable] FluxContextWrite.ContextWriteSubscriber)
        //2020-12-27 00:39:46,123 [Test worker] INFO  setUserId.info - | request(unbounded)
        //2020-12-27 00:39:46,126 [Test worker] INFO  getUserId.info - onSubscribe([Synchronous Fuseable] Operators.ScalarSubscription)
        //2020-12-27 00:39:46,126 [Test worker] INFO  getUserId.info - request(unbounded)
        //2020-12-27 00:39:46,127 [Test worker] INFO  getUserId.info - onNext(1)
        //2020-12-27 00:39:46,128 [Test worker] INFO  flatMap.info - onSubscribe([Synchronous Fuseable] Operators.ScalarSubscription)
        //2020-12-27 00:39:46,128 [Test worker] INFO  flatMap.info - request(unbounded)
        //2020-12-27 00:39:46,128 [Test worker] INFO  flatMap.info - onNext(1)
        //2020-12-27 00:39:46,129 [Test worker] INFO  setUserId.info - | onNext(1)
        //2020-12-27 00:39:46,129 [Test worker] INFO  setUserId.info - | onComplete()
        //2020-12-27 00:39:46,129 [Test worker] INFO  flatMap.info - onComplete()
        //2020-12-27 00:39:46,130 [Test worker] INFO  getUserId.info - onComplete()
    }

    @Test
    void propagateContext_deferContextual_flatMap_contextWrite() {
        log.info("在 flatMap 中传播一些共享变量");
        Mono
                .deferContextual(contextView -> Mono.just(contextView.get("userId")))
                .log("getUserId")
                .flatMap(o -> Mono.deferContextual(contextView -> Mono.just(contextView.get("userId"))).contextWrite(Context.of("userId", 2)).log("flatMap"))
                .contextWrite(Context.of("userId", 1))
                .log("setUserId")
                .subscribe();
    }

    @Test
    void propagateContext_deferContextual_flatMap_contextWrite_error() {
        log.info("在 flatMap 中传播一些共享变量");
        Mono
                .deferContextual(contextView -> Mono.just(contextView.get("userId")))
                .log("getUserId")
                .flatMap(o -> Mono.deferContextual(contextView -> Mono.just(contextView.get("userId")).contextWrite(Context.of("userId", 2)).log("flatMap")))
                .contextWrite(Context.of("userId", 1))
                .log("setUserId")
                .subscribe();
    }

    @Test
    void propagateContext_deferContextual_flatMap_contextWrite_error_adjust() {
        log.info("在 flatMap 中传播一些共享变量");
        Mono
                .deferContextual(contextView -> Mono.<Integer>just(contextView.get("userId")))
                .log("getUserId")
                .transformDeferredContextual((objectMono, contextView) -> {
                    return objectMono
                            .flatMap(integer ->
                                    Mono
                                            .just(integer)
                                            .log("flatMap")
                                            .flatMap(integer1 -> {
                                                return Mono.deferContextual(contextView1 -> Mono.just(contextView1.get("userId")));
                                            })
                                            .contextWrite(Context.of("userId", 2))
                            )
                            ;
                })
                .contextWrite(Context.of("userId", 1))
                .log("setUserId")
                .subscribe();
    }

    @Test
    void propagateContext_contextWrite() {
        log.info("contextWrite 使用函数");
        Mono
                .deferContextual(contextView -> Mono.just(contextView.get("userId")))
                .log("getUserId")
                .flatMap(o -> Mono.deferContextual(contextView -> Mono.just(contextView.get("userId"))).log("flatMap"))
                .contextWrite(context -> context.put("userId", 1))
                .log("setUserId")
                .subscribe()
        ;
        //2020-12-27 00:39:46,121 [Test worker] INFO  setUserId.info - | onSubscribe([Fuseable] FluxContextWrite.ContextWriteSubscriber)
        //2020-12-27 00:39:46,123 [Test worker] INFO  setUserId.info - | request(unbounded)
        //2020-12-27 00:39:46,126 [Test worker] INFO  getUserId.info - onSubscribe([Synchronous Fuseable] Operators.ScalarSubscription)
        //2020-12-27 00:39:46,126 [Test worker] INFO  getUserId.info - request(unbounded)
        //2020-12-27 00:39:46,127 [Test worker] INFO  getUserId.info - onNext(1)
        //2020-12-27 00:39:46,128 [Test worker] INFO  flatMap.info - onSubscribe([Synchronous Fuseable] Operators.ScalarSubscription)
        //2020-12-27 00:39:46,128 [Test worker] INFO  flatMap.info - request(unbounded)
        //2020-12-27 00:39:46,128 [Test worker] INFO  flatMap.info - onNext(1)
        //2020-12-27 00:39:46,129 [Test worker] INFO  setUserId.info - | onNext(1)
        //2020-12-27 00:39:46,129 [Test worker] INFO  setUserId.info - | onComplete()
        //2020-12-27 00:39:46,129 [Test worker] INFO  flatMap.info - onComplete()
        //2020-12-27 00:39:46,130 [Test worker] INFO  getUserId.info - onComplete()
    }

    @Test
    void propagateContext_transformDeferredContextual() {
        log.info("传播一些共享变量");
        Mono
                .deferContextual(contextView -> {
                    return Mono.just(contextView.get("userId")).log("getUserId1");
                })
                .contextWrite(Context.of("userId", 1))
                .log("setUserId=1")
                .doOnNext(o -> log.info("outUserId: {}", o))
                .transformDeferredContextual((integerMono, contextView) -> {
//                    return integerMono
//                            .flatMap(o -> Mono.just(contextView.get("userId")).log("getUserId2"));
                    return Mono.just(contextView.get("userId")).log("getUserId2");
                })
                .contextWrite(Context.of("userId", 2))
                .log("setUserId=2")
                .subscribe();
    }

    @Test
    void shareVariable() {
        Set<Integer> cache = new HashSet<>();
        Flux.just(1, 2, 3, 4, 5)
                .filter(integer -> !cache.contains(integer))
                .doOnNext(integer -> log.info("item: {}", integer))
                .flatMap(integer -> Flux.just(1, 2).doOnNext(cache::add))
                .subscribe()
        ;
    }

    @Test
    void doOnSuccess() {
        Mono
                .empty()
                .doOnSuccess(o -> System.out.println("o"))
                .subscribeOn(Schedulers.boundedElastic())
                .subscribe()
        ;
    }
}
