package com.worldcup.model;

import lombok.*;
import java.time.LocalDateTime;

/** 比赛实体 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Match {
    private Long id;
    private String stage;           // 阶段: GROUP, ROUND_OF_16, QUARTER, SEMI, FINAL, THIRD_PLACE
    private String groupLetter;     // 小组编号（仅小组赛有值）
    private Long homeTeamId;        // 主队ID
    private Long awayTeamId;        // 客队ID
    private Integer homeGoals;      // 主队进球
    private Integer awayGoals;      // 客队进球
    private MatchStatus status;     // 比赛状态
    private LocalDateTime matchTime;// 比赛时间
    private int matchOrder;         // 比赛序号（用于排序）
}