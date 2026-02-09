package net.cosette.columbina.team;

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


    public boolean createTeam(String name) {
        if (teamPoints.containsKey(name)) {
            return false;
        }
        teamPoints.put(name, 0);
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
        save();
        return true;
    }
    public boolean leaveTeam(ServerPlayerEntity player) {
        boolean removed = playerTeams.remove(player.getUuid()) != null;
        if (removed) {
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

    /* =========================
       GESTION DES COULEURS
       ========================= */

    /**
     * Définit la couleur d'une équipe
     */
    public boolean setTeamColor(String teamName, Formatting color) {
        if (!teamExists(teamName)) {
            return false;
        }
        teamColors.put(teamName, color);
        save();
        return true;
    }

    /**
     * Récupère la couleur d'une équipe (WHITE par défaut)
     */
    public Formatting getTeamColor(String teamName) {
        return teamColors.getOrDefault(teamName, Formatting.WHITE);
    }

    /**
     * Récupère le code couleur Minecraft d'une équipe (§ format)
     */
    public String getTeamColorCode(String teamName) {
        Formatting color = getTeamColor(teamName);
        Integer colorIndex = color.getColorIndex();
        if (colorIndex == null) {
            return "§f"; // Blanc par défaut si pas de couleur
        }
        return "§" + Integer.toHexString(colorIndex).toLowerCase();
    }
}