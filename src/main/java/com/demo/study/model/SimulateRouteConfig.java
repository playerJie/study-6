package com.demo.study.model;

import com.demo.study.model.algo.Algorithm;
import com.demo.study.model.algo.MyRouteAlgo;
import lombok.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.TreeMap;

/**
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/5/28 22:38
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class SimulateRouteConfig {
    // 三维空间x轴长度
    private int x = 500;
    // 三维空间y轴长度
    private int y = 500;
    // 三维空间z轴长度
    private int z = 500;
    // 总模拟路由轮数
    private int totalRound = 2000;
    private NodeConfig config;
    private List<Node> nodes;
    private SimulationConfig simulationConfig;
    private String data;
    private TreeMap<Integer, Packet> sendingPackets;
    private List<Trap> traps;
    private List<List<Integer>> pdrResults;
    private int totalEnergy;
    private List<List<Integer>> residualEnergyPercentResults;
    private List<List<Integer>> lifetimeResults;
    private List<List<BigDecimal>> end2EndDelayResults;
    private Algorithm algorithm;
}
