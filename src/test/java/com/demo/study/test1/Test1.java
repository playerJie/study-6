package com.demo.study.test1;

import com.demo.study.model.*;

import com.demo.study.model.algo.*;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 模拟测试的入口
 *
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/5/9 21:47
 */
@Slf4j
@ExtendWith(SpringExtension.class)
public class Test1 {
    /**
     * 测试方法1：模拟的入口函数
     * dddddddddddddddddddddddddddddddddddddddd
     */
    @Test
    void test1() {
    }

    @Test
    void test2() {
        SimulateRouteConfig simulateRouteConfig = SimulateRouteConfig.builder()
                .x(500)
                .y(500)
                .z(500)
                .totalRound(1000)
                .config(new NodeConfig())
                .build();

        NodeConfig config = simulateRouteConfig.getConfig();

        GenerateConfig generateConfig = GenerateConfig.builder()
                .x(simulateRouteConfig.getX())
                .y(simulateRouteConfig.getY())
                .z(simulateRouteConfig.getZ())
                .majorNodeSize(20)
                .gapDistance(50)
                .aroundNodeSize(5)
                .initEnergy(config.getEnergy())
                .build();

        List<List<Integer>> pdrResults = Lists.newArrayList();
        pdrResults.add(new ArrayList<>());
        pdrResults.add(new ArrayList<>());

        List<List<Integer>> residualEnergyPercentResults = Lists.newArrayList();
        residualEnergyPercentResults.add(new ArrayList<>());
        residualEnergyPercentResults.add(new ArrayList<>());

        List<List<Integer>> lifetimeResults = Lists.newArrayList();
        lifetimeResults.add(new ArrayList<>());
        lifetimeResults.add(new ArrayList<>());

        List<List<BigDecimal>> end2EndDelayResults = Lists.newArrayList();
        end2EndDelayResults.add(new ArrayList<>());
        end2EndDelayResults.add(new ArrayList<>());

        int totalSimulateCount = 10;
        // 1：节点；其他：轮数
        //int focusType = 1;
        int focusType = 2;

        List<Node> nodes = MyUtils.generateNodes2(generateConfig);

        simulateRouteConfig.setAlgorithm(new MyRouteAlgo());

        for (int simulateCount = 0; simulateCount < totalSimulateCount; simulateCount++) {
            // 要发送的数据
            String data = "Hello, world! It's my first java program. I am very happy to share with you!";

            // 把要发送的数据包装成数据包，如果数据超过一次可发送的数据大小，则进行分包
            TreeMap<Integer, Packet> sendingPackets = MyUtils.getSubPackets(nodes.get(0),
                    nodes.get(nodes.size() - 1),
                    data,
                    config.getBytesByOptical()
            );

            if (sendingPackets.size() <= 2) {
                throw new RuntimeException("数据包的数量不能小于2");
            }

            simulateRouteConfig.setPdrResults(pdrResults);
            simulateRouteConfig.setResidualEnergyPercentResults(residualEnergyPercentResults);
            simulateRouteConfig.setLifetimeResults(lifetimeResults);
            simulateRouteConfig.setEnd2EndDelayResults(end2EndDelayResults);
            simulateRouteConfig.setNodes(nodes);
            simulateRouteConfig.setData(data);
            simulateRouteConfig.setSendingPackets(sendingPackets);

            List<Trap> traps = Lists.newArrayListWithCapacity(simulateRouteConfig.getTotalRound());
            for (int i = 0; i < simulateRouteConfig.getTotalRound(); i++) {
                Trap trap = new Trap();
                trap.setNodeSequence(MyUtils.generateInt(3, 6));
                trap.setPacketSequence(MyUtils.generateInt(2, sendingPackets.size() - 1));
                traps.add(trap);
            }

            simulateRouteConfig.setTraps(traps);

            // 建立三维空间中，所有节点在光通信距离内的邻里关系
            CompletableFuture<Boolean> initResult1 = CompletableFuture.supplyAsync(() -> {
                MyUtils.initNeighbouringNodesForAll(nodes,
                        config.getOpticalDistance(),
                        config.getEnergyByOptical(),
                        config.getOpticalFlag());
                return true;
            }, ThreadPoolConfig.EXECUTOR);

            // 建立三维空间中，所有节点在声通信距离内的邻里关系
            CompletableFuture<Boolean> initResult2 = CompletableFuture.supplyAsync(() -> {
                MyUtils.initNeighbouringNodesForAll(nodes,
                        config.getAcousticDistance(),
                        config.getEnergyByAcoustic(),
                        config.getAcousticFlag());
                return true;
            }, ThreadPoolConfig.EXECUTOR);

            // 等待上面两个任务完成
            CompletableFuture.allOf(initResult1, initResult2).join();

            // 模拟所需的参数配置，具体每个变量的说明参考类里面的说明
            SimulationConfig simulationConfig = SimulationConfig.builder()
                    .nodes(nodes)
                    .config(config)
                    .build();

            simulateRouteConfig.setSimulationConfig(simulationConfig);

            nodes.get(0).setEnergy(config.getEnergy() * 200);
            simulationConfig.setEnableRelayFlag(false);
            simulationConfig.setUnavailableInfo(new UnavailableInfo());
            simulationConfig.setFailureReasons(Maps.newHashMap());

            int totalEnergy = nodes.stream()
                    .map(Node::getEnergy)
                    .mapToInt(Integer::intValue)
                    .sum();
            simulateRouteConfig.setTotalEnergy(totalEnergy);

            // 优化前
            simulateRoute(simulateRouteConfig);

            // 重置节点部分信息
            MyUtils.resetNodes(nodes, config.getEnergy());

            nodes.get(0).setEnergy(config.getEnergy() * 200);
            simulationConfig.setEnableRelayFlag(true);
            simulationConfig.setUnavailableInfo(new UnavailableInfo());
            simulationConfig.setFailureReasons(Maps.newHashMap());

            // 优化后
            simulateRoute(simulateRouteConfig);

            if (focusType == 1) {
                MyUtils.enlarge(generateConfig, nodes, 3);
                generateConfig.setAroundNodeSize(generateConfig.getAroundNodeSize() + 1);
            } else {
                simulateRouteConfig.setTotalRound(simulateRouteConfig.getTotalRound() + 50);
            }

            MyUtils.resetNodes3(nodes, config.getEnergy());
        }

        log.info("优化前-投递率-随{}变化：{}", focusType == 1 ? "节点" : "轮数", pdrResults.get(0));
        log.info("优化后-投递率-随{}变化：{}", focusType == 1 ? "节点" : "轮数", pdrResults.get(1));
        log.info("优化前-剩余能量-随{}变化：{}", focusType == 1 ? "节点" : "轮数", residualEnergyPercentResults.get(0));
        log.info("优化后-剩余能量-随{}变化：{}", focusType == 1 ? "节点" : "轮数", residualEnergyPercentResults.get(1));
        log.info("优化前-网络寿命-随{}变化：{}", focusType == 1 ? "节点" : "轮数", lifetimeResults.get(0));
        log.info("优化后-网络寿命-随{}变化：{}", focusType == 1 ? "节点" : "轮数", lifetimeResults.get(1));
        log.info("优化前-端到端延迟-随{}变化：{}", focusType == 1 ? "节点" : "轮数", end2EndDelayResults.get(0));
        log.info("优化后-端到端延迟-随{}变化：{}", focusType == 1 ? "节点" : "轮数", end2EndDelayResults.get(1));
    }

    private void simulateRoute(SimulateRouteConfig simulateRouteConfig) {
        Algorithm algorithm = simulateRouteConfig.getAlgorithm();

        // 总模拟轮数
        int totalRound = simulateRouteConfig.getTotalRound();
        // 全部节点
        List<Node> nodes = simulateRouteConfig.getNodes();
        SimulationConfig simulationConfig = simulateRouteConfig.getSimulationConfig();
        simulationConfig.setAlgorithm(algorithm);
        int x = simulateRouteConfig.getX();
        int y = simulateRouteConfig.getY();
        int z = simulateRouteConfig.getZ();

        simulationConfig.setDangerousDistance(z / 2);

        // 路由成功次数
        int successCount = 0;
        // 路由失败次数
        int failCount = 0;

        // 0：失败，1：成功
        List<Integer> routeResults = Lists.newArrayListWithCapacity(totalRound);
        List<BigDecimal> consumeTimes = Lists.newArrayListWithCapacity(totalRound);
        List<Integer> consumeEnergies = Lists.newArrayListWithCapacity(totalRound);
        List<Integer> hops = Lists.newArrayListWithCapacity(totalRound);
        List<Integer> residualEnergyPercents = Lists.newArrayList();

        // 模拟totalRound轮路由过程
        for (int i = 1; i <= totalRound; i++) {
            // 把要发送的数据包装成数据包，如果数据超过一次可发送的数据大小，则进行分包
            TreeMap<Integer, Packet> sendingPackets = simulateRouteConfig.getSendingPackets();

            simulationConfig.setSubPacketMapping(sendingPackets);
            simulationConfig.setConsumeTimes(Lists.newArrayList());
            simulationConfig.setConsumeEnergies(Lists.newArrayList());

            // 优化后的一些配置，具体每个变量的说明参考类里面的说明
            simulationConfig.setRoutingPath(Lists.newArrayList());

            List<List<Integer>> routingPaths = Lists.newArrayList();
            routingPaths.add(simulationConfig.getRoutingPath());
            simulationConfig.setRoutingPaths(routingPaths);
            simulationConfig.setTrap(simulateRouteConfig.getTraps().get(i - 1));

            simulationConfig.setUnavailableLatch(new AtomicInteger(simulationConfig.getTrap().getNodeSequence()));

            simulationConfig.setDangerousFlag(false);
            simulationConfig.setDangerousCount(1);

            // 模拟优化后的路由过程
            boolean isSuccess = algorithm.route(simulationConfig);

            routeResults.add(isSuccess ? 1 : 0);

            if (isSuccess) {
                BigDecimal sum = BigDecimal.ZERO;
                for (BigDecimal consumeTime : simulationConfig.getConsumeTimes()) {
                    sum = sum.add(consumeTime);
                }
                consumeTimes.add(sum);
            } else {
                consumeTimes.add(BigDecimal.ZERO);
            }

            if (isSuccess) {
                int sum = 0;
                for (Integer consumeEnergy : simulationConfig.getConsumeEnergies()) {
                    sum += consumeEnergy;
                }
                consumeEnergies.add(sum);
            } else {
                consumeEnergies.add(0);
            }

            if (isSuccess) {
                int sum = 0;
                for (List<Integer> routingPath : routingPaths) {
                    sum += routingPath.size();
                }
                hops.add(sum);
            } else {
                hops.add(0);
            }

            // 根据模拟结果记录成功数量和失败数量
            if (isSuccess) {
                successCount++;
                /*// 打印遇到故障幸存下来的一些说明信息
                if (routingPaths.size() > 1) {
                    log.info("第{}轮: ", i);
                    //log.info("故障节点: {}", simulationConfig.getUnavailableNodeIdMapping());
                    log.info("路由路线: {}", routingPaths);
                    // 用于分类终点的包的来源与每个来源的数据包的数量
                    TreeMap<Integer, Packet> subPacketMapping = nodes.get(nodes.size() - 1)
                            .getCompletePacketMapping()
                            .get(sendingPackets.firstEntry().getValue().getId());
                    // 获取数据源和对应数据包的数量
                    HashMap<Integer, Integer> countMap = MyUtils.getCountMap(subPacketMapping);
                    log.info("数据分布: {}, 数据包数: {}", countMap, sendingPackets.size());
                    log.info("");
                }*/
            } else {
                failCount++;
            }

            if (i % 100 == 0) {
                int residualEnergy = 0;
                for (Node node : nodes) {
                    residualEnergy += node.getEnergy();
                }

                BigDecimal residualEnergyPercent = BigDecimal.valueOf(residualEnergy)
                        .divide(BigDecimal.valueOf(simulateRouteConfig.getTotalEnergy()), 2, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100));

                residualEnergyPercents.add((int) Double.parseDouble(residualEnergyPercent.toString()));
            }

            MyUtils.resetNodes2(nodes);
        }

        log.info("{}", simulationConfig.isEnableRelayFlag() ? "优化后:" : "优化前:");

        int round = 100;

        List<Integer> roundSuccessCounts = Lists.newArrayListWithCapacity(routeResults.size() % round == 0 ?
                routeResults.size() / round : (routeResults.size() / round) + 1);

        List<Integer> roundRouteResults = Lists.newArrayListWithCapacity(routeResults.size() % round == 0 ?
                routeResults.size() / round : (routeResults.size() / round) + 1);
        for (int i = 0; i < routeResults.size() / round; i++) {
            int success = 0;
            int sum = 0;
            for (int j = i * round; j < i * round + round; j++) {
                if (routeResults.get(j) == 1) {
                    success++;
                }
                sum = sum + routeResults.get(j);
            }
            roundSuccessCounts.add(success);
            roundRouteResults.add(sum);
        }
        int averagePdr = roundRouteResults.stream().mapToInt(Integer::intValue).sum() / roundRouteResults.size();
        simulateRouteConfig.getPdrResults().get(simulationConfig.isEnableRelayFlag() ? 1 : 0).add(averagePdr);
        log.info("每{}轮平均包投递率: {}", round, roundRouteResults);
        //log.info("平均包投递率：{}", averagePdr);

        List<BigDecimal> roundConsumeTimes = Lists.newArrayListWithCapacity(consumeTimes.size() % round == 0 ?
                consumeTimes.size() / round : (consumeTimes.size() / round) + 1);
        for (int i = 0; i < consumeTimes.size() / round; i++) {
            //StringBuilder time = new StringBuilder();
            BigDecimal sum = BigDecimal.ZERO;
            for (int j = i * round; j < i * round + round; j++) {
                //time.append(consumeTimes.get(j)).append(" ");
                sum = sum.add(consumeTimes.get(j));
            }
            if (roundSuccessCounts.get(i) == 0) {
                roundConsumeTimes.add(BigDecimal.ZERO);
                continue;
            }
            //log.info("总消耗时间: {}, 路由成功次数: {}", sum, roundSuccessCounts.get(i));
            //log.info("消耗时间详情：{}", time);
            roundConsumeTimes.add(sum.divide(BigDecimal.valueOf(roundSuccessCounts.get(i)), 2, RoundingMode.HALF_UP));
        }
        log.info("每{}轮平均时间消耗: {}", round, roundConsumeTimes);
        BigDecimal totalConsumeTimes = BigDecimal.ZERO;
        for (BigDecimal consumeTime : roundConsumeTimes) {
            totalConsumeTimes = totalConsumeTimes.add(consumeTime);
        }
        simulateRouteConfig.getEnd2EndDelayResults()
                .get(simulationConfig.isEnableRelayFlag() ? 1 : 0)
                .add(totalConsumeTimes.divide(BigDecimal.valueOf(roundConsumeTimes.size()), 2, RoundingMode.HALF_UP));

        List<Integer> roundConsumeEnergies = Lists.newArrayListWithCapacity(consumeEnergies.size() % round == 0 ?
                consumeEnergies.size() / round : (consumeEnergies.size() / round) + 1);
        for (int i = 0; i < consumeEnergies.size() / round; i++) {
            int sum = 0;
            for (int j = i * round; j < i * round + round; j++) {
                sum += consumeEnergies.get(j);
            }
            if (roundSuccessCounts.get(i) == 0) {
                roundConsumeEnergies.add(0);
                continue;
            }
            roundConsumeEnergies.add(sum / roundSuccessCounts.get(i));
        }
        log.info("每{}轮平均能量消耗: {}", round, roundConsumeEnergies);
        log.info("每{}轮剩余能量占比: {}", round, residualEnergyPercents);
        int averageResidualEnergyPercent = residualEnergyPercents.stream().mapToInt(Integer::intValue).sum() / residualEnergyPercents.size();
        simulateRouteConfig.getResidualEnergyPercentResults().get(simulationConfig.isEnableRelayFlag() ? 1 : 0).add(averageResidualEnergyPercent);

        List<Integer> roundHops = Lists.newArrayListWithCapacity(hops.size() % round == 0 ?
                hops.size() / round : (hops.size() / round) + 1);
        for (int i = 0; i < hops.size() / round; i++) {
            int sum = 0;
            for (int j = i * round; j < i * round + round; j++) {
                sum += hops.get(j);
            }
            if (roundSuccessCounts.get(i) == 0) {
                roundHops.add(0);
                continue;
            }
            roundHops.add(sum / roundSuccessCounts.get(i));
        }
        log.info("每{}轮平均路由跳数: {}", round, roundHops);

        int roundLive = -1;
        for (int i = routeResults.size() - 1; i >= 0; i--) {
            if (routeResults.get(i) == 0) {
                continue;
            }
            roundLive = i;
            break;
        }
        int lifetime = roundLive == -1 ? 0 : roundLive + 1;
        /*int lifetime = MyUtils.findFirstZero(routeResults, 10);
        if (lifetime == -1) {
            lifetime = totalRound;
        }*/
        simulateRouteConfig.getLifetimeResults().get(simulationConfig.isEnableRelayFlag() ? 1 : 0).add(lifetime);
        log.info("网络生命周期: {}", lifetime);

        // 下面打印一些总的结果说明信息
        log.info("");
        log.info("向量空间: ({},{},{}), 节点数量: {}, 模拟轮数: {}", x, y, z, nodes.size(), totalRound);

        log.info("{}: 成功: {}, 失败: {}, 遇到故障节点次数: {}, 幸存次数: {}",
                simulationConfig.isEnableRelayFlag() ? "优化后" : "优化前",
                successCount, failCount,
                simulationConfig.getUnavailableInfo().getEncountered().get(), simulationConfig.getUnavailableInfo().getSurvived().get());

        if (MapUtils.isNotEmpty(simulationConfig.getFailureReasons())) {
            log.info("");
            log.info("失败原因统计:");
            Map<FailureReason, Integer> collect = simulationConfig.getFailureReasons()
                    .entrySet()
                    .stream()
                    .sorted(Map.Entry.comparingByValue(Comparator.reverseOrder()))
                    .collect(Collectors.toMap(Map.Entry::getKey,
                            Map.Entry::getValue,
                            (oldValue, newValue) -> oldValue,
                            LinkedHashMap::new));
            AtomicInteger sequence = new AtomicInteger(1);
            collect.forEach((k, v) -> {
                log.info("({}) 失败原因: {}, 次数: {}", sequence.getAndIncrement(), k.getMessage(), v);
            });
        }

        log.info("");
    }

    @Test
    void test3() {
        List<Integer> list = Arrays.asList(0, 0, 1, 0, 0, 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0);
        int index = MyUtils.findFirstZero(list, 5);
        log.info("The index of the first zero in six consecutive zeros is: " + index);
    }
}