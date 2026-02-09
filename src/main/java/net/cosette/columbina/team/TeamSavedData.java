package net.cosette.columbina.team;

import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.*;

public class TeamSavedData extends PersistentState {

    public static final String NAME = "columbina_teams";

    private final Map<String, Integer> teamPoints = new HashMap<>();
    private final Map<UUID, String> playerTeams = new HashMap<>();


    public static TeamSavedData fromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        TeamSavedData data = new TeamSavedData();
        NbtCompound teamsTag = tag.getCompound("Teams");
        for (String team : teamsTag.getKeys()) {
            data.teamPoints.put(team, teamsTag.getInt(team));
        }
        NbtCompound playersTag = tag.getCompound("Players");
        for (String uuid : playersTag.getKeys()) {
            data.playerTeams.put(UUID.fromString(uuid), playersTag.getString(uuid));
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        NbtCompound teamsTag = new NbtCompound();
        teamPoints.forEach(teamsTag::putInt);

        NbtCompound playersTag = new NbtCompound();
        playerTeams.forEach((uuid, team) ->
                playersTag.putString(uuid.toString(), team)
        );

        tag.put("Teams", teamsTag);
        tag.put("Players", playersTag);

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
}
