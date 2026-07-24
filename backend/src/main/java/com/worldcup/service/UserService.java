package com.worldcup.service;

import com.worldcup.model.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 用户服务 - 管理用户注册、登录、CRUD
 * 使用 ConcurrentHashMap 内存存储，预留 JPA 替换接口
 */
@Service
public class UserService {

    private final Map<Long, User> users = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);
    private final PasswordEncoder passwordEncoder;

    public UserService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    /** 注册新用户 */
    public User register(String username, String password, String email, Role role) {
        // 检查用户名是否重复
        if (users.values().stream().anyMatch(u -> u.getUsername().equals(username))) {
            throw new IllegalArgumentException("用户名已存在: " + username);
        }
        if (password.length() < 6) {
            throw new IllegalArgumentException("密码长度不能少于6位");
        }
        User user = User.builder()
                .id(idGen.getAndIncrement())
                .username(username)
                .password(passwordEncoder.encode(password))
                .email(email)
                .role(role != null ? role : Role.VIEWER)
                .enabled(true)
                .createdAt(LocalDateTime.now())
                .build();
        users.put(user.getId(), user);
        return user;
    }

    /** 验证登录 - 返回用户对象或 null */
    public User authenticate(String username, String password) {
        return users.values().stream()
                .filter(u -> u.getUsername().equals(username) && u.isEnabled())
                .filter(u -> passwordEncoder.matches(password, u.getPassword()))
                .findFirst()
                .orElse(null);
    }

    /** 根据用户名查找 */
    public Optional<User> findByUsername(String username) {
        return users.values().stream()
                .filter(u -> u.getUsername().equals(username))
                .findFirst();
    }

    /** 获取所有用户 */
    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    /** 更新用户 */
    public User update(Long id, User updated) {
        User user = users.get(id);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        if (updated.getEmail() != null) user.setEmail(updated.getEmail());
        if (updated.getRole() != null) user.setRole(updated.getRole());
        user.setEnabled(updated.isEnabled());
        return user;
    }

    /** 重置密码 */
    public void resetPassword(Long id, String newPassword) {
        User user = users.get(id);
        if (user == null) throw new IllegalArgumentException("用户不存在");
        if (newPassword == null || newPassword.length() < 6) throw new IllegalArgumentException("密码长度不能少于6位");
        user.setPassword(passwordEncoder.encode(newPassword));
    }

    /** 删除用户 */
    public void delete(Long id) {
        users.remove(id);
    }
}