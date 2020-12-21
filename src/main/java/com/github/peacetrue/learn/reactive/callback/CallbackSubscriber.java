package com.github.peacetrue.learn.reactive.callback;

import java.util.concurrent.Flow;

/**
 * @author : xiayx
 * @since : 2020-12-21 04:55
 **/
public interface CallbackSubscriber<T> extends Flow.Subscriber<T>, CallbackHandler<T> {

    @Override
    default void onSubscribe(Flow.Subscription subscription) {
    }

    @Override
    default void onSuccess(T result) {
    }

    @Override
    default void onNext(T item) {
        onSuccess(item);
    }

    @Override
    default void onError(Throwable throwable) {
    }

    @Override
    default void onComplete() {
    }
}
