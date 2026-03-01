package net.cosette.columbina;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.minecraft.server.command.ServerCommandSource;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.RootCommandNode;
import java.lang.reflect.Field;
import java.util.Map;

public class SafariCommandBlocker {
    public static void register() {
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            var dispatcher = server.getCommandManager().getDispatcher();
            RootCommandNode<ServerCommandSource> root = dispatcher.getRoot();
            CommandNode<ServerCommandSource> safariNode = root.getChild("safari");
            if (safariNode == null) return;
            for (String cmd : java.util.List.of("enter", "info", "buy", "reload")) {
                CommandNode<ServerCommandSource> child = safariNode.getChild(cmd);
                if (child != null) {
                    setRequirement(child, source -> source.hasPermissionLevel(4));
                }
            }
            Columbina.LOGGER.info("[Columbina] Commandes Safari restreintes.");
        });
    }
    private static void setRequirement(CommandNode<ServerCommandSource> node, java.util.function.Predicate<ServerCommandSource> requirement) {
        try {
            Field field = CommandNode.class.getDeclaredField("requirement");
            field.setAccessible(true);
            field.set(node, requirement);
            for (CommandNode<ServerCommandSource> child : node.getChildren()) {
                setRequirement(child, requirement);
            }
        } catch (Exception e) {
            Columbina.LOGGER.error("[Columbina] Erreur restriction commande Safari", e);
        }
    }
}