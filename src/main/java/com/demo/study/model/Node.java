package com.demo.study.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 描述水下空间中节点的信息
 *
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/5/10 13:03
 */
@Getter
@Setter
@ToString
public class Node {
    /**
     * 节点ID
     */
    private int id;
    /**
     * 节点位置
     */
    private Point point;
    /**
     * 节点能量
     */
    private int energy;
    /**
     * 节点是否可用标志（false：不可用；true：可用）
     * 当值为false的时候，表明此节点出故障了，后续就不能被其他节点发现
     */
    private boolean availableFlag;
    /**
     * 在节点光通信距离内的节点集合
     */
    private List<Node> opticalNeighborNodes;
    /**
     * 在节点声通信距离内的节点集合
     */
    private List<Node> acousticNeighborNodes;
    /**
     * 接收缓冲区：从其他节点发送过来的数据包先保存在这里
     */
    private LinkedList<Packet> receivedPackets;
    /**
     * 不完整数据包缓冲区：在处理接收缓冲区的数据包的时候，如果数据包是子包，则会把子包移动到这个缓冲区。
     * 这个缓冲区有容量上限，采用最近最少使用（LRU）算法管理缓冲区内的数据包，即最近最少被使用的数据包在缓冲区空间不足时，会被删除
     */
    private LinkedHashMap<String, TreeMap<Integer, Packet>> incompletePacketMapping;
    /**
     * 完整数据包缓冲区：在处理不完整数据包缓冲区的时候，如果同一个ID的多个数据包可以组成完整的数据包，
     * 则把这些数据包移动到这个缓冲区。这个缓冲区和不完整数据包缓冲区一样采用相同的空间管理策略
     */
    private LinkedHashMap<String, TreeMap<Integer, Packet>> completePacketMapping;
    /**
     * 数据包ID生成器：用于生成由此节点产生并发出的数据包的ID，为了标识数据包ID的唯一性，数据包ID的内容
     * 通常包括节点ID，例如：节点ID假如是100，生成的ID是50，则完整的数据包ID是：100:50
     */
    private AtomicInteger packetIdGenerator;
    private List<Node> continueNodes;
}