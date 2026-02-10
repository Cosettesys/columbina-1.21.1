package net.cosette.columbina.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.cosette.columbina.scoreboard.ScoreboardManager;
import net.cosette.columbina.team.TeamManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

public class ColumbinaCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        registerCommands(dispatcher)
        );
    }
    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(
                literal("columbina")
                        .then(
                                literal("team")
                                        .requires(source -> source.hasPermissionLevel(4))
                                        .then(
                                                literal("create")
                                                        .then(
                                                                argument("name", StringArgumentType.word())
                                                                        .executes(context -> {
                                                                            String name = StringArgumentType.getString(context, "name");
                                                                            boolean success = TeamManager.getInstance().createTeam(name);
                                                                            if (success) {
                                                                                context.getSource().sendFeedback(
                                                                                        () -> Text.literal("§aÉquipe créée : " + name),
                                                                                        true
                                                                                );
                                                                            } else {
                                                                                context.getSource().sendError(
                                                                                        Text.literal("§cCette équipe existe déjà.")
                                                                                );
                                                                            }
                                                                            return 1;
                                                                        })
                                                        )
                                        )
                                        .then(
                                                literal("join")
                                                        .then(
                                                                argument("team", StringArgumentType.word())
                                                                        .then(
                                                                                argument("player", EntityArgumentType.player())
                                                                                        .executes(context -> {
                                                                                            String teamName = StringArgumentType.getString(context, "team");
                                                                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                                                                            boolean success = TeamManager.getInstance().joinTeam(player, teamName);
                                                                                            if (success) {
                                                                                                context.getSource().sendFeedback(
                                                                                                        () -> Text.literal("§a" + player.getName().getString() + " a rejoint l'équipe " + teamName),
                                                                                                        true
                                                                                                );
                                                                                            } else {
                                                                                                context.getSource().sendError(
                                                                                                        Text.literal("§cImpossible d'ajouter ce joueur à l'équipe (équipe inexistante ou joueur déjà dans une équipe).")
                                                                                                );
                                                                                            }
                                                                                            return 1;
                                                                                        })
                                                                        )
                                                        )
                                        )
                                        .then(
                                                literal("leave")
                                                        .then(
                                                                argument("player", EntityArgumentType.player())
                                                                        .executes(context -> {
                                                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(context, "player");
                                                                            boolean success = TeamManager.getInstance().leaveTeam(player);
                                                                            if (success) {
                                                                                context.getSource().sendFeedback(
                                                                                        () -> Text.literal("§e" + player.getName().getString() + " a quitté son équipe."),
                                                                                        true
                                                                                );
                                                                            } else {
                                                                                context.getSource().sendError(
                                                                                        Text.literal("§cCe joueur n'est dans aucune équipe.")
                                                                                );
                                                                            }
                                                                            return 1;
                                                                        })
                                                        )
                                        )
                                        .then(
                                                literal("list")
                                                        .executes(context -> {
                                                            TeamManager tm = TeamManager.getInstance();
                                                            Set<String> teams = tm.getAllTeams();
                                                            if (teams.isEmpty()) {
                                                                context.getSource().sendFeedback(
                                                                        () -> Text.literal("§eAucune équipe n'existe pour le moment."),
                                                                        false
                                                                );
                                                                return 1;
                                                            }
                                                            context.getSource().sendFeedback(
                                                                    () -> Text.literal("§6=== Liste des équipes ==="),
                                                                    false
                                                            );
                                                            for (String team : teams) {
                                                                int points = tm.getPoints(team);
                                                                int memberCount = tm.getTeamMembers(team).size();
                                                                context.getSource().sendFeedback(
                                                                        () -> Text.literal("§e" + team + " §7- §a" + points + " points §7- §b" + memberCount + " membre(s)"),
                                                                        false
                                                                );
                                                            }
                                                            return 1;
                                                        })
                                        )
                                        .then(
                                                literal("info")
                                                        .then(
                                                                argument("team", StringArgumentType.word())
                                                                        .executes(context -> {
                                                                            String teamName = StringArgumentType.getString(context, "team");
                                                                            TeamManager tm = TeamManager.getInstance();
                                                                            if (!tm.teamExists(teamName)) {
                                                                                context.getSource().sendError(
                                                                                        Text.literal("§cCette équipe n'existe pas.")
                                                                                );
                                                                                return 0;
                                                                            }
                                                                            int points = tm.getPoints(teamName);
                                                                            List<UUID> memberUUIDs = tm.getTeamMembers(teamName);
                                                                            context.getSource().sendFeedback(
                                                                                    () -> Text.literal("§6=== " + teamName + " ==="),
                                                                                    false
                                                                            );
                                                                            context.getSource().sendFeedback(
                                                                                    () -> Text.literal("§eNombre de points : §a" + points),
                                                                                    false
                                                                            );
                                                                            if (memberUUIDs.isEmpty()) {
                                                                                context.getSource().sendFeedback(
                                                                                        () -> Text.literal("§eMembres : §7Aucun"),
                                                                                        false
                                                                                );
                                                                            } else {
                                                                                StringBuilder members = new StringBuilder("§eMembres : §b");
                                                                                for (int i = 0; i < memberUUIDs.size(); i++) {
                                                                                    UUID uuid = memberUUIDs.get(i);
                                                                                    ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(uuid);
                                                                                    String playerName;
                                                                                    if (player != null) {
                                                                                        playerName = player.getName().getString();
                                                                                    } else {
                                                                                        com.mojang.authlib.GameProfile profile = context.getSource().getServer().getUserCache().getByUuid(uuid).orElse(null);
                                                                                        if (profile != null) {
                                                                                            playerName = profile.getName();
                                                                                        } else {
                                                                                            playerName = uuid.toString();
                                                                                        }
                                                                                    }
                                                                                    members.append(playerName);
                                                                                    if (i < memberUUIDs.size() - 1) {
                                                                                        members.append("§7, §b");
                                                                                    }
                                                                                }
                                                                                String finalMembers = members.toString();
                                                                                context.getSource().sendFeedback(
                                                                                        () -> Text.literal(finalMembers),
                                                                                        false
                                                                                );
                                                                            }
                                                                            return 1;
                                                                        })
                                                        )
                                        )
                                        .then(
                                                literal("color")
                                                        .then(
                                                                literal("set")
                                                                        .then(
                                                                                argument("team", StringArgumentType.word())
                                                                                        .then(
                                                                                                argument("color", StringArgumentType.word())
                                                                                                        .suggests((ctx, builder) -> {
                                                                                                            return builder
                                                                                                                    .suggest("red")
                                                                                                                    .suggest("blue")
                                                                                                                    .suggest("green")
                                                                                                                    .suggest("yellow")
                                                                                                                    .suggest("aqua")
                                                                                                                    .suggest("gold")
                                                                                                                    .suggest("light_purple")
                                                                                                                    .suggest("dark_red")
                                                                                                                    .suggest("dark_blue")
                                                                                                                    .suggest("dark_green")
                                                                                                                    .suggest("dark_aqua")
                                                                                                                    .suggest("dark_purple")
                                                                                                                    .suggest("white")
                                                                                                                    .suggest("gray")
                                                                                                                    .suggest("dark_gray")
                                                                                                                    .suggest("black")
                                                                                                                    .buildFuture();
                                                                                                        })
                                                                                                        .executes(ctx -> {
                                                                                                            String team = StringArgumentType.getString(ctx, "team");
                                                                                                            String colorName = StringArgumentType.getString(ctx, "color");
                                                                                                            TeamManager tm = TeamManager.getInstance();
                                                                                                            if (!tm.teamExists(team)) {
                                                                                                                ctx.getSource().sendError(
                                                                                                                        Text.literal("§cCette équipe n'existe pas.")
                                                                                                                );
                                                                                                                return 0;
                                                                                                            }
                                                                                                            Formatting color = Formatting.byName(colorName.toLowerCase());
                                                                                                            if (color == null || !color.isColor()) {
                                                                                                                ctx.getSource().sendError(
                                                                                                                        Text.literal("§cCouleur invalide. Utilisez: red, blue, green, yellow, aqua, gold, etc.")
                                                                                                                );
                                                                                                                return 0;
                                                                                                            }
                                                                                                            tm.setTeamColor(team, color);
                                                                                                            ScoreboardManager.getInstance().updateAllScoreboards();
                                                                                                            ctx.getSource().sendFeedback(
                                                                                                                    () -> Text.literal("§eCouleur de l'équipe " + team + " définie à ").append(Text.literal(colorName).formatted(color)),
                                                                                                                    true
                                                                                                            );

                                                                                                            return 1;
                                                                                                        })
                                                                                        )
                                                                        )
                                                        )
                                        )
                                        .then(
                                                literal("points")
                                                        .then(
                                                                literal("get")
                                                                        .then(
                                                                                argument("team", StringArgumentType.word())
                                                                                        .executes(ctx -> {
                                                                                            String team = StringArgumentType.getString(ctx, "team");
                                                                                            TeamManager tm = TeamManager.getInstance();
                                                                                            if (!tm.teamExists(team)) {
                                                                                                ctx.getSource().sendError(Text.literal("§cCette équipe n'existe pas."));
                                                                                                return 0;
                                                                                            }
                                                                                            int points = tm.getPoints(team);
                                                                                            ctx.getSource().sendFeedback(
                                                                                                    () -> Text.literal("§eL'équipe " + team + " a §a" + points + " §epoints"),
                                                                                                    false
                                                                                            );
                                                                                            return 1;
                                                                                        })
                                                                        )
                                                        )
                                                        .then(
                                                                literal("add")
                                                                        .then(
                                                                                argument("team", StringArgumentType.word())
                                                                                        .then(
                                                                                                argument("value", IntegerArgumentType.integer(1))
                                                                                                        .executes(ctx -> {
                                                                                                            String team = StringArgumentType.getString(ctx, "team");
                                                                                                            int value = IntegerArgumentType.getInteger(ctx, "value");
                                                                                                            TeamManager tm = TeamManager.getInstance();

                                                                                                            if (!tm.teamExists(team)) {
                                                                                                                ctx.getSource().sendError(Text.literal("§cCette équipe n'existe pas."));
                                                                                                                return 0;
                                                                                                            }

                                                                                                            tm.addPoints(team, value);
                                                                                                            ScoreboardManager.getInstance().updateAllScoreboards();

                                                                                                            ctx.getSource().sendFeedback(
                                                                                                                    () -> Text.literal("§a+" + value + " §epoints pour l'équipe " + team),
                                                                                                                    true
                                                                                                            );
                                                                                                            return 1;
                                                                                                        })
                                                                                        )
                                                                        )
                                                                        .then(
                                                                                argument("player", EntityArgumentType.player())
                                                                                        .then(
                                                                                                argument("value", IntegerArgumentType.integer(1))
                                                                                                        .executes(ctx -> {
                                                                                                            ServerPlayerEntity player = EntityArgumentType.getPlayer(ctx, "player");
                                                                                                            int value = IntegerArgumentType.getInteger(ctx, "value");
                                                                                                            TeamManager tm = TeamManager.getInstance();

                                                                                                            String teamName = tm.getPlayerTeam(player);
                                                                                                            if (teamName == null) {
                                                                                                                ctx.getSource().sendError(
                                                                                                                        Text.literal("§c" + player.getName().getString() + " n'est dans aucune équipe.")
                                                                                                                );
                                                                                                                return 0;
                                                                                                            }

                                                                                                            tm.addPoints(teamName, value);
                                                                                                            ScoreboardManager.getInstance().updateAllScoreboards();

                                                                                                            ctx.getSource().sendFeedback(
                                                                                                                    () -> Text.literal("§a+" + value + " §epoints ajoutés à l'équipe " + teamName + " via " + player.getName().getString()),
                                                                                                                    true
                                                                                                            );
                                                                                                            player.sendMessage(
                                                                                                                    Text.literal("§aTu as gagné §6" + value + " §apoints pour ton équipe !"),
                                                                                                                    false
                                                                                                            );
                                                                                                            return 1;
                                                                                                        })
                                                                                        )
                                                                        )
                                                        )
                                                        .then(
                                                                literal("set")
                                                                        .then(
                                                                                argument("team", StringArgumentType.word())
                                                                                        .then(
                                                                                                argument("value", IntegerArgumentType.integer())
                                                                                                        .executes(ctx -> {
                                                                                                            String team = StringArgumentType.getString(ctx, "team");
                                                                                                            int value = IntegerArgumentType.getInteger(ctx, "value");
                                                                                                            TeamManager tm = TeamManager.getInstance();
                                                                                                            if (!tm.setPoints(team, value)) {
                                                                                                                ctx.getSource().sendError(Text.literal("§cImpossible de définir les points (équipe inexistante)."));
                                                                                                                return 0;
                                                                                                            }
                                                                                                            ctx.getSource().sendFeedback(
                                                                                                                    () -> Text.literal("§ePoints de l'équipe " + team + " définis à §a" + value),
                                                                                                                    true
                                                                                                            );
                                                                                                            ScoreboardManager.getInstance().updateAllScoreboards();
                                                                                                            return 1;
                                                                                                        })
                                                                                        )
                                                                        )
                                                        )
                                        )
                        )
                        .then(
                                literal("pay")
                                        .then(
                                                argument("player", EntityArgumentType.player())
                                                        .then(
                                                                argument("value", IntegerArgumentType.integer(1))
                                                                        .executes(ctx -> {
                                                                            ServerPlayerEntity sender = ctx.getSource().getPlayerOrThrow();
                                                                            ServerPlayerEntity receiver = EntityArgumentType.getPlayer(ctx, "player");
                                                                            int value = IntegerArgumentType.getInteger(ctx, "value");
                                                                            TeamManager tm = TeamManager.getInstance();

                                                                            String senderTeam = tm.getPlayerTeam(sender);
                                                                            if (senderTeam == null) {
                                                                                ctx.getSource().sendError(
                                                                                        Text.literal("§cTu n'es dans aucune équipe.")
                                                                                );
                                                                                return 0;
                                                                            }
                                                                            String receiverTeam = tm.getPlayerTeam(receiver);
                                                                            if (receiverTeam == null) {
                                                                                ctx.getSource().sendError(
                                                                                        Text.literal("§c" + receiver.getName().getString() + " n'est dans aucune équipe.")
                                                                                );
                                                                                return 0;
                                                                            }
                                                                            int currentPoints = tm.getPoints(senderTeam);
                                                                            if (currentPoints < value) {
                                                                                ctx.getSource().sendError(
                                                                                        Text.literal("§cTon équipe n'a pas assez de points. Solde actuel : §6" + currentPoints)
                                                                                );
                                                                                return 0;
                                                                            }
                                                                            tm.addPoints(senderTeam, -value);
                                                                            tm.addPoints(receiverTeam, value);
                                                                            ScoreboardManager.getInstance().updateAllScoreboards();
                                                                            ctx.getSource().sendFeedback(
                                                                                    () -> Text.literal("§eTu as payé §6" + value + " §epoints à l'équipe de " + receiver.getName().getString()),
                                                                                    false
                                                                            );
                                                                            receiver.sendMessage(
                                                                                    Text.literal("§aTon équipe a reçu §6" + value + " §apoints de " + sender.getName().getString()),
                                                                                    false
                                                                            );
                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )
                        .then(
                                literal("token")
                                        .then(
                                                literal("take")
                                                        .then(
                                                                argument("value", IntegerArgumentType.integer(1, 64))
                                                                        .executes(ctx -> {
                                                                            ServerPlayerEntity player = ctx.getSource().getPlayerOrThrow();
                                                                            int value = IntegerArgumentType.getInteger(ctx, "value");
                                                                            TeamManager tm = TeamManager.getInstance();
                                                                            String teamName = tm.getPlayerTeam(player);
                                                                            if (teamName == null) {
                                                                                ctx.getSource().sendError(
                                                                                        Text.literal("§cTu n'es dans aucune équipe.")
                                                                                );
                                                                                return 0;
                                                                            }
                                                                            int currentPoints = tm.getPoints(teamName);
                                                                            if (currentPoints < value) {
                                                                                ctx.getSource().sendError(
                                                                                        Text.literal("§cTon équipe n'a pas assez de points. Solde actuel : §6" + currentPoints)
                                                                                );
                                                                                return 0;
                                                                            }
                                                                            net.minecraft.entity.player.PlayerInventory inventory = player.getInventory();
                                                                            boolean hasSpace = false;
                                                                            for (int slot = 0; slot < inventory.size(); slot++) {
                                                                                net.minecraft.item.ItemStack stackInSlot = inventory.getStack(slot);
                                                                                if (stackInSlot.isEmpty()) {
                                                                                    hasSpace = true;
                                                                                    break;
                                                                                } else if (stackInSlot.getItem() == net.cosette.columbina.item.ModItems.TOKEN
                                                                                        && stackInSlot.getCount() + value <= stackInSlot.getMaxCount()) {
                                                                                    // Stack de tokens avec assez de place pour empiler
                                                                                    hasSpace = true;
                                                                                    break;
                                                                                }
                                                                            }
                                                                            if (!hasSpace) {
                                                                                ctx.getSource().sendError(
                                                                                        Text.literal("§cTon inventaire est plein ! Libère de l'espace avant de retirer des tokens.")
                                                                                );
                                                                                return 0;
                                                                            }
                                                                            net.minecraft.item.ItemStack tokenStack = new net.minecraft.item.ItemStack(
                                                                                    net.cosette.columbina.item.ModItems.TOKEN,
                                                                                    value
                                                                            );
                                                                            if (!player.giveItemStack(tokenStack)) {
                                                                                ctx.getSource().sendError(
                                                                                        Text.literal("§cErreur lors du retrait des tokens.")
                                                                                );
                                                                                return 0;
                                                                            }
                                                                            tm.addPoints(teamName, -value);
                                                                            ScoreboardManager.getInstance().updateAllScoreboards();

                                                                            ctx.getSource().sendFeedback(
                                                                                    () -> Text.literal("§eTu as retiré §c" + value + " §epoints de ton équipe et reçu §6" + value + " §etoken(s)"),
                                                                                    false
                                                                            );
                                                                            return 1;
                                                                        })
                                                        )
                                        )
                        )
                        .then(
                                literal("scoreboard")
                                        .requires(source -> source.hasPermissionLevel(4))
                                        .then(
                                                literal("spawn")
                                                        .then(
                                                                argument("name", StringArgumentType.word())
                                                                        .then(
                                                                                argument("x", DoubleArgumentType.doubleArg())
                                                                                        .then(
                                                                                                argument("y", DoubleArgumentType.doubleArg())
                                                                                                        .then(
                                                                                                                argument("z", DoubleArgumentType.doubleArg())
                                                                                                                        .then(
                                                                                                                                argument("teamOrList", StringArgumentType.word())
                                                                                                                                        .executes(ctx -> {
                                                                                                                                            String name = StringArgumentType.getString(ctx, "name");
                                                                                                                                            double x = DoubleArgumentType.getDouble(ctx, "x");
                                                                                                                                            double y = DoubleArgumentType.getDouble(ctx, "y");
                                                                                                                                            double z = DoubleArgumentType.getDouble(ctx, "z");
                                                                                                                                            String teamOrList = StringArgumentType.getString(ctx, "teamOrList");
                                                                                                                                            ScoreboardManager sm = ScoreboardManager.getInstance();
                                                                                                                                            boolean success;
                                                                                                                                            if ("list".equalsIgnoreCase(teamOrList)) {
                                                                                                                                                success = sm.spawnListScoreboard(name, x, y, z);
                                                                                                                                                if (success) {
                                                                                                                                                    ctx.getSource().sendFeedback(
                                                                                                                                                            () -> Text.literal("§aScoreboard de classement '" + name + "' créé à " + x + ", " + y + ", " + z),
                                                                                                                                                            true
                                                                                                                                                    );
                                                                                                                                                } else {
                                                                                                                                                    ctx.getSource().sendError(
                                                                                                                                                            Text.literal("§cUn scoreboard avec ce nom existe déjà.")
                                                                                                                                                    );
                                                                                                                                                }
                                                                                                                                            } else {
                                                                                                                                                success = sm.spawnTeamScoreboard(name, x, y, z, teamOrList);
                                                                                                                                                if (success) {
                                                                                                                                                    ctx.getSource().sendFeedback(
                                                                                                                                                            () -> Text.literal("§aScoreboard pour l'équipe '" + teamOrList + "' créé à " + x + ", " + y + ", " + z),
                                                                                                                                                            true
                                                                                                                                                    );
                                                                                                                                                } else {
                                                                                                                                                    ctx.getSource().sendError(
                                                                                                                                                            Text.literal("§cUn scoreboard avec ce nom existe déjà ou l'équipe n'existe pas.")
                                                                                                                                                    );
                                                                                                                                                }
                                                                                                                                            }
                                                                                                                                            return success ? 1 : 0;
                                                                                                                                        })
                                                                                                                        )
                                                                                                        )
                                                                                        )
                                                                        )
                                                        )
                                        )
                                        .then(
                                                literal("delete")
                                                        .then(
                                                                argument("name", StringArgumentType.word())
                                                                        .executes(ctx -> {
                                                                            String name = StringArgumentType.getString(ctx, "name");
                                                                            ScoreboardManager sm = ScoreboardManager.getInstance();
                                                                            boolean success = sm.deleteScoreboard(name);
                                                                            if (success) {
                                                                                ctx.getSource().sendFeedback(
                                                                                        () -> Text.literal("§eScoreboard '" + name + "' supprimé."),
                                                                                        true
                                                                                );
                                                                            } else {
                                                                                ctx.getSource().sendError(
                                                                                        Text.literal("§cAucun scoreboard avec ce nom n'existe.")
                                                                                );
                                                                            }
                                                                            return success ? 1 : 0;
                                                                        })
                                                        )
                                        )
                                        .then(
                                                literal("refresh")
                                                        .executes(ctx -> {
                                                            ScoreboardManager.getInstance().updateAllScoreboards();
                                                            ctx.getSource().sendFeedback(
                                                                    () -> Text.literal("§aTous les scoreboards ont été rafraîchis."),
                                                                    true
                                                            );
                                                            return 1;
                                                        })
                                                        .then(
                                                                argument("name", StringArgumentType.word())
                                                                        .executes(ctx -> {
                                                                            String name = StringArgumentType.getString(ctx, "name");
                                                                            ScoreboardManager sm = ScoreboardManager.getInstance();
                                                                            boolean success = sm.updateScoreboard(name);
                                                                            if (success) {
                                                                                ctx.getSource().sendFeedback(
                                                                                        () -> Text.literal("§eScoreboard '" + name + "' rafraîchi."),
                                                                                        true
                                                                                );
                                                                            } else {
                                                                                ctx.getSource().sendError(
                                                                                        Text.literal("§cAucun scoreboard avec ce nom n'existe.")
                                                                                );
                                                                            }
                                                                            return success ? 1 : 0;
                                                                        })
                                                        )
                                        )
                        )
        );
    }
}