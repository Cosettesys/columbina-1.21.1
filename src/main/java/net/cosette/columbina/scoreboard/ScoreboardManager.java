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
        load();
    }
    private void load() {
        ScoreboardSavedData data = ScoreboardSavedData.get(world);
        scoreboards.clear();
        for (Map.Entry<String, ScoreboardSavedData.ScoreboardEntry> entry : data.getScoreboards().entrySet()) {
            ScoreboardSavedData.ScoreboardEntry savedEntry = entry.getValue();
            scoreboards.put(
                    entry.getKey(),
                    new ScoreboardData(savedEntry.entityId, savedEntry.type, savedEntry.teamName)
            );
        }
    }
    private void save() {
        if (world == null) return;
        ScoreboardSavedData data = ScoreboardSavedData.get(world);
        data.getScoreboards().clear();
        for (Map.Entry<String, ScoreboardData> entry : scoreboards.entrySet()) {
            ScoreboardData scoreboardData = entry.getValue();
            data.getScoreboards().put(
                    entry.getKey(),
                    new ScoreboardSavedData.ScoreboardEntry(
                            scoreboardData.entityId,
                            scoreboardData.type,
                            scoreboardData.teamName
                    )
            );
        }
        data.markDirty();
    }
    public boolean spawnTeamScoreboard(String name, double x, double y, double z, String teamName) {
        if (scoreboards.containsKey(name)) {
            return false;
        }
        TeamManager tm = TeamManager.getInstance();
        if (!tm.teamExists(teamName)) {
            return false;
        }
        String text = buildTeamScoreboardText(teamName);
        UUID entityId = spawnTextDisplay(x, y, z, text);
        scoreboards.put(name, new ScoreboardData(entityId, "team", teamName));
        save();
        return true;
    }
    public boolean spawnListScoreboard(String name, double x, double y, double z) {
        if (scoreboards.containsKey(name)) {
            return false;
        }
        String text = buildListScoreboardText();
        UUID entityId = spawnTextDisplay(x, y, z, text);
        scoreboards.put(name, new ScoreboardData(entityId, "list", null));
        save();
        return true;
    }
    public boolean deleteScoreboard(String name) {
        ScoreboardData data = scoreboards.get(name);
        if (data == null) {
            return false;
        }
        DisplayEntity.TextDisplayEntity entity = (DisplayEntity.TextDisplayEntity) world.getEntity(data.entityId);
        if (entity != null) {
            entity.discard();
        }
        scoreboards.remove(name);
        save();
        return true;
    }
    public void updateAllScoreboards() {
        for (Map.Entry<String, ScoreboardData> entry : scoreboards.entrySet()) {
            ScoreboardData data = entry.getValue();
            DisplayEntity.TextDisplayEntity entity = (DisplayEntity.TextDisplayEntity) world.getEntity(data.entityId);

            if (entity == null) {
                continue;
            }
            String newText;
            if ("team".equals(data.type)) {
                newText = buildTeamScoreboardText(data.teamName);
            } else {
                newText = buildListScoreboardText();
            }
            entity.setText(Text.literal(newText));
        }
    }
    public boolean updateScoreboard(String name) {
        ScoreboardData data = scoreboards.get(name);
        if (data == null) {
            return false;
        }
        DisplayEntity.TextDisplayEntity entity = (DisplayEntity.TextDisplayEntity) world.getEntity(data.entityId);
        if (entity == null) {
            return false;
        }
        String newText;
        if ("team".equals(data.type)) {
            newText = buildTeamScoreboardText(data.teamName);
        } else {
            newText = buildListScoreboardText();
        }
        entity.setText(Text.literal(newText));
        return true;
    }
    private String buildTeamScoreboardText(String teamName) {
        TeamManager tm = TeamManager.getInstance();
        Formatting color = tm.getTeamColor(teamName);
        int points = tm.getPoints(teamName);
        List<UUID> members = tm.getTeamMembers(teamName);
        StringBuilder text = new StringBuilder();
        text.append(color).append("╔═══════════════════╗\n");
        text.append(color).append("║  ").append(teamName.toUpperCase()).append("\n");
        text.append(color).append("╠═══════════════════╣\n");
        text.append(Formatting.WHITE).append("  Points: ").append(Formatting.GOLD).append(points).append("\n\n");
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
    private String buildListScoreboardText() {
        TeamManager tm = TeamManager.getInstance();
        Set<String> teams = tm.getAllTeams();
        List<String> sortedTeams = teams.stream()
                .sorted((t1, t2) -> Integer.compare(tm.getPoints(t2), tm.getPoints(t1)))
                .collect(Collectors.toList());
        StringBuilder text = new StringBuilder();
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
    private UUID spawnTextDisplay(double x, double y, double z, String text) {
        DisplayEntity.TextDisplayEntity textDisplay = new DisplayEntity.TextDisplayEntity(
                EntityType.TEXT_DISPLAY,
                world
        );
        textDisplay.setPosition(x, y, z);
        textDisplay.setText(Text.literal(text));
        textDisplay.setTextOpacity((byte) 255); // Opacité max
        textDisplay.setBillboardMode(DisplayEntity.BillboardMode.CENTER); // Face toujours le joueur
        textDisplay.setBackground(0x40000000); // Fond semi-transparent noir
        textDisplay.setTransformation(new AffineTransformation(
                new Vector3f(0, 0, 0),
                new Quaternionf(),
                new Vector3f(1.5f, 1.5f, 1.5f), // Scale 1.5x
                new Quaternionf()
        ));
        world.spawnEntity(textDisplay);
        return textDisplay.getUuid();
    }
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