package com.github.peacetrue.learn.reactive.promise;

import com.github.peacetrue.learn.reactive.MealProcess;
import com.github.peacetrue.learn.reactive.promise.PromiseImpl;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.concurrent.locks.LockSupport;

/**
 * @author : xiayx
 * @since : 2020-12-20 09:21
 **/
@Slf4j
class MealProcessParallelPromiseTest {

    @Test
    void parallelPromise() throws Exception {
        Thread thread = Thread.currentThread();
        MealProcess mealProcess = new MealProcess();
        long start = System.currentTimeMillis();
        PromiseImpl<String> promise = PromiseImpl.invoke(mealProcess::cookRice)
                .zip(PromiseImpl.invoke(mealProcess::buyFood).thenApplyAsync(mealProcess::cookFood))
                .thenApplyAsync(tuple2 -> {
                    mealProcess.eat(tuple2.getFirst(), tuple2.getSecond());
                    return "";
                })
                .whenSuccess(s -> {
                    log.info("吃饭流程（并行）共花费 {} 毫秒", System.currentTimeMillis() - start);
                })
                .whenComplete(() -> LockSupport.unpark(thread));
        log.info("吃饭流程（阻塞）共花费 {} 毫秒", System.currentTimeMillis() - start);
        LockSupport.park();
        Assertions.assertTrue(promise.isSuccess());
    }
}
