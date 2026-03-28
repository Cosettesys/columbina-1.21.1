package net.cosette.columbina.portal;

import net.cosette.columbina.poketopia.PoketopiaManager;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import net.minecraft.world.BlockView;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class PoketopiaPortalBlock extends Block {

    public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;

    private static final VoxelShape X_SHAPE = Block.createCuboidShape(0, 0, 6, 16, 16, 10);
    private static final VoxelShape Z_SHAPE = Block.createCuboidShape(6, 0, 0, 10, 16, 16);

    private static final Map<UUID, Integer> COOLDOWNS = new ConcurrentHashMap<>();

    public static void tickCooldowns() {
        COOLDOWNS.replaceAll((uuid, t) -> t - 1);
        COOLDOWNS.entrySet().removeIf(e -> e.getValue() <= 0);
    }

    public PoketopiaPortalBlock(Settings settings) {
        super(settings);
        setDefaultState(getStateManager().getDefaultState().with(AXIS, Direction.Axis.X));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(AXIS);
    }

    @Override
    public VoxelShape getOutlineShape(BlockState state, BlockView world,
                                      BlockPos pos, ShapeContext ctx) {
        return state.get(AXIS) == Direction.Axis.X ? X_SHAPE : Z_SHAPE;
    }

    @Override
    public VoxelShape getCollisionShape(BlockState state, BlockView world,
                                        BlockPos pos, ShapeContext ctx) {
        return VoxelShapes.empty();
    }

    @Override
    public void onEntityCollision(BlockState state, World world, BlockPos pos,
                                  Entity entity) {
        if (world.isClient()) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;
        if (player.hasVehicle()) return;
        if (COOLDOWNS.containsKey(player.getUuid())) return;

        COOLDOWNS.put(player.getUuid(), 60);

        ServerWorld serverWorld = (ServerWorld) world;
        if (PoketopiaManager.POKETOPIA_KEY.equals(serverWorld.getRegistryKey())) {
            teleportToOverworld(player);
        } else {
            player.getServer().execute(() ->
                    PoketopiaManager.getInstance().onFirstJoin(player));
        }
    }

    private static void teleportToOverworld(ServerPlayerEntity player) {
        ServerWorld overworld = player.getServer().getOverworld();
        BlockPos spawn;
        if (player.getSpawnPointPosition() != null
                && World.OVERWORLD.equals(player.getSpawnPointDimension())) {
            spawn = player.getSpawnPointPosition();
        } else {
            spawn = overworld.getSpawnPos();
        }
        TeleportTarget target = new TeleportTarget(
                overworld,
                Vec3d.ofBottomCenter(spawn),
                Vec3d.ZERO,
                0f, 0f,
                TeleportTarget.NO_OP
        );
        player.getServer().execute(() -> player.teleportTo(target));
    }

    @Override
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        Direction.Axis axis = ctx.getHorizontalPlayerFacing().getAxis();
        return getDefaultState().with(AXIS, axis);
    }

}