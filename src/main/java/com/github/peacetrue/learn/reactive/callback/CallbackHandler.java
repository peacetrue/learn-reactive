package com.github.peacetrue.learn.reactive.callback;

import java.util.function.Supplier;

/**
 * 回调处理器
 **/
public interface CallbackHandler<T> {

    void onSuccess(T result);

    default void onError(Throwable exception) {
    }

    default void onComplete() {
    }

    static <T> void invoke(Supplier<T> action, CallbackHandler<T> handler) {
        new Thread(() -> {
            try {
                handler.onSuccess(action.get());
            } catch (Throwable exception) {
                handler.onError(exception);
            } finally {
                handler.onComplete();
            }
        }).start();
    }

}
