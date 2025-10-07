package io.github.fripe070.pirkko.block;

import com.mojang.serialization.MapCodec;

import eu.pb4.polymer.blocks.api.BlockModelType;
import eu.pb4.polymer.blocks.api.PolymerBlockModel;
import eu.pb4.polymer.blocks.api.PolymerBlockResourceUtils;
import eu.pb4.polymer.blocks.api.PolymerTexturedBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.WallMountedBlock;
import net.minecraft.block.enums.BlockFace;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.state.StateManager;
import net.minecraft.util.ActionResult;
import net.minecraft.util.BlockMirror;
import net.minecraft.util.BlockRotation;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;
import xyz.nucleoid.packettweaker.PacketContext;

import static io.github.fripe070.pirkko.Pirkko.MOD_ID;


// TODO: Allow waterlogging
public class PirkkoBlock extends WallMountedBlock implements PolymerTexturedBlock {
    public static final MapCodec<PirkkoBlock> CODEC = createCodec(PirkkoBlock::new);

    private final BlockState polymerBlockState;

    @Override
    public MapCodec<PirkkoBlock> getCodec() {
        return CODEC;
    }

    public PirkkoBlock(AbstractBlock.Settings settings) {
        super(settings);
        this.setDefaultState(this.stateManager.getDefaultState()
            .with(FACING, Direction.NORTH)
            .with(FACE, BlockFace.WALL)
        );

        this.polymerBlockState = PolymerBlockResourceUtils.requestBlock(
            BlockModelType.TRIPWIRE_BLOCK_FLAT,
            PolymerBlockModel.of(Identifier.of(MOD_ID, "block/pirkko_small")));
    }

    @Override
    public BlockState getPolymerBlockState(BlockState state, PacketContext context) {
        return this.polymerBlockState;
    }

    @Override
    protected ActionResult onUse(BlockState state, World world, BlockPos pos, PlayerEntity player, BlockHitResult hit) {
        // TODO: Play noise
        return ActionResult.SUCCESS;
    }

    @Override
    protected BlockState rotate(BlockState state, BlockRotation rotation) {
        return state.with(FACING, rotation.rotate(state.get(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, BlockMirror mirror) {
        return state.rotate(mirror.getRotation(state.get(FACING)));
    }

    @Override
    protected void appendProperties(StateManager.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE);
    }
}
