package com.worldcup.service;

import com.worldcup.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 淘汰赛服务 - 管理16强到决赛的对阵
 *
 * 淘汰赛对阵规则（FIFA标准）：
 * 16强赛:
 *   M1: A1 vs B2    M2: C1 vs D2    M3: E1 vs F2    M4: G1 vs H2
 *   M5: B1 vs A2    M6: D1 vs C2    M7: F1 vs E2    M8: H1 vs G2
 * 8强赛: M1胜 vs M2胜, M3胜 vs M4胜, M5胜 vs M6胜, M7胜 vs M8胜
 * 4强: QF1胜 vs QF2胜, QF3胜 vs QF4胜
 * 决赛: SF1胜 vs SF2胜
 * 季军赛: SF1负 vs SF2负
 */
@Service
public class KnockoutService {

    private final MatchService matchService;
    private final GroupStageService groupStageService;

    public KnockoutService(MatchService matchService, GroupStageService groupStageService) {
        this.matchService = matchService;
        this.groupStageService = groupStageService;
    }

    /**
     * 根据小组赛排名生成16强对阵
     * 返回创建的16强比赛列表
     */
    public List<Match> generateRoundOf16() {
        boolean allGroupMatchesComplete = groupStageService.getAllGroups().stream()
                .map(matchService::findByGroup)
                .allMatch(matches -> matches.size() == 6 && matches.stream()
                        .allMatch(match -> match.getStatus() == MatchStatus.COMPLETED));
        if (!allGroupMatchesComplete) {
            throw new IllegalArgumentException("所有小组赛完成后才能生成淘汰赛");
        }

        boolean knockoutStarted = getBracket().values().stream()
                .flatMap(Collection::stream)
                .anyMatch(match -> match.getStatus() == MatchStatus.COMPLETED);
        if (knockoutStarted) {
            throw new IllegalArgumentException("淘汰赛已经开始，不能重新生成对阵");
        }

        // 清除旧的淘汰赛
        matchService.findByStage("ROUND_OF_16").forEach(m -> matchService.delete(m.getId()));
        matchService.findByStage("QUARTER").forEach(m -> matchService.delete(m.getId()));
        matchService.findByStage("SEMI").forEach(m -> matchService.delete(m.getId()));
        matchService.findByStage("FINAL").forEach(m -> matchService.delete(m.getId()));
        matchService.findByStage("THIRD_PLACE").forEach(m -> matchService.delete(m.getId()));

        // 获取各组前两名
        Map<String, List<Team>> groupTop2 = new LinkedHashMap<>();
        for (String g : groupStageService.getAllGroups()) {
            groupTop2.put(g, groupStageService.getTopTwo(g));
        }

        // 16强对阵配对（FIFA标准交叉对阵）
        String[][] r16Pairs = {
            {"A", "B"}, {"C", "D"}, {"E", "F"}, {"G", "H"},  // 上半区: 1st vs 2nd
            {"B", "A"}, {"D", "C"}, {"F", "E"}, {"H", "G"}   // 下半区: 1st vs 2nd
        };

        List<Match> r16Matches = new ArrayList<>();
        for (int i = 0; i < r16Pairs.length; i++) {
            List<Team> g1 = groupTop2.get(r16Pairs[i][0]);
            List<Team> g2 = groupTop2.get(r16Pairs[i][1]);
            Long homeId = (g1 != null && g1.size() > 0) ? g1.get(0).getId() : null; // 小组第1
            Long awayId = (g2 != null && g2.size() > 1) ? g2.get(1).getId() : null; // 小组第2

            Match match = Match.builder()
                    .stage("ROUND_OF_16")
                    .homeTeamId(homeId)
                    .awayTeamId(awayId)
                    .status(MatchStatus.SCHEDULED)
                    .matchOrder(i + 1)
                    .build();
            r16Matches.add(matchService.create(match));
        }

        // 生成8强占位（待16强结束后填充）
        for (int i = 0; i < 4; i++) {
            matchService.create(Match.builder()
                    .stage("QUARTER")
                    .status(MatchStatus.SCHEDULED)
                    .matchOrder(i + 1)
                    .build());
        }

        // 生成4强占位
        for (int i = 0; i < 2; i++) {
            matchService.create(Match.builder()
                    .stage("SEMI")
                    .status(MatchStatus.SCHEDULED)
                    .matchOrder(i + 1)
                    .build());
        }

        // 决赛和季军赛占位
        matchService.create(Match.builder().stage("FINAL").status(MatchStatus.SCHEDULED).matchOrder(1).build());
        matchService.create(Match.builder().stage("THIRD_PLACE").status(MatchStatus.SCHEDULED).matchOrder(1).build());

        return r16Matches;
    }

    /** 录入淘汰赛比分，并保证后续对阵不会与修正后的胜者冲突。 */
    public Match recordScore(Long matchId, int homeGoals, int awayGoals) {
        Match match = matchService.findById(matchId)
                .orElseThrow(() -> new IllegalArgumentException("比赛不存在"));
        if (match.getHomeTeamId() == null || match.getAwayTeamId() == null) {
            throw new IllegalArgumentException("对阵双方尚未确定");
        }
        if (homeGoals == awayGoals) {
            throw new IllegalArgumentException("淘汰赛必须决出胜负");
        }

        Long previousWinner = completedWinner(match);
        Long newWinner = homeGoals > awayGoals ? match.getHomeTeamId() : match.getAwayTeamId();
        if (previousWinner != null && !previousWinner.equals(newWinner) && hasCompletedDependentMatch(match)) {
            throw new IllegalArgumentException("后续比赛已经完成，不能修改本场胜者");
        }

        Match updated = matchService.recordScore(matchId, homeGoals, awayGoals);
        advanceWinner(matchId);
        return updated;
    }

    /**
     * 晋级胜者到下一轮
     * 当一场比赛完成后，自动将胜者填入下一轮对阵
     */
    public void advanceWinner(Long matchId) {
        Match match = matchService.findById(matchId).orElseThrow();
        if (match.getStatus() != MatchStatus.COMPLETED) return;

        // 确定胜者
        Long winnerId;
        Long loserId;
        if (match.getHomeGoals() > match.getAwayGoals()) {
            winnerId = match.getHomeTeamId();
            loserId = match.getAwayTeamId();
        } else if (match.getAwayGoals() > match.getHomeGoals()) {
            winnerId = match.getAwayTeamId();
            loserId = match.getHomeTeamId();
        } else {
            return; // 平局不晋级（实际淘汰赛不存在平局，需加时/点球）
        }

        String stage = match.getStage();
        int order = match.getMatchOrder();

        // 根据当前阶段和序号，找到下一轮对应比赛
        String nextStage = "";
        int nextMatchIndex = 0;
        int slot = 0; // 0=主队位, 1=客队位

        switch (stage) {
            case "ROUND_OF_16":
                nextStage = "QUARTER";
                nextMatchIndex = (order - 1) / 2;
                slot = (order - 1) % 2;
                break;
            case "QUARTER":
                nextStage = "SEMI";
                nextMatchIndex = (order - 1) / 2;
                slot = (order - 1) % 2;
                break;
            case "SEMI":
                // 4强胜者进决赛，负者进季军赛
                slot = (order - 1) % 2;
                if (winnerId != null) {
                    advanceToSlot("FINAL", slot, winnerId);
                }
                if (loserId != null) {
                    advanceToSlot("THIRD_PLACE", slot, loserId);
                }
                return;
            default:
                return;
        }

        advanceToSlot(nextStage, nextMatchIndex, slot, winnerId);
    }

    private void clearMatchResult(Match match) {
        match.setHomeGoals(null);
        match.setAwayGoals(null);
        match.setStatus(MatchStatus.SCHEDULED);
    }

    private Long completedWinner(Match match) {
        if (match.getStatus() != MatchStatus.COMPLETED
                || match.getHomeGoals() == null || match.getAwayGoals() == null
                || match.getHomeGoals().equals(match.getAwayGoals())) {
            return null;
        }
        return match.getHomeGoals() > match.getAwayGoals()
                ? match.getHomeTeamId() : match.getAwayTeamId();
    }

    private boolean hasCompletedDependentMatch(Match match) {
        int dependentIndex = (match.getMatchOrder() - 1) / 2;
        return switch (match.getStage()) {
            case "ROUND_OF_16" -> isCompleted("QUARTER", dependentIndex);
            case "QUARTER" -> isCompleted("SEMI", dependentIndex);
            case "SEMI" -> isCompleted("FINAL", 0) || isCompleted("THIRD_PLACE", 0);
            default -> false;
        };
    }

    private boolean isCompleted(String stage, int matchIndex) {
        List<Match> matches = matchService.findByStage(stage);
        return matchIndex < matches.size() && matches.get(matchIndex).getStatus() == MatchStatus.COMPLETED;
    }

    private void advanceToSlot(String stage, int matchIndex, int slot, Long teamId) {
        List<Match> nextMatches = matchService.findByStage(stage);
        if (matchIndex < nextMatches.size()) {
            Match next = nextMatches.get(matchIndex);
            Long currentTeamId = slot == 0 ? next.getHomeTeamId() : next.getAwayTeamId();
            if (currentTeamId != null && !currentTeamId.equals(teamId)) {
                clearMatchResult(next);
            }
            if (slot == 0) next.setHomeTeamId(teamId);
            else next.setAwayTeamId(teamId);
            matchService.update(next.getId(), next);
        }
    }

    private void advanceToSlot(String stage, int slot, Long teamId) {
        List<Match> nextMatches = matchService.findByStage(stage);
        if (!nextMatches.isEmpty()) {
            Match next = nextMatches.get(0);
            Long currentTeamId = slot == 0 ? next.getHomeTeamId() : next.getAwayTeamId();
            if (currentTeamId != null && !currentTeamId.equals(teamId)) {
                clearMatchResult(next);
            }
            if (slot == 0) next.setHomeTeamId(teamId);
            else next.setAwayTeamId(teamId);
            matchService.update(next.getId(), next);
        }
    }

    /** 获取淘汰赛完整树结构 */
    public Map<String, List<Match>> getBracket() {
        Map<String, List<Match>> bracket = new LinkedHashMap<>();
        bracket.put("ROUND_OF_16", matchService.findByStage("ROUND_OF_16"));
        bracket.put("QUARTER", matchService.findByStage("QUARTER"));
        bracket.put("SEMI", matchService.findByStage("SEMI"));
        bracket.put("FINAL", matchService.findByStage("FINAL"));
        bracket.put("THIRD_PLACE", matchService.findByStage("THIRD_PLACE"));
        return bracket;
    }
}
