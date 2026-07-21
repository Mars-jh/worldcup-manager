package com.worldcup.model;

import lombok.*;
import java.time.LocalDateTime;

/** 用户实体 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    private Long id;
    private String username;
    private String password;    // BCrypt加密存储
    private String email;
    private Role role;
    private boolean enabled;
    private LocalDateTime createdAt;
}