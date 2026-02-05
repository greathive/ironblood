
package net.mcreator.ironblood.block;

import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.context.BlockPlaceContext;

public class HalfmetalSlabBlock extends SlabBlock {
	public static final IntegerProperty DAMAGE = IntegerProperty.create("damage", 0, 4);

	public HalfmetalSlabBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(40f));
		this.registerDefaultState(this.stateDefinition.any().setValue(DAMAGE, 0));
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
