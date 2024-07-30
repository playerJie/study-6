package com.demo.study.model.algo;

import com.demo.study.model.AlgoType;
import com.demo.study.model.Node;
import com.demo.study.model.SimulationConfig;
import com.demo.study.model.result.Result;

import java.util.List;
import java.util.Map;

/**
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/7/21 19:29
 */
public class MyRouteAlgo extends Algorithm {
    private final Algorithm ds = new DsAlgo();

    @Override
    public Result<List<Node>> getCandidates(SimulationConfig param,
                                            Node currentNode,
                                            Node destinationNode,
                                            Map<String, Object> extraInfo) {
        return ds.getCandidates(param, currentNode, destinationNode, extraInfo);
    }

    @Override
    public int getTransmissionDistance(SimulationConfig param, Map<String, Object> extraInfo) {
        return ds.getTransmissionDistance(param, extraInfo);
    }

    @Override
    public int getNodeIdWithMaxMeasure(SimulationConfig param,
                                       List<Node> candidateNodes,
                                       Node currentNode,
                                       Node destinationNode,
                                       int transmissionDistance) {
        return ds.getNodeIdWithMaxMeasure(param, candidateNodes, currentNode, destinationNode, transmissionDistance);
    }

    @Override
    public String getAlgoName() {
        return "我的算法";
    }

    @Override
    public int getAlgoType() {
        return AlgoType.OPTICAL_ACOUSTIC.getAlgoType();
    }

    @Override
    public boolean getControlFlag() {
        return true;
    }
}