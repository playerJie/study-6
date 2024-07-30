package com.demo.study.model;

import lombok.Getter;
import lombok.ToString;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 描述路由过程中发生发送端故障的次数，以及发生这种情况后，成功把数据包发送到目标节点的次数
 *
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/5/13 18:21
 */
@Getter
@ToString
public class UnavailableInfo {
    /**
     * 记录路由过程中发送端出现故障的次数
     */
    private final AtomicInteger encountered = new AtomicInteger(0);
    /**
     * 记录路由过程中发送端出现故障后，成功把数据包发送到目标节点的次数
     */
    private final AtomicInteger survived = new AtomicInteger(0);
}
