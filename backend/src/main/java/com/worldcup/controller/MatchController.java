package com.worldcup.controller;

import com.worldcup.model.*;
import com.worldcup.service.MatchService;
import com.worldcup.service.KnockoutService;
import com.worldcup.service.GroupStageService;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 比赛控制器 - 赛程管理和比分录入
 * 比分录入需要 OPERATOR 或 ADMIN 权限
 */
@RestController
@RequestMapping("/api/matches")
public class MatchController {

    private final MatchService matchService;
    private final KnockoutService knockoutService;
    private final GroupStageService groupStageService;
    private final SimpMessagingTemplate wsTemplate; // WebSocket 推送

    public MatchController(MatchService matchService, KnockoutService knockoutService,
                           GroupStageService groupStageService, SimpMessagingTemplate wsTemplate) {
        this.matchService = matchService;
        this.knockoutService = knockoutService;
        this.groupStageService = groupStageService;
        this.wsTemplate = wsTemplate;
    }

    /** 获取所有比赛 */
    @GetMapping
    public ApiResponse<List<Match>> list(
            @RequestParam(required = false) String stage,
            @RequestParam(required = false) String group) {
        if (stage != null) return ApiResponse.ok(matchService.findByStage(stage));
        if (group != null) return ApiResponse.ok(matchService.findByGroup(group));
        return ApiResponse.ok(matchService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Match> getById(@PathVariable Long id) {
        return matchService.findById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error("比赛不存在"));
    }

    /** 录入比分 - 核心操作 */
    @PutMapping("/{id}/score")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ApiResponse<Match> recordScore(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        Integer homeGoals = body.get("homeGoals");
        Integer awayGoals = body.get("awayGoals");
        if (homeGoals == null || awayGoals == null) {
            throw new IllegalArgumentException("主客队比分不能为空");
        }

        Match existing = matchService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("比赛不存在"));
        Match match = "GROUP".equals(existing.getStage())
                ? matchService.recordScore(id, homeGoals, awayGoals)
                : knockoutService.recordScore(id, homeGoals, awayGoals);

        // WebSocket 广播比分更新给所有在线用户
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("matchId", id);
        payload.put("homeGoals", homeGoals);
        payload.put("awayGoals", awayGoals);
        payload.put("stage", match.getStage());
        wsTemplate.convertAndSend("/topic/score", payload);

        return ApiResponse.ok("比分已录入", match);
    }

    /** 创建比赛（ADMIN） */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Match> create(@RequestBody Match match) {
        return ApiResponse.ok("创建成功", matchService.create(match));
    }

    /** 更新比赛信息 */
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ApiResponse<Match> update(@PathVariable Long id, @RequestBody Match match) {
        return ApiResponse.ok("更新成功", matchService.update(id, match));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> delete(@PathVariable Long id) {
        matchService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}
