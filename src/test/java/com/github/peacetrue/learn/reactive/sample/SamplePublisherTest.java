package com.github.peacetrue.learn.reactive.sample;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.Flow;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author : xiayx
 * @since : 2020-12-21 16:01
 **/
@Slf4j
class SamplePublisherTest {
    @Test
    void subscribe() {
        List<Integer> integers = IntStream.range(0, 10).boxed().collect(Collectors.toList());
        SamplePublisher<Integer> publisher = new SamplePublisher<>(integers);
        publisher.subscribe(new LogSubscriber<>() {
            private int times = 0;

            public void onSubscribe(Flow.Subscription subscription) {
                //限流控制
                times++;
                log.info("第 {} 次，请求 {} 条记录", times, times * 2);
                subscription.request(times * 2);
            }
        });
        //2020-12-21 16:14:25,540 [Test worker] INFO  c.g.p.l.r.sample.LogSubscriber.onSubscribe - 第 1 次，请求 2 条记录
        //2020-12-21 16:14:25,547 [Test worker] INFO  c.g.p.l.r.sample.LogSubscriber.onNext - item: 0
        //2020-12-21 16:14:25,547 [Test worker] INFO  c.g.p.l.r.sample.LogSubscriber.onNext - item: 1
        //2020-12-21 16:14:25,548 [Test worker] INFO  c.g.p.l.r.sample.LogSubscriber.onSubscribe - 第 2 次，请求 4 条记录
        //2020-12-21 16:14:25,549 [Test worker] INFO  c.g.p.l.r.sample.LogSubscriber.onNext - item: 1
        //2020-12-21 16:14:25,549 [Test worker] INFO  c.g.p.l.r.sample.LogSubscriber.onNext - item: 2
        //2020-12-21 16:14:25,550 [Test worker] INFO  c.g.p.l.r.sample.LogSubscriber.onNext - item: 3
        //2020-12-21 16:14:25,550 [Test worker] INFO  c.g.p.l.r.sample.LogSubscriber.onNext - item: 4
        //2020-12-21 16:14:25,550 [Test worker] INFO  c.g.p.l.r.sample.LogSubscriber.onSubscribe - 第 3 次，请求 6 条记录
        //2020-12-21 16:14:25,551 [Test worker] INFO  c.g.p.l.r.sample.LogSubscriber.onNext - item: 4
        //2020-12-21 16:14:25,551 [Test worker] INFO  c.g.p.l.r.sample.LogSubscriber.onNext - item: 5
        //2020-12-21 16:14:25,551 [Test worker] INFO  c.g.p.l.r.sample.LogSubscriber.onNext - item: 6
        //2020-12-21 16:14:25,551 [Test worker] INFO  c.g.p.l.r.sample.LogSubscriber.onNext - item: 7
        //2020-12-21 16:14:25,551 [Test worker] INFO  c.g.p.l.r.sample.LogSubscriber.onNext - item: 8
        //2020-12-21 16:14:25,552 [Test worker] INFO  c.g.p.l.r.sample.LogSubscriber.onNext - item: 9
        //2020-12-21 16:14:25,552 [Test worker] INFO  c.g.p.l.r.sample.LogSubscriber.onComplete - complete
    }
}
