package com.demo.study.model.result;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/3/19 10:51
 */
@Getter
@Setter
@ToString
public class Result<T> {
    private int code;
    private String message;
    private T data;

    private Result(Integer code, String message) {
        this.code = code;
        this.message = message;
    }

    private Result(Integer code, String message, T data) {
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public Result(ResultStatus resultStatus) {
        this(resultStatus.getCode(), resultStatus.getMessage());
    }

    public Result(ResultStatus resultStatus, T data) {
        this(resultStatus.getCode(), resultStatus.getMessage(), data);
    }

    public boolean isSuccess() {
        return this.code == ResultStatus.SUCCESS.getCode();
    }

    public boolean isNotSuccess() {
        return !isSuccess();
    }
}
