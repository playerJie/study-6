package com.demo.study.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 描述通过的配置信息
 *
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/5/12 0:20
 */
@Getter
@Setter
@ToString
public class NodeConfig {
    /**
     * 光通信距离
     */
    private int opticalDistance = 100;
    /**
     *
     */
    private int opticalShortDistance = 80;
    /**
     * 声通信距离
     */
    private int acousticDistance = 200;
    /**
     * 光通信速度
     */
    private int opticalSpeed = 500;
    /**
     * 声通信速度
     */
    private int acousticSpeed = 50;
    /**
     * 光通信携带的数据量
     */
    private int bytesByOptical = 20;
    /**
     * 声通信携带的数据量
     */
    private int bytesByAcoustic = 2;
    /**
     * 光通信一次传输消耗能量
     */
    private int energyByOptical = 1;
    /**
     * 声通信一次传输消耗能量
     */
    private int energyByAcoustic = 10;
    /**
     * 节点的初始能量
     */
    private int energy = 500;
    /**
     * 一次路由允许的最大跳数（超过此跳数则认为路由失败）
     */
    private int maxHop = 20;
    /**
     * 光通信标志
     */
    private int opticalFlag = 0;
    /**
     * 声通信标志
     */
    private int acousticFlag = 1;
}
