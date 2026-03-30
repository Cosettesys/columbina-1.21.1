package net.cosette.columbina.scoreboard;

import net.cosette.columbina.team.TeamManager;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.AffineTransformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import java.util.*;
import java.util.stream.Collectors;

public class ScoreboardManager {
    private static final ScoreboardManager INSTANCE = new ScoreboardManager();
    private static class ScoreboardData {
        UUID entityId;
        String type;
        String teamName;
        String dimensionKey;
        ScoreboardData(UUID entityId, String type, String teamName, String dimensionKey) {
            this.entityId = entityId;
            this.type = type;
            this.teamName = teamName;
            this.dimensionKey = dimensionKey;
        }
    }
    private final Map<String, ScoreboardData> scoreboards = new HashMap<>();
    private MinecraftServer server;
    private ScoreboardManager() {}
    public static ScoreboardManager getInstance() {
        return INSTANCE;
    }
    public void init(ServerWorld world) {
        this.server = world.getServer();
        load(world);
    }
    private void load(ServerWorld anyWorld) {
        ServerWorld overworld = server.getOverworld();
        ScoreboardSavedData data = ScoreboardSavedData.get(overworld);
        scoreboards.clear();
        for (Map.Entry<String, ScoreboardSavedData.ScoreboardEntry> entry : data.getScoreboards().entrySet()) {
            ScoreboardSavedData.ScoreboardEntry saved = entry.getValue();
            scoreboards.put(entry.getKey(),
                    new ScoreboardData(saved.entityId, saved.type, saved.teamName, saved.dimensionKey));
        }
    }
    private void save() {
        if (server == null) return;
        ServerWorld overworld = server.getOverworld();
        ScoreboardSavedData data = ScoreboardSavedData.get(overworld);
        data.getScoreboards().clear();
        for (Map.Entry<String, ScoreboardData> entry : scoreboards.entrySet()) {
            ScoreboardData d = entry.getValue();
            data.getScoreboards().put(entry.getKey(),
                    new ScoreboardSavedData.ScoreboardEntry(d.entityId, d.type, d.teamName, d.dimensionKey));
        }
        data.markDirty();
    }
    private ServerWorld getWorldForKey(String dimensionKey) {
        if (server == null) return null;
        RegistryKey<net.minecraft.world.World> key = RegistryKey.of(
                RegistryKeys.WORLD,
                Identifier.of(dimensionKey)
        );
        return server.getWorld(key);
    }
    public boolean spawnTeamScoreboard(String name, double x, double y, double z, String teamName, ServerWorld world) {
        if (scoreboards.containsKey(name)) return false;
        TeamManager tm = TeamManager.getInstance();
        if (!tm.teamExists(teamName)) return false;
        String dimKey = world.getRegistryKey().getValue().toString();
        String text = buildTeamScoreboardText(teamName);
        UUID entityId = spawnTextDisplay(world, x, y, z, text);
        scoreboards.put(name, new ScoreboardData(entityId, "team", teamName, dimKey));
        save();
        return true;
    }
    public boolean spawnListScoreboard(String name, double x, double y, double z, ServerWorld world) {
        if (scoreboards.containsKey(name)) return false;

        String dimKey = world.getRegistryKey().getValue().toString();
        String text = buildListScoreboardText();
        UUID entityId = spawnTextDisplay(world, x, y, z, text);
        scoreboards.put(name, new ScoreboardData(entityId, "list", null, dimKey));
        save();
        return true;
    }
    public boolean deleteScoreboard(String name) {
        ScoreboardData data = scoreboards.get(name);
        if (data == null) return false;
        ServerWorld world = getWorldForKey(data.dimensionKey);
        if (world != null) {
            DisplayEntity.TextDisplayEntity entity =
                    (DisplayEntity.TextDisplayEntity) world.getEntity(data.entityId);
            if (entity != null) entity.discard();
        }
        scoreboards.remove(name);
        save();
        return true;
    }
    public void updateAllScoreboards() {
        for (Map.Entry<String, ScoreboardData> entry : scoreboards.entrySet()) {
            ScoreboardData data = entry.getValue();
            ServerWorld world = getWorldForKey(data.dimensionKey);
            if (world == null) continue;
            DisplayEntity.TextDisplayEntity entity =
                    (DisplayEntity.TextDisplayEntity) world.getEntity(data.entityId);
            if (entity == null) continue;
            String newText = "team".equals(data.type)
                    ? buildTeamScoreboardText(data.teamName)
                    : buildListScoreboardText();
            entity.setText(Text.literal(newText));
        }
    }
    public boolean updateScoreboard(String name) {
        ScoreboardData data = scoreboards.get(name);
        if (data == null) return false;
        ServerWorld world = getWorldForKey(data.dimensionKey);
        if (world == null) return false;
        DisplayEntity.TextDisplayEntity entity =
                (DisplayEntity.TextDisplayEntity) world.getEntity(data.entityId);
        if (entity == null) return false;
        String newText = "team".equals(data.type)
                ? buildTeamScoreboardText(data.teamName)
                : buildListScoreboardText();
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
        text.append(color).append("║  ").append(teamName.toUpperCase()).append("  ║\n");
        text.append(color).append("╠═══════════════════╣\n");
        text.append(Formatting.WHITE).append("  Points: ").append(Formatting.GOLD).append(points).append("\n\n");
        text.append(Formatting.WHITE).append("  Membres:\n");
        if (members.isEmpty()) {
            text.append(Formatting.GRAY).append("  • Aucun\n");
        } else {
            for (UUID uuid : members) {
                text.append(color).append("  • ").append(getPlayerName(uuid)).append("\n");
            }
        }
        text.append(color).append("╚═══════════════════╝");
        return text.toString();
    }
    private String buildListScoreboardText() {
        TeamManager tm = TeamManager.getInstance();
        List<String> sortedTeams = tm.getAllTeams().stream()
                .sorted((t1, t2) -> Integer.compare(tm.getPoints(t2), tm.getPoints(t1)))
                .collect(Collectors.toList());
        StringBuilder text = new StringBuilder();
        text.append(Formatting.GOLD).append("╔════════════════════╗\n");
        text.append(Formatting.GOLD).append("║   ").append(Formatting.YELLOW).append("🏆 CLASSEMENT 🏆")
                .append(Formatting.GOLD).append("   ║\n");
        text.append(Formatting.GOLD).append("╠════════════════════╣\n");
        if (sortedTeams.isEmpty()) {
            text.append(Formatting.GRAY).append("  Aucune équipe\n");
        } else {
            int position = 1;
            for (String team : sortedTeams) {
                Formatting color = tm.getTeamColor(team);
                int points = tm.getPoints(team);
                String medal = position == 1 ? "🥇 " : position == 2 ? "🥈 " : position == 3 ? "🥉 " : position + ". ";
                text.append(Formatting.WHITE).append("  ").append(medal)
                        .append(color).append(team)
                        .append(Formatting.GRAY).append(" - ")
                        .append(Formatting.GOLD).append(points).append(" pts\n");
                position++;
            }
        }
        text.append(Formatting.GOLD).append("╚════════════════════╝");
        return text.toString();
    }
    private UUID spawnTextDisplay(ServerWorld world, double x, double y, double z, String text) {
        DisplayEntity.TextDisplayEntity textDisplay = new DisplayEntity.TextDisplayEntity(
                EntityType.TEXT_DISPLAY, world
        );
        textDisplay.setPosition(x, y, z);
        textDisplay.setText(Text.literal(text));
        textDisplay.setTextOpacity((byte) 255);
        textDisplay.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
        textDisplay.setBackground(0x40000000);
        textDisplay.setTransformation(new AffineTransformation(
                new Vector3f(0, 0, 0),
                new Quaternionf(),
                new Vector3f(1.5f, 1.5f, 1.5f),
                new Quaternionf()
        ));
        world.spawnEntity(textDisplay);
        return textDisplay.getUuid();
    }
    private String getPlayerName(UUID uuid) {
        if (server == null) return uuid.toString();
        var player = server.getPlayerManager().getPlayer(uuid);
        if (player != null) return player.getName().getString();
        var profile = server.getUserCache().getByUuid(uuid).orElse(null);
        return profile != null ? profile.getName() : uuid.toString();
    }
}