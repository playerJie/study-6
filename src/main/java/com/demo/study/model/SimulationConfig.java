package com.demo.study.model;

import com.demo.study.model.algo.Algorithm;
import lombok.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 模拟路由过程需要设置的一些变量
 *
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/5/10 21:37
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class SimulationConfig {
    /**
     *
     */
    private Algorithm algorithm;
    /**
     * 分布在水下空间不同位置的节点集合
     */
    private List<Node> nodes;
    /**
     *
     */
    private NodeConfig config;
    /**
     * 路由过程经过的节点的ID集合
     */
    private List<Integer> routingPath;
    /**
     * 发送端要给接收端发送的所有数据包
     */
    private TreeMap<Integer, Packet> subPacketMapping;
    /**
     * 表示是否开启数据包分路传送（true：开启；false：关闭）
     * true：开启后，发送端发送数据过程中发生故障的时候，接收端会寻找接力端，然后接收端和接力端会传送不同部分的数据包
     * false：关闭后，发送端发送数据过程中发生故障的时候，直接路由失败
     */
    private boolean enableRelayFlag;
    /**
     * 保存路由过程的所有路径
     */
    private List<List<Integer>> routingPaths;
    /**
     * 优化后算法处理故障的情况
     */
    private UnavailableInfo unavailableInfo;
    /**
     * 控制路由路径中第几个节点开始模拟故障
     */
    private AtomicInteger unavailableLatch;
    /**
     *
     */
    private List<BigDecimal> consumeTimes;
    /**
     *
     */
    private List<Integer> consumeEnergies;
    /**
     *
     */
    private HashMap<FailureReason, Integer> failureReasons;
    private Trap trap;
    private boolean dangerousFlag = false;
    private int dangerousDistance;
    private int dangerousCount;
    private List<Node> routeNodes;
}