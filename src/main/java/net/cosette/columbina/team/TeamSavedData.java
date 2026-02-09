package net.cosette.columbina.team;

import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Formatting;
import net.minecraft.world.PersistentState;

import java.util.*;

public class TeamSavedData extends PersistentState {

    public static final String NAME = "columbina_teams";

    private final Map<String, Integer> teamPoints = new HashMap<>();
    private final Map<UUID, String> playerTeams = new HashMap<>();
    private final Map<String, Formatting> teamColors = new HashMap<>();


    public static TeamSavedData fromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        TeamSavedData data = new TeamSavedData();

        // Charger les équipes et points
        NbtCompound teamsTag = tag.getCompound("Teams");
        for (String team : teamsTag.getKeys()) {
            data.teamPoints.put(team, teamsTag.getInt(team));
        }

        // Charger les joueurs
        NbtCompound playersTag = tag.getCompound("Players");
        for (String uuid : playersTag.getKeys()) {
            data.playerTeams.put(UUID.fromString(uuid), playersTag.getString(uuid));
        }

        // Charger les couleurs
        if (tag.contains("TeamColors")) {
            NbtCompound colorsCompound = tag.getCompound("TeamColors");
            for (String key : colorsCompound.getKeys()) {
                String colorName = colorsCompound.getString(key);
                Formatting color = Formatting.byName(colorName);
                if (color != null) {
                    data.teamColors.put(key, color);
                }
            }
        }

        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        // Sauvegarder les équipes et points
        NbtCompound teamsTag = new NbtCompound();
        teamPoints.forEach(teamsTag::putInt);

        // Sauvegarder les joueurs
        NbtCompound playersTag = new NbtCompound();
        playerTeams.forEach((uuid, team) ->
                playersTag.putString(uuid.toString(), team)
        );

        // Sauvegarder les couleurs
        NbtCompound colorsCompound = new NbtCompound();
        for (Map.Entry<String, Formatting> entry : teamColors.entrySet()) {
            colorsCompound.putString(entry.getKey(), entry.getValue().getName());
        }

        tag.put("Teams", teamsTag);
        tag.put("Players", playersTag);
        tag.put("TeamColors", colorsCompound);

        return tag;
    }


    public static TeamSavedData get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                new PersistentState.Type<>(
                        TeamSavedData::new,
                        TeamSavedData::fromNbt,
                        null
                ),
                NAME
        );
    }


    public Map<String, Integer> getTeamPoints() {
        return teamPoints;
    }
    public Map<UUID, String> getPlayerTeams() {
        return playerTeams;
    }
    public Map<String, Formatting> getTeamColors() {
        return teamColors;
    }
}