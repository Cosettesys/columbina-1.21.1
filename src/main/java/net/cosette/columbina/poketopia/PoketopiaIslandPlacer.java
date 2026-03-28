package net.cosette.columbina.poketopia;

import net.cosette.columbina.Columbina;
import net.cosette.columbina.ColumbinaConfig;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.NbtSizeTracker;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.structure.StructurePlacementData;
import net.minecraft.structure.StructureTemplate;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.math.BlockPos;

import java.io.InputStream;

/**
 * Handles one-time placement of the Poketopia spawn island NBT structure.
 *
 * <p>The island NBT file must be placed at:
 * {@code src/main/resources/data/columbina/structures/poketopia_island.nbt}
 *
 * <p>Placement is tracked via {@link PoketopiaSavedData} so it only ever
 * happens once per world save, even across server restarts.
 */
public class PoketopiaIslandPlacer {

    private static final String STRUCTURE_RESOURCE =
            "/data/columbina/structures/poketopia_island.nbt";

    private PoketopiaIslandPlacer() {}

    /**
     * Called on {@code SERVER_STARTED}. Loads the island NBT from mod resources
     * and places it in the Poketopia dimension, centred on the configured spawn
     * coordinates, if it has not been placed before.
     */
    public static void tryPlaceIsland(MinecraftServer server) {
        ServerWorld poketopia = server.getWorld(PoketopiaManager.POKETOPIA_KEY);
        if (poketopia == null) {
            Columbina.LOGGER.warn("[Poketopia] Dimension columbina:poketopia absente — " +
                    "impossible de placer l'île de spawn.");
            return;
        }

        PoketopiaSavedData data = PoketopiaSavedData.getOrCreate(poketopia);
        if (data.isIslandPlaced()) {
            Columbina.LOGGER.info("[Poketopia] Île de spawn déjà placée, rien à faire.");
            return;
        }

        StructureTemplate template = loadTemplate(server);
        if (template == null) return;

        ColumbinaConfig cfg = ColumbinaConfig.getInstance();
        // Centre the structure on the configured spawn XZ.
        // The floor of the structure is placed 1 block below the player's feet.
        var size = template.getSize();
        int originX = cfg.getPoketopiaSpawnX() - size.getX() / 2;
        int originY = cfg.getPoketopiaSpawnY() - 1;
        int originZ = cfg.getPoketopiaSpawnZ() - size.getZ() / 2;
        BlockPos origin = new BlockPos(originX, originY, originZ);

        StructurePlacementData placement = new StructurePlacementData()
                .setMirror(BlockMirror.NONE)
                .setRotation(BlockRotation.NONE)
                .setIgnoreEntities(false);

        template.place(poketopia, origin, origin, placement, poketopia.getRandom(), 2);

        data.setIslandPlaced(true);
        Columbina.LOGGER.info("[Poketopia] Île de spawn placée à ({}, {}, {}).",
                originX, originY, originZ);
    }

    // -------------------------------------------------------------------------

    private static StructureTemplate loadTemplate(MinecraftServer server) {
        try (InputStream is = PoketopiaIslandPlacer.class.getResourceAsStream(STRUCTURE_RESOURCE)) {
            if (is == null) {
                Columbina.LOGGER.error(
                        "[Poketopia] Fichier NBT introuvable : {}\n" +
                                "  → Créez data/columbina/structures/poketopia_island.nbt dans les resources.",
                        STRUCTURE_RESOURCE);
                return null;
            }
            // Yarn 1.21.1: NbtIo.readCompressed(InputStream, NbtSizeTracker)
            NbtCompound nbt = NbtIo.readCompressed(is, NbtSizeTracker.ofUnlimitedBytes());
            StructureTemplate template = new StructureTemplate();
            // Yarn 1.21.1: readNbt takes a RegistryEntryLookup<Block>, not a DynamicRegistryManager
            template.readNbt(
                    server.getRegistryManager().createRegistryLookup().getOrThrow(net.minecraft.registry.RegistryKeys.BLOCK),
                    nbt
            );
            return template;
        } catch (Exception e) {
            Columbina.LOGGER.error("[Poketopia] Erreur lors du chargement du NBT de l'île.", e);
            return null;
        }
    }
}