package net.cosette.columbina.mysterychest;

import net.cosette.columbina.ColumbinaConfig;
import net.cosette.columbina.team.TeamManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Coffre mystère : posé manuellement par les admins, donne des points
 * (aléatoires entre un min/max configurable selon la taille) au clic droit.
 * Utilisable une seule fois PAR COFFRE (position), pas par joueur.
 * Nécessite d'être dans une équipe pour en profiter — sinon message d'erreur
 * et le coffre reste intact (ne consomme pas son unique utilisation).
 */
public class MysteryChestBlock extends Block {

    public enum ChestSize {
        PETIT, MOYEN, GRAND
    }

    private final ChestSize size;

    public MysteryChestBlock(Settings settings, ChestSize size) {
        super(settings);
        this.size = size;
    }

    // NOTE: en 1.21.1 la signature de Block#onUse n'a plus de paramètre Hand
    // (le usage sur bloc lié à l'item est géré côté Item#useOnBlock).
    // Si Eclipse te signale une erreur de @Override ici, fais un clic droit
    // sur la méthode > Source > Override/Implement Methods pour récupérer
    // la signature exacte de tes mappings Yarn et ajuste en conséquence.
    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos,
                                 PlayerEntity player, BlockHitResult hit) {
        if (world.isClient()) {
            return ActionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayerEntity serverPlayer)) {
            return ActionResult.PASS;
        }

        ServerWorld serverWorld = (ServerWorld) world;
        MysteryChestSavedData data = MysteryChestSavedData.get(serverWorld);

        if (data.isUsed(pos)) {
            serverPlayer.sendMessage(Text.literal("§7Ce coffre a déjà été ouvert."), false);
            return ActionResult.CONSUME;
        }

        String team = TeamManager.getInstance().getPlayerTeam(serverPlayer);
        if (team == null) {
            // Pas d'équipe -> pas de points, et le coffre N'EST PAS consommé.
            serverPlayer.sendMessage(Text.literal("§cTu n'es dans aucune équipe !"), false);
            return ActionResult.CONSUME;
        }

        int points = rollPoints();
        data.markUsed(pos);
        TeamManager.getInstance().addPoints(team, points);
        serverPlayer.sendMessage(Text.literal(
                "§a+" + points + " pts §7pour l'équipe §r" + team + " §7— coffre mystère"), false);

        return ActionResult.CONSUME;
    }

    private int rollPoints() {
        ColumbinaConfig config = ColumbinaConfig.getInstance();
        int min;
        int max;
        switch (size) {
            case PETIT -> {
                min = config.getChestPetitMin();
                max = config.getChestPetitMax();
            }
            case MOYEN -> {
                min = config.getChestMoyenMin();
                max = config.getChestMoyenMax();
            }
            default -> {
                min = config.getChestGrandMin();
                max = config.getChestGrandMax();
            }
        }
        if (max < min) {
            int tmp = min;
            min = max;
            max = tmp;
        }
        return ThreadLocalRandom.current().nextInt(min, max + 1);
    }
}