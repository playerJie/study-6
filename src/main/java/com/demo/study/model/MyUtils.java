package com.demo.study.model;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 工具类，提供常用的方法
 *
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/5/10 16:31
 */
public class MyUtils {
    /**
     * 用于生成随机数
     */
    public static final ThreadLocalRandom RANDOM = ThreadLocalRandom.current();

    /**
     * 在指定三维空间，生成指定数量节点，并初始化每个节点的信息
     *
     * @param x          x轴长度
     * @param y          y轴长度
     * @param z          z轴长度
     * @param size       空间中的节点数量
     * @param initEnergy 节点初始能量
     * @return 节点集合
     */
    public static List<Node> generateNodes(int x,
                                           int y,
                                           int z,
                                           int size,
                                           int initEnergy) {
        // 节点数不能小于3
        if (size < 3) {
            throw new RuntimeException();
        }
        // 用于保存所有节点的集合
        List<Node> nodes = Lists.newArrayListWithCapacity(size);
        // 生成源节点位置
        Point sourcePoint = new Point(generateInt(0, x), generateInt(0, y), generateInt(0, 0));
        // 初始化源节点，并把源节点放进集合
        nodes.add(initNode(0, sourcePoint, initEnergy));
        // 生成除源节点和目标节点外，剩余的其他节点
        for (int i = 1; i < size - 1; i++) {
            // 创建节点位置
            Point point = new Point(generateInt(0, x), generateInt(0, y), generateInt(1, z - 1));
            // 初始化节点
            Node node = initNode(i, point, initEnergy);
            // 把节点放进集合
            nodes.add(node);
        }
        // 生成目标节点位置
        Point destinationPoint = new Point(generateInt(0, x), generateInt(0, y), generateInt(z, z));
        // 初始化目标节点，并把目标节点放进集合
        nodes.add(initNode(size - 1, destinationPoint, initEnergy));
        return nodes;
    }

    /**
     * @param param
     * @return
     */
    public static List<Node> generateNodes2(GenerateConfig param) {
        //int majorNodeSize = param.getMajorNodeSize();
        if (param.getZ() % param.getGapDistance() != 0) {
            throw new IllegalArgumentException();
        }
        int majorNodeSize = param.getZ() / param.getGapDistance() + 1;
        int gapDistance = param.getGapDistance();
        int generateRadius = gapDistance / 2 - 1;
        int aroundNodeSize = param.getAroundNodeSize();
        int totalNodeSize = majorNodeSize + (majorNodeSize - 2) * aroundNodeSize;
        // 用于保存所有节点的集合
        List<Node> nodes = Lists.newArrayListWithCapacity(totalNodeSize);
        List<Node> majorNodes = Lists.newArrayListWithCapacity(majorNodeSize - 2);
        // 生成源节点位置（xoy平面）
        Point sourcePoint = new Point(generateInt(0, param.x), generateInt(0, param.y), generateInt(0, 0));
        // 初始化源节点，并把源节点放进集合
        nodes.add(initNode(0, sourcePoint, param.initEnergy));
        for (int i = 1; i < majorNodeSize - 1; i++) {
            // 创建节点位置
            Point point = new Point(sourcePoint.getXAxis(), sourcePoint.getYAxis(), i * gapDistance);
            // 初始化节点
            Node node = initNode(i, point, param.initEnergy);
            // 把节点放进集合
            nodes.add(node);
            majorNodes.add(node);
        }
        int nodeId = majorNodes.get(majorNodes.size() - 1).getId() + 1;
        for (Node majorNode : majorNodes) {
            for (int i = 0; i < aroundNodeSize; i++) {
                Point point;
                point = new Point(generateInt(majorNode.getPoint().getXAxis() - generateRadius, majorNode.getPoint().getXAxis() + generateRadius),
                        generateInt(majorNode.getPoint().getYAxis() - generateRadius, majorNode.getPoint().getYAxis() + generateRadius),
                        generateInt(i % 2 == 0 ?
                                        majorNode.getPoint().getZAxis() : majorNode.getPoint().getZAxis() - generateRadius,
                                i % 2 == 0 ?
                                        majorNode.getPoint().getZAxis() + generateRadius : majorNode.getPoint().getZAxis()));
                Node node = initNode(nodeId, point, param.initEnergy);
                nodes.add(node);
                nodeId++;
            }
        }
        // 生成目标节点位置
        Point destinationPoint = new Point(sourcePoint.getXAxis(),
                sourcePoint.getYAxis(),
                majorNodes.get(majorNodes.size() - 1).getPoint().getZAxis() + gapDistance);
        // 初始化目标节点，并把目标节点放进集合
        nodes.add(initNode(nodeId, destinationPoint, param.initEnergy));
        return nodes;
    }

    public static List<Node> enlarge(GenerateConfig param, List<Node> nodes, int increment) {
        if (param.getZ() % param.getGapDistance() != 0) {
            throw new IllegalArgumentException();
        }
        int majorNodeSize = param.getZ() / param.getGapDistance() + 1;
        int gapDistance = param.getGapDistance();
        int generateRadius = gapDistance / 2 - 1;
        Node destinationNode = nodes.get(nodes.size() - 1);
        int nodeId = destinationNode.getId();
        nodes.remove(nodes.size() - 1);
        for (int i = 1; i < majorNodeSize - 1; i++) {
            Node majorNode = nodes.get(i);
            for (int j = 0; j < increment; j++) {
                Point point;
                point = new Point(generateInt(majorNode.getPoint().getXAxis() - generateRadius, majorNode.getPoint().getXAxis() + generateRadius),
                        generateInt(majorNode.getPoint().getYAxis() - generateRadius, majorNode.getPoint().getYAxis() + generateRadius),
                        generateInt(j % 2 == 0 ?
                                        majorNode.getPoint().getZAxis() : majorNode.getPoint().getZAxis() - generateRadius,
                                j % 2 == 0 ?
                                        majorNode.getPoint().getZAxis() + generateRadius : majorNode.getPoint().getZAxis()));
                Node node = initNode(nodeId, point, param.initEnergy);
                nodes.add(node);
                nodeId++;
            }
        }
        destinationNode.setId(nodeId);
        nodes.add(destinationNode);
        return nodes;
    }

    /**
     * 初始化节点信息
     *
     * @param id         节点ID
     * @param point      节点位置
     * @param initEnergy 节点初始能量
     * @return 初始化完成的节点
     */
    public static Node initNode(int id,
                                Point point,
                                int initEnergy) {

        // 创建一个节点，并给节点填充信息，最后返回节点信息
        Node node = new Node();
        node.setId(id);
        node.setPoint(point);
        node.setEnergy(initEnergy);
        node.setAvailableFlag(true);
        node.setOpticalNeighborNodes(Lists.newArrayList());
        node.setAcousticNeighborNodes(Lists.newArrayList());
        node.setReceivedPackets(Lists.newLinkedList());
        node.setIncompletePacketMapping(Maps.newLinkedHashMap());
        node.setCompletePacketMapping(Maps.newLinkedHashMap());
        node.setPacketIdGenerator(new AtomicInteger(1));
        return node;
    }

    /**
     * 生成指定范围的随机整数
     *
     * @param min 最小值
     * @param max 最大值
     * @return [min, max]
     */
    public static int generateInt(int min,
                                  int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }

    /**
     * 建立空间中每个节点与周围节点的关系
     *
     * @param nodes                 空间中所有节点
     * @param transmissionDistance  当flag=0的时候，表示光通信距离；当flag=1的时候，表示声通信距离
     * @param minTransmissionEnergy 发送一个数据包所需要的能量
     * @param flag                  [0,1]，当flag=0的时候，表示光通信；当flag=1的时候，表示声通信
     */
    public static void initNeighbouringNodesForAll(List<Node> nodes,
                                                   int transmissionDistance,
                                                   int minTransmissionEnergy,
                                                   int flag) {
        // 测试flag的值是不是为0或者1，不是抛异常
        if (flag != 0 && flag != 1) {
            throw new RuntimeException();
        }
        // 遍历每个节点
        for (int i = 0, size = nodes.size(); i < size; i++) {
            // 遍历每个节点
            for (int j = 0; j < size; j++) {
                // 检查的节点是自己，跳过
                if (i == j) {
                    continue;
                }
                // 测试能量、两点之间距离
                if (nodes.get(j).getEnergy() >= minTransmissionEnergy
                        && isNeighbor(nodes.get(i), nodes.get(j), transmissionDistance)) {
                    // 如果是光通信，把节点放进自己的光学邻居集合
                    if (flag == 0) {
                        nodes.get(i).getOpticalNeighborNodes().add(nodes.get(j));
                        continue;
                    }
                    // 如果是声通信，把节点放进自己的声学邻居集合
                    nodes.get(i).getAcousticNeighborNodes().add(nodes.get(j));
                }
            }
        }
    }

    /**
     * 判断两个节点是不是邻居
     *
     * @param node1                节点1
     * @param node2                节点2
     * @param transmissionDistance 通信距离
     * @return true：是邻居；false：不是邻居
     */
    public static boolean isNeighbor(Node node1,
                                     Node node2,
                                     int transmissionDistance) {
        // 计算两点之间的距离
        double d = distance(node1.getPoint(), node2.getPoint());
        // 测试距离是否在指定范围并返回结果
        return d > 0 && d <= transmissionDistance;
    }

    /**
     * 返回两个位置之间的距离
     *
     * @param point1 位置1
     * @param point2 位置2
     * @return 两个位置之间的距离
     */
    public static double distance(Point point1,
                                  Point point2) {
        // 计算x2-x1
        double x = point2.getXAxis() - point1.getXAxis();
        // 计算y2-y1
        double y = point2.getYAxis() - point1.getYAxis();
        // 计算z2-z1
        double z = point2.getZAxis() - point1.getZAxis();
        // 计算((x2-x1)^2 + (y2-y1)^2 + (z2-z1)^2)^(1/2)并返回
        return Math.sqrt(x * x + y * y + z * z);
    }

    /**
     * 返回两个节点之间的距离
     *
     * @param node1 节点1
     * @param node2 节点2
     * @return 两个节点之间的距离
     */
    public static double distance(Node node1,
                                  Node node2) {
        return distance(node1.getPoint(), node2.getPoint());
    }

    /**
     * 计算cosine值
     *
     * @param currentNode     当前节点
     * @param candidateNode   候选节点
     * @param destinationNode 目标节点
     * @return cosine值
     */
    public static double cosine(Node currentNode,
                                Node candidateNode,
                                Node destinationNode) {
        // 当前节点的位置
        Point currentNodePoint = currentNode.getPoint();
        // 候选节点的位置
        Point candidateNodePoint = candidateNode.getPoint();
        // 目标节点的位置
        Point destinationNodePoint = destinationNode.getPoint();
        // 候选节点与目标节点的距离
        double a = MyUtils.distance(candidateNodePoint, destinationNodePoint);
        // 当前节点与目标节点的距离
        double b = MyUtils.distance(currentNodePoint, destinationNodePoint);
        // 当前节点与候选节点的距离
        double c = MyUtils.distance(currentNodePoint, candidateNodePoint);
        // 根据公式计算cosine值
        return (b * b + c * c - a * a) / (2 * b * c);
    }

    /**
     * 是否在平面上
     *
     * @param currentNode     当前节点
     * @param candidateNode   候选节点
     * @param destinationNode 目标节点
     * @return 计算结果
     */
    public static double isAbovePlane(Node currentNode,
                                      Node candidateNode,
                                      Node destinationNode) {
        // 当前节点的位置
        Point currentNodePoint = currentNode.getPoint();
        // 候选节点的位置
        Point candidateNodePoint = candidateNode.getPoint();
        // 目标节点的位置
        Point destinationNodePoint = destinationNode.getPoint();
        // 下面是根据公式计算
        double a = (destinationNodePoint.getXAxis() - currentNodePoint.getXAxis())
                * (candidateNodePoint.getXAxis() - currentNodePoint.getXAxis());
        double b = (destinationNodePoint.getYAxis() - currentNodePoint.getYAxis())
                * (candidateNodePoint.getYAxis() - currentNodePoint.getYAxis());
        double c = (destinationNodePoint.getZAxis() - currentNodePoint.getZAxis())
                * (candidateNodePoint.getZAxis() - currentNodePoint.getZAxis());
        // 求和，返回结果
        return a + b + c;
    }

    /**
     * 解码数据，把字节数据转换为文本数据
     *
     * @param subPackets 数据包集合
     * @return 数据内容
     */
    public static String decodeData(TreeMap<Integer, Packet> subPackets) {
        List<Byte> bytes1 = Lists.newArrayList();
        for (Map.Entry<Integer, Packet> entry : subPackets.entrySet()) {
            bytes1.addAll(entry.getValue().getData());
        }
        byte[] bytes2 = new byte[bytes1.size()];
        for (int i = 0, size = bytes1.size(); i < size; i++) {
            bytes2[i] = bytes1.get(i);
        }
        return new String(bytes2, StandardCharsets.UTF_8);
    }

    /**
     * 创建待发送的数据包集合
     *
     * @param sourceNode      源节点
     * @param destinationNode 目标节点
     * @param data            待发送的文本数据
     * @return 待发送的数据包集合
     */
    public static TreeMap<Integer, Packet> getSubPackets(Node sourceNode,
                                                         Node destinationNode,
                                                         String data,
                                                         int bytes) {
        // 创建原始数据包
        Packet packet = getPacket(sourceNode, destinationNode, data);
        // 测试源节点的完整数据包缓冲区是否包含指定数据包ID的表
        if (!sourceNode.getCompletePacketMapping().containsKey(packet.getId())) {
            // 创建表
            TreeMap<Integer, Packet> treeMap = Maps.newTreeMap();
            // 在完整数据包缓冲区，关联指定数据包ID和表
            sourceNode.getCompletePacketMapping().put(packet.getId(), treeMap);
        }
        // 把原始数据包拆分成多个数据包并保存
        List<Packet> subPackets = splitPacket(packet, bytes);
        // 获取指定数据包在完整数据包缓冲区中关联的表
        TreeMap<Integer, Packet> completePacketMapping = sourceNode.getCompletePacketMapping().get(packet.getId());
        // 遍历原始数据包的所有子包
        for (Packet subPacket : subPackets) {
            // 把当前子包放进完整数据包缓冲区中关联的表，以包的序号作为查找索引
            completePacketMapping.put(subPacket.getSequence(), subPacket);
        }
        // 返回在完整数据包缓冲区中关联的表
        return completePacketMapping;
    }

    /**
     * 创建数据包
     *
     * @param sourceNode      源节点
     * @param destinationNode 目标节点
     * @param data            待发送的文本数据
     * @return 数据包
     */
    public static Packet getPacket(Node sourceNode,
                                   Node destinationNode,
                                   String data) {
        // 用于保存文本数据转成字节数组后的字节数据
        List<Byte> bytes = Lists.newArrayList();
        for (byte item : data.getBytes(StandardCharsets.UTF_8)) {
            bytes.add(item);
        }
        // 创建数据包，填充数据包的各项属性，最后返回
        Packet packet = new Packet();
        packet.setId(String.format("%s:%s", sourceNode.getId(), sourceNode.getPacketIdGenerator().getAndIncrement()));
        packet.setSourceNodeId(sourceNode.getId());
        packet.setDestinationNodeId(destinationNode.getId());
        packet.setSequence(1);
        packet.setTotalBytes(bytes.size());
        packet.setData(bytes);
        return packet;
    }

    /**
     * 把一个大的数据包拆分成多个小数据包
     *
     * @param packet 原始数据包
     * @return 大数据包的小数据包结合
     */
    public static List<Packet> splitPacket(Packet packet, int bytes) {
        // 用于保存大数据包的所有小数据包
        List<Packet> subPackets = Lists.newArrayList();
        // 测试原始数据包携带的数据大小是否超过阈值，如果是，则进行分包
        if (packet.getTotalBytes() > bytes) {
            // 把原始数据包的数据分成多个部分
            List<List<Byte>> dataPartitions = Lists.partition(packet.getData(), bytes);
            // 数据包的初始序号
            int sequence = 1;
            // 遍历原始数据的多个部分，分包创建不同的数据包存放每一部分数据，并把数据包放入集合
            for (List<Byte> dataPartition : dataPartitions) {
                Packet subPacket = new Packet();
                subPacket.setId(packet.getId());
                subPacket.setSourceNodeId(packet.getSourceNodeId());
                subPacket.setDestinationNodeId(packet.getDestinationNodeId());
                subPacket.setSequence(sequence++);
                subPacket.setTotalBytes(packet.getTotalBytes());
                subPacket.setData(dataPartition);
                subPackets.add(subPacket);
            }
        } else {
            // 原始数据包携带的数据大小没有超过阈值，不用分包，直接把原始数据包放入集合
            subPackets.add(packet);
        }
        // 返回子包集合
        return subPackets;
    }

    /**
     * 重置所有节点的部分状态信息
     *
     * @param nodes 空间中的所有节点
     */
    public static void resetNodes(List<Node> nodes, int energy) {
        // 遍历所有节点，重置部分信息
        for (Node node : nodes) {
            node.setEnergy(energy);
            node.setAvailableFlag(true);
            node.getReceivedPackets().clear();
            node.getIncompletePacketMapping().clear();
            node.getCompletePacketMapping().clear();
            node.getPacketIdGenerator().set(1);
        }
    }

    public static void resetNodes2(List<Node> nodes) {
        // 遍历所有节点，重置部分信息
        for (Node node : nodes) {
            //node.setEnergy(CommonConfig.DEFAULT_ENERGY);
            node.setAvailableFlag(true);
            node.getReceivedPackets().clear();
            node.getIncompletePacketMapping().clear();
            node.getCompletePacketMapping().clear();
            //node.getPacketIdGenerator().set(1);
        }
    }

    public static void resetNodes3(List<Node> nodes, int energy) {
        // 遍历所有节点，重置部分信息
        for (Node node : nodes) {
            node.setEnergy(energy);
            node.setAvailableFlag(true);
            node.getOpticalNeighborNodes().clear();
            node.getAcousticNeighborNodes().clear();
            node.getReceivedPackets().clear();
            node.getIncompletePacketMapping().clear();
            node.getCompletePacketMapping().clear();
            node.getPacketIdGenerator().set(1);
        }
    }

    /**
     * 分类子包的来源信息
     *
     * @param subPacketMapping 存放同一个数据包ID的不同序号的数据包的表
     * @return 返回分类信息表，key：节点ID，value：数据包个数
     */
    public static HashMap<Integer, Integer> getCountMap(TreeMap<Integer, Packet> subPacketMapping) {
        // 用于保存分类信息
        HashMap<Integer, Integer> countMap = Maps.newHashMap();
        // 遍历表的每一项
        for (Map.Entry<Integer, Packet> subPacketEntry : subPacketMapping.entrySet()) {
            // 缓存数据包
            Packet subPacket = subPacketEntry.getValue();
            // 测试分类信息里面是否包含此源节点ID
            if (countMap.containsKey(subPacket.getSourceNodeId())) {
                // 更新此源节点ID与数据包数的关系
                countMap.put(subPacket.getSourceNodeId(), countMap.get(subPacket.getSourceNodeId()) + 1);
                continue;
            }
            // 初始化此源节点ID与数据包数的关系
            countMap.put(subPacket.getSourceNodeId(), 1);
        }
        // 返回分类信息
        return countMap;
    }

    public static void saveFailureReason(HashMap<FailureReason, Integer> failureReasons, FailureReason reason) {
        if (failureReasons.containsKey(reason)) {
            failureReasons.put(reason, failureReasons.get(reason) + 1);
            return;
        }
        failureReasons.put(reason, 1);
    }

    public static int findFirstZero(List<Integer> routeResults, int consecutive) {
        int consecutiveZeros = 0;
        int lastZeroIndex;

        for (int i = 0, size = routeResults.size(); i < size; i++) {
            if (routeResults.get(i) == 0) {
                consecutiveZeros++;
                lastZeroIndex = i;
                if (consecutiveZeros == consecutive) {
                    return lastZeroIndex - (consecutive - 1);
                }
            } else {
                consecutiveZeros = 0;
            }
        }

        return -1;
    }
}