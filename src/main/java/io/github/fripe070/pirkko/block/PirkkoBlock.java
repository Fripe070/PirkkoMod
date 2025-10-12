package io.github.fripe070.pirkko.block;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import io.github.fripe070.pirkko.PirkkoKind;
import io.github.fripe070.pirkko.item.PirkkoItem;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.Waterloggable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.RotationPropertyHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.block.WireOrientation;
import net.minecraft.world.event.GameEvent;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Locale;


public class PirkkoBlock extends Block implements BlockWithElementHolder, PolymerTexturedBlock, Waterloggable {
    public static final MapCodec<PirkkoBlock> CODEC = createCodec(PirkkoBlock::new);

    public static final IntProperty ROTATION = Properties.ROTATION;
    public static final EnumProperty<Direction> UP_DIRECTION = Properties.FACING;
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final BooleanProperty POWERED = BooleanProperty.of("powered");
    public static final EnumProperty<PirkkoKind> KIND = EnumProperty.of("kind", PirkkoKind.class);

    protected static final int SQUISH_TICKS = 4;
    public static final IntProperty SQUISH_TICK = IntProperty.of("squish_tick", 0, SQUISH_TICKS);

    protected final Random random;

    @Override
    public MapCodec<PirkkoBlock> getCodec() {
        return CODEC;
    }
    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(ROTATION, UP_DIRECTION, WATERLOGGED, POWERED, KIND, SQUISH_TICK);
    }

    public PirkkoBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.random = Random.create();
        this.setDefaultState(this.stateManager.getDefaultState()
            .with(ROTATION, RotationPropertyHelper.fromDirection(Direction.NORTH))
            .with(UP_DIRECTION, Direction.UP)
            .with(WATERLOGGED, false)
            .with(POWERED, false)
            .with(KIND, PirkkoKind.BLANK)
            .with(SQUISH_TICK, 0)
        );
    }

    protected ItemStack getItemStack(BlockState blockState) {
        return PirkkoItem.getStack(blockState.get(KIND));
    }
    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return this.getItemStack(state);
    }
    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootWorldContext.Builder builder) {
        return List.of(getItemStack(state));
    }

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        var side = ctx.getSide();
        BlockState blockState = this.getDefaultState();
        blockState = blockState.with(UP_DIRECTION, side);
        blockState = blockState.with(KIND, PirkkoItem.getPirkkoKind(ctx.getStack()));
        blockState = blockState.with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);

        // Determine the rotation based on the side it is placed on and where the player is looking
        // Yes, this is horrible
        if (side == Direction.UP || side == Direction.DOWN) {
            // Display entity will be rotated such that the horizontal rotation inverts when upside down, hence we invert
            var horizontalAngle = RotationPropertyHelper.fromYaw(side == Direction.DOWN
                ? ctx.getPlayerYaw()
                : -ctx.getPlayerYaw());
            blockState = blockState.with(ROTATION, horizontalAngle);
        } else {
            @Nullable PlayerEntity player = ctx.getPlayer();
            Vec3d playerLook = player != null ? player.getRotationVector() : new Vec3d(0, -1, 0);

            Vector3f perpToSide = side.getUnitVector().cross(Direction.DOWN.getUnitVector());
            double angle = Math.atan2(playerLook.getY(), perpToSide.dot(playerLook.toVector3f()));

            blockState = blockState.with(ROTATION, RotationPropertyHelper.fromYaw((float) Math.toDegrees(angle) + 90));
        }

        if (!blockState.canPlaceAt(ctx.getWorld(), ctx.getBlockPos()))
            return null;
        return blockState;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return squish(state, world, pos);
    }

    protected ActionResult squish(BlockState state, World world, BlockPos pos) {
        this.playPirkko(world, pos, state);
        world.setBlockState(pos, state.with(SQUISH_TICK, SQUISH_TICKS));
        world.scheduleBlockTick(pos, this, 1);
        world.emitGameEvent(null, GameEvent.NOTE_BLOCK_PLAY, pos);
        return ActionResult.SUCCESS;
    }

    protected int getSquishTicks(BlockState state) {
        return state.get(SQUISH_TICK);
    }
    protected boolean isSquishing(BlockState state) {
        var squishTick = getSquishTicks(state);
        return squishTick > 0;
    }

    public void playPirkko(World world, BlockPos pos, BlockState state) {
        this.playPirkko(world, pos, state, 1.1f + (this.random.nextFloat() - 0.5f) * 0.3f);
    }
    public void playPirkko(World world, BlockPos pos, BlockState state, float pitch) {
        world.playSound(null, pos, state.get(KIND).getSound(), SoundCategory.BLOCKS, 0.8f, pitch);
    }

    protected BlockState getFloorBlock(BlockState state, World world, BlockPos pos) {
        var direction = state.get(UP_DIRECTION).getOpposite();
        var newPos = pos.add(direction.getVector());
        return world.getBlockState(newPos);
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.scheduledTick(state, world, pos, random);
        if (isSquishing(state)) {
            world.setBlockState(pos, state.with(SQUISH_TICK, state.get(SQUISH_TICK) - 1));
            world.scheduleBlockTick(pos, this, 1);
        }
    }

    @Override
    protected void neighborUpdate(BlockState state, World world, BlockPos pos, Block sourceBlock, @Nullable WireOrientation wireOrientation, boolean notify) {
        super.neighborUpdate(state, world, pos, sourceBlock, wireOrientation, notify);
        boolean hasPower = world.isReceivingRedstonePower(pos);
        if (hasPower != state.get(POWERED)) {
            BlockState poweredState = state.with(POWERED, hasPower);
            world.setBlockState(pos, poweredState);
            if (hasPower) {
                this.squish(poweredState, world, pos);
            }
        }
    }

    @Override
    protected boolean hasComparatorOutput(BlockState state) {
        super.hasComparatorOutput(state);
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos, Direction direction) {
        return Math.max(0, Math.min(15, getSquishTicks(state) * 15 / (SQUISH_TICKS - 1)));
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        this.squish(state, world, pos);
    }

    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        super.onBroken(world, pos, state);
        this.playPirkko((World) world, pos, state);
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return true;
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.get(WATERLOGGED) ? Fluids.WATER.getStill(false) : super.getFluidState(state);
    }

    @Override
    protected BlockState getStateForNeighborUpdate(BlockState state, WorldView world, ScheduledTickView tickView, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, Random random) {
        if (state.get(WATERLOGGED)) {
            tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
        }
        return super.getStateForNeighborUpdate(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return PolymerBlockResourceUtils.requestEmpty(BlockModelType.valueOf((switch (state.get(UP_DIRECTION)) {
            case Direction.UP -> "BOTTOM_TRAPDOOR";
            case Direction.DOWN -> "TOP_TRAPDOOR";
            default -> state.get(UP_DIRECTION).asString().toUpperCase(Locale.ROOT) + "_TRAPDOOR";
        }) + (state.get(WATERLOGGED) ? "_WATERLOGGED" : "")));
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new PirkkoHolder(initialBlockState, this.getItemStack(initialBlockState));
    }

    @Override
    public boolean tickElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @SuppressWarnings("removal")
    static class PirkkoHolder extends ElementHolder {
        private final ItemDisplayElement display;
        private final Matrix4f baseTransform;

        public PirkkoHolder(BlockState initialBlockState, ItemStack itemStack) {
            this.baseTransform = this.computeTransform(initialBlockState);

            this.display = addElement(new ItemDisplayElement(itemStack));
            this.display.setModelTransformation(ItemDisplayContext.NONE);
            this.display.setTransformation(this.baseTransform);
            this.display.setInterpolationDuration(1); // Blockstate is only updated each tick, this smooths it on the client
        }

        public Matrix4f computeTransform(BlockState blockState) {
            var transform = new Matrix4f();
            var facing = blockState.get(UP_DIRECTION);
            if (facing == Direction.DOWN) {
                transform.rotateY((float) Math.toRadians(180));
                transform.rotateX((float) Math.toRadians(180));
            } else if (facing != Direction.UP) {
                var rotation = Direction.getHorizontalDegreesOrThrow(facing);
                transform.rotateY((float) Math.toRadians(-rotation));
                transform.rotateX((float) Math.toRadians(90));
            }
            transform.rotateY((float) Math.toRadians(RotationPropertyHelper.toDegrees(blockState.get(ROTATION))));
            return transform;
        }

        @Override
        protected void onTick() {
            var attachment = this.getAttachment();
            if (attachment == null) throw new IllegalStateException("Attachment is null");
            var squishTick = ((BlockBoundAttachment) attachment).getBlockState().get(SQUISH_TICK);

            if (squishTick > 0) {
                float squishFactor = squishTick / (float) SQUISH_TICKS;
                this.display.setTransformation(new Matrix4f(this.baseTransform)
                        .translate(0, -0.5f, 0) // Scale down from this origin (offset from center)
                        .scale(new Vector3f(
                            1f + 0.2f * squishFactor,
                            1f - 0.3f * squishFactor,
                            1f + 0.2f * squishFactor
                        ))
                        .translate(0, 0.5f, 0)
                );
                this.display.startInterpolation();
            } else {
                this.display.setTransformation(this.baseTransform);
            }
        }
    }
}
