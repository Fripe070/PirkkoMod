package io.github.fripe070.pirkko.block;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.attachment.BlockBoundAttachment;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import eu.pb4.polymer.virtualentity.api.elements.TextDisplayElement;
import io.github.fripe070.pirkko.Pirkko;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.DisplayEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.EnumProperty;
import net.minecraft.state.property.IntProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.DyeColor;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Locale;

import static io.github.fripe070.pirkko.Pirkko.PIRKKO_SOUND;


// TODO: Allow waterlogging
public class PirkkoBlock extends WallMountedBlock implements BlockWithElementHolder, PolymerTexturedBlock, Waterloggable {
    public static final MapCodec<PirkkoBlock> CODEC = createCodec(PirkkoBlock::new);
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    // TODO: Support more granular rotation
//    public static final IntProperty ROTATION = Properties.ROTATION;

    protected static final int SQUISH_TICKS = 4;
    public static final IntProperty SQUISH_TICK = IntProperty.of("squish_tick", 0, SQUISH_TICKS);

    protected final Random random;

    @Override
    public MapCodec<PirkkoBlock> getCodec() {
        return CODEC;
    }

    public PirkkoBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.random = Random.create();
        this.setDefaultState(this.stateManager.getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(FACE, BlockFace.WALL)
            .with(WATERLOGGED, false)
            .with(SQUISH_TICK, 0)
        );
    }

    public void playPirkko(World world, BlockPos pos) {
        world.playSound(null, pos, PIRKKO_SOUND, SoundCategory.BLOCKS, 0.8f, 0.9f + this.random.nextFloat() * 0.4f);
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        this.playPirkko(world, pos);
        world.setBlockState(pos, state.with(SQUISH_TICK, SQUISH_TICKS));
        world.scheduleBlockTick(pos, this, 1);
        return ActionResult.SUCCESS;
    }

    @Override
    protected void scheduledTick(BlockState state, ServerWorld world, BlockPos pos, Random random) {
        super.scheduledTick(state, world, pos, random);
        if (state.get(SQUISH_TICK) > 0) {
            world.setBlockState(pos, state.with(SQUISH_TICK, state.get(SQUISH_TICK) - 1));
            world.scheduleBlockTick(pos, this, 1);
        }
    }

    @Override
    public void onPlaced(World world, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack itemStack) {
        super.onPlaced(world, pos, state, placer, itemStack);
        this.playPirkko(world, pos);
    }
    @Override
    public void onBroken(WorldAccess world, BlockPos pos, BlockState state) {
        super.onBroken(world, pos, state);
        this.playPirkko((World) world, pos);
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx)
            .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
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
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE, WATERLOGGED, SQUISH_TICK);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return PolymerBlockResourceUtils.requestEmpty(BlockModelType.valueOf((switch (state.get(FACE)) {
            case FLOOR -> "BOTTOM_TRAPDOOR";
            case CEILING -> "TOP_TRAPDOOR";
            default -> state.get(FACING).asString().toUpperCase(Locale.ROOT) + "_TRAPDOOR";
        }) + (state.get(WATERLOGGED) ? "_WATERLOGGED" : "")));
    }

    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return new PirkkoHolder(world, pos, initialBlockState);
    }

    @Override
    public boolean tickElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        return true;
    }

    @SuppressWarnings("removal")
    static class PirkkoHolder extends ElementHolder {
        private final ItemDisplayElement display;
        private final TextDisplayElement debugText;
        private final Matrix4f baseTransform;

        public PirkkoHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
            this.baseTransform = new Matrix4f()
                .rotate(initialBlockState.get(FACING).getRotationQuaternion())
                .rotateZ(initialBlockState.get(FACE) == BlockFace.CEILING ? (float) Math.toRadians(180) : 0f)
                .rotateY(initialBlockState.get(FACE) == BlockFace.WALL ? (float) Math.toRadians(180) : 0f)
                .rotateX((float) Math.toRadians(switch (initialBlockState.get(FACE)) {
                    case FLOOR -> - 90;
                    case CEILING -> 90;
                    case WALL -> 0;
                }));

            this.display = addElement(new ItemDisplayElement(Pirkko.PIRKKO_BLOCK.asItem()));
            this.display.setModelTransformation(ItemDisplayContext.NONE);
            this.display.setTransformation(this.baseTransform);
            this.display.setInterpolationDuration(1);

            this.debugText = addElement(new TextDisplayElement(Text.of("Debug")));
            this.debugText.setBillboardMode(DisplayEntity.BillboardMode.CENTER);
            this.debugText.setScale(new Vector3f(0.5f));
        }

        @Override
        protected void onTick() {
            var attachment = this.getAttachment();
            if (attachment == null) throw new IllegalStateException("Attachment is null");
            var squishTick = ((BlockBoundAttachment)attachment).getBlockState().get(SQUISH_TICK);

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

            this.debugText.setText(Text.of("Squish: %d/%d".formatted(squishTick, SQUISH_TICKS)));
        }
    }
}
