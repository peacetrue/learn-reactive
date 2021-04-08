package com.github.peacetrue.learn.reactive;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

/**
 * @author : xiayx
 * @since : 2020-12-21 07:36
 **/
@Slf4j
public class SchedulerTest {

    @Test
    void defaultAction() {
        log.info("观察默认行为所使用的线程");
        Mono.just(1)
                .subscribe(integer -> log.info("subscriber: {}", integer));
        log.info("main");
        //2020-12-21 19:29:24,198 [Test worker] INFO  c.g.p.learn.reactive.SchedulerTest.lambda$defaults$0 - subscriber: 1
        //2020-12-21 19:29:24,200 [Test worker] INFO  c.g.p.learn.reactive.SchedulerTest.defaults - main
        // Test worker 线程先输出 'subscriber' 然后输出 'main'，直接发布在当前线程上
    }

    @Test
    void defaultActionParameter() {
        log.info("推测默认行为所使用的参数");
        Mono.just(1)
                .subscribeOn(Schedulers.immediate())//<1> 默认行为等效于 immediate，直接发布在当前线程上
                .subscribe(integer -> log.info("subscriber: {}", integer));
        log.info("main");
        //2020-12-21 19:29:24,198 [Test worker] INFO  c.g.p.learn.reactive.SchedulerTest.lambda$defaults$0 - subscriber: 1
        //2020-12-21 19:29:24,200 [Test worker] INFO  c.g.p.learn.reactive.SchedulerTest.defaults - main
        // 发布者和订阅者都位于 Test worker 线程，因此订阅输出信息 'subscriber' 在后续 'main' 信息之前
    }

    @Test
    void comparisonDefaultAction() {
        log.info("对比默认行为");
        Mono.just(1)
                .subscribeOn(Schedulers.single())//<1> 在一个单独的线程发布，此线程为守护线程
                .subscribe(integer -> log.info("subscriber: {}", integer))
        ;
        log.info("main");
        //2020-12-21 20:17:31,196 [Test worker] INFO  c.g.p.learn.reactive.SchedulerTest.comparisonDefaultAction - main
        //2020-12-21 20:17:31,199 [single-1] INFO  c.g.p.learn.reactive.SchedulerTest.lambda$comparisonDefaultAction$2 - subscriber: 1
        // Test worker 线程输出 main，然后 single-1 线程输出 subscriber。发布行为在一个新的线程上执行，所以没有阻塞后续代码
        // 正常来说，主线程执行完毕，后续线程
    }

    public static void main(String[] args) throws Exception {
        new SchedulerTest()
                .comparisonDefaultAction()
        ;
    }

    @Test
    void basic() {
        log.info("查看发布者和订阅者所处的线程");
        Mono
                .just(1) //<1> 发布者
                .log("test")
                .subscribe(
                        integer -> log.info("out: {}", integer),
                        error -> log.warn("error", error),
                        () -> log.info("complete")
                )
        ;
        //2020-12-21 18:10:10,850 [Test worker] INFO  reactor.Mono.Just.1.info - | onSubscribe([Synchronous Fuseable] Operators.ScalarSubscription)
        //2020-12-21 18:10:10,853 [Test worker] INFO  reactor.Mono.Just.1.info - | request(unbounded)
        //2020-12-21 18:10:10,854 [Test worker] INFO  reactor.Mono.Just.1.info - | onNext(1)
        //2020-12-21 18:10:10,854 [Test worker] INFO  c.g.p.learn.reactive.SchedulerTest.lambda$basic$0 - out: 1
        //2020-12-21 18:10:10,855 [Test worker] INFO  reactor.Mono.Just.1.info - | onComplete()
        //发布者和订阅者所处的线程都是：Test worker
    }

    @Test
    void publishOn() {
        log.info("修改发布所处的线程");
        Mono
                .just(1)
                //Run onNext, onComplete and onError on a supplied Scheduler Worker.
                .publishOn(Schedulers.newSingle("publish"))
                .log("test")
                .subscribe(
                        integer -> log.info("out: {}", integer),
                        error -> log.warn("error", error),
                        () -> log.info("complete")
                )
        ;
        //2020-12-21 18:38:37,650 [Test worker] INFO  test.info - onSubscribe([Fuseable] FluxSubscribeOnValue.ScheduledScalar)
        //2020-12-21 18:38:37,654 [Test worker] INFO  test.info - request(unbounded)
        //2020-12-21 18:38:37,656 [publish-1] INFO  test.info - onNext(1)
        //2020-12-21 18:38:37,659 [publish-1] INFO  c.g.p.learn.reactive.SchedulerTest.lambda$publishOn$3 - out: 1
        //2020-12-21 18:38:37,659 [publish-1] INFO  c.g.p.learn.reactive.SchedulerTest.lambda$publishOn$5 - complete
        //2020-12-21 18:38:37,660 [publish-1] INFO  test.info - onComplete()
        //从 onNext 开始，线程从 Test worker 转变为 publish-1，订阅者所处线程与发布者相同
    }

    @Test
    void subscribeOn() {
        log.info("修改订阅所处的线程");
        Mono
                .just(1)
                //Run subscribe, onSubscribe and request on a specified Scheduler's Scheduler.Worker.
                .subscribeOn(Schedulers.newSingle("subscribe"))
                .log("test")
                .subscribe(
                        integer -> log.info("out: {}", integer),
                        error -> log.warn("error", error),
                        () -> log.info("complete")
                )
        ;
        log.info("Test worker");
        //2020-12-21 18:37:41,952 [Test worker] INFO  test.info - onSubscribe([Fuseable] FluxSubscribeOnValue.ScheduledScalar)
        //2020-12-21 18:37:41,955 [Test worker] INFO  test.info - request(unbounded)
        //2020-12-21 18:37:41,957 [subscribe-1] INFO  test.info - onNext(1)
        //2020-12-21 18:37:41,959 [subscribe-1] INFO  c.g.p.learn.reactive.SchedulerTest.lambda$subscribeOn$6 - out: 1
        //2020-12-21 18:37:41,959 [subscribe-1] INFO  c.g.p.learn.reactive.SchedulerTest.lambda$subscribeOn$8 - complete
        //2020-12-21 18:37:41,960 [subscribe-1] INFO  test.info - onComplete()
        //从 onNext 开始，线程从 Test worker 转变为 subscribe-1
    }

}
