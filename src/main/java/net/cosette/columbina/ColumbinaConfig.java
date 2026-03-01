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
                    "# Coût en points d'équipe pour entrer dans le Safari Zone.\n" +
                    "# Mettre à 0 pour désactiver le coût.\n" +
                    "safari_cost = 500\n";
    private static ColumbinaConfig INSTANCE;
    private List<String> dailyQuestIds = new ArrayList<>();
    private int safariCost = 500;
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
        boolean dirty = false;
        if (!config.contains("daily_quest_ids")) {
            try {
                String current = Files.readString(CONFIG_PATH);
                Files.writeString(CONFIG_PATH, current +
                        "\n# Liste des IDs de quêtes FTBQuests à reset chaque jour.\n" +
                        "# Exemple : daily_quest_ids = [\"3A2F1B\", \"7C4E2A\"]\n" +
                        "daily_quest_ids = [\"REMPLACE_PAR_UN_VRAI_ID\"]\n");
                config.load();
            } catch (IOException e) {
                Columbina.LOGGER.error("[Columbina] Erreur ajout daily_quest_ids", e);
            }
        }
        if (!config.contains("safari_cost")) {
            try {
                String current = Files.readString(CONFIG_PATH);
                Files.writeString(CONFIG_PATH, current +
                        "\n# Coût en points d'équipe pour entrer dans le Safari Zone.\n" +
                        "# Mettre à 0 pour désactiver le coût.\n" +
                        "safari_cost = 500\n");
                config.load();
            } catch (IOException e) {
                Columbina.LOGGER.error("[Columbina] Erreur ajout safari_cost", e);
            }
        }
        INSTANCE.dailyQuestIds = config.getOrElse("daily_quest_ids", new ArrayList<>());
        INSTANCE.safariCost = config.getOrElse("safari_cost", 500);
        config.close();
        Columbina.LOGGER.info("[Columbina] Config chargée : {} quête(s), coût Safari : {} points",
                INSTANCE.dailyQuestIds.size(), INSTANCE.safariCost);
    }
    public List<String> getDailyQuestIds() {
        return dailyQuestIds;
    }
    public int getSafariCost() {
        return safariCost;
    }
}