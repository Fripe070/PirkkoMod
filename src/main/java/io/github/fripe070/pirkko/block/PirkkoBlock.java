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
import net.minecraft.block.*;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.*;
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

import static io.github.fripe070.pirkko.Pirkko.PIRKKO_SOUND;


// TODO: Allow waterlogging
public class PirkkoBlock extends Block implements BlockWithElementHolder, PolymerTexturedBlock, Waterloggable {
    public static final MapCodec<PirkkoBlock> CODEC = createCodec(PirkkoBlock::new);
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    public static final IntProperty ROTATION = Properties.ROTATION;
    public static final EnumProperty<Direction> UP_DIRECTION = Properties.FACING;
    public static final EnumProperty<PirkkoKind> PIRKKO_KIND = EnumProperty.of("pirkko_kind", PirkkoKind.class);

    protected static final int SQUISH_TICKS = 4;
    public static final IntProperty SQUISH_TICK = IntProperty.of("squish_tick", 0, SQUISH_TICKS);
    public static final BooleanProperty POWERED = BooleanProperty.of("powered");

    protected final Random random;

    @Override
    public MapCodec<PirkkoBlock> getCodec() {
        return CODEC;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(UP_DIRECTION, WATERLOGGED, SQUISH_TICK, POWERED, ROTATION, PIRKKO_KIND);
    }

    protected ItemStack getItemStack(BlockState blockState) {
        var pirkkoKind = blockState.get(PIRKKO_KIND);
        return PirkkoItem.getStack(pirkkoKind);
    }

    @Override
    protected ItemStack getPickStack(WorldView world, BlockPos pos, BlockState state, boolean includeData) {
        return this.getItemStack(state);
    }

    @Override
    protected List<ItemStack> getDroppedStacks(BlockState state, LootWorldContext.Builder builder) {
        return List.of(getItemStack(state));
    }

    public PirkkoBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.random = Random.create();
        this.setDefaultState(this.stateManager.getDefaultState()
                .with(UP_DIRECTION, Direction.NORTH)
                .with(WATERLOGGED, false)
                .with(SQUISH_TICK, 0)
                .with(POWERED, false)
                .with(ROTATION, 2)
                .with(PIRKKO_KIND, PirkkoKind.BLANK)
        );
    }

    public void playPirkko(World world, BlockPos pos) {
        playPirkko(world, pos, 1.1f + (this.random.nextFloat() - 0.5f) * 0.3f);
    }

    public void playPirkko(World world, BlockPos pos, float pitch) {
        world.playSound(null, pos, PIRKKO_SOUND, SoundCategory.BLOCKS, 0.8f, pitch);
    }

    protected Direction getPlacedDirection(BlockState state) {
        var facing = state.get(UP_DIRECTION);
        return facing.getOpposite();
    }

    protected BlockState getPlacedAgainstBlock(BlockState state, World world, BlockPos pos) {
        var direction = getPlacedDirection(state);
        var newPos = pos.add(direction.getVector());
        return world.getBlockState(newPos);
    }

    protected ActionResult squish(BlockState state, World world, BlockPos pos) {
        this.playPirkko(world, pos);
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

    @Nullable
    public BlockState getPlacementState(ItemPlacementContext ctx) {
        var side = ctx.getSide();
        BlockState blockState = this.getDefaultState();
        blockState = blockState.with(UP_DIRECTION, side);

        // Determine the rotation
        // Yes, this is horrible
        if (side == Direction.UP || side == Direction.DOWN) {
            var playerYaw = ctx.getPlayerYaw();
            if (side == Direction.UP) {
                playerYaw = -playerYaw; // yaw needs to be inverted for some reason
            }
            var horizontalLookAngle = RotationPropertyHelper.fromYaw(playerYaw);
            blockState = blockState.with(ROTATION, horizontalLookAngle);
        } else {
            if (ctx.getPlayer() != null) {
                var playerPitch = Math.toRadians(ctx.getPlayer().getPitch());
                var playerYaw = Math.toRadians(ctx.getPlayer().getYaw());
                var playerLookingDirection = new Vec3d(-Math.sin(playerYaw) * Math.cos(playerPitch), Math.sin(playerPitch), Math.cos(playerYaw) * Math.cos(playerPitch));
                var perpToSide = side.getUnitVector().cross(Direction.UP.getUnitVector());
                var angle = Math.atan2(playerLookingDirection.getY(), perpToSide.dot(playerLookingDirection.toVector3f()));
                blockState = blockState.with(ROTATION, RotationPropertyHelper.fromYaw((float) Math.toDegrees(angle) - 90));
            }
        }

        var pirkkoKind = PirkkoItem.getPirkkoKind(ctx.getStack());
        blockState = blockState.with(PIRKKO_KIND, pirkkoKind);
        if (blockState.canPlaceAt(ctx.getWorld(), ctx.getBlockPos())) {
            return blockState.with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
        }
        return null;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        return squish(state, world, pos);
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
        return true;
    }

    @Override
    protected int getComparatorOutput(BlockState state, World world, BlockPos pos) {
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
        this.playPirkko((World) world, pos);
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
        if (state.get(WATERLOGGED)) tickView.scheduleFluidTick(pos, Fluids.WATER, Fluids.WATER.getTickRate(world));
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
        //private final TextDisplayElement debugText;
        private Matrix4f baseTransform;

        public PirkkoHolder(BlockState initialBlockState, ItemStack itemStack) {
            this.baseTransform = new Matrix4f();
            this.RecomputeBaseTransform(initialBlockState);
            this.display = addElement(new ItemDisplayElement(itemStack));
            this.display.setModelTransformation(ItemDisplayContext.NONE);
            this.display.setTransformation(this.baseTransform);
            this.display.setInterpolationDuration(1);

            //this.debugText = addElement(new TextDisplayElement(Text.of("Debug")));
            //this.debugText.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
            //this.debugText.setScale(new Vector3f(0.5f));
        }

        public void RecomputeBaseTransform(BlockState blockState) {
            this.baseTransform = new Matrix4f();
            var facing = blockState.get(UP_DIRECTION);
            if (facing == Direction.DOWN) {
                this.baseTransform.rotateY((float) Math.toRadians(180));
                this.baseTransform.rotateX((float) Math.toRadians(180));
            } else if (facing != Direction.UP) {
                var rotation = Direction.getHorizontalDegreesOrThrow(facing);
                this.baseTransform.rotateY((float) Math.toRadians(-rotation));
                this.baseTransform.rotateX((float) Math.toRadians(90));
            }
            this.baseTransform.rotateY((float) Math.toRadians(RotationPropertyHelper.toDegrees(blockState.get(ROTATION))));
        }

        public PirkkoHolder(BlockState initialBlockState, Item item) {
            this(initialBlockState, item.getDefaultStack());
        }

        @Override
        protected void onTick() {
            var attachment = this.getAttachment();
            if (attachment == null) throw new IllegalStateException("Attachment is null");
            var squishTick = ((BlockBoundAttachment) attachment).getBlockState().get(SQUISH_TICK);

            if (squishTick > 0) {
                float squishFactor = squishTick / (float) SQUISH_TICKS;
                Vector3f squishScale = new Vector3f(
                        1f + 0.2f * squishFactor,
                        1f - 0.3f * squishFactor,
                        1f + 0.2f * squishFactor
                );
                this.display.setTransformation(new Matrix4f(this.baseTransform)
                        .translate(0, -0.5f, 0)
                        .scale(squishScale)
                        .translate(0, 0.5f, 0)
                );
                this.display.startInterpolation();
            } else {
                this.display.setTransformation(this.baseTransform);
            }

            //this.debugText.setText(Text.of("Squish: %d/%d".formatted(squishTick, SQUISH_TICKS)));
        }
    }
}
