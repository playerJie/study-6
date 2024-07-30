package com.demo.study.model;

import lombok.*;

/**
 * @author Tongyu Wu
 * @version 1.0
 * @date 2024/5/22 21:13
 */
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
@ToString
public class GenerateConfig {
    int x;
    int y;
    int z;
    int size;
    int initEnergy;
    int majorNodeSize;
    int gapDistance;
    int aroundNodeSize;
}
