package io.github.fripe070.pirkko.block;

import com.mojang.serialization.MapCodec;
import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import eu.pb4.polymer.virtualentity.api.BlockWithElementHolder;
import eu.pb4.polymer.virtualentity.api.ElementHolder;
import eu.pb4.polymer.virtualentity.api.elements.ItemDisplayElement;
import io.github.fripe070.pirkko.Pirkko;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.Waterloggable;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.FluidState;
import net.minecraft.fluid.Fluids;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemPlacementContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.random.Random;
import net.minecraft.world.World;
import net.minecraft.world.WorldView;
import net.minecraft.world.tick.ScheduledTickView;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Locale;

import static io.github.fripe070.pirkko.Pirkko.PIRKKO_SOUND;


// TODO: Allow waterlogging
public class PirkkoBlock extends WallMountedBlock implements BlockWithElementHolder, PolymerTexturedBlock, Waterloggable {
    public static final MapCodec<PirkkoBlock> CODEC = createCodec(PirkkoBlock::new);
    public static final BooleanProperty WATERLOGGED = Properties.WATERLOGGED;
    // TODO: Support more granular rotation
//    public static final IntProperty ROTATION = Properties.ROTATION;

    @Override
    public MapCodec<PirkkoBlock> getCodec() {
        return CODEC;
    }

    public PirkkoBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(FACE, BlockFace.WALL)
            .with(WATERLOGGED, false)
        );
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        world.playSound(null, pos, PIRKKO_SOUND, SoundCategory.BLOCKS, 1, 1.3f);
        return ActionResult.SUCCESS;
    }

    @Override
    public @Nullable BlockState getPlacementState(ItemPlacementContext ctx) {
        return super.getPlacementState(ctx)
            .with(WATERLOGGED, ctx.getWorld().getFluidState(ctx.getBlockPos()).getFluid() == Fluids.WATER);
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
        builder.add(FACING, FACE, WATERLOGGED);
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return PolymerBlockResourceUtils.requestEmpty(BlockModelType.valueOf((switch (state.get(FACE)) {
            case FLOOR -> "BOTTOM_TRAPDOOR";
            case CEILING -> "TOP_TRAPDOOR";
            default -> state.get(FACING).asString().toUpperCase(Locale.ROOT) + "_TRAPDOOR";
        }) + (state.get(WATERLOGGED) ? "_WATERLOGGED" : "")));
    }

    @SuppressWarnings("removal")
    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        var holder = new ElementHolder();
        ItemDisplayElement element = holder.addElement(new ItemDisplayElement(Pirkko.PIRKKO_BLOCK.asItem()));
        element.setModelTransformation(ItemDisplayContext.FIXED);

        element.setTransformation(new Matrix4f()
            .rotate(initialBlockState.get(FACING).getRotationQuaternion())
            .rotateZ(initialBlockState.get(FACE) == BlockFace.FLOOR ? (float)Math.toRadians(180) : 0f)
            .rotateX((float) Math.toRadians(switch (initialBlockState.get(FACE)) {
                case FLOOR -> - 90;
                case CEILING -> 90;
                case WALL -> 0;
            }))
            .translate(0.0f, - 0.25f, 0.0f)
            .scale(0.5f)
        );
        return holder;
    }

    @Override
    protected boolean canPlaceAt(BlockState state, WorldView world, BlockPos pos) {
        return true;
    }
}
