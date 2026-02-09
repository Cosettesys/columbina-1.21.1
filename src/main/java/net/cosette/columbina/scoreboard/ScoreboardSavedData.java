package net.cosette.columbina.scoreboard;

import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import java.util.*;

public class ScoreboardSavedData extends PersistentState {
    public static final String NAME = "columbina_scoreboards";
    // Données des scoreboards : nom -> (entityId, type, teamName)
    private final Map<String, ScoreboardEntry> scoreboards = new HashMap<>();
    public static class ScoreboardEntry {
        public UUID entityId;
        public String type; // "team" ou "list"
        public String teamName; // null si type = "list"
        public ScoreboardEntry(UUID entityId, String type, String teamName) {
            this.entityId = entityId;
            this.type = type;
            this.teamName = teamName;
        }
    }
    public static ScoreboardSavedData fromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        ScoreboardSavedData data = new ScoreboardSavedData();
        NbtCompound scoreboardsTag = tag.getCompound("Scoreboards");
        for (String name : scoreboardsTag.getKeys()) {
            NbtCompound entryTag = scoreboardsTag.getCompound(name);
            UUID entityId = entryTag.getUuid("EntityId");
            String type = entryTag.getString("Type");
            String teamName = entryTag.contains("TeamName") ? entryTag.getString("TeamName") : null;
            data.scoreboards.put(name, new ScoreboardEntry(entityId, type, teamName));
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        NbtCompound scoreboardsTag = new NbtCompound();
        for (Map.Entry<String, ScoreboardEntry> entry : scoreboards.entrySet()) {
            NbtCompound entryTag = new NbtCompound();
            ScoreboardEntry scoreboardEntry = entry.getValue();
            entryTag.putUuid("EntityId", scoreboardEntry.entityId);
            entryTag.putString("Type", scoreboardEntry.type);
            if (scoreboardEntry.teamName != null) {
                entryTag.putString("TeamName", scoreboardEntry.teamName);
            }
            scoreboardsTag.put(entry.getKey(), entryTag);
        }
        tag.put("Scoreboards", scoreboardsTag);
        return tag;
    }
    public static ScoreboardSavedData get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                new PersistentState.Type<>(
                        ScoreboardSavedData::new,
                        ScoreboardSavedData::fromNbt,
                        null
                ),
                NAME
        );
    }
    public Map<String, ScoreboardEntry> getScoreboards() {
        return scoreboards;
    }
}