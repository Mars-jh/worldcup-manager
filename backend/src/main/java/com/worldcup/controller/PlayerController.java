package com.worldcup.controller;

import com.worldcup.model.*;
import com.worldcup.service.PlayerService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** 球员控制器 - 球员 CRUD */
@RestController
@RequestMapping("/api/players")
public class PlayerController {

    private final PlayerService playerService;

    public PlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @GetMapping
    public ApiResponse<List<Player>> list(
            @RequestParam(required = false) Long teamId,
            @RequestParam(required = false) Position position) {
        if (teamId != null && position != null) {
            return ApiResponse.ok(playerService.findByTeamAndPosition(teamId, position));
        }
        if (teamId != null) {
            return ApiResponse.ok(playerService.findByTeam(teamId));
        }
        if (position != null) {
            return ApiResponse.ok(playerService.findByPosition(position));
        }
        return ApiResponse.ok(playerService.findAll());
    }

    @GetMapping("/{id}")
    public ApiResponse<Player> getById(@PathVariable Long id) {
        return playerService.findById(id)
                .map(ApiResponse::ok)
                .orElse(ApiResponse.error("球员不存在"));
    }

    @GetMapping("/top-scorers")
    public ApiResponse<List<Player>> topScorers(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.ok(playerService.getTopScorers(limit));
    }

    @GetMapping("/top-assists")
    public ApiResponse<List<Player>> topAssists(@RequestParam(defaultValue = "10") int limit) {
        return ApiResponse.ok(playerService.getTopAssists(limit));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<Player> create(@RequestBody Player player) {
        return ApiResponse.ok("创建成功", playerService.create(player));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'OPERATOR')")
    public ApiResponse<Player> update(@PathVariable Long id, @RequestBody Player player) {
        return ApiResponse.ok("更新成功", playerService.update(id, player));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ApiResponse<?> delete(@PathVariable Long id) {
        playerService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }
}