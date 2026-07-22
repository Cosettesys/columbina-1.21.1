package net.cosette.columbina.mysterychest;

import net.cosette.columbina.ColumbinaConfig;
import net.cosette.columbina.team.TeamManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.world.BlockView;
import net.minecraft.world.World;

import java.util.concurrent.ThreadLocalRandom;
public class MysteryChestBlock extends Block {
    public enum ChestSize {
        PETIT, MOYEN, GRAND
    }
    public static final EnumProperty<Direction> FACING = Properties.HORIZONTAL_FACING;
    private static final VoxelShape SHAPE = Block.createCuboidShape(5, 0, 5, 11, 6, 11);
    private final ChestSize size;
    public MysteryChestBlock(Settings settings, ChestSize size) {
        super(settings);
        this.size = size;
        setDefaultState(getStateManager().getDefaultState().with(FACING, Direction.NORTH));
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        return getDefaultState().with(FACING, ctx.getHorizontalPlayerFacing().getOpposite());
    }
    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world,
                                      BlockPos pos, ShapeContext ctx) {
        return SHAPE;
    }
    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world,
                                        BlockPos pos, ShapeContext ctx) {
        return SHAPE;
    }
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

        if (data.hasOpened(serverPlayer.getUuid(), pos)) {
            serverPlayer.sendMessage(Text.literal("§7Tu as déjà ouvert ce coffre."), false);
            return ActionResult.CONSUME;
        }
        String team = TeamManager.getInstance().getPlayerTeam(serverPlayer);
        if (team == null) {
            serverPlayer.sendMessage(Text.literal("§cTu n'es dans aucune équipe !"), false);
            return ActionResult.CONSUME;
        }
        int points = rollPoints();
        data.markOpened(serverPlayer.getUuid(), pos);
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