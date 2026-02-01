package net.cosette.columbina.team;

import net.minecraft.server.network.ServerPlayerEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;


public class TeamManager {

    private static final TeamManager INSTANCE = new TeamManager();

    // nom de la team -> points
    private final Map<String, Integer> teamPoints = new HashMap<>();

    // joueur -> nom de la team
    private final Map<UUID, String> playerTeams = new HashMap<>();

    private TeamManager() {}

    public static TeamManager getInstance() {
        return INSTANCE;
    }

    /* =========================
       TEAMS
       ========================= */

    public boolean createTeam(String name) {
        if (teamPoints.containsKey(name)) {
            return false;
        }
        teamPoints.put(name, 0);
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
        if (!teamPoints.containsKey(teamName)) {
            return false;
        }

        UUID uuid = player.getUuid();

        if (playerTeams.containsKey(uuid)) {
            return false;
        }

        playerTeams.put(uuid, teamName);
        return true;
    }

    public boolean leaveTeam(ServerPlayerEntity player) {
        return playerTeams.remove(player.getUuid()) != null;
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
        return true;
    }

    public int getPoints(String teamName) {
        return teamPoints.getOrDefault(teamName, 0);
    }
    public boolean setPoints(String teamName, int value) {
        if (!teamExists(teamName)) {
            return false;
        }
        teamPoints.put(teamName, value);
        return true;
    }
}