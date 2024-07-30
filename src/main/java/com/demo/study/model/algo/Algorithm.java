package com.demo.study.model.algo;

import com.demo.study.model.*;
import com.demo.study.model.result.Result;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

/**
 * 描述路由过程、传输过程、处理故障过程等主要代码
 *
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/5/10 21:52
 */
@Slf4j
public abstract class Algorithm {
    /**
     * 控制发送数据线程安全的锁
     */
    private static final Object SEND_LOCK = new Object();

    /**
     * 模拟路由过程
     *
     * @param param 模拟所需的参数配置
     * @return 路由结果，true：成功，false：失败
     */
    public boolean route(SimulationConfig param) {
        // 缓存待发送数据包的第一个数据包
        Packet firstSubPacket = param.getSubPacketMapping().firstEntry().getValue();
        // 缓存源节点
        Node sourceNode = param.getNodes().get(firstSubPacket.getSourceNodeId());
        // 缓存目标节点
        Node destinationNode = param.getNodes().get(firstSubPacket.getDestinationNodeId());

        // 缓存路由路径
        List<Integer> nodeIds = param.getRoutingPath();
        // 把源节点放进路由路径
        nodeIds.add(sourceNode.getId());

        // 创建队列
        LinkedList<Node> queue = Lists.newLinkedList();
        // 把源节点放进队列
        queue.addLast(sourceNode);

        // 测试队列是否为空，不为空则进入循环
        while (!queue.isEmpty()) {
            // 取出并删除队列的头节点，作为当前节点
            Node currentNode = queue.removeFirst();

            // 如果本次路由过程总跳数超过阈值，则路由失败
            if (nodeIds.size() > param.getConfig().getMaxHop()) {
                MyUtils.saveFailureReason(param.getFailureReasons(), FailureReason.REASON_1);
                return false;
            }

            Map<String, Object> extraData = Maps.newHashMap();

            // 创建一个集合，用于保存可作为下一跳的候选节点
            Result<List<Node>> getCandiddatesResult = getCandidates(param, currentNode, destinationNode, extraData);
            if (getCandiddatesResult.isNotSuccess()) {
                return false;
            }
            List<Node> candidateNodes = getCandiddatesResult.getData();

            // 遍历候选节点
            for (Node candidateNode : candidateNodes) {
                // 如果当前遍历的候选节点是目标节点
                if (candidateNode.getId() == destinationNode.getId()) {
                    // 向目标节点发送数据包
                    int routingResult = sendPocket(param, currentNode, candidateNode);
                    // 如果发送成功
                    if (routingResult == RoutingStatus.SUCCESS.getCode()) {
                        // 把目标节点的ID放进路由路径
                        nodeIds.add(candidateNode.getId());
                        // 路由成功
                        return true;
                    } else {
                        // 计算路由结果，返回路由结果
                        return routingResult == RoutingStatus.SENDING_END_SUCCESS.getCode();
                    }
                }
            }

            int transmissionDistance = getTransmissionDistance(param, extraData);

            // 优先级最高的节点的ID
            int nodeIdWithMaxMeasure = getNodeIdWithMaxMeasure(param, candidateNodes, currentNode, destinationNode, transmissionDistance);

            // 缓存最大优先级的节点作为下一跳节点
            Node nextHopNode = param.getNodes().get(nodeIdWithMaxMeasure);

            BigDecimal distance = BigDecimal.valueOf(MyUtils.distance(currentNode, destinationNode));
            if (param.getDangerousCount() > 0 && distance.intValue() <= param.getDangerousDistance()) {
                param.setDangerousFlag(true);
            }

            // 当前节点往下一跳节点发送所有数据包
            int routingResult = sendPocket(param, currentNode, nextHopNode);
            // 发送成功
            if (routingResult == RoutingStatus.SUCCESS.getCode()) {
                // 把下一跳节点放进路由路径中
                nodeIds.add(nextHopNode.getId());
                // 下一跳节点入队
                queue.addLast(nextHopNode);
            } else if (routingResult == RoutingStatus.SENDING_END_SUCCESS.getCode()) {
                // 如果在传输数据包的过程中，发送端节点出现了故障，当分路最终把数据包发送到目标节点的时候，这里直接返回成功
                return true;
            } else if (routingResult == RoutingStatus.RECEIVING_END_SUCCESS.getCode()) {
                //log.info("接收端故障, 发送端重新发送数据，发送端节点ID: {}", currentNode.getId());
                queue.addLast(currentNode);
            }
        }

        // 路由失败
        return false;
    }

    /**
     * @param param
     * @param currentNode
     * @param destinationNode
     * @param extraInfo
     * @return
     */
    public abstract Result<List<Node>> getCandidates(SimulationConfig param,
                                                     Node currentNode,
                                                     Node destinationNode,
                                                     Map<String, Object> extraInfo);

    /**
     * @param param
     * @param extraInfo
     * @return
     */
    public abstract int getTransmissionDistance(SimulationConfig param, Map<String, Object> extraInfo);

    /**
     * @param param
     * @param candidateNodes
     * @param currentNode
     * @param destinationNode
     * @param transmissionDistance
     * @return
     */
    public abstract int getNodeIdWithMaxMeasure(SimulationConfig param,
                                                List<Node> candidateNodes,
                                                Node currentNode,
                                                Node destinationNode,
                                                int transmissionDistance);

    public abstract String getAlgoName();

    public abstract int getAlgoType();

    public abstract boolean getControlFlag();

    /**
     * 模拟传输过程
     *
     * @param param            模拟所需的参数配置
     * @param sendingEndNode   发送端节点
     * @param receivingEndNode 接收端节点
     * @return 传输结果，具体返回值的说明参考枚举文件：RoutingStatus
     */
    private int sendPocket(SimulationConfig param,
                           Node sendingEndNode,
                           Node receivingEndNode) {
        // 缓存待发送的所有数据包
        TreeMap<Integer, Packet> sendingPackets = param.getSubPacketMapping();
        // 如果没有要发送的数据包，抛出异常
        if (MapUtils.isEmpty(sendingPackets)) {
            throw new RuntimeException();
        }

        // 记录当前待发送的数据包是第几个数据包
        int subPacketSequence = 0;
        // 用于激活故障，当subPacketSequence与latch相等时，激活故障
        int latch = 0;
        // 如果满足了预激活故障的条件
        if (Objects.nonNull(param.getUnavailableLatch()) && param.getUnavailableLatch().decrementAndGet() == 0) {
            if (sendingPackets.size() > 2) {
                // 初始化latch的值
                latch = param.getTrap().getPacketSequence();
                if (latch < sendingPackets.size() * 0.6) {
                    latch = -1;
                }
            } else {
                latch = -1;
            }
        }

        // 遍历所有待发送的数据包
        for (Map.Entry<Integer, Packet> subPacketEntry : sendingPackets.entrySet()) {
            // 缓存当前数据包
            Packet subPacket = subPacketEntry.getValue();

            // 发送端能量不足，返回失败
            if (sendingEndNode.getEnergy() < param.getConfig().getEnergyByOptical()) {
                MyUtils.saveFailureReason(param.getFailureReasons(), FailureReason.REASON_4);
                return RoutingStatus.FAILURE.getCode();
            }

            // 表示数据包次序的变量自增
            subPacketSequence++;

            // 如果满足发生故障的条件，处理发送节点故障
            if (
                    //subPacketSequence == latch
                    param.isDangerousFlag() && ((double) subPacketSequence / sendingPackets.size()) > 0.6
            ) {
                /*boolean receiveEndUnavailableFlag = new Random().nextBoolean();
                if (receiveEndUnavailableFlag
                        && receivingEndNode.getId() != param.getNodes().get(param.getNodes().size() - 1).getId()) {
                    log.info("接收端故障, 接收端节点ID: {}", receivingEndNode.getId());
                    receivingEndNode.setAvailableFlag(false);
                    return RoutingStatus.RECEIVING_END_SUCCESS.getCode();
                }*/

                // 设置发送端节点为不可用状态，这意味着这个节点不能被其他节点扫描到
                sendingEndNode.setAvailableFlag(false);

                param.setDangerousFlag(false);
                param.setDangerousCount(param.getDangerousCount() - 1);

                // 没有后路
                if (!param.isEnableRelayFlag()) {
                    // 更新优化前故障次数
                    param.getUnavailableInfo().getEncountered().incrementAndGet();
                    MyUtils.saveFailureReason(param.getFailureReasons(), FailureReason.REASON_5);
                    // 返回失败
                    return RoutingStatus.FAILURE.getCode();
                }

                // 更新优化后故障次数
                param.getUnavailableInfo().getEncountered().incrementAndGet();

                // 接收端没有声通信距离内的邻居
                if (CollectionUtils.isEmpty(receivingEndNode.getAcousticNeighborNodes())) {
                    MyUtils.saveFailureReason(param.getFailureReasons(), FailureReason.REASON_6);
                    // 返回失败
                    return RoutingStatus.FAILURE.getCode();
                }

                List<Node> relayNodes = Lists.newArrayList();
                // 遍历接收端声通信距离内的所有邻居
                for (Node acousticNeighborNode : receivingEndNode.getAcousticNeighborNodes()) {
                    // 如果当前遍历的节点可用、能量充足、声通信距离内有邻居、完整数据包缓冲区里面包含所需数据包的ID的表
                    if (acousticNeighborNode.isAvailableFlag()
                            && acousticNeighborNode.getEnergy() >= param.getConfig().getEnergyByAcoustic()
                            && CollectionUtils.isNotEmpty(acousticNeighborNode.getAcousticNeighborNodes())
                            && acousticNeighborNode.getCompletePacketMapping().containsKey(subPacket.getId())
                            && acousticNeighborNode.getPoint().getZAxis() <= receivingEndNode.getPoint().getZAxis()) {
                        relayNodes.add(acousticNeighborNode);
                    }
                }

                if (CollectionUtils.isEmpty(relayNodes)) {
                    MyUtils.saveFailureReason(param.getFailureReasons(), FailureReason.REASON_7);
                    return RoutingStatus.FAILURE.getCode();
                }

                Node relayNodeNearest = relayNodes.get(0);
                double minDistance = MyUtils.distance(receivingEndNode, relayNodes.get(0));
                for (int i = 1, size = relayNodes.size(); i < size; i++) {
                    double distance = MyUtils.distance(receivingEndNode, relayNodes.get(i));
                    if (distance <= minDistance) {
                        minDistance = distance;
                        relayNodeNearest = relayNodes.get(i);
                    }
                }

                // 缓存接收端不完整数据包缓冲区中该数据包的所有子包
                TreeMap<Integer, Packet> incompletePacketMap =
                        receivingEndNode.getIncompletePacketMapping().get(subPacket.getId());
                // 缓存接力端完整数据包缓冲区中该数据包的所有子包
                TreeMap<Integer, Packet> completePacketMap =
                        relayNodeNearest.getCompletePacketMapping().get(subPacket.getId());

                // 兵分两路，接收端传输已收到的所有数据包，接力端传输接收端未收到的所有数据包
                if (handleUnavailableCondition(param,
                        receivingEndNode,
                        relayNodeNearest,
                        incompletePacketMap,
                        completePacketMap)) {
                    // 分路传输成功，更新优化后幸存次数
                    param.getUnavailableInfo().getSurvived().incrementAndGet();
                    // 返回成功
                    return RoutingStatus.SENDING_END_SUCCESS.getCode();
                }

                MyUtils.saveFailureReason(param.getFailureReasons(), FailureReason.REASON_8);
                // 返回失败
                return RoutingStatus.FAILURE.getCode();
            }

            // 加锁
            synchronized (SEND_LOCK) {
                // 发送端向接收端发送当前数据包
                if (!handleSubPacket(param, sendingEndNode, receivingEndNode, subPacket)) {
                    MyUtils.saveFailureReason(param.getFailureReasons(), FailureReason.REASON_9);
                    // 发送失败，返回失败
                    return RoutingStatus.FAILURE.getCode();
                }
            }
        }

        // 发送成功
        return RoutingStatus.SUCCESS.getCode();
    }

    /**
     * 处理发送端故障的情况
     *
     * @param param                模拟所需的参数配置
     * @param receivingEndNode     接收端节点
     * @param acousticNeighborNode 接力端节点
     * @param incompletePacketMap  接收端不完整数据缓冲区缓存
     * @param completePacketMap    接力端完整数据缓冲区缓存
     * @return 处理结果
     */
    private boolean handleUnavailableCondition(SimulationConfig param,
                                               Node receivingEndNode,
                                               Node acousticNeighborNode,
                                               TreeMap<Integer, Packet> incompletePacketMap,
                                               TreeMap<Integer, Packet> completePacketMap) {
        // 用于保存接收端未接收的数据包，即接力端需要发送的数据包
        TreeMap<Integer, Packet> notReceivedSubPacketMapping = Maps.newTreeMap();
        // 遍历接力端完整数据缓冲区中，关于指定数据包的表
        for (Map.Entry<Integer, Packet> entry : completePacketMap.entrySet()) {
            // 缓存当前遍历的数据包
            Packet subPacket = entry.getValue();
            // 如果当前遍历的数据包已被接收端接收过，则跳过
            if (incompletePacketMap.containsKey(subPacket.getSequence())) {
                continue;
            }
            // 复制当前遍历的数据包
            subPacket = duplicateSubPacket(subPacket);
            // 把数据包的源节点ID改成接力端节点的ID
            subPacket.setSourceNodeId(acousticNeighborNode.getId());
            // 把当前数据包放到用于保存接收端未接收的数据包表中
            notReceivedSubPacketMapping.put(entry.getKey(), subPacket);
        }

        // 用于保存接收端已接收的数据包，即接收端需要发送的数据包
        TreeMap<Integer, Packet> receivedSubPacketMapping = Maps.newTreeMap();
        // 遍历接收端不完整数据缓冲区中，关于指定数据包的表
        for (Map.Entry<Integer, Packet> entry : incompletePacketMap.entrySet()) {
            // 复制当前遍历的数据包
            Packet subPacket = duplicateSubPacket(entry.getValue());
            // 把数据包的源节点ID改成接收端节点的ID
            subPacket.setSourceNodeId(receivingEndNode.getId());
            // 把当前数据包放到用于保存接收端已接收的数据包表中
            receivedSubPacketMapping.put(entry.getKey(), subPacket);
        }

        // 发送已收到的数据包
        // 初始化接收端路由的配置
        SimulationConfig config1 = Converter.INSTANCE.convertToDsConfig(param);
        config1.setRoutingPath(Lists.newArrayList());
        config1.setSubPacketMapping(receivedSubPacketMapping);
        param.getRoutingPaths().add(config1.getRoutingPath());
        config1.setConsumeTimes(param.getConsumeTimes());
        config1.setConsumeEnergies(param.getConsumeEnergies());
        // 执行接收端路由任务
        CompletableFuture<Boolean> handleReceivedSubPacketsResult =
                CompletableFuture.supplyAsync(() -> param.getAlgorithm().route(config1), ThreadPoolConfig.EXECUTOR);

        // 发送未收到的数据包
        // 初始化接力端路由的配置
        SimulationConfig config2 = Converter.INSTANCE.convertToDsConfig(param);
        config2.setRoutingPath(Lists.newArrayList());
        config2.setSubPacketMapping(notReceivedSubPacketMapping);
        param.getRoutingPaths().add(config2.getRoutingPath());
        config2.setConsumeTimes(param.getConsumeTimes());
        config2.setConsumeEnergies(param.getConsumeEnergies());
        // 执行接力端路由任务
        CompletableFuture<Boolean> handleNotReceivedSubPacketsResult =
                CompletableFuture.supplyAsync(() -> param.getAlgorithm().route(config2), ThreadPoolConfig.EXECUTOR);

        // 当前线程等待接收端和接力端任务完成
        CompletableFuture.allOf(handleNotReceivedSubPacketsResult, handleReceivedSubPacketsResult).join();

        try {
            // 接收端路由结果
            Boolean result1 = handleNotReceivedSubPacketsResult.get();
            // 接力端路由结果
            Boolean result2 = handleReceivedSubPacketsResult.get();
            // 如果接收端和接力端都路由失败，则处理失败
            if (!result1 || !result2) {
                return false;
            }
        } catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }

        // 接收端和接力端都成功把数据包发送到终点，处理成功
        return true;
    }

    /**
     * 模拟发送端向接收端发送一个数据包的过程
     *
     * @param param            模拟所需的参数配置
     * @param sendingEndNode   发送端节点
     * @param receivingEndNode 接收端节点
     * @param packetPart       待发送的数据包
     * @return 发送结果
     */
    private boolean handleSubPacket(SimulationConfig param,
                                    Node sendingEndNode,
                                    Node receivingEndNode,
                                    Packet packetPart) {
        int transmissionSpeed;
        if (param.getAlgorithm().getAlgoType() == 1 || param.getAlgorithm().getAlgoType() == 3) {
            transmissionSpeed = param.getConfig().getOpticalSpeed();
        } else if (param.getAlgorithm().getAlgoType() == 2) {
            transmissionSpeed = param.getConfig().getAcousticSpeed();
        } else {
            throw new IllegalStateException();
        }

        BigDecimal distance = BigDecimal.valueOf(MyUtils.distance(sendingEndNode, receivingEndNode));
        BigDecimal consumeTime = distance.divide(BigDecimal.valueOf(transmissionSpeed), 2, RoundingMode.HALF_UP);

        int transmissionEnergy;
        if (param.getAlgorithm().getAlgoType() == 1 || param.getAlgorithm().getAlgoType() == 3) {
            transmissionEnergy = param.getConfig().getEnergyByOptical();
        } else if (param.getAlgorithm().getAlgoType() == 2) {
            transmissionEnergy = param.getConfig().getEnergyByAcoustic();
        } else {
            throw new IllegalStateException();
        }

        // 发送端消耗发送一个数据包的能量（源节点不消耗能量）
        if (!sendingEndNode.equals(param.getNodes().get(0))) {
            sendingEndNode.setEnergy(sendingEndNode.getEnergy() - transmissionEnergy);
        }

        param.getConsumeTimes().add(consumeTime);
        param.getConsumeEnergies().add(transmissionEnergy);

        // 接收端收到数据包
        receivingEndNode.getReceivedPackets().add(packetPart);
        // 接收端取出数据包
        Packet receivedPocketPart = receivingEndNode.getReceivedPackets().removeFirst();
        // 接收端复制数据包
        Packet duplicate = duplicateSubPacket(receivedPocketPart);

        // 缓存接收端的完整数据包缓冲区
        LinkedHashMap<String, TreeMap<Integer, Packet>> completePacketMapping =
                receivingEndNode.getCompletePacketMapping();

        // 数据包是整包
        if (duplicate.getData().size() == duplicate.getTotalBytes()) {
            // 完整数据包缓冲区不包含该数据包ID的表，则建表，关联该数据包ID
            if (!completePacketMapping.containsKey(duplicate.getId())) {
                TreeMap<Integer, Packet> treeMap = Maps.newTreeMap();
                completePacketMapping.put(duplicate.getId(), treeMap);
            }
            // 保存数据包到完整数据缓冲区
            completePacketMapping.get(duplicate.getId()).put(duplicate.getSequence(), duplicate);
            // 处理成功
            return true;
        }

        // 数据包是子包
        if (duplicate.getData().size() < duplicate.getTotalBytes()) {
            // 缓存接收端不完整数据包缓冲区
            LinkedHashMap<String, TreeMap<Integer, Packet>> incompletePacketMapping =
                    receivingEndNode.getIncompletePacketMapping();
            // 不完整数据包缓冲区不包含该数据包ID的表，则建表，关联该数据包ID
            if (!incompletePacketMapping.containsKey(duplicate.getId())) {
                TreeMap<Integer, Packet> treeMap = Maps.newTreeMap();
                incompletePacketMapping.put(duplicate.getId(), treeMap);
            }
            // 保存数据包到不完整数据缓冲区
            incompletePacketMapping.get(duplicate.getId()).put(duplicate.getSequence(), duplicate);
            // 在不完整数据缓冲区中，当前数据包ID的所有子包加起来的字节数
            int currentTotalBytes = 0;
            // 遍历不完整数据缓冲区中当前数据包ID的所有子包，对所有子包中的数据大小求和
            for (Map.Entry<Integer, Packet> entry : incompletePacketMapping.get(duplicate.getId()).entrySet()) {
                currentTotalBytes += entry.getValue().getData().size();
            }
            // 如果求和结果和数据包中的总字节数大小相同，则说明这些子包可以组成完整的数据包，也就是接收端已经收到了当前数据包ID的所有子包
            if (currentTotalBytes == duplicate.getTotalBytes()) {
                // 完整数据包缓冲区不包含该数据包ID的表，则建表，关联该数据包ID
                if (!completePacketMapping.containsKey(duplicate.getId())) {
                    TreeMap<Integer, Packet> treeMap = Maps.newTreeMap();
                    completePacketMapping.put(duplicate.getId(), treeMap);
                }
                // 把不完整数据缓冲区的数据包移动到完整数据缓冲区
                completePacketMapping.put(duplicate.getId(), incompletePacketMapping.remove(duplicate.getId()));
            }
            // 处理成功
            return true;
        }

        // 处理失败
        return false;
    }

    /**
     * 复制数据包
     *
     * @param subPacket 数据包
     * @return 复制的数据包
     */
    private Packet duplicateSubPacket(Packet subPacket) {
        Packet subPacketCopy = Converter.INSTANCE.convertToPacket(subPacket);
        subPacketCopy.setData(Lists.newArrayList(subPacket.getData()));
        return subPacketCopy;
    }
}