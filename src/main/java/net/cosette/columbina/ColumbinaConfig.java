package net.cosette.columbina;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ColumbinaConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("columbina.toml");

    private static final String DEFAULT_CONFIG =
            "# Liste des IDs de quêtes FTBQuests à reset chaque jour.\n" +
                    "# Pour trouver l'ID : clic droit sur la quête dans l'éditeur FTBQuests > 'Copy ID'.\n" +
                    "# Exemple : daily_quest_ids = [\"3A2F1B\", \"7C4E2A\"]\n" +
                    "daily_quest_ids = [\"REMPLACE_PAR_UN_VRAI_ID\"]\n" +
                    "\n" +
                    "# Coût en points pour entrer dans le CobbleSafari.\n" +
                    "cobblesafari_cost = 300\n" +
                    "\n" +
                    "# Coût en points d'équipe pour entrer dans le Safari Zone.\n" +
                    "# Mettre à 0 pour désactiver le coût.\n" +
                    "safari_cost = 500\n" +
                    "\n" +
                    "# ── Dimension Poketopia ──────────────────────────────────────────────────\n" +
                    "# Coordonnées du spawn dans columbina:poketopia.\n" +
                    "# Les joueurs y arrivent lors de leur premier spawn, et lors de tout\n" +
                    "# respawn tant qu'ils n'ont pas de lit ou d'ancre de réapparition.\n" +
                    "poketopia_spawn_x = 0\n" +
                    "poketopia_spawn_y = 64\n" +
                    "poketopia_spawn_z = 0\n" +
                    "# Direction du regard au spawn : north | south | east | west\n" +
                    "poketopia_spawn_facing = \"south\"\n";

    private static ColumbinaConfig INSTANCE;

    // ── existing fields ───────────────────────────────────────────────────────
    private List<String> dailyQuestIds = new ArrayList<>();
    private int safariCost = 500;
    private int cobbleSafariCost = 300;

    // ── Poketopia fields ──────────────────────────────────────────────────────
    private int poketopiaSpawnX = 0;
    private int poketopiaSpawnY = 64;
    private int poketopiaSpawnZ = 0;
    private String poketopiaSpawnFacing = "south";

    private ColumbinaConfig() {}

    public static ColumbinaConfig getInstance() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }

    public static void load() {
        INSTANCE = new ColumbinaConfig();
        if (!CONFIG_PATH.toFile().exists()) {
            try {
                Files.writeString(CONFIG_PATH, DEFAULT_CONFIG);
                Columbina.LOGGER.info("[Columbina] Config créée dans {}", CONFIG_PATH);
            } catch (IOException e) {
                Columbina.LOGGER.error("[Columbina] Impossible de créer la config", e);
            }
        }

        CommentedFileConfig config = CommentedFileConfig.builder(CONFIG_PATH)
                .preserveInsertionOrder()
                .build();
        config.load();

        // ── migrate: add missing keys ─────────────────────────────────────────
        addMissingKey(config,
                "daily_quest_ids",
                "\n# Liste des IDs de quêtes FTBQuests à reset chaque jour.\n" +
                        "# Exemple : daily_quest_ids = [\"3A2F1B\", \"7C4E2A\"]\n" +
                        "daily_quest_ids = [\"REMPLACE_PAR_UN_VRAI_ID\"]\n");

        addMissingKey(config,
                "safari_cost",
                "\n# Coût en points d'équipe pour entrer dans le Safari Zone.\n" +
                        "# Mettre à 0 pour désactiver le coût.\n" +
                        "safari_cost = 500\n");

        addMissingKey(config,
                "poketopia_spawn_x",
                "\n# ── Dimension Poketopia ──────────────────────────────────────────────────\n" +
                        "poketopia_spawn_x = 0\n" +
                        "poketopia_spawn_y = 64\n" +
                        "poketopia_spawn_z = 0\n" +
                        "# Direction du regard au spawn : north | south | east | west\n" +
                        "poketopia_spawn_facing = \"south\"\n");

        // ── read values ───────────────────────────────────────────────────────
        INSTANCE.dailyQuestIds    = config.getOrElse("daily_quest_ids", new ArrayList<>());
        INSTANCE.safariCost       = config.getOrElse("safari_cost", 500);
        INSTANCE.cobbleSafariCost = config.getOrElse("cobblesafari_cost", 300);

        INSTANCE.poketopiaSpawnX      = config.getOrElse("poketopia_spawn_x", 0);
        INSTANCE.poketopiaSpawnY      = config.getOrElse("poketopia_spawn_y", 64);
        INSTANCE.poketopiaSpawnZ      = config.getOrElse("poketopia_spawn_z", 0);
        INSTANCE.poketopiaSpawnFacing = config.getOrElse("poketopia_spawn_facing", "south");

        config.close();
        Columbina.LOGGER.info(
                "[Columbina] Config chargée : {} quête(s), coût Safari : {} points, " +
                        "spawn Poketopia : ({}, {}, {}) facing {}",
                INSTANCE.dailyQuestIds.size(), INSTANCE.safariCost,
                INSTANCE.poketopiaSpawnX, INSTANCE.poketopiaSpawnY, INSTANCE.poketopiaSpawnZ,
                INSTANCE.poketopiaSpawnFacing);
    }

    // ── migration helper ──────────────────────────────────────────────────────

    private static void addMissingKey(CommentedFileConfig config, String key, String toAppend) {
        if (!config.contains(key)) {
            try {
                String current = Files.readString(CONFIG_PATH);
                Files.writeString(CONFIG_PATH, current + toAppend);
                config.load();
            } catch (IOException e) {
                Columbina.LOGGER.error("[Columbina] Erreur ajout clé '{}' dans la config", key, e);
            }
        }
    }

    // ── getters ───────────────────────────────────────────────────────────────

    public List<String> getDailyQuestIds()    { return dailyQuestIds; }
    public int getSafariCost()                { return safariCost; }
    public int getCobbleSafariCost()          { return cobbleSafariCost; }

    public int    getPoketopiaSpawnX()        { return poketopiaSpawnX; }
    public int    getPoketopiaSpawnY()        { return poketopiaSpawnY; }
    public int    getPoketopiaSpawnZ()        { return poketopiaSpawnZ; }
    public String getPoketopiaSpawnFacing()   { return poketopiaSpawnFacing; }
}
