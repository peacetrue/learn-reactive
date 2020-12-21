package com.github.peacetrue.learn.reactive.promise;

import com.github.peacetrue.learn.reactive.MealProcess;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.LockSupport;

/**
 * @author : xiayx
 * @since : 2020-12-20 15:14
 **/
@Slf4j
class PromiseImplTest {

    @Test
    void when() {
        Thread thread = Thread.currentThread();
        MealProcess mealProcess = new MealProcess();
        PromiseImpl<String> promise = PromiseImpl.invoke(mealProcess::buyFood)
                .whenSuccess(s -> log.info("success: {}", s))
                .whenSuccess(s -> Assertions.assertEquals(mealProcess.buyFood(), s))
                .whenSuccess(s -> {
                    throw new IllegalStateException();
                })
                .whenError(exception -> log.warn("invoke error", exception))
                .whenComplete(() -> LockSupport.unpark(thread));
        LockSupport.park(this);
        Assertions.assertTrue(promise.isSuccess());
    }

    @Test
    void zip() {
        Thread thread = Thread.currentThread();
        MealProcess mealProcess = new MealProcess();
        PromiseImpl<Tuple2<String, String>> promise = PromiseImpl.invoke(mealProcess::buyFood)
                .zip(PromiseImpl.invoke(mealProcess::cookRice))
                .whenSuccess(tuple2 -> log.info("tuple2: {}", tuple2))
                .whenComplete(() -> LockSupport.unpark(thread));
        LockSupport.park(this);
        Assertions.assertTrue(promise.isSuccess());
    }

    @Test
    void thenApplyAsync() {
        Thread thread = Thread.currentThread();
        MealProcess mealProcess = new MealProcess();
        PromiseImpl<String> promise = PromiseImpl.invoke(mealProcess::buyFood)
                .thenApplyAsync(mealProcess::cookFood)
                .whenSuccess(s -> Assertions.assertEquals("熟的菜", s))
                .whenComplete(() -> LockSupport.unpark(thread));
        LockSupport.park(this);
        Assertions.assertTrue(promise.isSuccess());
    }
}
