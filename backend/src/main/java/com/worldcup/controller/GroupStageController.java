package com.worldcup.controller;

import com.worldcup.model.*;
import com.worldcup.service.GroupStageService;
import com.worldcup.service.KnockoutService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 小组赛控制器
 * 查看积分榜所有角色可访问，生成淘汰赛需要 ADMIN 权限
 */
@RestController
@RequestMapping("/api/groups")
public class GroupStageController {

    private final GroupStageService groupStageService;
    private final KnockoutService knockoutService;

    public GroupStageController(GroupStageService groupStageService, KnockoutService knockoutService) {
        this.groupStageService = groupStageService;
        this.knockoutService = knockoutService;
    }

    /** 获取所有小组名称 */
    @GetMapping
    public ApiResponse<List<String>> listGroups() {
        return ApiResponse.ok(groupStageService.getAllGroups());
    }

    /** 获取指定小组的积分榜 */
    @GetMapping("/{groupLetter}/standings")
    public ApiResponse<List<Team>> standings(@PathVariable String groupLetter) {
        return ApiResponse.ok(groupStageService.getStandings(groupLetter.toUpperCase()));
    }

    /** 获取所有小组的积分榜汇总 */
    @GetMapping("/all-standings")
    public ApiResponse<Map<String, List<Team>>> allStandings() {
        Map<String, List<Team>> result = new LinkedHashMap<>();
        for (String g : groupStageService.getAllGroups()) {
            result.put(g, groupStageService.getStandings(g));
        }
        return ApiResponse.ok(result);
    }

    /** 生成淘汰赛对阵（小组赛全部结束后由 ADMIN 触发） */
    @PostMapping("/generate-knockout")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> generateKnockout() {
        List<Match> r16 = knockoutService.generateRoundOf16();
        return ApiResponse.ok("淘汰赛对阵已生成", r16);
    }
}