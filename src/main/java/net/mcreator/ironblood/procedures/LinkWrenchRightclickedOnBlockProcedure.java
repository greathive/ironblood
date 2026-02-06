package net.mcreator.ironblood.procedures;

import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.api.ships.ServerShip;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;

import net.mcreator.ironblood.ships.LinkWrenchManager;
import net.mcreator.ironblood.ships.LinkWrenchData;
import net.mcreator.ironblood.ships.JointUtil;
import net.mcreator.ironblood.block.entity.MechanicalJointBlockEntity;
import net.mcreator.ironblood.block.entity.MechanicalJointAltBlockEntity;
import net.mcreator.ironblood.block.entity.MechanicalSwivelJointBlockEntity;
import net.mcreator.ironblood.init.IronbloodModBlocks;

import javax.annotation.Nullable;

public class LinkWrenchRightclickedOnBlockProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null || !(entity instanceof Player player)) {
			return;
		}
		if (!(world instanceof ServerLevel serverLevel)) {
			return;
		}

		BlockPos blockPos = BlockPos.containing(x, y, z);
		BlockState blockState = serverLevel.getBlockState(blockPos);

		boolean isMechanicalJoint       = blockState.getBlock() == IronbloodModBlocks.MECHANICAL_JOINT.get();
		boolean isMechanicalJointAlt    = blockState.getBlock() == IronbloodModBlocks.MECHANICAL_JOINT_ALT.get();
		boolean isMechanicalSwivelJoint = blockState.getBlock() == IronbloodModBlocks.MECHANICAL_SWIVEL_JOINT.get();

		if (!isMechanicalJoint && !isMechanicalJointAlt && !isMechanicalSwivelJoint) {
			return;
		}

		Direction facing = getBlockFacing(blockState);
		if (facing == null) {
			player.displayClientMessage(Component.literal("§cError: Could not determine block facing!"), true);
			return;
		}

		LinkWrenchData data = LinkWrenchManager.getData(player);

		if (player.isShiftKeyDown()) {
			data.reset();
			player.displayClientMessage(Component.literal("§eLink wrench reset!"), true);
			return;
		}

		String blockType;
		if (isMechanicalJoint) {
			blockType = "mechanical_joint";
		} else if (isMechanicalJointAlt) {
			blockType = "mechanical_joint_alt";
		} else {
			blockType = "mechanical_swivel_joint";
		}

		// ----------------------------------------------------------------
		// FIRST CLICK
		// ----------------------------------------------------------------
		if (!data.hasFirstLink()) {
			// Verify the block entity exists and isn't already linked
			if (isMechanicalJoint) {
				var be = serverLevel.getBlockEntity(blockPos);
				if (!(be instanceof MechanicalJointBlockEntity jointBE)) {
					player.displayClientMessage(Component.literal("§cNo block entity found!"), true);
					return;
				}
				if (jointBE.hasJoint()) {
					player.displayClientMessage(Component.literal("§cThis block is already linked!"), true);
					return;
				}
			} else if (isMechanicalJointAlt) {
				var be = serverLevel.getBlockEntity(blockPos);
				if (!(be instanceof MechanicalJointAltBlockEntity altBE)) {
					player.displayClientMessage(Component.literal("§cNo block entity found!"), true);
					return;
				}
				if (altBE.hasJoint()) {
					player.displayClientMessage(Component.literal("§cThis block is already linked!"), true);
					return;
				}
			} else { // swivel joint
				var be = serverLevel.getBlockEntity(blockPos);
				if (!(be instanceof MechanicalSwivelJointBlockEntity swivelBE)) {
					player.displayClientMessage(Component.literal("§cNo block entity found!"), true);
					return;
				}
				if (swivelBE.hasJoint()) {
					player.displayClientMessage(Component.literal("§cThis block is already linked!"), true);
					return;
				}
			}

			ServerShip ship = VSGameUtilsKt.getShipManagingPos(serverLevel, blockPos);
			data.setFirstLink(ship, blockPos, facing, blockType);

			if (isMechanicalSwivelJoint) {
				player.displayClientMessage(Component.literal("§aFirst swivel joint selected! Now click another swivel joint on a different ship."), true);
			} else {
				player.displayClientMessage(Component.literal("§aFirst block selected! Now click the opposite joint type on a different ship."), true);
			}
			return;
		}

		// ----------------------------------------------------------------
		// SECOND CLICK
		// ----------------------------------------------------------------

		String firstBlockType = data.getFirstBlockType();

		// Validate pairing rules:
		// - mechanical_joint <-> mechanical_joint_alt
		// - mechanical_swivel_joint <-> mechanical_swivel_joint
		boolean validPair = false;
		if (firstBlockType.equals("mechanical_joint") && blockType.equals("mechanical_joint_alt")) {
			validPair = true;
		} else if (firstBlockType.equals("mechanical_joint_alt") && blockType.equals("mechanical_joint")) {
			validPair = true;
		} else if (firstBlockType.equals("mechanical_swivel_joint") && blockType.equals("mechanical_swivel_joint")) {
			validPair = true;
		}

		if (!validPair) {
			if (firstBlockType.equals("mechanical_swivel_joint")) {
				player.displayClientMessage(Component.literal("§cSwivel joints can only link with other swivel joints!"), true);
			} else {
				player.displayClientMessage(Component.literal("§cYou must link a Mechanical Joint with an Alternate Mechanical Joint!"), true);
			}
			data.reset();
			return;
		}

		BlockPos firstBlockPos = data.getFirstBlockPos();

		// Must be on different ships (null = world, always "different")
		ServerShip firstShip  = data.getFirstShip();
		ServerShip secondShip = VSGameUtilsKt.getShipManagingPos(serverLevel, blockPos);
		if (firstShip != null && secondShip != null && firstShip.getId() == secondShip.getId()) {
			player.displayClientMessage(Component.literal("§cBoth blocks are on the same ship!"), true);
			data.reset();
			return;
		}

		// Handle swivel joint pairing (both are same type)
		if (firstBlockType.equals("mechanical_swivel_joint")) {
			var firstRaw  = serverLevel.getBlockEntity(firstBlockPos);
			var secondRaw = serverLevel.getBlockEntity(blockPos);
			if (!(firstRaw instanceof MechanicalSwivelJointBlockEntity firstSwivel)) {
				player.displayClientMessage(Component.literal("§cFirst block entity no longer exists!"), true);
				data.reset();
				return;
			}
			if (!(secondRaw instanceof MechanicalSwivelJointBlockEntity secondSwivel)) {
				player.displayClientMessage(Component.literal("§cSecond block entity not found!"), true);
				data.reset();
				return;
			}
			if (firstSwivel.hasJoint()) {
				player.displayClientMessage(Component.literal("§cFirst swivel joint is already linked!"), true);
				data.reset();
				return;
			}
			if (secondSwivel.hasJoint()) {
				player.displayClientMessage(Component.literal("§cThis swivel joint is already linked!"), true);
				data.reset();
				return;
			}

			// Build the joint with side-to-side rotation
			Direction firstFacing = data.getFirstBlockFacing();
			Vec3 pos1 = new Vec3(firstBlockPos.getX() + 0.5, firstBlockPos.getY() + 0.5, firstBlockPos.getZ() + 0.5);
			Vec3 pos2 = new Vec3(blockPos.getX()      + 0.5, blockPos.getY()      + 0.5, blockPos.getZ()      + 0.5);
			Vec3 rot1 = getSwivelRotationFromFacing(firstFacing);
			// For the second joint, use opposite rotation so they face each other
			Vec3 rot2 = getOppositeSwivelRotation(rot1);

			VSJoint joint = JointUtil.makeRevoluteJoint(
					firstShip, secondShip,
					rot1, rot2,
					pos1, pos2,
					0.0, Math.toRadians(90.0)
			);

			if (joint != null) {
				JointUtil.addJoint(serverLevel, joint, (jointId) -> {
					// Both swivel joints store the joint ID
					firstSwivel.setJointId(jointId);
					firstSwivel.setLinkedBlockPos(blockPos);
					secondSwivel.setJointId(jointId);
					secondSwivel.setLinkedBlockPos(firstBlockPos);

					// CRITICAL: Save the joint creation data for persistence across world reloads
					// Both swivel joints need the data since either could reload first
					firstSwivel.setJointCreationData(pos1, pos2, rot1, rot2, firstShip, secondShip, "revolute", 0.0, Math.toRadians(90.0));
					secondSwivel.setJointCreationData(pos1, pos2, rot1, rot2, firstShip, secondShip, "revolute", 0.0, Math.toRadians(90.0));

					player.displayClientMessage(Component.literal("§aSwivel joints linked! Joint ID: " + jointId), true);
				});
			} else {
				player.displayClientMessage(Component.literal("§cFailed to create joint!"), true);
			}

			data.reset();
			return;
		}

		// Handle mechanical joint + alt pairing
		MechanicalJointBlockEntity mainBE;
		MechanicalJointAltBlockEntity altBE;
		BlockPos mainPos;
		BlockPos altPos;

		if (isMechanicalJoint) {
			// Second click is the main joint — first click was alt
			var raw1 = serverLevel.getBlockEntity(firstBlockPos);
			var raw2 = serverLevel.getBlockEntity(blockPos);
			if (!(raw1 instanceof MechanicalJointAltBlockEntity a)) {
				player.displayClientMessage(Component.literal("§cFirst block entity no longer exists!"), true);
				data.reset();
				return;
			}
			if (!(raw2 instanceof MechanicalJointBlockEntity m)) {
				player.displayClientMessage(Component.literal("§cSecond block entity not found!"), true);
				data.reset();
				return;
			}
			altBE  = a;
			mainBE = m;
			altPos  = firstBlockPos;
			mainPos = blockPos;
		} else {
			// Second click is the alt joint — first click was main
			var raw1 = serverLevel.getBlockEntity(firstBlockPos);
			var raw2 = serverLevel.getBlockEntity(blockPos);
			if (!(raw1 instanceof MechanicalJointBlockEntity m)) {
				player.displayClientMessage(Component.literal("§cFirst block entity no longer exists!"), true);
				data.reset();
				return;
			}
			if (!(raw2 instanceof MechanicalJointAltBlockEntity a)) {
				player.displayClientMessage(Component.literal("§cSecond block entity not found!"), true);
				data.reset();
				return;
			}
			mainBE  = m;
			altBE   = a;
			mainPos = firstBlockPos;
			altPos  = blockPos;
		}

		if (mainBE.hasJoint()) {
			player.displayClientMessage(Component.literal("§cThe Mechanical Joint is already linked!"), true);
			data.reset();
			return;
		}
		if (altBE.hasJoint()) {
			player.displayClientMessage(Component.literal("§cThe Alternate Mechanical Joint is already linked!"), true);
			data.reset();
			return;
		}

		// Build the joint with up/down rotation
		Direction firstFacing  = data.getFirstBlockFacing();
		Vec3 pos1 = new Vec3(firstBlockPos.getX() + 0.5, firstBlockPos.getY() + 0.5, firstBlockPos.getZ() + 0.5);
		Vec3 pos2 = new Vec3(blockPos.getX()      + 0.5, blockPos.getY()      + 0.5, blockPos.getZ()      + 0.5);
		Vec3 rot1 = getRotationFromFacing(firstFacing);
		Vec3 rot2 = getRotationFromFacing(facing);

		VSJoint joint = JointUtil.makeRevoluteJoint(
				firstShip, secondShip,
				rot1, rot2,
				pos1, pos2,
				0.0, Math.toRadians(90.0)
		);

		if (joint != null) {
			JointUtil.addJoint(serverLevel, joint, (jointId) -> {
				// Main BE owns the jointId; alt just stores the link back
				mainBE.setJointId(jointId);
				mainBE.setLinkedBlockPos(altPos);
				altBE.setLinkedBlockPos(mainPos);
				player.displayClientMessage(Component.literal("§aShips linked! Joint ID: " + jointId), true);
			});
		} else {
			player.displayClientMessage(Component.literal("§cFailed to create joint!"), true);
		}

		data.reset();
	}

	// ----------------------------------------------------------------
	// Helpers
	// ----------------------------------------------------------------

	/**
	 * Rotation for regular joints - produces up/down hinge motion
	 */
	private static Vec3 getRotationFromFacing(Direction facing) {
		switch (facing) {
			case NORTH:
			case SOUTH:
				return new Vec3(Math.toRadians(90), 0, 0);
			case EAST:
			case WEST:
				return new Vec3(0, 0, Math.toRadians(90));
			case UP:
			case DOWN:
			default:
				return new Vec3(Math.toRadians(90), 0, 0);
		}
	}

	/**
	 * Rotation for swivel joints - rotation axis depends on block orientation
	 * When placed on walls (NORTH/SOUTH/EAST/WEST): rotates up/down (pitch)
	 * When placed on floor/ceiling (UP/DOWN): rotates side-to-side (yaw)
	 */
	private static Vec3 getSwivelRotationFromFacing(Direction facing) {
		switch (facing) {
			case NORTH:
			case SOUTH:
				// On wall - swap to allow vertical rotation
				return new Vec3(0, 0, Math.toRadians(90));
			case EAST:
			case WEST:
				// On wall - swap to allow vertical rotation
				return new Vec3(Math.toRadians(90), 0, 0);
			case UP:
			case DOWN:
				// On floor/ceiling - rotate around vertical axis for horizontal swivel
				return new Vec3(0, Math.toRadians(90), 0);
			default:
				return new Vec3(0, Math.toRadians(90), 0);
		}
	}

	/**
	 * Returns the opposite rotation (180 degrees flipped) for swivel joints to face each other
	 */
	private static Vec3 getOppositeSwivelRotation(Vec3 rotation) {
		// Add 180 degrees (PI radians) to flip the orientation
		return new Vec3(
				rotation.x + Math.PI,
				rotation.y + Math.PI,
				rotation.z + Math.PI
		);
	}

	@Nullable
	private static Direction getBlockFacing(BlockState state) {
		if (state.hasProperty(BlockStateProperties.FACING)) {
			return state.getValue(BlockStateProperties.FACING);
		}
		if (state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			return state.getValue(BlockStateProperties.HORIZONTAL_FACING);
		}
		return Direction.NORTH;
	}
}