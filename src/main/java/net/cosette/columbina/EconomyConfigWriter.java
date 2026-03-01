package net.cosette.columbina;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.util.WorldSavePath;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class EconomyConfigWriter {
    private static final String ECONOMY_CONFIG =
            "{\n" +
                    "  \"main_currency\": \"cobeco\",\n" +
                    "  \"startingBalance\": 0,\n" +
                    "  \"startingPco\": 0,\n" +
                    "  \"battleVictoryReward\": 0,\n" +
                    "  \"raidDenVictoryReward\": 0,\n" +
                    "  \"cobbleDollarsToPokedollarsRate\": 0,\n" +
                    "  \"impactorToPokedollarsRate\": 0,\n" +
                    "  \"captureReward\": 0,\n" +
                    "  \"newDiscoveryReward\": 0,\n" +
                    "  \"battleVictoryPcoReward\": 0,\n" +
                    "  \"battleTowerCompletionPcoBonus\": 0,\n" +
                    "  \"shinyMultiplier\": 0,\n" +
                    "  \"radiantMultiplier\": 0,\n" +
                    "  \"legendaryMultiplier\": 0,\n" +
                    "  \"paradoxMultiplier\": 0,\n" +
                    "  \"enableProfiling\": false,\n" +
                    "  \"profilingThresholdMs\": 5,\n" +
                    "  \"shops\": {}\n" +
                    "}\n";
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            if (!net.fabricmc.loader.api.FabricLoader.getInstance().isModLoaded("cobblemon-economy")) return;
            Path configDir = server.getSavePath(WorldSavePath.ROOT)
                    .resolve("config/cobblemon-economy");
            Path configFile = configDir.resolve("config.json");
            try {
                Files.createDirectories(configDir);
                Files.writeString(configFile, ECONOMY_CONFIG);
                Columbina.LOGGER.info("[Columbina] config.json cobblemon-economy appliqué.");
            } catch (IOException e) {
                Columbina.LOGGER.error("[Columbina] Impossible d'écrire le config.json cobblemon-economy", e);
            }
        });
    }
}