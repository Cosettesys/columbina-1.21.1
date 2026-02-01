package net.cosette.columbina.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.cosette.columbina.team.TeamManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import static net.minecraft.server.command.CommandManager.literal;
import static net.minecraft.server.command.CommandManager.argument;

public class ColumbinaCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register(
                (dispatcher, registryAccess, environment) ->
                        registerCommands(dispatcher)
        );
    }
    private static void registerCommands(CommandDispatcher<ServerCommandSource> dispatcher) {

        dispatcher.register(
                CommandManager.literal("columbina")
                        .then(
                                CommandManager.literal("team")

                                        // /columbina team create <name>
                                        .then(
                                                CommandManager.literal("create")
                                                        .then(
                                                                CommandManager.argument("name", StringArgumentType.word())
                                                                        .executes(context -> {
                                                                            String name = StringArgumentType.getString(context, "name");

                                                                            boolean success = TeamManager.getInstance().createTeam(name);

                                                                            if (success) {
                                                                                context.getSource().sendFeedback(
                                                                                        () -> Text.literal("§aÉquipe créée : " + name),
                                                                                        false
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
                                        // /columbina team join <name>
                                        .then(
                                                CommandManager.literal("join")
                                                        .then(
                                                                CommandManager.argument("name", StringArgumentType.word())
                                                                        .executes(context -> {
                                                                            ServerPlayerEntity player = context.getSource().getPlayer();
                                                                            String name = StringArgumentType.getString(context, "name");

                                                                            boolean success = TeamManager.getInstance().joinTeam(player, name);

                                                                            if (success) {
                                                                                context.getSource().sendFeedback(
                                                                                        () -> Text.literal("§aTu as rejoint l'équipe " + name),
                                                                                        false
                                                                                );
                                                                            } else {
                                                                                context.getSource().sendError(
                                                                                        Text.literal("§cImpossible de rejoindre cette équipe.")
                                                                                );
                                                                            }

                                                                            return 1;
                                                                        })
                                                        )
                                        )
                                        // /columbina team leave
                                        .then(
                                                CommandManager.literal("leave")
                                                        .executes(context -> {
                                                            ServerPlayerEntity player = context.getSource().getPlayer();

                                                            boolean success = TeamManager.getInstance().leaveTeam(player);

                                                            if (success) {
                                                                context.getSource().sendFeedback(
                                                                        () -> Text.literal("§eTu as quitté ta team."),
                                                                        false
                                                                );
                                                            } else {
                                                                context.getSource().sendError(
                                                                        Text.literal("§cTu n'es dans aucune team.")
                                                                );
                                                            }
                                                            return 1;
                                                        })
                                        )
                                        .then(literal("points")
                                                /* =========================
                                                   /columbina team points get <team>
                                                   ========================= */
                                                .then(literal("get")
                                                        .then(argument("team", StringArgumentType.word())
                                                                .executes(ctx -> {
                                                                    String team = StringArgumentType.getString(ctx, "team");
                                                                    TeamManager tm = TeamManager.getInstance();

                                                                    if (!tm.teamExists(team)) {
                                                                        ctx.getSource().sendError(Text.literal("Cette team n'existe pas"));
                                                                        return 0;
                                                                    }

                                                                    int points = tm.getPoints(team);
                                                                    ctx.getSource().sendFeedback(
                                                                            () -> Text.literal("La team " + team + " a " + points + " points"),
                                                                            false
                                                                    );
                                                                    return 1;
                                                                })
                                                        )
                                                )

                                                /* =========================
                                                   /columbina team points add <team> <value>
                                                   ========================= */
                                                .then(literal("add")
                                                        .then(argument("team", StringArgumentType.word())
                                                                .then(argument("value", IntegerArgumentType.integer())
                                                                        .executes(ctx -> {
                                                                            String team = StringArgumentType.getString(ctx, "team");
                                                                            int value = IntegerArgumentType.getInteger(ctx, "value");
                                                                            TeamManager tm = TeamManager.getInstance();

                                                                            if (!tm.addPoints(team, value)) {
                                                                                ctx.getSource().sendError(Text.literal("Impossible d'ajouter des points"));
                                                                                return 0;
                                                                            }

                                                                            ctx.getSource().sendFeedback(
                                                                                    () -> Text.literal("+" + value + " points pour la team " + team),
                                                                                    true
                                                                            );
                                                                            return 1;
                                                                        })
                                                                )
                                                        )
                                                )

                                                /* =========================
                                                   /columbina team points set <team> <value>
                                                   ========================= */
                                                .then(literal("set")
                                                        .then(argument("team", StringArgumentType.word())
                                                                .then(argument("value", IntegerArgumentType.integer())
                                                                        .executes(ctx -> {
                                                                            String team = StringArgumentType.getString(ctx, "team");
                                                                            int value = IntegerArgumentType.getInteger(ctx, "value");
                                                                            TeamManager tm = TeamManager.getInstance();

                                                                            if (!tm.setPoints(team, value)) {
                                                                                ctx.getSource().sendError(Text.literal("Impossible de définir les points"));
                                                                                return 0;
                                                                            }

                                                                            ctx.getSource().sendFeedback(
                                                                                    () -> Text.literal("Points de la team " + team + " définis à " + value),
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
