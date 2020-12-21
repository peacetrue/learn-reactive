package com.github.peacetrue.learn.reactive.promise;

import com.github.peacetrue.learn.reactive.callback.CallbackHandler;
import com.github.peacetrue.learn.reactive.callback.CallbackResult;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Flow;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * @author : xiayx
 * @since : 2020-12-20 21:00
 **/
@Slf4j
public class PromiseProcess<T> extends CallbackResult<T> implements Promise<T>, Flow.Processor<T, T>, CallbackHandler<T> {

    private final List<Consumer<T>> successConsumers = new LinkedList<>();
    private final List<Consumer<Throwable>> errorConsumers = new LinkedList<>();
    private final List<Runnable> completeRunnables = new LinkedList<>();
    private List<Thread> threads;

    //-----------------Publisher<R>------------------------
    @Override
    public void subscribe(Flow.Subscriber<? super T> subscriber) {
        this
                .whenSuccess(subscriber::onNext)
                .whenError(subscriber::onError)
                .whenComplete(subscriber::onComplete);
        this.threads.forEach(Thread::start);
    }

    //-----------------Subscriber<R>------------------------
    @Override
    public void onSubscribe(Flow.Subscription subscription) {
    }

    @Override
    public void onNext(T item) {
        this.onSuccess(item);
    }

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


    //-----------------Promise<R>------------------------
    @Override
    public PromiseProcess<T> whenSuccess(Consumer<T> consumer) {
        this.successConsumers.add(consumer);
        return this;
    }

    @Override
    public PromiseProcess<T> whenError(Consumer<Throwable> consumer) {
        this.errorConsumers.add(consumer);
        return this;
    }

    @Override
    public PromiseProcess<T> whenComplete(Runnable runnable) {
        this.completeRunnables.add(runnable);
        return this;
    }

    @Override
    public <V> PromiseProcess<Tuple2<T, V>> zip(Promise<V> promise) {
        PromiseProcess<V> promiseImpl = (PromiseProcess<V>) promise;
        PromiseProcess<Tuple2<T, V>> zipPromise = new PromiseProcess<>();
        zipPromise.threads = new ArrayList<>(this.threads.size() + promiseImpl.threads.size());
        zipPromise.threads.addAll(this.threads);
        zipPromise.threads.addAll(promiseImpl.threads);

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

    public <V> PromiseProcess<V> thenApplyAsync(Function<T, V> function) {
        PromiseProcess<V> promise = new PromiseProcess<>();
        promise.threads = this.threads;
        this.successConsumers.add(value -> {
            invoke(promise, () -> function.apply(value))
                    .threads.forEach(Thread::start);
        });
        this.errorConsumers.add(promise::onError);
        return promise;
    }

    public static <T> PromiseProcess<T> invoke(Supplier<T> action) {
        return invoke(new PromiseProcess<>(), action);
    }

    private static <T> PromiseProcess<T> invoke(PromiseProcess<T> promise, Supplier<T> action) {
        promise.threads = Collections.singletonList(new Thread(() -> {
            try {
                promise.onNext(action.get());
            } catch (Throwable exception) {
                promise.onError(exception);
            } finally {
                promise.onComplete();
            }
        }));
        return promise;
    }


}
