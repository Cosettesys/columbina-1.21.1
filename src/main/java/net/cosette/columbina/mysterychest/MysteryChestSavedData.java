package net.cosette.columbina.mysterychest;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class MysteryChestSavedData extends PersistentState {
    public static final String NAME = "columbina_mystery_chests";
    private final Map<UUID, Set<Long>> openedChests = new HashMap<>();
    public static MysteryChestSavedData get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                new PersistentState.Type<>(
                        MysteryChestSavedData::new,
                        MysteryChestSavedData::fromNbt,
                        null
                ),
                NAME
        );
    }
    public static MysteryChestSavedData fromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        MysteryChestSavedData data = new MysteryChestSavedData();
        NbtList playersList = tag.getList("Players", NbtElement.COMPOUND_TYPE);
        for (int i = 0; i < playersList.size(); i++) {
            NbtCompound entry = playersList.getCompound(i);
            UUID uuid = entry.getUuid("Uuid");
            Set<Long> positions = new HashSet<>();
            NbtList posList = entry.getList("Chests", NbtElement.LONG_TYPE);
            for (int j = 0; j < posList.size(); j++) {
                positions.add(((NbtLong) posList.get(j)).longValue());
            }
            data.openedChests.put(uuid, positions);
        }
        return data;
    }
    @Override
    public NbtCompound writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        NbtList playersList = new NbtList();
        for (Map.Entry<UUID, Set<Long>> entry : openedChests.entrySet()) {
            NbtCompound playerTag = new NbtCompound();
            playerTag.putUuid("Uuid", entry.getKey());
            NbtList posList = new NbtList();
            for (long pos : entry.getValue()) {
                posList.add(NbtLong.of(pos));
            }
            playerTag.put("Chests", posList);
            playersList.add(playerTag);
        }
        tag.put("Players", playersList);
        return tag;
    }
    public boolean hasOpened(UUID player, BlockPos pos) {
        Set<Long> positions = openedChests.get(player);
        return positions != null && positions.contains(pos.asLong());
    }
    public void markOpened(UUID player, BlockPos pos) {
        openedChests.computeIfAbsent(player, k -> new HashSet<>()).add(pos.asLong());
        markDirty();
    }
    public void reset(UUID player, BlockPos pos) {
        Set<Long> positions = openedChests.get(player);
        if (positions != null && positions.remove(pos.asLong())) {
            markDirty();
        }
    }
}