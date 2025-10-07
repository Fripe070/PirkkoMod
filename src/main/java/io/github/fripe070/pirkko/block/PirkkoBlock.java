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
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.state.StateManager;
import net.minecraft.state.property.BooleanProperty;
import net.minecraft.state.property.Properties;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import xyz.nucleoid.packettweaker.PacketContext;

import java.util.Locale;


// TODO: Allow waterlogging
public class PirkkoBlock extends WallMountedBlock implements BlockWithElementHolder, PolymerTexturedBlock {
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
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return PolymerBlockResourceUtils.requestEmpty(BlockModelType.valueOf((switch (state.get(FACE)) {
            case FLOOR -> "BOTTOM_TRAPDOOR";
            case CEILING -> "TOP_TRAPDOOR";
            default -> state.get(FACING).asString().toUpperCase(Locale.ROOT) + "_TRAPDOOR";
        }) + (state.get(WATERLOGGED) ? "_WATERLOGGED" : "")));
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        world.playSound(null, pos, SoundEvents.ENTITY_COD_FLOP, SoundCategory.BLOCKS, 3.0f, 1.0f);
        return ActionResult.SUCCESS;
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE, WATERLOGGED);
    }

    @SuppressWarnings("removal")
    @Override
    public @Nullable ElementHolder createElementHolder(ServerWorld world, BlockPos pos, BlockState initialBlockState) {
        var holder = new ElementHolder();
        ItemDisplayElement element = holder.addElement(new ItemDisplayElement(Pirkko.PIRKKO_BLOCK.asItem()));
        element.setModelTransformation(ItemDisplayContext.FIXED);

        element.setTransformation(new Matrix4f()
            .rotate(initialBlockState.get(FACING).getRotationQuaternion())
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
}
