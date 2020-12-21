package com.github.peacetrue.learn.reactive.callback;

import com.github.peacetrue.learn.reactive.MealProcess;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @author : xiayx
 * @since : 2020-12-20 14:12
 **/
@Slf4j
public class MealProcessParallelCallbackTest {

    @Test
    void parallelCallback() throws Exception {
        MealProcess mealProcess = new MealProcess();
        CallbackResult<String> foodResult = new CallbackResult<>();
        CallbackResult<String> richResult = new CallbackResult<>();
        long start = System.currentTimeMillis();
        //买菜
        CallbackHandler.invoke(mealProcess::buyFood, new CallbackHandler<String>() {
            public void onSuccess(String food) {
                foodResult.setValue(food);
                //买好菜了，开始做菜
                CallbackHandler.invoke(() -> mealProcess.cookFood(food), new CallbackHandler<String>() {
                    public void onSuccess(String result) {
                        //想吃饭？除非饭做好了
                        if (richResult.isSuccess()) {
                            mealProcess.eat(richResult.getValue(), result);
                            log.info("吃饭流程（并行回调）共花费 {} 毫秒", System.currentTimeMillis() - start);
                        }
                    }
                });
            }

            public void onError(Exception exception) {
                foodResult.setException(exception);
            }

            public void onComplete() {
                foodResult.setCompleted(true);
            }
        });

        //做饭
        CallbackHandler.invoke(mealProcess::cookRice, new CallbackHandler<String>() {
            @Override
            public void onSuccess(String result) {
                //想吃饭？除非菜做好了
                if (foodResult.isSuccess()) {
                    mealProcess.eat(result, foodResult.getValue());
                    log.info("吃饭流程（并行回调）共花费 {} 毫秒", System.currentTimeMillis() - start);
                }

            }

            public void onError(Exception exception) {
                richResult.setException(exception);
            }

            public void onComplete() {
                richResult.setCompleted(true);
            }
        });

        Thread.sleep(1000L);
    }
}
