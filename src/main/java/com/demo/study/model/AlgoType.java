package com.demo.study.model;

import lombok.Getter;

/**
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/7/22 18:01
 */
@Getter
public enum AlgoType {
    OPTICAL(1),
    ACOUSTIC(2),
    OPTICAL_ACOUSTIC(3);

    private final int algoType;

    AlgoType(int algoType) {
        this.algoType = algoType;
    }
}