package net.cosette.columbina.team;

import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.Team;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import java.util.*;
import java.util.stream.Collectors;

public class TeamManager {
    private static final TeamManager INSTANCE = new TeamManager();
    private final Map<String, Integer> teamPoints = new HashMap<>();
    private final Map<UUID, String> playerTeams = new HashMap<>();
    private final Map<String, Formatting> teamColors = new HashMap<>();
    private ServerWorld world;
    private TeamManager() {}
    public static TeamManager getInstance() {
        return INSTANCE;
    }
    public void init(ServerWorld world) {
        this.world = world;
        load();
        syncAllTeamsToScoreboard();
    }
    private void load() {
        TeamSavedData data = TeamSavedData.get(world);
        teamPoints.clear();
        teamPoints.putAll(data.getTeamPoints());
        playerTeams.clear();
        playerTeams.putAll(data.getPlayerTeams());
        teamColors.clear();
        teamColors.putAll(data.getTeamColors());
    }
    private void save() {
        if (world == null) return;
        TeamSavedData data = TeamSavedData.get(world);
        data.getTeamPoints().clear();
        data.getTeamPoints().putAll(teamPoints);
        data.getPlayerTeams().clear();
        data.getPlayerTeams().putAll(playerTeams);
        data.getTeamColors().clear();
        data.getTeamColors().putAll(teamColors);
        data.markDirty();
    }
    private void syncAllTeamsToScoreboard() {
        Scoreboard scoreboard = world.getScoreboard();

        for (String teamName : teamPoints.keySet()) {
            getOrCreateScoreboardTeam(scoreboard, teamName);
        }
        for (Map.Entry<UUID, String> entry : playerTeams.entrySet()) {
            ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(entry.getKey());
            if (player != null) {
                addPlayerToScoreboardTeam(player, entry.getValue());
            }
        }
    }
    private Team getOrCreateScoreboardTeam(Scoreboard scoreboard, String teamName) {
        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.addTeam(teamName);
        }
        Formatting color = getTeamColor(teamName);
        team.setColor(color);
        return team;
    }
    private void addPlayerToScoreboardTeam(ServerPlayerEntity player, String teamName) {
        if (world == null) return;
        Scoreboard scoreboard = world.getScoreboard();
        Team team = getOrCreateScoreboardTeam(scoreboard, teamName);
        scoreboard.addScoreHolderToTeam(player.getNameForScoreboard(), team);
    }
    private void removePlayerFromScoreboardTeam(ServerPlayerEntity player) {
        if (world == null) return;
        Scoreboard scoreboard = world.getScoreboard();
        scoreboard.clearTeam(player.getNameForScoreboard());
    }
    public boolean createTeam(String name) {
        if (teamPoints.containsKey(name)) {
            return false;
        }
        teamPoints.put(name, 0);
        if (world != null) {
            getOrCreateScoreboardTeam(world.getScoreboard(), name);
        }
        save();
        return true;
    }
    public boolean teamExists(String name) {
        return teamPoints.containsKey(name);
    }
    public boolean isPlayerInTeam(ServerPlayerEntity player) {
        return playerTeams.containsKey(player.getUuid());
    }
    public boolean joinTeam(ServerPlayerEntity player, String teamName) {
        if (!teamExists(teamName)) {
            return false;
        }
        UUID uuid = player.getUuid();
        if (playerTeams.containsKey(uuid)) {
            return false;
        }
        playerTeams.put(uuid, teamName);
        addPlayerToScoreboardTeam(player, teamName);
        save();
        return true;
    }
    public boolean leaveTeam(ServerPlayerEntity player) {
        boolean removed = playerTeams.remove(player.getUuid()) != null;
        if (removed) {
            removePlayerFromScoreboardTeam(player);
            save();
        }
        return removed;
    }
    public String getPlayerTeam(ServerPlayerEntity player) {
        return playerTeams.get(player.getUuid());
    }
    public boolean addPoints(String teamName, int value) {
        if (!teamExists(teamName)) {
            return false;
        }
        teamPoints.put(teamName, getPoints(teamName) + value);
        save();
        return true;
    }
    public boolean setPoints(String teamName, int value) {
        if (!teamExists(teamName)) {
            return false;
        }
        teamPoints.put(teamName, value);
        save();
        return true;
    }
    public int getPoints(String teamName) {
        return teamPoints.getOrDefault(teamName, 0);
    }
    public Set<String> getAllTeams() {
        return new HashSet<>(teamPoints.keySet());
    }
    public List<UUID> getTeamMembers(String teamName) {
        if (!teamExists(teamName)) {
            return new ArrayList<>();
        }
        return playerTeams.entrySet().stream()
                .filter(entry -> teamName.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
    public boolean setTeamColor(String teamName, Formatting color) {
        if (!teamExists(teamName)) {
            return false;
        }
        teamColors.put(teamName, color);
        if (world != null) {
            Scoreboard scoreboard = world.getScoreboard();
            Team team = scoreboard.getTeam(teamName);
            if (team != null) {
                team.setColor(color);
            }
        }
        save();
        return true;
    }
    public Formatting getTeamColor(String teamName) {
        return teamColors.getOrDefault(teamName, Formatting.WHITE);
    }
    public String getTeamColorCode(String teamName) {
        Formatting color = getTeamColor(teamName);
        Integer colorIndex = color.getColorIndex();
        if (colorIndex == null) {
            return "§f"; // Blanc si pas de couleur
        }
        return "§" + Integer.toHexString(colorIndex).toLowerCase();
    }
}