package com.demo.study.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.List;

/**
 * 描述数据包的信息
 *
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/5/10 14:08
 */
@Getter
@Setter
@ToString
public class Packet {
    /**
     * 数据包唯一标识，如：节点ID：数据包ID
     */
    private String id;
    /**
     * 数据包来源节点ID（从哪里来）
     */
    private int sourceNodeId;
    /**
     * 数据包目标节点ID（到哪里去）
     */
    private int destinationNodeId;
    /**
     * 数据包的序号，如果一个数据包被拆分成了多个数据包，则每个数据包的序号按顺序递增
     * 例如：假如一个数据包最多携带10字节的数据，那么一个携带26字节的数据包，发送端会在发送数据包之前，
     * 先把原来的数据包拆分成3个分别携带10，10，6字节数据的子包，按顺序分别发送
     */
    private int sequence;
    /**
     * 完整数据包的数据的总字节数
     */
    private int totalBytes;
    /**
     * 当前数据包的数据
     */
    private List<Byte> data;
    /**
     * 传输类型（0：光传输；1：声传输）
     */
    private int transmissionType;
    /**
     * 包类型
     */
    private int packetType;
}
