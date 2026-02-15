package net.mcreator.ironblood.block;

import org.valkyrienskies.core.impl.shadow.id;
import org.valkyrienskies.core.impl.shadow.bs;
import org.valkyrienskies.core.impl.shadow.br;
import org.valkyrienskies.core.impl.shadow.bp;
import org.valkyrienskies.core.impl.shadow.be;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import net.minecraftforge.network.NetworkHooks;

import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.Containers;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.mcreator.ironblood.world.inventory.JointManagerMenu;
import net.mcreator.ironblood.block.entity.PilotSeatBlockEntity;

import io.netty.buffer.Unpooled;

import java.util.List;

public class PilotSeatBlock extends Block implements EntityBlock {
	public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;

	public PilotSeatBlock() {
		super(BlockBehaviour.Properties.of().sound(SoundType.WOOL).strength(35f).noOcclusion().isRedstoneConductor((bs, br, bp) -> false));
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
		// Simple half slab shape - 8 pixels high (half block)
		return box(0, 0, 0, 16, 8, 16);
	}

	@Override
	protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
		super.createBlockStateDefinition(builder);
		builder.add(FACING);
	}

	@Override
	public BlockState getStateForPlacement(BlockPlaceContext context) {
		return super.getStateForPlacement(context).setValue(FACING, context.getHorizontalDirection().getOpposite());
	}

	public BlockState rotate(BlockState state, Rotation rot) {
		return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
	}

	public BlockState mirror(BlockState state, Mirror mirrorIn) {
		return state.rotate(mirrorIn.getRotation(state.getValue(FACING)));
	}

	@Override
	public InteractionResult use(BlockState blockstate, Level world, BlockPos pos, Player entity, InteractionHand hand, BlockHitResult hit) {
		super.use(blockstate, world, pos, entity, hand, hit);
		
		// Shift + right-click opens the Joint Manager menu
		if (entity.isShiftKeyDown()) {
			if (entity instanceof ServerPlayer player) {
				NetworkHooks.openScreen(player, new MenuProvider() {
					@Override
					public Component getDisplayName() {
						return Component.literal("Joint Manager");
					}

					@Override
					public AbstractContainerMenu createMenu(int id, Inventory inventory, Player player) {
						return new JointManagerMenu(id, inventory, new FriendlyByteBuf(Unpooled.buffer()).writeBlockPos(pos));
					}
				}, pos);
			}
			return InteractionResult.SUCCESS;
		}
		
		// Regular right-click makes the player sit
		if (!world.isClientSide && entity instanceof ServerPlayer) {
			// Calculate sit position based on block facing
			Direction facing = blockstate.getValue(FACING);
			Vec3 sitPos = getSitPosition(pos, facing);
			
			// Create an invisible armor stand to sit on
			ArmorStand seat = new ArmorStand(EntityType.ARMOR_STAND, world);
			seat.setPos(sitPos.x, sitPos.y, sitPos.z);
			seat.setInvisible(true);
			seat.setInvulnerable(true);
			seat.setNoGravity(true);
			
			// Spawn the armor stand and make the player ride it
			world.addFreshEntity(seat);
			entity.startRiding(seat);
		}
		
		return InteractionResult.SUCCESS;
	}
	
	/**
	 * Calculate the sitting position based on the block position and facing direction
	 */
	private Vec3 getSitPosition(BlockPos pos, Direction facing) {
		double x = pos.getX() + 0.5;
		double y = pos.getY() + 0.25 - 1.5; // Sit height adjusted for half slab, lowered by 1.5 blocks
		double z = pos.getZ() + 0.5;
		
		return new Vec3(x, y, z);
	}

	@Override
	public void onRemove(BlockState state, Level world, BlockPos pos, BlockState newState, boolean isMoving) {
		if (state.getBlock() != newState.getBlock()) {
			BlockEntity blockEntity = world.getBlockEntity(pos);
			if (blockEntity instanceof PilotSeatBlockEntity) {
				Containers.dropContents(world, pos, (PilotSeatBlockEntity) blockEntity);
				world.updateNeighbourForOutputSignal(pos, this);
			}
			
			// Remove any sitting entities (armor stands) at this position
			AABB searchBox = new AABB(pos).inflate(1.0);
			List<ArmorStand> stands = world.getEntitiesOfClass(ArmorStand.class, searchBox);
			for (ArmorStand stand : stands) {
				if (stand.isInvisible() && stand.isInvulnerable()) {
					// Eject any passengers before removing
					stand.ejectPassengers();
					stand.discard();
				}
			}
			
			super.onRemove(state, world, pos, newState, isMoving);
		}
	}

	@Override
	public boolean hasAnalogOutputSignal(BlockState state) {
		return true;
	}

	@Override
	public int getAnalogOutputSignal(BlockState blockState, Level world, BlockPos pos) {
		BlockEntity tileentity = world.getBlockEntity(pos);
		if (tileentity instanceof PilotSeatBlockEntity be)
			return AbstractContainerMenu.getRedstoneSignalFromContainer(be);
		else
			return 0;
	}

	@Override
	public MenuProvider getMenuProvider(BlockState state, Level worldIn, BlockPos pos) {
		BlockEntity tileEntity = worldIn.getBlockEntity(pos);
		return tileEntity instanceof MenuProvider menuProvider ? menuProvider : null;
	}

	@Override
	public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
		return new PilotSeatBlockEntity(pos, state);
	}

	@Override
	public boolean triggerEvent(BlockState state, Level world, BlockPos pos, int eventID, int eventParam) {
		super.triggerEvent(state, world, pos, eventID, eventParam);
		BlockEntity blockEntity = world.getBlockEntity(pos);
		return blockEntity == null ? false : blockEntity.triggerEvent(eventID, eventParam);
	}
}