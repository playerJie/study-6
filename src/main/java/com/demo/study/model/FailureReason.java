package com.demo.study.model;

import lombok.Getter;

/**
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/5/27 23:02
 */
@Getter
public enum FailureReason {
    REASON_1(0, "路由次数超过阈值"),
    REASON_2(1, "节点既没有光学邻居也没有声学邻居"),
    REASON_3(2, "节点短距离大角度内没有候选节点"),
    REASON_4(3, "发送端能量不足"),
    REASON_5(4, "节点遇到故障没有退路"),
    REASON_6(5, "接收端向下没有可搜索的节点"),
    REASON_7(6, "接收端向下找不到持有完整数据的接力节点"),
    REASON_8(7, "兵分两路处理失败"),
    REASON_9(8, "发送数据失败"),
    REASON_10(9, "光传输距离范围内没有邻居"),
    REASON_11(11, "可用邻居里面没有候选节点"),
    REASON_12(12, "声传输距离范围内没有邻居"),
    ;

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
    FailureReason(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
