package com.github.peacetrue.learn.reactive.callback;

import com.github.peacetrue.learn.reactive.MealProcess;
import com.github.peacetrue.learn.reactive.callback.CallbackHandler;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @author : xiayx
 * @since : 2020-12-20 14:12
 **/
@Slf4j
public class MealProcessParallelCallback {

    @Test
    void parallelCallback() throws Exception {
        MealProcess mealProcess = new MealProcess();
        //买菜
        CallbackHandler.invoke(mealProcess::buyFood, new CallbackHandler<String>() {
            public void onSuccess(String food) {
                //买好菜了，开始做菜
                CallbackHandler.invoke(() -> mealProcess.cookFood(food), new CallbackHandler<String>() {
                    public void onSuccess(String result) {
                        //想吃饭？除非饭做好了
                        //...
                    }
                });
            }
        });

        //做饭
        CallbackHandler.invoke(mealProcess::cookRice, new CallbackHandler<String>() {
            @Override
            public void onSuccess(String result) {
                //想吃饭？除非菜做好了
                //...
            }
        });
    }
}
