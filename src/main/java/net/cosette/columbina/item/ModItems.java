package net.cosette.columbina.item;

import net.cosette.columbina.Columbina;
import net.cosette.columbina.scoreboard.ScoreboardManager;
import net.cosette.columbina.team.TeamManager;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.TypedActionResult;
import net.minecraft.world.World;

import net.minecraft.registry.Registry;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;
import net.minecraft.text.Text;

public class ModItems {
    // Déclaration du Token
    public static final Item TOKEN = new Item(new Item.Settings()) {
        @Override
        public TypedActionResult<ItemStack> use(World world, PlayerEntity player, Hand hand) {
            ItemStack stack = player.getStackInHand(hand);

            if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
                return handleTokenUse(serverPlayer, stack);
            }
            return TypedActionResult.pass(stack);
        }
    };

    // Enregistrement de l'item
    public static void registerItems() {
        Registry.register(Registries.ITEM, Identifier.of(Columbina.MOD_ID, "token"), TOKEN);
    }

    // Gestion du clic droit
    private static TypedActionResult<ItemStack> handleTokenUse(ServerPlayerEntity player, ItemStack stack) {
        String teamName = TeamManager.getInstance().getPlayerTeam(player);
        if (teamName == null) {
            player.sendMessage(Text.literal("Tu n'es dans aucune team !"), false);
            return TypedActionResult.fail(stack);
        }
        TeamManager.getInstance().addPoints(teamName, 1);

        // Rafraîchir les scoreboards après ajout de points
        ScoreboardManager.getInstance().updateAllScoreboards();

        if (!player.isCreative()) {
            stack.decrement(1);
        }
        player.sendMessage(Text.literal("1 point ajouté à ta team " + teamName + "!"), false);
        return TypedActionResult.success(stack);
    }
}