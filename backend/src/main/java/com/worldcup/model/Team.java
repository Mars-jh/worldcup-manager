package com.worldcup.model;

import lombok.*;
import java.time.LocalDateTime;

/** 球队实体 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Team {
    private Long id;
    private String name;           // 国家名称
    private String code;           // 三字母缩写
    private Continent continent;   // 所属大洲
    private String groupLetter;    // 小组编号 A-H
    private String coach;          // 主教练
    private String flagEmoji;      // 国旗emoji
    private int worldRanking;      // 世界排名
    private LocalDateTime createdAt;

    // 小组赛统计（动态计算）
    private transient int played;
    private transient int won;
    private transient int drawn;
    private transient int lost;
    private transient int goalsFor;
    private transient int goalsAgainst;
    private transient int goalDifference;
    private transient int points;
}