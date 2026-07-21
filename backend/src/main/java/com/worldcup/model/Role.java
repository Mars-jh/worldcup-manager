package com.worldcup.model;

/** 用户角色枚举 */
public enum Role {
    ADMIN,      // 管理员：全部权限
    OPERATOR,   // 操作员：可编辑比分和赛程
    VIEWER      // 观众：只读权限
}