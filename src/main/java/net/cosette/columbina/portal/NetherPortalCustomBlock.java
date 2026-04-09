package net.cosette.columbina.portal;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.ShapeContext;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.Identifier;
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

public class NetherPortalCustomBlock extends Block {
    public static final EnumProperty<Direction.Axis> AXIS = Properties.HORIZONTAL_AXIS;
    private static final VoxelShape X_SHAPE = Block.createCuboidShape(0, 0, 6, 16, 16, 10);
    private static final VoxelShape Z_SHAPE = Block.createCuboidShape(6, 0, 0, 10, 16, 16);
    private static final Map<UUID, Integer> COOLDOWNS = new ConcurrentHashMap<>();
    private static final RegistryKey<World> NETHER_KEY = RegistryKey.of(
            RegistryKeys.WORLD,
            Identifier.of("minecraft", "the_nether")
    );
    public static void tickCooldowns() {
        COOLDOWNS.replaceAll((uuid, t) -> t - 1);
        COOLDOWNS.entrySet().removeIf(e -> e.getValue() <= 0);
    }
    public NetherPortalCustomBlock(Settings settings) {
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
    public void onEntityCollision(BlockState state, World world, BlockPos pos, Entity entity) {
        if (world.isClient()) return;
        if (!(entity instanceof ServerPlayerEntity player)) return;
        if (player.hasVehicle()) return;
        if (COOLDOWNS.containsKey(player.getUuid())) return;
        COOLDOWNS.put(player.getUuid(), 60);
        ServerWorld nether = player.getServer().getWorld(NETHER_KEY);
        if (nether == null) return;
        BlockPos netherPos = new BlockPos(
                (int) (player.getX() / 8.0),
                (int) player.getY(),
                (int) (player.getZ() / 8.0)
        );
        TeleportTarget target = new TeleportTarget(
                nether,
                Vec3d.ofBottomCenter(netherPos),
                Vec3d.ZERO,
                player.getYaw(),
                player.getPitch(),
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