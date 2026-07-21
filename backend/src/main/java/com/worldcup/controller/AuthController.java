package com.worldcup.controller;

import com.worldcup.config.JwtUtil;
import com.worldcup.model.*;
import com.worldcup.service.UserService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * 认证控制器 - 处理用户登录和注册
 * 所有 /api/auth/** 路径不需要 JWT 认证
 */
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    /** 用户登录 */
    @PostMapping("/login")
    public ApiResponse<?> login(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");

        User user = userService.authenticate(username, password);
        if (user == null) {
            return ApiResponse.error("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());
        data.put("role", user.getRole());
        data.put("userId", user.getId());
        return ApiResponse.ok("登录成功", data);
    }

    /** 用户注册 */
    @PostMapping("/register")
    public ApiResponse<?> register(@RequestBody Map<String, String> body) {
        String username = body.get("username");
        String password = body.get("password");
        String email = body.get("email");

        if (username == null || username.isBlank()) {
            return ApiResponse.error("用户名不能为空");
        }
        if (password == null || password.length() < 6) {
            return ApiResponse.error("密码长度不能少于6位");
        }

        User user = userService.register(username, password, email, Role.VIEWER);
        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("token", token);
        data.put("username", user.getUsername());
        data.put("role", user.getRole());
        data.put("userId", user.getId());
        return ApiResponse.ok("注册成功", data);
    }

    /** 获取当前用户信息 */
    @GetMapping("/me")
    public ApiResponse<?> me(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        String username = jwtUtil.getUsername(token);
        return userService.findByUsername(username)
                .map(user -> {
                    Map<String, Object> data = new LinkedHashMap<>();
                    data.put("id", user.getId());
                    data.put("username", user.getUsername());
                    data.put("email", user.getEmail());
                    data.put("role", user.getRole());
                    return ApiResponse.ok(data);
                })
                .orElse(ApiResponse.error("用户不存在"));
    }
}