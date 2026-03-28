package net.cosette.columbina.poketopia;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;
import net.minecraft.world.PersistentStateManager;

/**
 * Persistent NBT data stored in the Poketopia dimension's {@code data/} folder.
 *
 * <p>Tracks whether the spawn island has already been placed so it is never
 * placed again on subsequent server restarts.
 *
 * <p>Yarn 1.21.1: {@link PersistentState.Type} constructor takes three arguments:
 * factory, fromNbt, and a codec (nullable for non-synced state).
 */
public class PoketopiaSavedData extends PersistentState {

    private static final String DATA_KEY          = "columbina_poketopia";
    private static final String NBT_ISLAND_PLACED = "island_placed";

    private boolean islandPlaced = false;

    // -------------------------------------------------------------------------
    // PersistentState boilerplate (Yarn 1.21.1)
    // -------------------------------------------------------------------------

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

    // -------------------------------------------------------------------------
    // Factory
    // -------------------------------------------------------------------------

    /**
     * Retrieves the saved data from the given world, creating a fresh instance
     * if none exists yet.
     */
    public static PoketopiaSavedData getOrCreate(ServerWorld world) {
        PersistentStateManager mgr = world.getPersistentStateManager();
        return mgr.getOrCreate(TYPE, DATA_KEY);
    }

    // -------------------------------------------------------------------------
    // Accessors
    // -------------------------------------------------------------------------

    public boolean isIslandPlaced() { return islandPlaced; }

    public void setIslandPlaced(boolean value) {
        this.islandPlaced = value;
        markDirty();
    }
}
