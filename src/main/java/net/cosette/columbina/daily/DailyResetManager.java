package net.cosette.columbina.daily;

import net.cosette.columbina.ColumbinaConfig;
import net.cosette.columbina.shop.ShopPlayerStockData;
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
            lastResetDate.set(LocalDate.now(PARIS).minusDays(1));
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
    public void performReset(LocalDate today) {
        lastResetDate.set(today);
        long resetTs = today.atStartOfDay(PARIS).toEpochSecond();
        DailyResetSavedData data = DailyResetSavedData.get(world);
        data.setLastResetTimestamp(resetTs);

        MinecraftServer server = world.getServer();
        server.execute(() -> {
            server.getCommandManager().executeWithPrefix(
                    server.getCommandSource(),
                    "tag @a remove dailyquest"
            );
            server.getPlayerManager().broadcast(
                    Text.literal("§6[COLUMBINA] §eReset quotidien effectué ! Les quêtes quotidiennes ont été réinitialisées !"),
                    false
            );
            for (ServerPlayerEntity onlinePlayer : server.getPlayerManager().getPlayerList()) {
                String name = onlinePlayer.getName().getString();
                for (String questId : ColumbinaConfig.getInstance().getDailyQuestIds()) {
                    server.getCommandManager().executeWithPrefix(
                            server.getCommandSource(),
                            "ftbquests change_progress " + name + " reset " + questId
                    );
                }
            }
            ShopPlayerStockData.get(world).resetAllPlayerStocks();
            long nowTs = Instant.now().getEpochSecond();
            DailyResetSavedData resetData = DailyResetSavedData.get(world);
            for (ServerPlayerEntity onlinePlayer : server.getPlayerManager().getPlayerList()) {
                resetData.setLastLogin(onlinePlayer.getUuid(), nowTs);
            }
        });
    }
    public void forceReset() {
        performReset(LocalDate.now(PARIS));
    }
    public void resetPlayer(ServerPlayerEntity player) {
        world.getServer().execute(() -> {
            world.getServer().getCommandManager().executeWithPrefix(
                    world.getServer().getCommandSource(),
                    "tag " + player.getName().getString() + " remove dailyquest"
            );
            player.sendMessage(
                    Text.literal("§6[COLUMBINA] §eTa quête quotidienne a été réinitialisée !"),
                    false
            );
        });
        DailyResetSavedData data = DailyResetSavedData.get(world);
        data.setLastLogin(player.getUuid(), Instant.now().getEpochSecond());
    }
    public void onPlayerJoin(ServerPlayerEntity player) {
        UUID uuid = player.getUuid();
        DailyResetSavedData data = DailyResetSavedData.get(world);
        long lastLogin = data.getLastLogin(uuid);
        long lastResetTs = data.getLastResetTimestamp();
        // DEBUG
        net.cosette.columbina.Columbina.LOGGER.info("[DailyReset] onPlayerJoin: player={}, lastLogin={}, lastResetTs={}, diff={}",
                player.getName().getString(), lastLogin, lastResetTs, lastResetTs - lastLogin);
        if (lastLogin < lastResetTs && lastResetTs > 0) {
            world.getServer().execute(() -> {
                world.getServer().getCommandManager().executeWithPrefix(
                        world.getServer().getCommandSource(),
                        "tag " + player.getName().getString() + " remove dailyquest"
                );
                for (String questId : ColumbinaConfig.getInstance().getDailyQuestIds()) {
                    world.getServer().getCommandManager().executeWithPrefix(
                            world.getServer().getCommandSource(),
                            "ftbquests change_progress " + player.getName().getString() + " reset " + questId
                    );
                }
                player.sendMessage(
                        Text.literal("§6[COLUMBINA] §eTa quête quotidienne a été réinitialisée !"),
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
    public void fakeOffline(ServerPlayerEntity player) {
        DailyResetSavedData data = DailyResetSavedData.get(world);
        // Force le lastLogin à il y a 2 jours
        long twoDaysAgo = Instant.now().getEpochSecond() - (2 * 24 * 60 * 60);
        data.setLastLogin(player.getUuid(), twoDaysAgo);
        player.sendMessage(
                Text.literal("§7[DEBUG] LastLogin forcé à il y a 2 jours. Reconnecte-toi."),
                false
        );
    }
    public void testJoin(ServerPlayerEntity player) {
        onPlayerJoin(player);
    }
}