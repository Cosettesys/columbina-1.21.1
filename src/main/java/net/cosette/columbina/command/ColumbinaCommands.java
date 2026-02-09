package net.cosette.columbina.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.cosette.columbina.team.TeamManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.argument.EntityArgumentType;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import com.mojang.authlib.GameProfile;

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
                        .requires(source -> source.hasPermissionLevel(2)) // Require OP level 2
                        .then(
                                literal("team")
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
                                                                                    // Récupère le joueur connecté
                                                                                    ServerPlayerEntity player = context.getSource().getServer().getPlayerManager().getPlayer(uuid);
                                                                                    String playerName;
                                                                                    if (player != null) {
                                                                                        // si Joueur connecté
                                                                                        playerName = player.getName().getString();
                                                                                    } else {
                                                                                        // Joueur déconnecté - pas de recherche en cache
                                                                                        com.mojang.authlib.GameProfile profile = context.getSource().getServer().getUserCache().getByUuid(uuid).orElse(null);
                                                                                        if (profile != null) {
                                                                                            playerName = profile.getName();
                                                                                        } else {
                                                                                            playerName = uuid.toString(); // Fallback uuid si introuvable
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
                                                                                                argument("value", IntegerArgumentType.integer())
                                                                                                        .executes(ctx -> {
                                                                                                            String team = StringArgumentType.getString(ctx, "team");
                                                                                                            int value = IntegerArgumentType.getInteger(ctx, "value");
                                                                                                            TeamManager tm = TeamManager.getInstance();

                                                                                                            if (!tm.addPoints(team, value)) {
                                                                                                                ctx.getSource().sendError(Text.literal("§cImpossible d'ajouter des points (équipe inexistante)."));
                                                                                                                return 0;
                                                                                                            }

                                                                                                            ctx.getSource().sendFeedback(
                                                                                                                    () -> Text.literal("§a+" + value + " §epoints pour l'équipe " + team),
                                                                                                                    true
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
                                                                                                            return 1;
                                                                                                        })
                                                                                        )
                                                                        )
                                                        )
                                        )
                        )
        );
    }
}