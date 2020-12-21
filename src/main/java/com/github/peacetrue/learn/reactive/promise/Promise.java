package com.github.peacetrue.learn.reactive.promise;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 *
 **/
public interface Promise<T> {

    Promise<T> whenSuccess(Consumer<T> consumer);

    Promise<T> whenError(Consumer<Throwable> consumer);

    Promise<T> whenComplete(Runnable runnable);

    <V> Promise<Tuple2<T, V>> zip(Promise<V> promise);

    <V> Promise<V> thenApplyAsync(Function<T, V> function);
}
