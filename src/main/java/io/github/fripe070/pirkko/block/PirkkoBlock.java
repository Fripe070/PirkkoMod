package io.github.fripe070.pirkko.block;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import io.github.fripe070.pirkko.Pirkko;
import io.github.fripe070.pirkko.PirkkoKind;
import io.github.fripe070.pirkko.item.PirkkoItem;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.RotationSegment;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.ScheduledTickAccess;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.List;
import java.util.Locale;


public class PirkkoBlock extends Block implements BlockWithElementHolder, PolymerTexturedBlock, SimpleWaterloggedBlock {
    public static final MapCodec<PirkkoBlock> CODEC = simpleCodec(PirkkoBlock::new);

    public static final IntegerProperty ROTATION = BlockStateProperties.ROTATION_16;
    public static final EnumProperty<@NotNull Direction> UP_DIRECTION = BlockStateProperties.FACING;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");
    public static final EnumProperty<@NotNull PirkkoKind> KIND = EnumProperty.create("kind", PirkkoKind.class);

    protected static final int SQUISH_TICKS = 4;
    public static final IntegerProperty SQUISH_TICK = IntegerProperty.create("squish_tick", 0, SQUISH_TICKS);

    protected final RandomSource random;

    @Override
    public @NotNull MapCodec<PirkkoBlock> codec() {
        return CODEC;
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<@NotNull Block, @NotNull BlockState> builder) {
        builder.add(ROTATION, UP_DIRECTION, WATERLOGGED, POWERED, KIND, SQUISH_TICK);
    }

    public PirkkoBlock(BlockBehaviour.Properties settings) {
        super(settings);
        this.random = RandomSource.create();
        this.registerDefaultState(this.stateDefinition.any()
            .setValue(ROTATION, RotationSegment.convertToSegment(Direction.NORTH))
            .setValue(UP_DIRECTION, Direction.UP)
            .setValue(WATERLOGGED, false)
            .setValue(POWERED, false)
            .setValue(KIND, PirkkoKind.BLANK)
            .setValue(SQUISH_TICK, 0)
        );
    }

    protected ItemStack getItemStack(BlockState blockState) {
        return PirkkoItem.getStack(blockState.getValue(KIND));
    }
    @Override
    protected @NotNull ItemStack getCloneItemStack(@NotNull LevelReader world, @NotNull BlockPos pos, @NotNull BlockState state, boolean includeData) {
        return this.getItemStack(state);
    }
    @Override
    protected @NotNull List<ItemStack> getDrops(@NotNull BlockState state, LootParams.@NotNull Builder builder) {
        return List.of(getItemStack(state));
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        var side = ctx.getClickedFace();
        BlockState blockState = this.defaultBlockState();
        blockState = blockState.setValue(UP_DIRECTION, side);
        blockState = blockState.setValue(WATERLOGGED, ctx.getLevel().getFluidState(ctx.getClickedPos()).getType() == Fluids.WATER);
        @Nullable PirkkoKind kind = PirkkoItem.getPirkkoKind(ctx.getItemInHand());
        blockState = blockState.setValue(KIND, kind != null ? kind : PirkkoKind.BLANK);

        // Determine the rotation based on the side it is placed on and where the player is looking
        // Yes, this is horrible
        if (side == Direction.UP || side == Direction.DOWN) {
            // Display entity will be rotated such that the horizontal rotation inverts when upside down, hence we invert
            var horizontalAngle = RotationSegment.convertToSegment(side == Direction.DOWN
                ? ctx.getRotation()
                : -ctx.getRotation());
            blockState = blockState.setValue(ROTATION, horizontalAngle);
        } else {
            @Nullable Player player = ctx.getPlayer();
            Vec3 playerLook = player != null ? player.getLookAngle() : new Vec3(0, -1, 0);

            Vector3f perpToSide = side.step().cross(Direction.DOWN.step());
            double angle = Math.atan2(playerLook.y(), perpToSide.dot(playerLook.toVector3f()));

            blockState = blockState.setValue(ROTATION, RotationSegment.convertToSegment((float) Math.toDegrees(angle) + 90));
        }

        if (!blockState.canSurvive(ctx.getLevel(), ctx.getClickedPos()))
            return null;
        return blockState;
    }

    @Override
    protected @NotNull InteractionResult useWithoutItem(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, Player player, @NotNull BlockHitResult hit) {
        player.awardStat(Pirkko.PIRKKO_CLICK_STAT);
        Pirkko.CLICK_PIRKKO.trigger((ServerPlayer) player,
            ((ServerPlayer) player).getStats().getValue(Stats.CUSTOM.get(Pirkko.PIRKKO_CLICK_STAT)));

        return squish(state, world, pos);
    }

    protected InteractionResult squish(BlockState state, Level world, BlockPos pos) {
        this.playPirkko(world, pos, state);
        world.setBlockAndUpdate(pos, state.setValue(SQUISH_TICK, SQUISH_TICKS));
        world.scheduleTick(pos, this, 1);
        world.gameEvent(null, GameEvent.NOTE_BLOCK_PLAY, pos);
        return InteractionResult.SUCCESS;
    }

    protected int getSquishTicks(BlockState state) {
        return state.getValue(SQUISH_TICK);
    }
    protected boolean isSquishing(BlockState state) {
        var squishTick = getSquishTicks(state);
        return squishTick > 0;
    }

    public void playPirkko(Level world, BlockPos pos, BlockState state) {
        this.playPirkko(world, pos, state, 1.1f + (this.random.nextFloat() - 0.5f) * 0.3f);
    }
    public void playPirkko(Level world, BlockPos pos, BlockState state, float pitch) {
        world.playSound(null, pos, state.getValue(KIND).getSound(), SoundSource.BLOCKS, 0.8f, pitch);
    }

    protected BlockState getFloorBlock(BlockState state, Level world, BlockPos pos) {
        var direction = state.getValue(UP_DIRECTION).getOpposite();
        var newPos = pos.offset(direction.getUnitVec3i());
        return world.getBlockState(newPos);
    }

    @Override
    protected void tick(@NotNull BlockState state, @NotNull ServerLevel world, @NotNull BlockPos pos, @NotNull RandomSource random) {
        super.tick(state, world, pos, random);
        if (isSquishing(state)) {
            world.setBlockAndUpdate(pos, state.setValue(SQUISH_TICK, state.getValue(SQUISH_TICK) - 1));
            world.scheduleTick(pos, this, 1);
        }
    }

    @Override
    protected void neighborChanged(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Block sourceBlock, @Nullable Orientation wireOrientation, boolean notify) {
        super.neighborChanged(state, world, pos, sourceBlock, wireOrientation, notify);
        boolean hasPower = world.hasNeighborSignal(pos);
        if (hasPower != state.getValue(POWERED)) {
            BlockState poweredState = state.setValue(POWERED, hasPower);
            world.setBlockAndUpdate(pos, poweredState);
            if (hasPower) {
                this.squish(poweredState, world, pos);
            }
        }
    }

    @Override
    protected boolean hasAnalogOutputSignal(@NotNull BlockState state) {
        super.hasAnalogOutputSignal(state);
        return true;
    }

    @Override
    protected int getAnalogOutputSignal(@NotNull BlockState state, @NotNull Level world, @NotNull BlockPos pos, @NotNull Direction direction) {
        return Math.max(0, Math.min(15, getSquishTicks(state) * 15 / (SQUISH_TICKS - 1)));
    }

    @Override
    public void setPlacedBy(@NotNull Level world, @NotNull BlockPos pos, @NotNull BlockState state, @Nullable LivingEntity placer, @NotNull ItemStack itemStack) {
        super.setPlacedBy(world, pos, state, placer, itemStack);
        this.squish(state, world, pos);
    }

    @Override
    public void destroy(@NotNull LevelAccessor world, @NotNull BlockPos pos, @NotNull BlockState state) {
        super.destroy(world, pos, state);
        this.playPirkko((Level) world, pos, state);
    }

    @Override
    protected boolean canSurvive(@NotNull BlockState state, @NotNull LevelReader world, @NotNull BlockPos pos) {
        return true;
    }

    @Override
    public @NotNull FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    protected @NotNull BlockState updateShape(BlockState state, @NotNull LevelReader world, @NotNull ScheduledTickAccess tickView, @NotNull BlockPos pos, @NotNull Direction direction, @NotNull BlockPos neighborPos, @NotNull BlockState neighborState, @NotNull RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickView.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }
        return super.updateShape(state, world, tickView, pos, direction, neighborPos, neighborState, random);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return PolymerBlockResourceUtils.requestEmpty(BlockModelType.valueOf((switch (state.getValue(UP_DIRECTION)) {
            case Direction.UP -> "BOTTOM_TRAPDOOR";
            case Direction.DOWN -> "TOP_TRAPDOOR";
            default -> state.getValue(UP_DIRECTION).getSerializedName().toUpperCase(Locale.ROOT) + "_TRAPDOOR";
        }) + (state.getValue(WATERLOGGED) ? "_WATERLOGGED" : "")));
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
        return new PirkkoHolder(initialBlockState, this.getItemStack(initialBlockState));
    }

    @Override
    public boolean tickElementHolder(ServerLevel world, BlockPos pos, BlockState initialBlockState) {
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
            var facing = blockState.getValue(UP_DIRECTION);
            if (facing == Direction.DOWN) {
                transform.rotateY((float) Math.toRadians(180));
                transform.rotateX((float) Math.toRadians(180));
            } else if (facing != Direction.UP) {
                var rotation = Direction.getYRot(facing);
                transform.rotateY((float) Math.toRadians(-rotation));
                transform.rotateX((float) Math.toRadians(90));
            }
            transform.rotateY((float) Math.toRadians(RotationSegment.convertToDegrees(blockState.getValue(ROTATION))));
            return transform;
        }

        @Override
        protected void onTick() {
            var attachment = this.getAttachment();
            if (attachment == null) throw new IllegalStateException("Attachment is null");
            int squishTick = ((BlockBoundAttachment) attachment).getBlockState().getValue(SQUISH_TICK);

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
