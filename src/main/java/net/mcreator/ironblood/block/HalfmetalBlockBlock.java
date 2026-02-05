
package net.mcreator.ironblood.block;

import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.BlockPos;

public class HalfmetalBlockBlock extends Block {
	public static final IntegerProperty DAMAGE = IntegerProperty.create("damage", 0, 4);

	public HalfmetalBlockBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(40f));
		this.registerDefaultState(this.stateDefinition.any().setValue(DAMAGE, 0));
	}

	@Override
	public int getLightBlock(BlockState state, BlockGetter worldIn, BlockPos pos) {
		return 15;
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(DAMAGE);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(DAMAGE, 0);
	}
}
