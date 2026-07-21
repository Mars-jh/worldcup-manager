package com.worldcup.model;

import lombok.*;

/** 球员实体 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Player {
    private Long id;
    private String name;         // 姓名
    private Long teamId;         // 所属球队ID
    private Position position;   // 位置
    private int jerseyNumber;    // 球衣号码
    private int age;             // 年龄
    private int height;          // 身高(cm)
    private int weight;          // 体重(kg)
    private int rating;          // 能力值 0-99
    private int goals;           // 进球数
    private int assists;         // 助攻数
}