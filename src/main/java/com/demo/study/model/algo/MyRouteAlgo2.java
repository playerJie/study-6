package com.demo.study.model.algo;

import com.demo.study.model.Node;
import com.demo.study.model.SimulationConfig;
import com.demo.study.model.result.Result;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.List;
import java.util.Map;

/**
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/7/21 19:29
 */
@NoArgsConstructor
@AllArgsConstructor
public class MyRouteAlgo2 extends Algorithm {
    private Algorithm algorithm = new DsAlgo();
    private int algoType;
    private String algoName;
    private boolean controlFlag;
    private int upDistance;
    private int downDistance;

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
        return algoName;
    }

    @Override
    public int getAlgoType() {
        return algoType;
    }

    @Override
    public boolean getControlFlag() {
        return controlFlag;
    }

    @Override
    public int getUpDistance() {
        return upDistance;
    }

    @Override
    public int getDownDistance() {
        return downDistance;
    }
}