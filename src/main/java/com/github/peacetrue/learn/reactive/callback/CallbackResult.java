package com.github.peacetrue.learn.reactive.callback;

import lombok.Data;
import lombok.ToString;

/**
 * 回调结果
 **/
@Data
@ToString
public class CallbackResult<T> {

    private volatile boolean completed;
    private T value;
    private Exception exception;

    public boolean isSuccess() {
        return completed && exception == null;
    }
}
