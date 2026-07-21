package com.worldcup.controller;

import com.worldcup.model.*;
import com.worldcup.service.KnockoutService;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/** 淘汰赛控制器 - 查看对阵树 */
@RestController
@RequestMapping("/api/knockout")
public class KnockoutController {

    private final KnockoutService knockoutService;

    public KnockoutController(KnockoutService knockoutService) {
        this.knockoutService = knockoutService;
    }

    /** 获取完整淘汰赛对阵树 */
    @GetMapping("/bracket")
    public ApiResponse<Map<String, List<Match>>> bracket() {
        return ApiResponse.ok(knockoutService.getBracket());
    }
}