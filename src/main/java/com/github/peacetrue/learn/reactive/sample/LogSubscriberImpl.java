package com.github.peacetrue.learn.reactive.sample;


import java.util.concurrent.Flow;

/**
 * @author : xiayx
 * @since : 2020-12-21 14:59
 **/
public class LogSubscriberImpl<T> implements LogSubscriber<T> {

    private final long count;

    public LogSubscriberImpl(long count) {
        this.count = count;
    }

    public void onSubscribe(Flow.Subscription subscription) {
        subscription.request(count);
    }
}
