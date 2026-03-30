package net.cosette.columbina.poketopia;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

public class PoketopiaSavedData extends PersistentState {
    private static final String DATA_KEY          = "columbina_poketopia";
    private static final String NBT_ISLAND_PLACED = "island_placed";
    private boolean islandPlaced = false;
    private static final Type<PoketopiaSavedData> TYPE = new Type<>(
            PoketopiaSavedData::new,
            PoketopiaSavedData::fromNbt,
            null   // no DataFixer needed
    );
    public PoketopiaSavedData() {}
    public static PoketopiaSavedData fromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        PoketopiaSavedData data = new PoketopiaSavedData();
        data.islandPlaced = nbt.getBoolean(NBT_ISLAND_PLACED);
        return data;
    }
    @Override
    public NbtCompound writeNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup lookup) {
        nbt.putBoolean(NBT_ISLAND_PLACED, islandPlaced);
        return nbt;
    }
    public static PoketopiaSavedData getOrCreate(ServerWorld world) {
        PersistentStateManager mgr = world.getPersistentStateManager();
        return mgr.getOrCreate(TYPE, DATA_KEY);
    }
    public boolean isIslandPlaced() { return islandPlaced; }
    public void setIslandPlaced(boolean value) {
        this.islandPlaced = value;
        markDirty();
    }
}
