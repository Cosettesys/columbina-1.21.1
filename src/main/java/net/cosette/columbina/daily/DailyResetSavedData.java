package net.cosette.columbina.daily;

import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.PersistentState;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class DailyResetSavedData extends PersistentState {
    public static final String NAME = "columbina_daily_reset";
    private final Map<UUID, Long> lastLoginMap = new HashMap<>();
    private long lastResetTimestamp = 0L;
    public static DailyResetSavedData fromNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        DailyResetSavedData data = new DailyResetSavedData();
        data.lastResetTimestamp = tag.getLong("LastReset");
        NbtCompound loginsTag = tag.getCompound("LastLogins");
        for (String key : loginsTag.getKeys()) {
            data.lastLoginMap.put(UUID.fromString(key), loginsTag.getLong(key));
        }
        return data;
    }
    @Override
    public NbtCompound writeNbt(NbtCompound tag, RegistryWrapper.WrapperLookup lookup) {
        tag.putLong("LastReset", lastResetTimestamp);
        NbtCompound loginsTag = new NbtCompound();
        for (Map.Entry<UUID, Long> entry : lastLoginMap.entrySet()) {
            loginsTag.putLong(entry.getKey().toString(), entry.getValue());
        }
        tag.put("LastLogins", loginsTag);
        return tag;
    }
    public static DailyResetSavedData get(ServerWorld world) {
        return world.getPersistentStateManager().getOrCreate(
                new PersistentState.Type<>(
                        DailyResetSavedData::new,
                        DailyResetSavedData::fromNbt,
                        null
                ),
                NAME
        );
    }
    public long getLastResetTimestamp() { return lastResetTimestamp; }
    public void setLastResetTimestamp(long ts) { this.lastResetTimestamp = ts; markDirty(); }
    public long getLastLogin(UUID uuid) {
        return lastLoginMap.getOrDefault(uuid, 0L);
    }
    public void setLastLogin(UUID uuid, long ts) {
        lastLoginMap.put(uuid, ts);
        markDirty();
    }
}