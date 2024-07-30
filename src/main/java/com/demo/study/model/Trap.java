package com.demo.study.model;

import lombok.*;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/5/28 23:13
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class Trap {
    private int nodeSequence;
    private int packetSequence;
}
