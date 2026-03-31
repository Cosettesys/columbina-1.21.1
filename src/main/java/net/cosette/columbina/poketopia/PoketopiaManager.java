package net.cosette.columbina.poketopia;

import net.cosette.columbina.Columbina;
import net.cosette.columbina.ColumbinaConfig;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import java.util.List;

public class PoketopiaManager {
    public static final RegistryKey<World> POKETOPIA_KEY = RegistryKey.of(
            RegistryKeys.WORLD,
            Identifier.of("columbina", "poketopia")
    );
    public static final String FIRST_JOIN_TAG = "columbina_poketopia_firstjoin_done";
    private static PoketopiaManager INSTANCE;
    private PoketopiaManager() {}
    public static PoketopiaManager getInstance() {
        if (INSTANCE == null) INSTANCE = new PoketopiaManager();
        return INSTANCE;
    }
    public void registerVoidRescue() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld poketopia = server.getWorld(POKETOPIA_KEY);
            if (poketopia == null) return;

            int voidY = ColumbinaConfig.getInstance().getPoketopiaVoidY();

            for (ServerPlayerEntity player : List.copyOf(poketopia.getPlayers())) {
                if (player.getY() < voidY) {
                    rescuePlayer(player, server);
                }
            }
        });
    }
    private void rescuePlayer(ServerPlayerEntity player, MinecraftServer server) {
        ColumbinaConfig cfg = ColumbinaConfig.getInstance();
        ServerWorld poketopia = server.getWorld(POKETOPIA_KEY);
        if (poketopia == null) return;
        double x = cfg.getPoketopiaSpawnX() + 0.5;
        double y = cfg.getPoketopiaSpawnY();
        double z = cfg.getPoketopiaSpawnZ() + 0.5;
        float yaw = directionToYaw(cfg.getPoketopiaSpawnFacing());
        player.fallDistance = 0;
        TeleportTarget target = new TeleportTarget(
                poketopia,
                new Vec3d(x, y, z),
                Vec3d.ZERO,
                yaw,
                0.0f,
                TeleportTarget.NO_OP
        );
        player.teleportTo(target);

        Columbina.LOGGER.info("[Poketopia] {} sauvé du void → poketopia ({}, {}, {})",
                player.getName().getString(), x, y, z);
    }
    public void onFirstJoin(ServerPlayerEntity player) {
        if (hasFirstJoinTag(player)) return;
        player.getServer().execute(() -> {
            ServerWorld poketopia = getPoketopiaWorld(player.getServer());
            if (poketopia == null) {
                Columbina.LOGGER.warn("[Poketopia] Dimension columbina:poketopia introuvable.");
                return;
            }
            teleportToSpawn(player, poketopia);
            setFirstJoinTag(player);
            Columbina.LOGGER.info("[Poketopia] {} téléporté à Poketopia (premier spawn).",
                    player.getName().getString());
        });
    }
    public void onRespawn(ServerPlayerEntity player) {
        if (player.getSpawnPointPosition() != null) return;
        ServerWorld poketopia = getPoketopiaWorld(player.getServer());
        if (poketopia == null) {
            Columbina.LOGGER.warn("[Poketopia] Dimension columbina:poketopia introuvable — " +
                    "respawn normal appliqué pour {}.", player.getName().getString());
            return;
        }
        teleportToSpawn(player, poketopia);
        Columbina.LOGGER.info("[Poketopia] {} renvoyé à Poketopia (respawn sans lit).",
                player.getName().getString());
    }
    private ServerWorld getPoketopiaWorld(MinecraftServer server) {
        return server.getWorld(POKETOPIA_KEY);
    }
    private void teleportToSpawn(ServerPlayerEntity player, ServerWorld poketopia) {
        ColumbinaConfig cfg = ColumbinaConfig.getInstance();
        double x = cfg.getPoketopiaSpawnX() + 0.5;
        double y = cfg.getPoketopiaSpawnY();
        double z = cfg.getPoketopiaSpawnZ() + 0.5;
        float yaw = directionToYaw(cfg.getPoketopiaSpawnFacing());
        player.setSpawnPoint(
                POKETOPIA_KEY,
                BlockPos.ofFloored(x, y, z),
                yaw,
                true,
                false
        );
        TeleportTarget target = new TeleportTarget(
                poketopia,
                new Vec3d(x, y, z),
                Vec3d.ZERO,
                yaw,
                0.0f,
                TeleportTarget.NO_OP
        );
        player.teleportTo(target);
    }
    private boolean hasFirstJoinTag(ServerPlayerEntity player) {
        return player.getCommandTags().contains(FIRST_JOIN_TAG);
    }
    private void setFirstJoinTag(ServerPlayerEntity player) {
        player.addCommandTag(FIRST_JOIN_TAG);
    }
    public static float directionToYaw(String facing) {
        if (facing == null) return 0f;
        return switch (facing.toLowerCase().trim()) {
            case "south" -> 0f;
            case "west"  -> 90f;
            case "north" -> 180f;
            case "east"  -> 270f;
            default      -> 0f;
        };
    }
}