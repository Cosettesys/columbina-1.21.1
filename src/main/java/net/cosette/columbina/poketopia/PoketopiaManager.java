package net.cosette.columbina.poketopia;

import net.cosette.columbina.Columbina;
import net.cosette.columbina.ColumbinaConfig;
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

/**
 * Manages teleportation of players to the Poketopia dimension on first spawn
 * or on respawn when they have no bed/respawn anchor set.
 *
 * <p>Uses the {@link TeleportTarget} API introduced in Minecraft 1.21 (Fabric/Yarn).
 */
public class PoketopiaManager {

    /** The ResourceKey for the columbina:poketopia dimension. */
    public static final RegistryKey<World> POKETOPIA_KEY = RegistryKey.of(
            RegistryKeys.WORLD,
            Identifier.of("columbina", "poketopia")
    );

    /**
     * Command tag stored on the player entity to mark that they have already
     * been sent to Poketopia at least once (first ever join).
     */
    public static final String FIRST_JOIN_TAG = "columbina_poketopia_firstjoin_done";

    private static PoketopiaManager INSTANCE;

    private PoketopiaManager() {}

    public static PoketopiaManager getInstance() {
        if (INSTANCE == null) INSTANCE = new PoketopiaManager();
        return INSTANCE;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Called when a player logs in for the very first time.
     * Teleports them to Poketopia and marks the tag so it does not trigger again.
     */
    public void onFirstJoin(ServerPlayerEntity player) {
        if (hasFirstJoinTag(player)) return;

        // Retarder d'un tick pour que le joueur soit pleinement initialisé
        // avant la téléportation cross-dimension
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

    /**
     * Called after a player respawns (death). If they have no valid bed or
     * respawn anchor, teleport them to Poketopia instead of the overworld spawn.
     */
    public void onRespawn(ServerPlayerEntity player) {
        // getSpawnPointPosition() returns null when no custom respawn point is set
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

    // -------------------------------------------------------------------------
    // Internal helpers
    // -------------------------------------------------------------------------

    private ServerWorld getPoketopiaWorld(MinecraftServer server) {
        return server.getWorld(POKETOPIA_KEY);
    }

    private void teleportToSpawn(ServerPlayerEntity player, ServerWorld poketopia) {
        ColumbinaConfig cfg = ColumbinaConfig.getInstance();
        double x = cfg.getPoketopiaSpawnX() + 0.5;
        double y = cfg.getPoketopiaSpawnY();
        double z = cfg.getPoketopiaSpawnZ() + 0.5;
        float yaw = directionToYaw(cfg.getPoketopiaSpawnFacing());

        // Set the respawn point inside Poketopia so that subsequent deaths without
        // a bed also bring the player back here (until they sleep somewhere).
        player.setSpawnPoint(
                POKETOPIA_KEY,
                BlockPos.ofFloored(x, y, z),
                yaw,
                true,   // forced — no bed block required
                false   // silent — don't send the "Respawn point set" chat message
        );

        // In Fabric/Yarn 1.21.x, cross-dimension teleportation is done via TeleportTarget.
        TeleportTarget target = new TeleportTarget(
                poketopia,
                new Vec3d(x, y, z),
                Vec3d.ZERO,          // velocity — stop the player
                yaw,
                0.0f,                // pitch — look straight ahead
                TeleportTarget.NO_OP // no post-teleport callback needed
        );
        player.teleportTo(target);
    }

    private boolean hasFirstJoinTag(ServerPlayerEntity player) {
        return player.getCommandTags().contains(FIRST_JOIN_TAG);
    }

    private void setFirstJoinTag(ServerPlayerEntity player) {
        player.addCommandTag(FIRST_JOIN_TAG);
    }

    /**
     * Converts a cardinal direction string (north/south/east/west) to a
     * Minecraft yaw angle. Defaults to south (0°) for unknown values.
     */
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