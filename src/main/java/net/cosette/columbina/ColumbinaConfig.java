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
                    "poketopia_spawn_x = 0\n" +
                    "poketopia_spawn_y = 64\n" +
                    "poketopia_spawn_z = 0\n" +
                    "# Direction du regard au spawn : north | south | east | west\n" +
                    "poketopia_spawn_facing = \"south\"\n" +
                    "\n" +
                    "# Coordonnée Y en dessous de laquelle le joueur est téléporté au spawn.\n" +
                    "poketopia_void_y = -64\n" +
                    "\n" +
                    "# ── Intégration Cobblemon ────────────────────────────────────────────────\n" +
                    "# Points de base à la capture d'un Pokémon.\n" +
                    "capture_points_base = 1\n" +
                    "# Bonus si le Pokémon est shiny.\n" +
                    "capture_points_shiny_bonus = 5\n" +
                    "# Bonus si le Pokémon est légendaire.\n" +
                    "capture_points_legendary_bonus = 10\n" +
                    "# Bonus si le Pokémon est mythique.\n" +
                    "capture_points_mythical_bonus = 10\n" +
                    "# Message envoyé au joueur après une capture.\n" +
                    "# Variables : {pokemon}, {points}, {team}, {tags}\n" +
                    "capture_message = \"§a+{points} pts §7pour l'équipe §r{team} §7— capture de §r{pokemon}{tags}{count}\"\n"+
                    "# Si false, les joueurs ne peuvent pas monter un Pokémon dans la dimension Poketopia.\n" +
                    "poketopia_ride_allowed = false\n";
    private static ColumbinaConfig INSTANCE;
    private List<String> dailyQuestIds = new ArrayList<>();
    private int safariCost = 500;
    private int cobbleSafariCost = 300;
    private int poketopiaSpawnX = 0;
    private int poketopiaSpawnY = 64;
    private int poketopiaSpawnZ = 0;
    private String poketopiaSpawnFacing = "south";
    private int poketopiaVoidY = -64;
    public int capturePointsBase = 1;
    public int capturePointsShinyBonus = 5;
    public int capturePointsLegendaryBonus = 10;
    public int capturePointsMythicalBonus = 10;
    public String captureMessage = "§a+{points} pts §7pour l'équipe §r{team} §7— capture de §r{pokemon}{tags}{count}";
    public boolean poketopiaRideAllowed = false;
    public int captureLimitPerSpecies = 3;
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
        addMissingKey(config, "daily_quest_ids",
                "\n# Liste des IDs de quêtes FTBQuests à reset chaque jour.\n" +
                        "daily_quest_ids = [\"REMPLACE_PAR_UN_VRAI_ID\"]\n");
        addMissingKey(config, "safari_cost",
                "\n# Coût en points d'équipe pour entrer dans le Safari Zone.\n" +
                        "safari_cost = 500\n");
        addMissingKey(config, "poketopia_spawn_x",
                "\n# ── Dimension Poketopia ──────────────────────────────────────────────────\n" +
                        "poketopia_spawn_x = 0\npoketopia_spawn_y = 64\npoketopia_spawn_z = 0\n" +
                        "poketopia_spawn_facing = \"south\"\n");
        addMissingKey(config, "poketopia_void_y",
                "\n# Coordonnée Y en dessous de laquelle le joueur est téléporté au spawn.\n" +
                        "poketopia_void_y = -64\n");
        addMissingKey(config, "capture_points_base",
                "\n# ── Intégration Cobblemon ────────────────────────────────────────────────\n" +
                        "capture_points_base = 1\n" +
                        "capture_points_shiny_bonus = 5\n" +
                        "capture_points_legendary_bonus = 10\n" +
                        "capture_points_mythical_bonus = 10\n" +
                        "capture_message = \"§a+{points} pts §7pour l'équipe §r{team} §7— capture de §r{pokemon}{tags}{count}\"\n");
        addMissingKey(config, "capture_limit_per_species",
                "\n# Nombre max de captures d'une même espèce donnant des points par joueur.\n" +
                        "# Mettre à 0 pour désactiver la limite.\n" +
                        "capture_limit_per_species = 3\n");
        addMissingKey(config, "poketopia_ride_allowed",
                "\n# Si false, les joueurs ne peuvent pas monter un Pokémon dans la dimension Poketopia.\n" +
                        "poketopia_ride_allowed = false\n");
        INSTANCE.dailyQuestIds        = config.getOrElse("daily_quest_ids", new ArrayList<>());
        INSTANCE.safariCost           = config.getOrElse("safari_cost", 500);
        INSTANCE.cobbleSafariCost     = config.getOrElse("cobblesafari_cost", 300);
        INSTANCE.poketopiaSpawnX      = config.getOrElse("poketopia_spawn_x", 0);
        INSTANCE.poketopiaSpawnY      = config.getOrElse("poketopia_spawn_y", 64);
        INSTANCE.poketopiaSpawnZ      = config.getOrElse("poketopia_spawn_z", 0);
        INSTANCE.poketopiaSpawnFacing = config.getOrElse("poketopia_spawn_facing", "south");
        INSTANCE.poketopiaVoidY       = config.getOrElse("poketopia_void_y", -64);
        INSTANCE.capturePointsBase           = config.getOrElse("capture_points_base", 1);
        INSTANCE.capturePointsShinyBonus     = config.getOrElse("capture_points_shiny_bonus", 5);
        INSTANCE.capturePointsLegendaryBonus = config.getOrElse("capture_points_legendary_bonus", 10);
        INSTANCE.capturePointsMythicalBonus  = config.getOrElse("capture_points_mythical_bonus", 10);
        INSTANCE.captureMessage              = config.getOrElse("capture_message",
                "§a+{points} pts §7pour l'équipe §r{team} §7— capture de §r{pokemon}{tags}{count}");
        INSTANCE.captureLimitPerSpecies = config.getOrElse("capture_limit_per_species", 3);
        INSTANCE.poketopiaRideAllowed = config.getOrElse("poketopia_ride_allowed", false);
        config.close();
        Columbina.LOGGER.info("[Columbina] Config chargée.");
    }
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
    public List<String> getDailyQuestIds()    { return dailyQuestIds; }
    public int getSafariCost()                { return safariCost; }
    public int getCobbleSafariCost()          { return cobbleSafariCost; }
    public int    getPoketopiaSpawnX()        { return poketopiaSpawnX; }
    public int    getPoketopiaSpawnY()        { return poketopiaSpawnY; }
    public int    getPoketopiaSpawnZ()        { return poketopiaSpawnZ; }
    public String getPoketopiaSpawnFacing()   { return poketopiaSpawnFacing; }
    public int    getPoketopiaVoidY()         { return poketopiaVoidY; }
}