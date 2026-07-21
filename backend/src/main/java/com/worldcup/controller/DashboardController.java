package com.worldcup.controller;

import com.worldcup.model.*;
import com.worldcup.service.*;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 仪表板控制器 - 提供汇总统计数据
 */
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final TeamService teamService;
    private final PlayerService playerService;
    private final MatchService matchService;
    private final GroupStageService groupStageService;

    public DashboardController(TeamService teamService, PlayerService playerService,
                               MatchService matchService, GroupStageService groupStageService) {
        this.teamService = teamService;
        this.playerService = playerService;
        this.matchService = matchService;
        this.groupStageService = groupStageService;
    }

    /** 获取仪表板汇总数据 */
    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("totalTeams", teamService.findAll().size());
        data.put("totalPlayers", playerService.findAll().size());
        data.put("completedMatches", matchService.countCompleted());
        data.put("totalMatches", matchService.findAll().size());
        data.put("topScorers", playerService.getTopScorers(5));
        data.put("topAssists", playerService.getTopAssists(5));

        // 各洲球队分布（用于图表）
        Map<String, Long> continentDist = teamService.findAll().stream()
                .collect(Collectors.groupingBy(t -> t.getContinent().name(), Collectors.counting()));
        data.put("continentDistribution", continentDist);

        return ApiResponse.ok(data);
    }

    /** 获取赛程日历数据（按日期分组） */
    @GetMapping("/schedule")
    public ApiResponse<List<Map<String, Object>>> schedule() {
        List<Match> matches = matchService.findAll();
        // 按 matchOrder 排序后分组展示
        List<Map<String, Object>> result = new ArrayList<>();
        for (Match m : matches) {
            Map<String, Object> item = new LinkedHashMap<>();
            item.put("id", m.getId());
            item.put("stage", m.getStage());
            item.put("group", m.getGroupLetter());
            item.put("homeTeamId", m.getHomeTeamId());
            item.put("awayTeamId", m.getAwayTeamId());
            item.put("homeTeamName", m.getHomeTeamId() != null ?
                    teamService.findById(m.getHomeTeamId()).map(Team::getName).orElse("TBD") : "TBD");
            item.put("awayTeamName", m.getAwayTeamId() != null ?
                    teamService.findById(m.getAwayTeamId()).map(Team::getName).orElse("TBD") : "TBD");
            item.put("homeGoals", m.getHomeGoals());
            item.put("awayGoals", m.getAwayGoals());
            item.put("status", m.getStatus());
            item.put("matchOrder", m.getMatchOrder());
            result.add(item);
        }
        return ApiResponse.ok(result);
    }
}