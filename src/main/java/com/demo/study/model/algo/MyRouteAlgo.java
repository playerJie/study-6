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
    private Algorithm algorithm = new DsAlgo();

    public MyRouteAlgo() {
    }

    public MyRouteAlgo(Algorithm algorithm) {
        this.algorithm = algorithm;
    }

    @Override
    public Result<List<Node>> getCandidates(SimulationConfig param,
                                            Node currentNode,
                                            Node destinationNode,
                                            Map<String, Object> extraInfo) {
        return algorithm.getCandidates(param, currentNode, destinationNode, extraInfo);
    }

    @Override
    public int getTransmissionDistance(SimulationConfig param, Map<String, Object> extraInfo) {
        return algorithm.getTransmissionDistance(param, extraInfo);
    }

    @Override
    public int getNodeIdWithMaxMeasure(SimulationConfig param,
                                       List<Node> candidateNodes,
                                       Node currentNode,
                                       Node destinationNode,
                                       int transmissionDistance) {
        return algorithm.getNodeIdWithMaxMeasure(param, candidateNodes, currentNode, destinationNode, transmissionDistance);
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

    @Override
    public int getDownDistance() {
        return 200;
    }
}