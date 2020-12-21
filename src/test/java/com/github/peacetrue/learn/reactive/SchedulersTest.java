package com.github.peacetrue.learn.reactive;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.logging.Level;

/**
 * @author : xiayx
 * @since : 2020-12-21 07:36
 **/
@Slf4j
public class SchedulersTest {

    @Test
    void basic() {
        Mono
                .fromCallable(() -> System.currentTimeMillis())
                .log("foo.bar", Level.INFO)
                .repeat()
                .log("foo.bar2",Level.FINEST)
                .publishOn(Schedulers.single())
                .log("foo.bar3",Level.FINEST)
                .flatMap(time ->
                                Mono.fromCallable(() -> {
                                    Thread.sleep(1000);
                                    return time;
                                })
                                        .subscribeOn(Schedulers.parallel())
                        , 8) //maxConcurrency 8
                .log("foo.bar4",Level.ALL)
                .subscribe();

    }
}
