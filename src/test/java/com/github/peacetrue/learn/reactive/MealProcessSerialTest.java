package com.github.peacetrue.learn.reactive;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

/**
 * @author : xiayx
 * @since : 2020-12-20 09:21
 **/
@Slf4j
class MealProcessSerialTest {

    @Test
    void serial() {
        MealProcess mealProcess = new MealProcess();
        long start = System.currentTimeMillis();
        String food = mealProcess.buyFood();
        String rice = mealProcess.cookRice();
        String cookFood = mealProcess.cookFood(food);
        mealProcess.eat(rice, cookFood);
        log.info("吃饭流程（串行）共花费 {} 毫秒", System.currentTimeMillis() - start);
    }

}
