package com.github.peacetrue.learn.reactive.callback;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.concurrent.Flow;
import java.util.function.Supplier;

/**
 * @author : xiayx
 * @since : 2020-12-21 04:33
 **/
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CallbackPublisher<T> implements Flow.Publisher<T> {

    private Supplier<T> action;

    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        new Thread(() -> {
            try {
                subscriber.onNext(action.get());
            } catch (Exception exception) {
                subscriber.onError(exception);
            } finally {
                subscriber.onComplete();
            }
        }).start();
    }
}
