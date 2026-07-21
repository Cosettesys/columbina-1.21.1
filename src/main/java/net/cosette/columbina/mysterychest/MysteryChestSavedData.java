package net.cosette.columbina.mysterychest;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.nbt.NbtLong;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.PersistentState;

import java.util.HashSet;
import java.util.Set;

/**
 * Suivi des coffres mystères déjà ouverts.
 * Le coffre est unique PAR POSITION (pas par joueur) : une fois ouvert par
 * n'importe qui, il ne redonne plus jamais de points, même à un autre joueur.
 */
public class MysteryChestSavedData extends PersistentState {
    public static final String NAME = "columbina_mystery_chests";
    private final Set<Long> usedChests = new HashSet<>();

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
        NbtList list = tag.getList("UsedChests", NbtElement.LONG_TYPE);
        for (int i = 0; i < list.size(); i++) {
            data.usedChests.add(((NbtLong) list.get(i)).longValue());
        }
        return data;
    }

    @Override
    public NbtCompound writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        NbtList list = new NbtList();
        for (long pos : usedChests) {
            list.add(NbtLong.of(pos));
        }
        tag.put("UsedChests", list);
        return tag;
    }

    public boolean isUsed(BlockPos pos) {
        return usedChests.contains(pos.asLong());
    }

    public void markUsed(BlockPos pos) {
        usedChests.add(pos.asLong());
        markDirty();
    }

    /** Utile si un admin casse/replace un coffre et veut qu'il redevienne utilisable. */
    public void reset(BlockPos pos) {
        if (usedChests.remove(pos.asLong())) {
            markDirty();
        }
    }
}