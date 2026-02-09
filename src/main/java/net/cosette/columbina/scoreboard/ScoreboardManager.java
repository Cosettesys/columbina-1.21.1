package net.cosette.columbina.scoreboard;

import net.cosette.columbina.team.TeamManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.AffineTransformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.stream.Collectors;

public class ScoreboardManager {
    private static final ScoreboardManager INSTANCE = new ScoreboardManager();

    // Stocke les informations des scoreboards
    private static class ScoreboardData {
        UUID entityId;
        String type; // "team" ou "list"
        String teamName; // null si type = "list"

        ScoreboardData(UUID entityId, String type, String teamName) {
            this.entityId = entityId;
            this.type = type;
            this.teamName = teamName;
        }
    }

    private final Map<String, ScoreboardData> scoreboards = new HashMap<>();
    private ServerWorld world;

    private ScoreboardManager() {}

    public static ScoreboardManager getInstance() {
        return INSTANCE;
    }

    public void init(ServerWorld world) {
        this.world = world;
    }

    /**
     * Spawn un scoreboard pour une équipe spécifique
     */
    public boolean spawnTeamScoreboard(String name, double x, double y, double z, String teamName) {
        if (scoreboards.containsKey(name)) {
            return false; // Un scoreboard avec ce nom existe déjà
        }

        TeamManager tm = TeamManager.getInstance();
        if (!tm.teamExists(teamName)) {
            return false; // L'équipe n'existe pas
        }

        // Créer le texte du scoreboard
        String text = buildTeamScoreboardText(teamName);

        // Spawn le text_display
        UUID entityId = spawnTextDisplay(x, y, z, text);
        scoreboards.put(name, new ScoreboardData(entityId, "team", teamName));

        return true;
    }

    /**
     * Spawn un scoreboard de classement général
     */
    public boolean spawnListScoreboard(String name, double x, double y, double z) {
        if (scoreboards.containsKey(name)) {
            return false; // Un scoreboard avec ce nom existe déjà
        }

        // Créer le texte du classement
        String text = buildListScoreboardText();

        // Spawn le text_display
        UUID entityId = spawnTextDisplay(x, y, z, text);
        scoreboards.put(name, new ScoreboardData(entityId, "list", null));

        return true;
    }

    /**
     * Supprime un scoreboard
     */
    public boolean deleteScoreboard(String name) {
        ScoreboardData data = scoreboards.get(name);
        if (data == null) {
            return false;
        }

        // Trouver et supprimer l'entité
        DisplayEntity.TextDisplayEntity entity = (DisplayEntity.TextDisplayEntity) world.getEntity(data.entityId);
        if (entity != null) {
            entity.discard();
        }

        scoreboards.remove(name);
        return true;
    }

    /**
     * Met à jour tous les scoreboards
     */
    public void updateAllScoreboards() {
        for (Map.Entry<String, ScoreboardData> entry : scoreboards.entrySet()) {
            ScoreboardData data = entry.getValue();
            DisplayEntity.TextDisplayEntity entity = (DisplayEntity.TextDisplayEntity) world.getEntity(data.entityId);

            if (entity == null) {
                continue; // L'entité n'existe plus
            }

            // Générer le nouveau texte selon le type
            String newText;
            if ("team".equals(data.type)) {
                newText = buildTeamScoreboardText(data.teamName);
            } else {
                newText = buildListScoreboardText();
            }

            // Mettre à jour le texte
            entity.setText(Text.literal(newText));
        }
    }

    /**
     * Met à jour un scoreboard spécifique
     */
    public boolean updateScoreboard(String name) {
        ScoreboardData data = scoreboards.get(name);
        if (data == null) {
            return false;
        }

        DisplayEntity.TextDisplayEntity entity = (DisplayEntity.TextDisplayEntity) world.getEntity(data.entityId);
        if (entity == null) {
            return false;
        }

        // Générer le nouveau texte selon le type
        String newText;
        if ("team".equals(data.type)) {
            newText = buildTeamScoreboardText(data.teamName);
        } else {
            newText = buildListScoreboardText();
        }

        // Mettre à jour le texte
        entity.setText(Text.literal(newText));
        return true;
    }

    /**
     * Construit le texte pour le scoreboard d'une équipe
     */
    private String buildTeamScoreboardText(String teamName) {
        TeamManager tm = TeamManager.getInstance();
        Formatting color = tm.getTeamColor(teamName);
        int points = tm.getPoints(teamName);
        List<UUID> members = tm.getTeamMembers(teamName);

        StringBuilder text = new StringBuilder();

        // En-tête avec le nom de l'équipe
        text.append(color).append("╔═══════════════════╗\n");
        text.append(color).append("║  ").append(teamName.toUpperCase()).append("\n");
        text.append(color).append("╠═══════════════════╣\n");

        // Points
        text.append(Formatting.WHITE).append("  Points: ").append(Formatting.GOLD).append(points).append("\n\n");

        // Membres
        text.append(Formatting.WHITE).append("  Membres:\n");
        if (members.isEmpty()) {
            text.append(Formatting.GRAY).append("  • Aucun\n");
        } else {
            for (UUID uuid : members) {
                String playerName = getPlayerName(uuid);
                text.append(color).append("  • ").append(playerName).append("\n");
            }
        }

        text.append(color).append("╚═══════════════════╝");

        return text.toString();
    }

    /**
     * Construit le texte pour le scoreboard de classement
     */
    private String buildListScoreboardText() {
        TeamManager tm = TeamManager.getInstance();
        Set<String> teams = tm.getAllTeams();

        // Trier les équipes par points (décroissant)
        List<String> sortedTeams = teams.stream()
                .sorted((t1, t2) -> Integer.compare(tm.getPoints(t2), tm.getPoints(t1)))
                .collect(Collectors.toList());

        StringBuilder text = new StringBuilder();

        // En-tête
        text.append(Formatting.GOLD).append("╔════════════════════╗\n");
        text.append(Formatting.GOLD).append("║   ").append(Formatting.YELLOW).append("🏆 CLASSEMENT 🏆").append(Formatting.GOLD).append("   ║\n");
        text.append(Formatting.GOLD).append("╠════════════════════╣\n");

        if (sortedTeams.isEmpty()) {
            text.append(Formatting.GRAY).append("  Aucune équipe\n");
        } else {
            int position = 1;
            for (String team : sortedTeams) {
                Formatting color = tm.getTeamColor(team);
                int points = tm.getPoints(team);

                // Médaille pour le top 3
                String medal = "";
                if (position == 1) medal = "🥇 ";
                else if (position == 2) medal = "🥈 ";
                else if (position == 3) medal = "🥉 ";
                else medal = position + ". ";

                text.append(Formatting.WHITE).append("  ").append(medal)
                        .append(color).append(team)
                        .append(Formatting.GRAY).append(" - ")
                        .append(Formatting.GOLD).append(points).append(" pts")
                        .append("\n");

                position++;
            }
        }

        text.append(Formatting.GOLD).append("╚════════════════════╝");

        return text.toString();
    }

    /**
     * Spawn une entité text_display
     */
    private UUID spawnTextDisplay(double x, double y, double z, String text) {
        DisplayEntity.TextDisplayEntity textDisplay = new DisplayEntity.TextDisplayEntity(
                EntityType.TEXT_DISPLAY,
                world
        );

        textDisplay.setPosition(x, y, z);
        textDisplay.setText(Text.literal(text));

        // Configuration visuelle
        textDisplay.setTextOpacity((byte) 255); // Opacité maximale
        textDisplay.setBillboardMode(DisplayEntity.BillboardMode.CENTER); // Face toujours le joueur
        textDisplay.setBackground(0x40000000); // Fond semi-transparent noir

        // Taille et transformation
        textDisplay.setTransformation(new AffineTransformation(
                new Vector3f(0, 0, 0),
                new Quaternionf(),
                new Vector3f(1.5f, 1.5f, 1.5f), // Scale 1.5x
                new Quaternionf()
        ));

        world.spawnEntity(textDisplay);

        return textDisplay.getUuid();
    }

    /**
     * Récupère le nom d'un joueur depuis son UUID
     */
    private String getPlayerName(UUID uuid) {
        var player = world.getServer().getPlayerManager().getPlayer(uuid);
        if (player != null) {
            return player.getName().getString();
        }

        var profile = world.getServer().getUserCache().getByUuid(uuid).orElse(null);
        if (profile != null) {
            return profile.getName();
        }

        return uuid.toString();
    }
}