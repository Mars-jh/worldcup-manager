package com.worldcup.controller;

import com.worldcup.model.*;
import com.worldcup.service.TeamService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 球队控制器 - 球队 CRUD
 * 查询接口所有角色可访问，增删改需要 ADMIN 权限
 */
@RestController
@RequestMapping("/api/teams")
public class TeamController {

    private final TeamService teamService;

    public TeamController(TeamService teamService) {
        this.teamService = teamService;
    }

    @GetMapping
    public ApiResponse<List<Team>> list(
            @RequestParam(required = false) String group,
            @RequestParam(required = false) Continent continent,
            @RequestParam(required = false) String keyword) {
        if (keyword != null && !keyword.isBlank()) {
            return ApiResponse.ok(teamService.search(keyword));
        }
        if (group != null) {
            return ApiResponse.ok(teamService.findByGroup(group));
        }
        if (continent != null) {
            return ApiResponse.ok(teamService.findByContinent(continent));
        }
        return ApiResponse.ok(teamService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Team> getById(@PathVariable Long id) {
        return teamService.findById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error("球队不存在"));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Team> create(@RequestBody Team team) {
        return ApiResponse.ok("创建成功", teamService.create(team));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Team> update(@PathVariable Long id, @RequestBody Team team) {
        return ApiResponse.ok("更新成功", teamService.update(id, team));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> delete(@PathVariable Long id) {
        teamService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}