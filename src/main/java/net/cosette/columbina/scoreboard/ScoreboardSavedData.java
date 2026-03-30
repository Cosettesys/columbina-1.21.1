package net.cosette.columbina.scoreboard;

import net.minecraft.nbt.*;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import java.util.*;

public class ScoreboardSavedData extends PersistentState {
    public static final String NAME = "columbina_scoreboards";
    private final Map<String, ScoreboardEntry> scoreboards = new HashMap<>();
    public static class ScoreboardEntry {
        public UUID entityId;
        public String type;
        public String teamName;
        public String dimensionKey;
        public ScoreboardEntry(UUID entityId, String type, String teamName, String dimensionKey) {
            this.entityId = entityId;
            this.type = type;
            this.teamName = teamName;
            this.dimensionKey = dimensionKey;
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
            String dimensionKey = entryTag.contains("DimensionKey") ? entryTag.getString("DimensionKey") : "minecraft:overworld";
            data.scoreboards.put(name, new ScoreboardEntry(entityId, type, teamName, dimensionKey));
        }
        return data;
    }
    @Override
    public NbtCompound writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        NbtCompound scoreboardsTag = new NbtCompound();
        for (Map.Entry<String, ScoreboardEntry> entry : scoreboards.entrySet()) {
            NbtCompound entryTag = new NbtCompound();
            ScoreboardEntry e = entry.getValue();
            entryTag.putUuid("EntityId", e.entityId);
            entryTag.putString("Type", e.type);
            if (e.teamName != null) entryTag.putString("TeamName", e.teamName);
            entryTag.putString("DimensionKey", e.dimensionKey != null ? e.dimensionKey : "minecraft:overworld");
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