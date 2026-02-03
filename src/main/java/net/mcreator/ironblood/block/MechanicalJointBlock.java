package net.mcreator.ironblood.block;

import org.valkyrienskies.core.impl.shadow.bs;
import org.valkyrienskies.core.impl.shadow.br;
import org.valkyrienskies.core.impl.shadow.bp;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.mcreator.ironblood.block.entity.MechanicalJointBlockEntity;
import net.mcreator.ironblood.init.IronbloodModBlockEntities;

import javax.annotation.Nullable;

public class MechanicalJointBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = DirectionalBlock.FACING;

	public MechanicalJointBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.METAL).strength(60f, 50f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
		this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
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
		return switch (state.getValue(FACING)) {
			default -> Shapes.or(box(0, 4, 0, 5, 12, 12), box(11, 4, 0, 16, 12, 12));
			case NORTH -> Shapes.or(box(11, 4, 4, 16, 12, 16), box(0, 4, 4, 5, 12, 16));
			case EAST -> Shapes.or(box(0, 4, 11, 12, 12, 16), box(0, 4, 0, 12, 12, 5));
			case WEST -> Shapes.or(box(4, 4, 0, 16, 12, 5), box(4, 4, 11, 16, 12, 16));
			case UP -> Shapes.or(box(11, 0, 4, 16, 12, 12), box(0, 0, 4, 5, 12, 12));
			case DOWN -> Shapes.or(box(11, 4, 4, 16, 16, 12), box(0, 4, 4, 5, 16, 12));
		};
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(FACING, context.getClickedFace());
	}

	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	@Nullable
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new MechanicalJointBlockEntity(pos, state);
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (!state.is(newState.getBlock())) {
			var be = world.getBlockEntity(pos);
			if (be instanceof MechanicalJointBlockEntity jointBE) {
				jointBE.removeJoint();
			}
		}
		super.onRemove(state, world, pos, newState, isMoving);
	}
}