package com.github.peacetrue.learn.reactive.sample;

import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.Flow;

/**
 * @author : xiayx
 * @since : 2020-12-21 14:22
 **/
@Slf4j
public class SamplePublisher<T> implements Flow.Publisher<T> {

    private final List<T> items;

    public SamplePublisher(List<T> items) {
        this.items = Objects.requireNonNull(items);
    }

    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        subscriber.onSubscribe(new Flow.Subscription() {
            private int fromIndex = 0;

            public void request(long l) {
                if (l < 0) {
                    subscriber.onError(new IllegalStateException("request count < 0, except > 0"));
                    return;
                }

                //请求一批数据
                int endCount = Math.min(items.size(), fromIndex + (int) l);
                for (int i = fromIndex; i < endCount; i++) {
                    subscriber.onNext(items.get(i));
                }

                //所有数据处理完毕
                if (endCount >= items.size()) {
                    subscriber.onComplete();
                    return;
                }

                //请求下一批数据
                fromIndex = endCount;
                subscriber.onSubscribe(this);
            }


            public void cancel() {
                //没用到线程池，此处无用
            }
        });
    }
}
