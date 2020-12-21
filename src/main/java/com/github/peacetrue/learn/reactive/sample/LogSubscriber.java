package com.github.peacetrue.learn.reactive.sample;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Flow;

/**
 * @author : xiayx
 * @since : 2020-12-21 14:57
 **/
public interface LogSubscriber<T> extends Flow.Subscriber<T> {

    Logger log = LoggerFactory.getLogger(LogSubscriber.class);

    default void onNext(T t) {
        log.info("item: {}", t);
    }

    default void onError(Throwable throwable) {
        log.warn("error: {}", throwable.getMessage());
    }

    default void onComplete() {
        log.info("complete");
    }
}
