package com.github.peacetrue.learn.reactive.callback;

import com.github.peacetrue.learn.reactive.MealProcess;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.util.concurrent.Flow;

/**
 * @author : xiayx
 * @since : 2020-12-20 14:12
 **/
@Slf4j
public class MealProcessParallelCallbackPublishTest {

    @Test
    void parallelCallback() throws Exception {
        MealProcess mealProcess = new MealProcess();
        CallbackResult<String> foodResult = new CallbackResult<>();
        CallbackResult<String> richResult = new CallbackResult<>();
        long start = System.currentTimeMillis();
        //买菜
        new CallbackPublisher<>(mealProcess::buyFood).subscribe(new Flow.Subscriber<String>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {

            }

            public void onNext(String food) {
                foodResult.setValue(food);
                //买好菜了，开始做菜
                new CallbackPublisher<>(() -> mealProcess.cookFood(food)).subscribe(new Flow.Subscriber<String>() {
                    @Override
                    public void onSubscribe(Flow.Subscription subscription) {

                    }

                    public void onNext(String result) {
                        //想吃饭？除非饭做好了
                        if (richResult.isSuccess()) {
                            mealProcess.eat(richResult.getValue(), result);
                            log.info("吃饭流程（并行回调）共花费 {} 毫秒", System.currentTimeMillis() - start);
                        }
                    }

                    @Override
                    public void onError(Throwable throwable) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
            }

            public void onError(Throwable exception) {
                foodResult.setException(exception);
            }

            public void onComplete() {
                foodResult.setCompleted(true);
            }
        });

        //做饭
        new CallbackPublisher<>(mealProcess::cookRice).subscribe(new Flow.Subscriber<String>() {
            @Override
            public void onSubscribe(Flow.Subscription subscription) {

            }

            @Override
            public void onNext(String result) {
                //想吃饭？除非菜做好了
                if (foodResult.isSuccess()) {
                    mealProcess.eat(result, foodResult.getValue());
                    log.info("吃饭流程（并行回调）共花费 {} 毫秒", System.currentTimeMillis() - start);
                }
            }

            public void onError(Throwable exception) {
                richResult.setException(exception);
            }

            public void onComplete() {
                richResult.setCompleted(true);
            }
        });

        Thread.sleep(1000L);
    }
}
