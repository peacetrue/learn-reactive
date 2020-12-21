package com.github.peacetrue.learn.reactive.promise;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

/**
 * @author : xiayx
 * @since : 2020-12-20 12:58
 **/
@Data
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Tuple2<T, V> {
    private T first;
    private V second;
}
