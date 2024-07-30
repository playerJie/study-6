package com.demo.study.model;

import lombok.Getter;

/**
 * 路由结果状态
 *
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/5/13 14:09
 */
@Getter
public enum RoutingStatus {
    /**
     * 路由成功
     */
    SUCCESS(0, "成功"),
    /**
     * 路由失败
     */
    FAILURE(1, "失败"),
    /**
     * 路由成功
     * 表示路由过程存在发送端在发送数据过程中途出现了故障，
     * 然后接收端寻找接力端发送未收到的数据，接收端本身发送已收到的数据
     * 最终接收端和接力端都成功把数据发送到终点
     */
    SENDING_END_SUCCESS(2, "发送端故障"),
    RECEIVING_END_SUCCESS(3, "接收端故障");

    /**
     * 状态码
     */
    private final int code;
    /**
     * 状态信息
     */
    private final String message;

    /**
     * 构造方法
     *
     * @param code    状态码
     * @param message 状态信息
     */
    RoutingStatus(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
