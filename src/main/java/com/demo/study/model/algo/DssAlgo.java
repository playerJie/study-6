package com.demo.study.model.algo;

import com.demo.study.model.*;
import com.demo.study.model.result.Result;
import com.demo.study.model.result.ResultUtils;
import com.google.common.collect.Lists;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;

/**
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/7/21 19:29
 */
public class DssAlgo extends Algorithm {
    @Override
    public Result<List<Node>> getCandidates(SimulationConfig param,
                                            Node currentNode,
                                            Node destinationNode,
                                            Map<String, Object> extraInfo) {
        // 没有邻居
        if (CollectionUtils.isEmpty(currentNode.getOpticalNeighborNodes())) {
            MyUtils.saveFailureReason(param.getFailureReasons(), FailureReason.REASON_10);
            return ResultUtils.failureWithMessage(FailureReason.REASON_10.getMessage());
        }

        // 候选节点
        List<Node> candidateNodes = Lists.newArrayList();

        searchCandidateNodes(param, currentNode, destinationNode, candidateNodes);

        // 没有候选节点
        if (CollectionUtils.isEmpty(candidateNodes)) {
            MyUtils.saveFailureReason(param.getFailureReasons(), FailureReason.REASON_11);
            return ResultUtils.failureWithMessage(FailureReason.REASON_11.getMessage());
        }

        return ResultUtils.successWithData(candidateNodes);
    }

    @Override
    public int getTransmissionDistance(SimulationConfig param, Map<String, Object> extraInfo) {
        return param.getConfig().getOpticalDistance();
    }

    @Override
    public int getNodeIdWithMaxMeasure(SimulationConfig param,
                                       List<Node> candidateNodes,
                                       Node currentNode,
                                       Node destinationNode,
                                       int transmissionDistance) {
        // 优先级最高的节点的ID
        int nodeIdWithMaxMeasure = 0;
        // 度量
        double measure = 0;
        // 下面根据论文中的公式计算优先级并更新优先级最高的节点的ID
        for (Node candidateNode : candidateNodes) {
            double d = MyUtils.distance(currentNode, candidateNode);
            double cosine = MyUtils.cosine(currentNode, candidateNode, destinationNode);
            double u = d * cosine / transmissionDistance;
            int e = candidateNode.getEnergy();
            double i = (double) e / param.getConfig().getEnergy();
            double j = u + i;
            if (measure < j) {
                measure = j;
                nodeIdWithMaxMeasure = candidateNode.getId();
            }
        }
        return nodeIdWithMaxMeasure;
    }

    @Override
    public String getAlgoName() {
        return "DSS算法";
    }

    @Override
    public int getAlgoType() {
        return AlgoType.OPTICAL.getAlgoType();
    }

    @Override
    public boolean getControlFlag() {
        return false;
    }

    /**
     * @param param
     * @param currentNode
     * @param destinationNode
     * @param candidateNodes
     */
    private void searchCandidateNodes(SimulationConfig param,
                                      Node currentNode,
                                      Node destinationNode,
                                      List<Node> candidateNodes) {
        // 遍历当前节点的光通信距离内的邻居
        for (Node neighborNode : currentNode.getOpticalNeighborNodes()) {
            // 测试计算出来的数值、能量、指定节点的可用状态、指定节点的光通信距离内的邻居数量
            if (
                    neighborNode.getPoint().getZAxis() >= currentNode.getPoint().getZAxis()
                            && neighborNode.getEnergy() >= param.getConfig().getEnergyByOptical()
                            && neighborNode.isAvailableFlag()
                            && CollectionUtils.isNotEmpty(neighborNode.getOpticalNeighborNodes())) {
                // 把符合条件的指定节点放入候选节点集合
                candidateNodes.add(neighborNode);
            }
        }
    }
}