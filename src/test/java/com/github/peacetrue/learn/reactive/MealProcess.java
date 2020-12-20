package com.github.peacetrue.learn.reactive;

import lombok.extern.slf4j.Slf4j;

/**
 * @author : xiayx
 * @since : 2020-12-19 21:06
 **/
@Slf4j
public class MealProcess {

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.warn("sleep interrupted", e);
        }
    }

    public String buyFood() {
        log.info("买菜");
        sleep(10);
        return "生的菜";
    }

    public String cookRice() {
        log.info("煮饭");
        sleep(15);
        return "熟的饭";
    }

    public String cookFood(String food) {
        log.info("做菜");
        sleep(10);
        return "熟的菜";
    }

    public void eat(String rice, String food) {
        log.info("吃饭");
        sleep(10);
    }

}
