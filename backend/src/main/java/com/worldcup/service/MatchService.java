package com.worldcup.service;

import com.worldcup.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 比赛服务 - 管理所有比赛（小组赛 + 淘汰赛）
 * 负责创建赛程、录入比分、查询比赛
 */
@Service
public class MatchService {

    private final Map<Long, Match> matches = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public Match create(Match match) {
        match.setId(idGen.getAndIncrement());
        if (match.getStatus() == null) match.setStatus(MatchStatus.SCHEDULED);
        matches.put(match.getId(), match);
        return match;
    }

    public Match update(Long id, Match updated) {
        Match match = matches.get(id);
        if (match == null) throw new IllegalArgumentException("比赛不存在");
        if (updated.getHomeTeamId() != null) match.setHomeTeamId(updated.getHomeTeamId());
        if (updated.getAwayTeamId() != null) match.setAwayTeamId(updated.getAwayTeamId());
        if (updated.getMatchTime() != null) match.setMatchTime(updated.getMatchTime());
        return match;
    }

    public void delete(Long id) {
        matches.remove(id);
    }

    public Optional<Match> findById(Long id) {
        return Optional.ofNullable(matches.get(id));
    }

    public List<Match> findAll() {
        return matches.values().stream()
                .sorted(Comparator.comparingInt(Match::getMatchOrder))
                .collect(Collectors.toList());
    }

    /** 按阶段查询比赛 */
    public List<Match> findByStage(String stage) {
        return matches.values().stream()
                .filter(m -> stage.equals(m.getStage()))
                .sorted(Comparator.comparingInt(Match::getMatchOrder))
                .collect(Collectors.toList());
    }

    /** 按小组查询比赛 */
    public List<Match> findByGroup(String groupLetter) {
        return matches.values().stream()
                .filter(m -> "GROUP".equals(m.getStage()) && groupLetter.equals(m.getGroupLetter()))
                .sorted(Comparator.comparingInt(Match::getMatchOrder))
                .collect(Collectors.toList());
    }

    /** 查询某队的比赛 */
    public List<Match> findByTeam(Long teamId) {
        return matches.values().stream()
                .filter(m -> teamId.equals(m.getHomeTeamId()) || teamId.equals(m.getAwayTeamId()))
                .sorted(Comparator.comparingInt(Match::getMatchOrder))
                .collect(Collectors.toList());
    }

    /** 按状态查询 */
    public List<Match> findByStatus(MatchStatus status) {
        return matches.values().stream()
                .filter(m -> m.getStatus() == status)
                .collect(Collectors.toList());
    }

    /** 统计已完赛场次 */
    public long countCompleted() {
        return matches.values().stream()
                .filter(m -> m.getStatus() == MatchStatus.COMPLETED)
                .count();
    }

    /** 录入比分并标记为已完赛 */
    public Match recordScore(Long matchId, int homeGoals, int awayGoals) {
        if (matchId == null) throw new IllegalArgumentException("比赛ID不能为空");
        if (homeGoals < 0 || homeGoals > 99 || awayGoals < 0 || awayGoals > 99) {
            throw new IllegalArgumentException("比分必须在0到99之间");
        }
        Match match = matches.get(matchId);
        if (match == null) throw new IllegalArgumentException("比赛不存在");
        match.setHomeGoals(homeGoals);
        match.setAwayGoals(awayGoals);
        match.setStatus(MatchStatus.COMPLETED);
        return match;
    }

    /** 获取某队在该小组的对手比赛（用于积分计算） */
    public List<Match> findGroupMatches(String groupLetter) {
        return matches.values().stream()
                .filter(m -> "GROUP".equals(m.getStage()) &&
                             groupLetter.equals(m.getGroupLetter()) &&
                             m.getStatus() == MatchStatus.COMPLETED)
                .collect(Collectors.toList());
    }
}
