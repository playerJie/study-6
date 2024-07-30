package com.demo.study.model;

import lombok.*;

/**
 * 描述节点在水下空间中的位置
 *
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/5/10 13:04
 */
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@ToString
public class Point {
    /**
     * x轴值
     */
    private int xAxis;
    /**
     * y轴值
     */
    private int yAxis;
    /**
     * z轴值
     */
    private int zAxis;
}
