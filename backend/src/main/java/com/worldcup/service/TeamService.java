package com.worldcup.service;

import com.worldcup.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 球队服务 - 管理32支参赛球队
 * 支持按小组、大洲筛选
 */
@Service
public class TeamService {

    private final Map<Long, Team> teams = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    /** 新增球队 */
    public Team create(Team team) {
        team.setId(idGen.getAndIncrement());
        team.setCreatedAt(LocalDateTime.now());
        teams.put(team.getId(), team);
        return team;
    }

    /** 更新球队 */
    public Team update(Long id, Team updated) {
        Team team = teams.get(id);
        if (team == null) throw new IllegalArgumentException("球队不存在");
        if (updated.getName() != null) team.setName(updated.getName());
        if (updated.getCode() != null) team.setCode(updated.getCode());
        if (updated.getContinent() != null) team.setContinent(updated.getContinent());
        if (updated.getGroupLetter() != null) team.setGroupLetter(updated.getGroupLetter());
        if (updated.getCoach() != null) team.setCoach(updated.getCoach());
        if (updated.getFlagEmoji() != null) team.setFlagEmoji(updated.getFlagEmoji());
        if (updated.getWorldRanking() > 0) team.setWorldRanking(updated.getWorldRanking());
        return team;
    }

    /** 删除球队 */
    public void delete(Long id) {
        teams.remove(id);
    }

    /** 根据ID获取 */
    public Optional<Team> findById(Long id) {
        return Optional.ofNullable(teams.get(id));
    }

    /** 获取所有球队 */
    public List<Team> findAll() {
        return new ArrayList<>(teams.values());
    }

    /** 按小组筛选 */
    public List<Team> findByGroup(String groupLetter) {
        return teams.values().stream()
                .filter(t -> groupLetter.equals(t.getGroupLetter()))
                .sorted(Comparator.comparing(Team::getName))
                .collect(Collectors.toList());
    }

    /** 按大洲筛选 */
    public List<Team> findByContinent(Continent continent) {
        return teams.values().stream()
                .filter(t -> t.getContinent() == continent)
                .collect(Collectors.toList());
    }

    /** 搜索球队 */
    public List<Team> search(String keyword) {
        String kw = keyword.toLowerCase();
        return teams.values().stream()
                .filter(t -> t.getName().toLowerCase().contains(kw) ||
                             t.getCode().toLowerCase().contains(kw))
                .collect(Collectors.toList());
    }

    /** 更新小组赛统计数据（由 GroupStageService 调用） */
    public void updateGroupStats(Long teamId, int played, int won, int drawn, int lost,
                                  int goalsFor, int goalsAgainst, int points) {
        Team team = teams.get(teamId);
        if (team != null) {
            team.setPlayed(played);
            team.setWon(won);
            team.setDrawn(drawn);
            team.setLost(lost);
            team.setGoalsFor(goalsFor);
            team.setGoalsAgainst(goalsAgainst);
            team.setGoalDifference(goalsFor - goalsAgainst);
            team.setPoints(points);
        }
    }
}