package com.demo.study.model.result;

import lombok.Getter;

/**
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/3/19 10:51
 */
@Getter
public enum ResultStatus {
    SUCCESS(1, "成功"),
    FAILURE(0, "失败"),
    ERROR(-1, "错误");

    private final int code;
    private final String message;

    ResultStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
