package net.cosette.columbina.compat;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CaptureCountSavedData extends PersistentState {
    public static final String NAME = "columbina_capture_counts";
    private final Map<String, Map<String, Integer>> counts = new HashMap<>();
    public static CaptureCountSavedData get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                new PersistentState.Type<>(
                        CaptureCountSavedData::new,
                        CaptureCountSavedData::fromNbt,
                        null
                ),
                NAME
        );
    }
    public static CaptureCountSavedData fromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        CaptureCountSavedData data = new CaptureCountSavedData();
        NbtCompound playersTag = tag.getCompound("Players");
        for (String uuidStr : playersTag.getKeys()) {
            NbtCompound speciesTag = playersTag.getCompound(uuidStr);
            Map<String, Integer> speciesMap = new HashMap<>();
            for (String species : speciesTag.getKeys()) {
                speciesMap.put(species, speciesTag.getInt(species));
            }
            data.counts.put(uuidStr, speciesMap);
        }
        return data;
    }
    @Override
    public NbtCompound writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        NbtCompound playersTag = new NbtCompound();
        for (Map.Entry<String, Map<String, Integer>> playerEntry : counts.entrySet()) {
            NbtCompound speciesTag = new NbtCompound();
            for (Map.Entry<String, Integer> speciesEntry : playerEntry.getValue().entrySet()) {
                speciesTag.putInt(speciesEntry.getKey(), speciesEntry.getValue());
            }
            playersTag.put(playerEntry.getKey(), speciesTag);
        }
        tag.put("Players", playersTag);
        return tag;
    }
    public int getCount(UUID player, String species) {
        return counts
                .getOrDefault(player.toString(), new HashMap<>())
                .getOrDefault(species, 0);
    }
    public void increment(UUID player, String species) {
        counts
                .computeIfAbsent(player.toString(), k -> new HashMap<>())
                .merge(species, 1, Integer::sum);
        markDirty();
    }
}