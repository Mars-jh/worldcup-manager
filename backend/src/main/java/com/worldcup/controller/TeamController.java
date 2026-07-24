package com.worldcup.controller;

import com.worldcup.model.*;
import com.worldcup.service.TeamService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

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
        List<Team> result = teamService.findAll();

        // 支持多条件组合筛选
        if (keyword != null && !keyword.isBlank()) {
            String kw = keyword.toLowerCase();
            result = result.stream()
                    .filter(t -> t.getName().toLowerCase().contains(kw) ||
                                 t.getCode().toLowerCase().contains(kw))
                    .collect(Collectors.toList());
        }
        if (group != null) {
            result = result.stream()
                    .filter(t -> group.equals(t.getGroupLetter()))
                    .collect(Collectors.toList());
        }
        if (continent != null) {
            result = result.stream()
                    .filter(t -> t.getContinent() == continent)
                    .collect(Collectors.toList());
        }
        return ApiResponse.ok(result);
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
