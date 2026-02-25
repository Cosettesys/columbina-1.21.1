package net.cosette.columbina.daily;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;

import java.time.*;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

public class DailyResetManager {
    private static final DailyResetManager INSTANCE = new DailyResetManager();
    private static final ZoneId PARIS = ZoneId.of("Europe/Paris");
    private ServerWorld world;
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final AtomicReference<LocalDate> lastResetDate = new AtomicReference<>();
    private DailyResetManager() {}
    public static DailyResetManager getInstance() { return INSTANCE; }
    public void init(ServerWorld world) {
        this.world = world;
        DailyResetSavedData data = DailyResetSavedData.get(world);
        long savedTs = data.getLastResetTimestamp();
        if (savedTs > 0) {
            lastResetDate.set(Instant.ofEpochSecond(savedTs).atZone(PARIS).toLocalDate());
        } else {
            lastResetDate.set(LocalDate.now(PARIS).minusDays(1)); // Force reset au prochain check
        }
        scheduler.scheduleAtFixedRate(this::checkReset, 0, 1, TimeUnit.MINUTES);
    }
    private void checkReset() {
        LocalDate today = LocalDate.now(PARIS);
        LocalTime now = LocalTime.now(PARIS);
        if (now.getHour() == 0 && today.isAfter(lastResetDate.get())) {
            performReset(today);
        }
    }
    private void performReset(LocalDate today) {
        lastResetDate.set(today);
        long resetTs = today.atStartOfDay(PARIS).toEpochSecond();
        DailyResetSavedData data = DailyResetSavedData.get(world);
        data.setLastResetTimestamp(resetTs);
        MinecraftServer server = world.getServer();
        server.execute(() -> {
            server.getCommandManager().executeWithPrefix(
                    server.getCommandSource(),
                    "tag @a remove quete_journaliere"
            );
            server.getPlayerManager().broadcast(
                    Text.literal("§6[COLUMBINA] §eReset quotidien effectué !"),
                    false
            );
        });
    }
    public void onPlayerJoin(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        DailyResetSavedData data = DailyResetSavedData.get(world);
        long lastLogin = data.getLastLogin(uuid);
        long lastResetTs = data.getLastResetTimestamp();
        if (lastLogin < lastResetTs && lastResetTs > 0) {
            world.getServer().execute(() -> {
                world.getServer().getCommandManager().executeWithPrefix(
                        world.getServer().getCommandSource(),
                        "tag " + player.getName().getString() + " remove quete_journaliere");
                        player.sendMessage(
                                Text.literal("§6[COLUMBINA] §eTa quête journalière a été réinitialisée ! Tu peux en obtenir une nouvelle."),
                                false
                );
            });
        }
        data.setLastLogin(uuid, Instant.now().getEpochSecond());
    }
    public void onPlayerDisconnect(ServerPlayerEntity player) {
        DailyResetSavedData data = DailyResetSavedData.get(world);
        data.setLastLogin(player.getUuid(), Instant.now().getEpochSecond());
    }
}