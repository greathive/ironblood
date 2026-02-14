package net.mcreator.ironblood.block;

import org.valkyrienskies.core.impl.shadow.bs;
import org.valkyrienskies.core.impl.shadow.br;
import org.valkyrienskies.core.impl.shadow.bp;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.core.BlockPos;

import net.mcreator.ironblood.block.entity.SwivelBearingBlockEntity;

import javax.annotation.Nullable;

public class SwivelBearingBlock extends Block implements EntityBlock {
	public SwivelBearingBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(50f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
	}

	@Override
	public boolean propagatesSkylightDown(BlockState state, BlockGetter reader, BlockPos pos) {
		return true;
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 0;
	}

	@Override
	public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		return Shapes.empty();
	}

	@Override
	public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
		// Matches mecha_bearing.json model: Y from 0 to 7 (7 pixels tall at bottom)
		return box(0, 0, 0, 16, 7, 16);
	}

	@Override
	@Nullable
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new SwivelBearingBlockEntity(pos, state);
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			var be = world.getBlockEntity(pos);
			if (be instanceof SwivelBearingBlockEntity swivelBE) {
				swivelBE.removeJoint();
			}
		}
		super.onRemove(state, world, pos, newState, isMoving);
	}
}