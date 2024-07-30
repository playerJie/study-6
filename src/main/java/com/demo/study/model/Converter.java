package com.demo.study.model;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

/**
 * 提供类与类之间的复制转换接口
 *
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/5/12 10:57
 */
@Mapper
public interface Converter {
    /**
     * 定义实例，代码中需要用到此实例来调用此接口中的方法
     */
    Converter INSTANCE = Mappers.getMapper(Converter.class);

    /**
     * 复制SimulationConfig类的变量到SimulationConfig类的变量
     *
     * @param param 源SimulationConfig类
     * @return 目标SimulationConfig类
     */
    @Mappings({
            // 忽略目标类的routingPath变量，其他同理
            @Mapping(target = "routingPath", ignore = true),
            @Mapping(target = "subPacketMapping", ignore = true),
            @Mapping(target = "unavailableLatch", ignore = true)
    })
    SimulationConfig convertToDsConfig(SimulationConfig param);

    /**
     * 复制Packet类的变量到Packet类的变量
     *
     * @param param 源Packet类
     * @return 目标Packet类
     */
    @Mapping(target = "data", ignore = true)
    Packet convertToPacket(Packet param);
}