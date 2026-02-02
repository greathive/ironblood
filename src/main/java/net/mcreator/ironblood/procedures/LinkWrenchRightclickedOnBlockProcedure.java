package net.mcreator.ironblood.procedures;

import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.api.ships.ServerShip;

import org.joml.Vector3d;
import org.joml.Quaterniondc;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.mcreator.ironblood.ships.LinkWrenchManager;
import net.mcreator.ironblood.ships.LinkWrenchData;
import net.mcreator.ironblood.ships.JointUtil;
import net.mcreator.ironblood.ships.JointTrackingAttachment;
import net.mcreator.ironblood.init.IronbloodModBlocks;

import javax.annotation.Nullable;

public class LinkWrenchRightclickedOnBlockProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null || !(entity instanceof Player)) {
			return;
		}
		
		if (!(world instanceof ServerLevel serverLevel)) {
			return;
		}
		
		Player player = (Player) entity;
		BlockPos blockPos = BlockPos.containing(x, y, z);
		BlockState blockState = world.getBlockState(blockPos);
		
		// Check if the block is a mechanical joint or alternate mechanical joint
		boolean isMechanicalJoint = blockState.getBlock() == IronbloodModBlocks.MECHANICAL_JOINT.get();
		boolean isMechanicalJointAlt = blockState.getBlock() == IronbloodModBlocks.MECHANICAL_JOINT_ALT.get();
		
		if (!isMechanicalJoint && !isMechanicalJointAlt) {
			return;
		}
		
		// Get the block's facing direction
		Direction facing = getBlockFacing(blockState);
		if (facing == null) {
			player.displayClientMessage(Component.literal("§cError: Could not determine block facing!"), true);
			return;
		}
		
		// Get the link wrench data for this player
		LinkWrenchData data = LinkWrenchManager.getData(player);
		
		// If shift-clicking, reset the link wrench
		if (player.isShiftKeyDown()) {
			data.reset();
			player.displayClientMessage(Component.literal("§eLink wrench reset!"), true);
			return;
		}
		
		// Determine block type
		String blockType = isMechanicalJoint ? "mechanical_joint" : "mechanical_joint_alt";
		
		// Get the ship managing this block position
		final LoadedServerShip ship = VSGameUtilsKt.getLoadedShipManagingPos(serverLevel, blockPos);
		
		// If this is the first link
		if (!data.hasFirstLink()) {
			// Store as ServerShip for compatibility
			ServerShip serverShip = ship;
			data.setFirstLink(serverShip, blockPos, facing, blockType);
			player.displayClientMessage(Component.literal("§aFirst block selected! Now click on the opposite joint type on a different ship."), true);
			return;
		}
		
		// This is the second link - validate and create joint
		
		// Check if the block types are opposite
		String firstBlockType = data.getFirstBlockType();
		if (firstBlockType.equals(blockType)) {
			player.displayClientMessage(Component.literal("§cYou must link a Mechanical Joint with an Alternate Mechanical Joint!"), true);
			data.reset();
			return;
		}
		
		// Get the first ship (need ServerShip for joint creation but LoadedServerShip for tracking)
		ServerShip firstShip = data.getFirstShip();
		BlockPos firstBlockPos = data.getFirstBlockPos();
		Direction firstFacing = data.getFirstBlockFacing();
		final LoadedServerShip firstLoadedShip = VSGameUtilsKt.getLoadedShipManagingPos(serverLevel, firstBlockPos);
		
		// Both blocks must be on ships OR one must be on a ship and the other in world
		if (firstShip != null && ship != null && firstShip.getId() == ship.getId()) {
			player.displayClientMessage(Component.literal("§cBoth blocks are on the same ship! They must be on different ships or one must be in the world."), true);
			data.reset();
			return;
		}
		
		// Calculate joint positions in ship/world space
		Vec3 firstJointPos = calculateJointPosition(serverLevel, firstBlockPos, firstShip);
		Vec3 secondJointPos = calculateJointPosition(serverLevel, blockPos, ship);
		
		// Calculate joint rotations based on block facing
		Vec3 firstJointRot = calculateJointRotation(firstFacing, firstShip, serverLevel, firstBlockPos);
		Vec3 secondJointRot = calculateJointRotation(facing, ship, serverLevel, blockPos);
		
		// Create the revolute joint with 0-90 degree limits
		VSJoint joint = JointUtil.makeRevoluteJoint(
			firstShip,
			ship,
			firstJointRot,
			secondJointRot,
			firstJointPos,
			secondJointPos,
			0.0,  // Lower limit: 0 degrees
			Math.toRadians(90.0)  // Upper limit: 90 degrees in radians
		);
		
		if (joint != null) {
			// Capture positions for the callback
			final BlockPos capturedFirstPos = firstBlockPos;
			final BlockPos capturedSecondPos = blockPos;
			
			JointUtil.addJoint((Level) world, joint, (jointId) -> {
				// Register the joint in both ships' tracking attachments
				if (firstLoadedShip != null) {
					JointTrackingAttachment.getOrCreate(firstLoadedShip).registerJoint(capturedFirstPos, jointId);
				}
				
				if (ship != null) {
					JointTrackingAttachment.getOrCreate(ship).registerJoint(capturedSecondPos, jointId);
				}
				
				player.displayClientMessage(Component.literal("§aShips linked successfully! Joint ID: " + jointId), true);
			});
		} else {
			player.displayClientMessage(Component.literal("§cFailed to create joint!"), true);
		}
		
		// Reset the link wrench data
		data.reset();
	}
	
	/**
	 * Calculate the joint position in ship or world space
	 * For ships, this should be in shipyard coordinates (the ship's local block grid)
	 * For world, this should be world coordinates
	 */
	private static Vec3 calculateJointPosition(ServerLevel level, BlockPos blockPos, @Nullable ServerShip ship) {
		// The joint position is simply the block position in its respective coordinate space
		// For ships: shipyard coordinates (the block grid of the ship)
		// For world: world coordinates
		return new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
	}
	
	/**
	 * Calculate the joint rotation based on block facing direction
	 */
	private static Vec3 calculateJointRotation(Direction facing, @Nullable ServerShip ship, ServerLevel level, BlockPos blockPos) {
		// The rotation should define the joint's local coordinate system
		// For a revolute joint, this defines the hinge axis
		
		// Get base rotation in radians based on facing
		Vec3 baseRotation = getRotationFromFacing(facing);
		
		// Return in radians (the joint system expects radians)
		return baseRotation;
	}
	
	/**
	 * Get the rotation vector based on block facing direction
	 * Returns rotation in radians defining the joint's local frame
	 * For revolute joints, the Z-axis of this frame is the hinge axis
	 * We want a hinge that allows up/down rotation (like a door opening up)
	 */
	private static Vec3 getRotationFromFacing(Direction facing) {
		// The revolute joint rotates around the LOCAL Z-axis
		// For up/down motion, we want the Z-axis horizontal and perpendicular to the facing
		
		switch (facing) {
			case NORTH:  // Facing north (-Z), hinge should be along Z axis (front-back)
				return new Vec3(Math.toRadians(90), 0, 0);
			case SOUTH:  // Facing south (+Z), hinge should be along Z axis (front-back)
				return new Vec3(Math.toRadians(90), 0, 0);
			case WEST:   // Facing west (-X), hinge should be along X axis (left-right)
				return new Vec3(0, 0, Math.toRadians(90));
			case EAST:   // Facing east (+X), hinge should be along X axis (left-right)
				return new Vec3(0, 0, Math.toRadians(90));
			case DOWN:   // Facing down (-Y), hinge should be along Z axis
				return new Vec3(Math.toRadians(90), 0, 0);
			case UP:     // Facing up (+Y), hinge should be along Z axis
				return new Vec3(Math.toRadians(90), 0, 0);
			default:
				return new Vec3(0, 0, 0);
		}
	}
	
	/**
	 * Get the facing direction from a block state
	 */
	@Nullable
	private static Direction getBlockFacing(BlockState blockState) {
		// Try to get the FACING property (most common for blocks that can face any direction)
		if (blockState.hasProperty(BlockStateProperties.FACING)) {
			return blockState.getValue(BlockStateProperties.FACING);
		}
		
		// Try HORIZONTAL_FACING (for blocks that only face horizontally)
		if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			return blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
		}
		
		// Default to NORTH if no facing property found
		return Direction.NORTH;
	}
}
