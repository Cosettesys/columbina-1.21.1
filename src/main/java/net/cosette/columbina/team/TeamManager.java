package net.cosette.columbina.team;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;

import java.util.*;
import java.util.stream.Collectors;

public class TeamManager {
    private static final TeamManager INSTANCE = new TeamManager();

    // nom de la team -> points
    private final Map<String, Integer> teamPoints = new HashMap<>();
    // joueur -> nom de la team
    private final Map<UUID, String> playerTeams = new HashMap<>();

    private ServerWorld world;
    private TeamManager() {}
    public static TeamManager getInstance() {
        return INSTANCE;
    }

    /* =========================
       INITIALISATION / SAVE
       ========================= */

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
    }
    private void save() {
        if (world == null) return;

        TeamSavedData data = TeamSavedData.get(world);

        data.getTeamPoints().clear();
        data.getTeamPoints().putAll(teamPoints);

        data.getPlayerTeams().clear();
        data.getPlayerTeams().putAll(playerTeams);

        data.markDirty();
    }
    /* =========================
       TEAMS
       ========================= */
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
    /* =========================
       JOUEURS
       ========================= */
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

    /* =========================
       GESTION DES POINTS
       ========================= */

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

    /* =========================
       UTILITAIRES
       ========================= */

    /**
     * Retourne l'ensemble de tous les noms d'équipes
     */
    public Set<String> getAllTeams() {
        return new HashSet<>(teamPoints.keySet());
    }

    /**
     * Retourne la liste des UUIDs des joueurs membres d'une équipe
     */
    public List<UUID> getTeamMembers(String teamName) {
        if (!teamExists(teamName)) {
            return new ArrayList<>();
        }
        return playerTeams.entrySet().stream()
                .filter(entry -> teamName.equals(entry.getValue()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}