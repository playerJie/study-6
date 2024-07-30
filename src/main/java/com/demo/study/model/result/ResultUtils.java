package com.demo.study.model.result;

/**
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/3/19 10:51
 */
public class ResultUtils {
    public static <T> Result<T> success() {
        return new Result<>(ResultStatus.SUCCESS);
    }

    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>(ResultStatus.SUCCESS);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> successWithMessage(String message) {
        Result<T> result = new Result<>(ResultStatus.SUCCESS);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> successWithData(T data) {
        return new Result<>(ResultStatus.SUCCESS, data);
    }

    public static <T> Result<T> failure() {
        return new Result<>(ResultStatus.FAILURE);
    }

    public static <T> Result<T> failure(String message, T data) {
        Result<T> result = new Result<>(ResultStatus.FAILURE);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> failureWithMessage(String message) {
        Result<T> result = new Result<>(ResultStatus.FAILURE);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> failureWithData(T data) {
        return new Result<>(ResultStatus.FAILURE, data);
    }

    public static <T> Result<T> error() {
        return new Result<>(ResultStatus.ERROR);
    }

    public static <T> Result<T> error(String message, T data) {
        Result<T> result = new Result<>(ResultStatus.ERROR);
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> errorWithMessage(String message) {
        Result<T> result = new Result<>(ResultStatus.ERROR);
        result.setMessage(message);
        return result;
    }

    public static <T> Result<T> errorWithData(T data) {
        return new Result<>(ResultStatus.ERROR, data);
    }
}
