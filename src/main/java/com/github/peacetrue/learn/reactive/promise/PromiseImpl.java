package com.github.peacetrue.learn.reactive.promise;

import com.github.peacetrue.learn.reactive.callback.CallbackHandler;
import com.github.peacetrue.learn.reactive.callback.CallbackResult;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author : xiayx
 * @since : 2020-12-20 12:47
 **/
@Slf4j
public class PromiseImpl<T> extends CallbackResult<T> implements Promise<T>, CallbackHandler<T> {

    private final List<Consumer<T>> successConsumers = new LinkedList<>();
    private final List<Consumer<Throwable>> errorConsumers = new LinkedList<>();
    private final List<Runnable> completeRunnables = new LinkedList<>();

    public void onSuccess(T result) {
        this.setValue(result);
        this.setCompleted(true);
        for (Consumer<T> successConsumer : successConsumers) {
            try {
                successConsumer.accept(getValue());
            } catch (Throwable exception) {
                log.warn("success consumer invoke exception", exception);
            }
        }
    }

    public void onError(Throwable exception) {
        this.setException(exception);
        this.setCompleted(true);
        for (Consumer<Throwable> errorConsumer : errorConsumers) {
            try {
                errorConsumer.accept(getException());
            } catch (Throwable consumerException) {
                log.warn("error consumer invoke exception", consumerException);
            }
        }
    }

    public void onComplete() {
        this.setCompleted(true);
        for (Runnable completeRunnable : completeRunnables) {
            try {
                completeRunnable.run();
            } catch (Throwable exception) {
                log.warn("complete runnable invoke exception", exception);
            }
        }
    }

    protected <V> PromiseImpl<V> init() {
        return new PromiseImpl<>();
    }

    @Override
    public PromiseImpl<T> whenSuccess(Consumer<T> consumer) {
        //TODO 存在并发问题，暂不考虑
        if (this.isSuccess()) consumer.accept(getValue());
        else this.successConsumers.add(consumer);
        return this;
    }

    @Override
    public PromiseImpl<T> whenError(Consumer<Throwable> consumer) {
        if (this.isError()) consumer.accept(getException());
        else this.errorConsumers.add(consumer);
        return this;
    }

    @Override
    public PromiseImpl<T> whenComplete(Runnable runnable) {
        if (this.isCompleted()) runnable.run();
        else this.completeRunnables.add(runnable);
        return this;
    }

    @Override
    public <V> PromiseImpl<Tuple2<T, V>> zip(Promise<V> promise) {
        PromiseImpl<V> promiseImpl = (PromiseImpl<V>) promise;
        PromiseImpl<Tuple2<T, V>> zipPromise = init();

        this.whenSuccess(t -> {
            if (promiseImpl.isSuccess()) zipPromise.onSuccess(new Tuple2<>(t, promiseImpl.getValue()));
        });
        promiseImpl.whenSuccess(t -> {
            if (this.isSuccess()) zipPromise.onSuccess(new Tuple2<>(this.getValue(), t));
        });

        this.whenError(exception -> {
            if (!zipPromise.isCompleted()) zipPromise.onError(exception);
        });
        promiseImpl.whenError(exception -> {
            if (!zipPromise.isCompleted()) zipPromise.onError(exception);
        });

        this.whenComplete(() -> {
            if (promiseImpl.isCompleted()) zipPromise.onComplete();
        });
        promiseImpl.whenComplete(() -> {
            if (this.isCompleted()) zipPromise.onComplete();
        });

        return zipPromise;
    }

    public <V> PromiseImpl<V> thenApplyAsync(Function<T, V> function) {
        PromiseImpl<V> promise = init();
        this.successConsumers.add(value -> invoke(promise, () -> function.apply(value)));
        this.errorConsumers.add(promise::onError);
        return promise;
    }

    public static <T> PromiseImpl<T> invoke(Supplier<T> action) {
        return invoke(new PromiseImpl<>(), action);
    }

    private static <T> PromiseImpl<T> invoke(PromiseImpl<T> promise, Supplier<T> action) {
        new Thread(() -> {
            try {
                promise.onSuccess(action.get());
            } catch (Throwable exception) {
                promise.onError(exception);
            } finally {
                promise.onComplete();
            }
        }).start();
        return promise;
    }
}
