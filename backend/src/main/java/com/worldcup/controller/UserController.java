package com.worldcup.controller;

import com.worldcup.model.*;
import com.worldcup.service.UserService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户管理控制器 - 仅 ADMIN 可访问
 * 注意：返回用户信息时脱敏（不返回密码）
 */
@RestController
@RequestMapping("/api/users")
@PreAuthorize("hasRole('ADMIN')")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public ApiResponse<List<Map<String, Object>>> list() {
        List<Map<String, Object>> result = userService.findAll().stream()
                .map(this::toSafeMap)
                .collect(Collectors.toList());
        return ApiResponse.ok(result);
    }

    @PutMapping("/{id}")
    public ApiResponse<Map<String, Object>> update(@PathVariable Long id, @RequestBody User user) {
        User updated = userService.update(id, user);
        return ApiResponse.ok("更新成功", toSafeMap(updated));
    }

    @PutMapping("/{id}/reset-password")
    public ApiResponse<?> resetPassword(@PathVariable Long id, @RequestBody Map<String, String> body) {
        String newPassword = body.get("newPassword");
        if (newPassword == null || newPassword.length() < 6) {
            return ApiResponse.error("密码长度不能少于6位");
        }
        userService.resetPassword(id, newPassword);
        return ApiResponse.ok("密码已重置", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<?> delete(@PathVariable Long id) {
        userService.delete(id);
        return ApiResponse.ok("删除成功", null);
    }

    /** 脱敏：不返回密码字段 */
    private Map<String, Object> toSafeMap(User user) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", user.getId());
        map.put("username", user.getUsername());
        map.put("email", user.getEmail());
        map.put("role", user.getRole());
        map.put("enabled", user.isEnabled());
        map.put("createdAt", user.getCreatedAt());
        return map;
    }
}