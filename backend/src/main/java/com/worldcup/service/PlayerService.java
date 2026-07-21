package com.worldcup.service;

import com.worldcup.model.*;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

/**
 * 球员服务 - 管理球员档案
 * 支持按球队、位置筛选
 */
@Service
public class PlayerService {

    private final Map<Long, Player> players = new ConcurrentHashMap<>();
    private final AtomicLong idGen = new AtomicLong(1);

    public Player create(Player player) {
        player.setId(idGen.getAndIncrement());
        players.put(player.getId(), player);
        return player;
    }

    public Player update(Long id, Player updated) {
        Player player = players.get(id);
        if (player == null) throw new IllegalArgumentException("球员不存在");
        if (updated.getName() != null) player.setName(updated.getName());
        if (updated.getTeamId() != null) player.setTeamId(updated.getTeamId());
        if (updated.getPosition() != null) player.setPosition(updated.getPosition());
        if (updated.getJerseyNumber() > 0) player.setJerseyNumber(updated.getJerseyNumber());
        if (updated.getAge() > 0) player.setAge(updated.getAge());
        if (updated.getHeight() > 0) player.setHeight(updated.getHeight());
        if (updated.getWeight() > 0) player.setWeight(updated.getWeight());
        if (updated.getRating() > 0) player.setRating(updated.getRating());
        return player;
    }

    public void delete(Long id) {
        players.remove(id);
    }

    public Optional<Player> findById(Long id) {
        return Optional.ofNullable(players.get(id));
    }

    public List<Player> findAll() {
        return new ArrayList<>(players.values());
    }

    /** 按球队筛选 */
    public List<Player> findByTeam(Long teamId) {
        return players.values().stream()
                .filter(p -> teamId.equals(p.getTeamId()))
                .sorted(Comparator.comparingInt(Player::getJerseyNumber))
                .collect(Collectors.toList());
    }

    /** 按位置筛选 */
    public List<Player> findByPosition(Position position) {
        return players.values().stream()
                .filter(p -> p.getPosition() == position)
                .collect(Collectors.toList());
    }

    /** 按球队和位置筛选 */
    public List<Player> findByTeamAndPosition(Long teamId, Position position) {
        return players.values().stream()
                .filter(p -> teamId.equals(p.getTeamId()) && p.getPosition() == position)
                .collect(Collectors.toList());
    }

    /** 射手榜（按进球数排序） */
    public List<Player> getTopScorers(int limit) {
        return players.values().stream()
                .filter(p -> p.getGoals() > 0)
                .sorted(Comparator.comparingInt(Player::getGoals).reversed()
                        .thenComparingInt(Player::getAssists).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /** 助攻榜 */
    public List<Player> getTopAssists(int limit) {
        return players.values().stream()
                .filter(p -> p.getAssists() > 0)
                .sorted(Comparator.comparingInt(Player::getAssists).reversed()
                        .thenComparingInt(Player::getGoals).reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    /** 增加进球数 */
    public void addGoals(Long playerId, int goals) {
        Player p = players.get(playerId);
        if (p != null) p.setGoals(p.getGoals() + goals);
    }

    /** 增加助攻数 */
    public void addAssists(Long playerId, int assists) {
        Player p = players.get(playerId);
        if (p != null) p.setAssists(p.getAssists() + assists);
    }
}