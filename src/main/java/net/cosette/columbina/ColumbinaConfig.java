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
        if (!CONFIG_PATH.toFile().exists()) {
            // Créer la config par défaut avec commentaires
            config.setComment("daily_quest_ids",
                    " Liste des IDs de quêtes FTBQuests journalières.\n" +
                            " Pour trouver l'ID d'une quête : ouvrez l'éditeur FTBQuests en mode créatif,\n" +
                            " faites un clic droit sur la quête > 'Copy ID'.\n" +
                            " Exemple : [ \"3A2F1B\", \"7C4E2A\" ]"
            );
            config.set("daily_quest_ids", List.of("REMPLACE_PAR_UN_VRAI_ID"));
            config.save();
            Columbina.LOGGER.info("[Columbina] Config TOML créée par défaut dans {}", CONFIG_PATH);
        } else {
            config.load();
        }
        INSTANCE.dailyQuestIds = config.getOrElse("daily_quest_ids", new ArrayList<>());
        config.close();
        Columbina.LOGGER.info("[Columbina] Config chargée : {} quête(s) journalière(s)", INSTANCE.dailyQuestIds.size());
    }
    public List<String> getDailyQuestIds() {
        return dailyQuestIds;
    }
}