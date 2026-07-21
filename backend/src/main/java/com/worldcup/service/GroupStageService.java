package com.worldcup.service;

import com.worldcup.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 小组赛服务 - 核心业务逻辑
 * 1. 生成小组赛赛程（每组4队单循环，共6场）
 * 2. 计算积分榜（胜3平1负0 + 净胜球 + 进球数排序）
 * 3. 获取各组前两名（晋级淘汰赛）
 */
@Service
public class GroupStageService {

    private final TeamService teamService;
    private final MatchService matchService;

    public GroupStageService(TeamService teamService, MatchService matchService) {
        this.teamService = teamService;
        this.matchService = matchService;
    }

    /**
     * 生成小组赛赛程
     * 每组4队（编号0,1,2,3），单循环共6场：
     *   第1轮: 0v3, 1v2
     *   第2轮: 0v2, 1v3
     *   第3轮: 0v1, 2v3
     */
    public List<Match> generateGroupSchedule(String groupLetter) {
        List<Team> teams = teamService.findByGroup(groupLetter);
        if (teams.size() != 4) {
            throw new IllegalArgumentException("小组 " + groupLetter + " 必须有4支球队，当前: " + teams.size());
        }

        // 对阵组合（固定赛程模板）
        int[][] fixtures = {
            {0, 3}, {1, 2},  // 第1轮
            {0, 2}, {1, 3},  // 第2轮
            {0, 1}, {2, 3}   // 第3轮
        };

        List<Match> created = new ArrayList<>();
        for (int i = 0; i < fixtures.length; i++) {
            Team home = teams.get(fixtures[i][0]);
            Team away = teams.get(fixtures[i][1]);
            Match match = Match.builder()
                    .stage("GROUP")
                    .groupLetter(groupLetter)
                    .homeTeamId(home.getId())
                    .awayTeamId(away.getId())
                    .status(MatchStatus.SCHEDULED)
                    .matchOrder(i + 1)
                    .build();
            created.add(matchService.create(match));
        }
        return created;
    }

    /**
     * 计算小组积分榜
     * 排名规则：1.积分 2.净胜球 3.进球数 4.球队名称
     */
    public List<Team> getStandings(String groupLetter) {
        List<Team> teams = teamService.findByGroup(groupLetter);
        List<Match> matches = matchService.findGroupMatches(groupLetter);

        // 重置所有球队统计
        for (Team team : teams) {
            teamService.updateGroupStats(team.getId(), 0, 0, 0, 0, 0, 0, 0);
        }

        // 遍历已完赛的比赛，累计积分
        for (Match match : matches) {
            Team home = findTeamById(teams, match.getHomeTeamId());
            Team away = findTeamById(teams, match.getAwayTeamId());
            if (home == null || away == null) continue;

            int hg = match.getHomeGoals();
            int ag = match.getAwayGoals();

            // 更新主队统计
            home.setPlayed(home.getPlayed() + 1);
            home.setGoalsFor(home.getGoalsFor() + hg);
            home.setGoalsAgainst(home.getGoalsAgainst() + ag);

            // 更新客队统计
            away.setPlayed(away.getPlayed() + 1);
            away.setGoalsFor(away.getGoalsFor() + ag);
            away.setGoalsAgainst(away.getGoalsAgainst() + hg);

            if (hg > ag) {
                // 主队胜
                home.setWon(home.getWon() + 1);
                home.setPoints(home.getPoints() + 3);
                away.setLost(away.getLost() + 1);
            } else if (hg < ag) {
                // 客队胜
                away.setWon(away.getWon() + 1);
                away.setPoints(away.getPoints() + 3);
                home.setLost(home.getLost() + 1);
            } else {
                // 平局
                home.setDrawn(home.getDrawn() + 1);
                home.setPoints(home.getPoints() + 1);
                away.setDrawn(away.getDrawn() + 1);
                away.setPoints(away.getPoints() + 1);
            }

            // 更新到存储
            teamService.updateGroupStats(home.getId(), home.getPlayed(), home.getWon(),
                    home.getDrawn(), home.getLost(), home.getGoalsFor(), home.getGoalsAgainst(), home.getPoints());
            teamService.updateGroupStats(away.getId(), away.getPlayed(), away.getWon(),
                    away.getDrawn(), away.getLost(), away.getGoalsFor(), away.getGoalsAgainst(), away.getPoints());
        }

        // 重新获取并排序
        List<Team> result = teamService.findByGroup(groupLetter);
        result.forEach(t -> {
            t.setGoalDifference(t.getGoalsFor() - t.getGoalsAgainst());
        });
        result.sort(Comparator
                .comparingInt(Team::getPoints).reversed()
                .thenComparing(Comparator.comparingInt(Team::getGoalDifference).reversed())
                .thenComparing(Comparator.comparingInt(Team::getGoalsFor).reversed())
                .thenComparing(Team::getName));
        return result;
    }

    /** 获取小组前两名（晋级淘汰赛） */
    public List<Team> getTopTwo(String groupLetter) {
        List<Team> standings = getStandings(groupLetter);
        return standings.size() >= 2 ? standings.subList(0, 2) : standings;
    }

    /** 获取所有小组名称 */
    public List<String> getAllGroups() {
        return Arrays.asList("A", "B", "C", "D", "E", "F", "G", "H");
    }

    private Team findTeamById(List<Team> teams, Long id) {
        return teams.stream().filter(t -> t.getId().equals(id)).findFirst().orElse(null);
    }
}