package net.cosette.columbina;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import net.fabricmc.loader.api.FabricLoader;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class ColumbinaConfig {
    private static final Path CONFIG_PATH = FabricLoader.getInstance()
            .getConfigDir()
            .resolve("columbina.toml");
    private static ColumbinaConfig INSTANCE;
    private List<String> dailyQuestIds = new ArrayList<>();
    private int safariCost = 10;
    private ColumbinaConfig() {}
    public static ColumbinaConfig getInstance() {
        if (INSTANCE == null) load();
        return INSTANCE;
    }
    public static void load() {
        INSTANCE = new ColumbinaConfig();
        CommentedFileConfig config = CommentedFileConfig.builder(CONFIG_PATH)
                .preserveInsertionOrder()
                .build();
        // Charger si le fichier existe, sinon créer
        if (CONFIG_PATH.toFile().exists()) {
            config.load();
        }
        // Ajouter les champs manquants (fichier nouveau ou ancien)
        boolean dirty = false;
        if (!config.contains("daily_quest_ids")) {
            config.setComment("daily_quest_ids",
                    " Liste des IDs de quêtes FTBQuests journalières.\n" +
                            " Pour trouver l'ID : clic droit sur la quête dans l'éditeur FTBQuests > 'Copy ID'.\n" +
                            " Exemple : [ \"3A2F1B\", \"7C4E2A\" ]"
            );
            config.set("daily_quest_ids", List.of("REMPLACE_PAR_UN_VRAI_ID"));
            dirty = true;
        }
        if (!config.contains("safari_cost")) {
            config.setComment("safari_cost",
                    " Coût en points d'équipe pour entrer dans le Safari Zone.\n" +
                            " Mettre à 0 pour désactiver le coût."
            );
            config.set("safari_cost", 10);
            dirty = true;
        }
        if (dirty) {
            config.save();
            Columbina.LOGGER.info("[Columbina] Config mise à jour dans {}", CONFIG_PATH);
        }
        INSTANCE.dailyQuestIds = config.getOrElse("daily_quest_ids", new ArrayList<>());
        INSTANCE.safariCost = config.getOrElse("safari_cost", 10);
        config.close();
        Columbina.LOGGER.info("[Columbina] Config chargée : {} quête(s) journalière(s), coût Safari : {} points",
                INSTANCE.dailyQuestIds.size(), INSTANCE.safariCost);
    }
    public List<String> getDailyQuestIds() {
        return dailyQuestIds;
    }
    public int getSafariCost() {
        return safariCost;
    }
}