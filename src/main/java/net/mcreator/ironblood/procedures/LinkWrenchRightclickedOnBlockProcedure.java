package net.mcreator.ironblood.procedures;

import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.api.ships.ServerShip;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.AttachFace;
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
import net.mcreator.ironblood.block.entity.VerticalRotatorBlockEntity;
import net.mcreator.ironblood.block.entity.VerticalHingeBlockEntity;
import net.mcreator.ironblood.block.entity.SwivelBearingBlockEntity;
import net.mcreator.ironblood.block.entity.SwivelBearingTableBlockEntity;
import net.mcreator.ironblood.block.entity.MechanicalJointBlockEntity;
import net.mcreator.ironblood.block.entity.MechanicalJointAltBlockEntity;
import net.mcreator.ironblood.block.entity.MechanicalSwivelJointBlockEntity;
import net.mcreator.ironblood.block.VerticalRotatorBlock;
import net.mcreator.ironblood.block.VerticalHingeBlock;
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
		boolean isSwivelBearing         = blockState.getBlock() == IronbloodModBlocks.SWIVEL_BEARING.get();
		boolean isSwivelBearingTable    = blockState.getBlock() == IronbloodModBlocks.SWIVEL_BEARING_TABLE.get();
		boolean isVerticalRotator       = blockState.getBlock() == IronbloodModBlocks.VERTICAL_ROTATOR.get();
		boolean isVerticalHinge         = blockState.getBlock() == IronbloodModBlocks.VERTICAL_HINGE.get();

		if (!isMechanicalJoint && !isMechanicalJointAlt && !isMechanicalSwivelJoint &&
				!isSwivelBearing && !isSwivelBearingTable && !isVerticalRotator && !isVerticalHinge) {
			return;
		}

		// Get facing/face for blocks that have them
		Direction facing = null;
		AttachFace face = null;
		if (isVerticalRotator || isVerticalHinge) {
			// These blocks have FACING and FACE properties
			if (blockState.hasProperty(VerticalRotatorBlock.FACING)) {
				facing = blockState.getValue(VerticalRotatorBlock.FACING);
			}
			if (blockState.hasProperty(VerticalRotatorBlock.FACE)) {
				face = blockState.getValue(VerticalRotatorBlock.FACE);
			}
		} else if (!isSwivelBearing && !isSwivelBearingTable) {
			// Mechanical joints have FACING property
			facing = getBlockFacing(blockState);
			if (facing == null) {
				player.displayClientMessage(Component.literal("§cError: Could not determine block facing!"), true);
				return;
			}
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
		} else if (isMechanicalSwivelJoint) {
			blockType = "mechanical_swivel_joint";
		} else if (isSwivelBearing) {
			blockType = "swivel_bearing";
		} else if (isSwivelBearingTable) {
			blockType = "swivel_bearing_table";
		} else if (isVerticalRotator) {
			blockType = "vertical_rotator";
		} else {
			blockType = "vertical_hinge";
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
			} else if (isMechanicalSwivelJoint) {
				var be = serverLevel.getBlockEntity(blockPos);
				if (!(be instanceof MechanicalSwivelJointBlockEntity swivelBE)) {
					player.displayClientMessage(Component.literal("§cNo block entity found!"), true);
					return;
				}
				if (swivelBE.hasJoint()) {
					player.displayClientMessage(Component.literal("§cThis block is already linked!"), true);
					return;
				}
			} else if (isSwivelBearing) {
				var be = serverLevel.getBlockEntity(blockPos);
				if (!(be instanceof SwivelBearingBlockEntity bearingBE)) {
					player.displayClientMessage(Component.literal("§cNo block entity found!"), true);
					return;
				}
				if (bearingBE.hasJoint()) {
					player.displayClientMessage(Component.literal("§cThis block is already linked!"), true);
					return;
				}
			} else if (isSwivelBearingTable) {
				var be = serverLevel.getBlockEntity(blockPos);
				if (!(be instanceof SwivelBearingTableBlockEntity tableBE)) {
					player.displayClientMessage(Component.literal("§cNo block entity found!"), true);
					return;
				}
				if (tableBE.hasJoint()) {
					player.displayClientMessage(Component.literal("§cThis block is already linked!"), true);
					return;
				}
			} else if (isVerticalRotator) {
				var be = serverLevel.getBlockEntity(blockPos);
				if (!(be instanceof VerticalRotatorBlockEntity rotatorBE)) {
					player.displayClientMessage(Component.literal("§cNo block entity found!"), true);
					return;
				}
				if (rotatorBE.hasJoint()) {
					player.displayClientMessage(Component.literal("§cThis block is already linked!"), true);
					return;
				}
			} else { // vertical hinge
				var be = serverLevel.getBlockEntity(blockPos);
				if (!(be instanceof VerticalHingeBlockEntity hingeBE)) {
					player.displayClientMessage(Component.literal("§cNo block entity found!"), true);
					return;
				}
				if (hingeBE.hasJoint()) {
					player.displayClientMessage(Component.literal("§cThis block is already linked!"), true);
					return;
				}
			}

			ServerShip ship = VSGameUtilsKt.getShipManagingPos(serverLevel, blockPos);
			data.setFirstLink(ship, blockPos, facing, blockType);
			// Store face for vertical rotator/hinge
			if (face != null) {
				data.setFirstFace(face);
			}

			if (isMechanicalSwivelJoint) {
				player.displayClientMessage(Component.literal("§aFirst swivel joint selected! Now click another swivel joint on a different ship."), true);
			} else if (isSwivelBearing || isSwivelBearingTable) {
				player.displayClientMessage(Component.literal("§aFirst block selected! Now click the opposite swivel bearing type on a different ship."), true);
			} else if (isVerticalRotator || isVerticalHinge) {
				player.displayClientMessage(Component.literal("§aFirst block selected! Now click the opposite type with OPPOSITE orientation."), true);
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
		// - swivel_bearing <-> swivel_bearing_table
		// - vertical_rotator <-> vertical_hinge
		boolean validPair = false;
		if (firstBlockType.equals("mechanical_joint") && blockType.equals("mechanical_joint_alt")) {
			validPair = true;
		} else if (firstBlockType.equals("mechanical_joint_alt") && blockType.equals("mechanical_joint")) {
			validPair = true;
		} else if (firstBlockType.equals("mechanical_swivel_joint") && blockType.equals("mechanical_swivel_joint")) {
			validPair = true;
		} else if (firstBlockType.equals("swivel_bearing") && blockType.equals("swivel_bearing_table")) {
			validPair = true;
		} else if (firstBlockType.equals("swivel_bearing_table") && blockType.equals("swivel_bearing")) {
			validPair = true;
		} else if (firstBlockType.equals("vertical_rotator") && blockType.equals("vertical_hinge")) {
			validPair = true;
		} else if (firstBlockType.equals("vertical_hinge") && blockType.equals("vertical_rotator")) {
			validPair = true;
		}

		if (!validPair) {
			if (firstBlockType.equals("mechanical_swivel_joint")) {
				player.displayClientMessage(Component.literal("§cSwivel joints can only link with other swivel joints!"), true);
			} else if (firstBlockType.equals("swivel_bearing") || firstBlockType.equals("swivel_bearing_table")) {
				player.displayClientMessage(Component.literal("§cYou must link a Swivel Bearing with a Swivel Bearing Table!"), true);
			} else if (firstBlockType.equals("vertical_rotator") || firstBlockType.equals("vertical_hinge")) {
				player.displayClientMessage(Component.literal("§cYou must link a Vertical Rotator with a Vertical Hinge!"), true);
			} else {
				player.displayClientMessage(Component.literal("§cYou must link a Mechanical Joint with an Alternate Mechanical Joint!"), true);
			}
			data.reset();
			return;
		}

		BlockPos firstBlockPos = data.getFirstBlockPos();

		// Must be on different ships or ship-to-world (null = world, always "different")
		ServerShip firstShip  = data.getFirstShip();
		ServerShip secondShip = VSGameUtilsKt.getShipManagingPos(serverLevel, blockPos);

		// Check if both are on the same ship
		if (firstShip != null && secondShip != null && firstShip.getId() == secondShip.getId()) {
			player.displayClientMessage(Component.literal("§cBoth blocks are on the same ship!"), true);
			data.reset();
			return;
		}

		// Check if both are in the world (both null)
		if (firstShip == null && secondShip == null) {
			player.displayClientMessage(Component.literal("§cBoth blocks are in the world! At least one must be on a ship."), true);
			data.reset();
			return;
		}

		// Handle vertical rotator/hinge pairing with opposite orientation check
		if (firstBlockType.equals("vertical_rotator") || firstBlockType.equals("vertical_hinge")) {
			VerticalRotatorBlockEntity rotatorBE;
			VerticalHingeBlockEntity hingeBE;
			BlockPos rotatorPos;
			BlockPos hingePos;
			Direction rotatorFacing;
			AttachFace rotatorFace;
			Direction hingeFacing;
			AttachFace hingeFace;

			if (isVerticalRotator) {
				// Second click is rotator, first click was hinge
				var raw1 = serverLevel.getBlockEntity(firstBlockPos);
				var raw2 = serverLevel.getBlockEntity(blockPos);
				if (!(raw1 instanceof VerticalHingeBlockEntity h)) {
					player.displayClientMessage(Component.literal("§cFirst block entity no longer exists!"), true);
					data.reset();
					return;
				}
				if (!(raw2 instanceof VerticalRotatorBlockEntity r)) {
					player.displayClientMessage(Component.literal("§cSecond block entity not found!"), true);
					data.reset();
					return;
				}
				hingeBE = h;
				rotatorBE = r;
				hingePos = firstBlockPos;
				rotatorPos = blockPos;
				hingeFacing = data.getFirstBlockFacing();
				hingeFace = data.getFirstFace();
				rotatorFacing = facing;
				rotatorFace = face;
			} else {
				// Second click is hinge, first click was rotator
				var raw1 = serverLevel.getBlockEntity(firstBlockPos);
				var raw2 = serverLevel.getBlockEntity(blockPos);
				if (!(raw1 instanceof VerticalRotatorBlockEntity r)) {
					player.displayClientMessage(Component.literal("§cFirst block entity no longer exists!"), true);
					data.reset();
					return;
				}
				if (!(raw2 instanceof VerticalHingeBlockEntity h)) {
					player.displayClientMessage(Component.literal("§cSecond block entity not found!"), true);
					data.reset();
					return;
				}
				rotatorBE = r;
				hingeBE = h;
				rotatorPos = firstBlockPos;
				hingePos = blockPos;
				rotatorFacing = data.getFirstBlockFacing();
				rotatorFace = data.getFirstFace();
				hingeFacing = facing;
				hingeFace = face;
			}

			if (rotatorBE.hasJoint()) {
				player.displayClientMessage(Component.literal("§cThe Vertical Rotator is already linked!"), true);
				data.reset();
				return;
			}
			if (hingeBE.hasJoint()) {
				player.displayClientMessage(Component.literal("§cThe Vertical Hinge is already linked!"), true);
				data.reset();
				return;
			}

			// CRITICAL: Check for opposite orientations
			if (!areOppositeOrientations(rotatorFacing, rotatorFace, hingeFacing, hingeFace)) {
				player.displayClientMessage(Component.literal("§cBlocks must have OPPOSITE orientations! (UP↔DOWN, NORTH↔SOUTH, EAST↔WEST)"), true);
				data.reset();
				return;
			}

			// Build the joint with vertical (up/down) rotation
			Vec3 pos1 = new Vec3(rotatorPos.getX() + 0.5, rotatorPos.getY() + 0.5, rotatorPos.getZ() + 0.5);
			Vec3 pos2 = new Vec3(hingePos.getX() + 0.5, hingePos.getY() + 0.5, hingePos.getZ() + 0.5);

			// Get rotation vectors based on the block orientations
			Vec3 rot1 = getVerticalRotationFromOrientation(rotatorFacing, rotatorFace);
			Vec3 rot2 = getVerticalRotationFromOrientation(hingeFacing, hingeFace);

			// Vertical rotation: -90° to +90° (up and down pitching)
			VSJoint joint = JointUtil.makeRevoluteJoint(
					firstShip, secondShip,
					rot1, rot2,
					pos1, pos2,
					-Math.PI / 2, Math.PI / 2 // -90° to +90°
			);

			if (joint != null) {
				JointUtil.addJoint(serverLevel, joint, (jointId) -> {
					// Rotator BE owns the jointId; hinge just stores the link back
					rotatorBE.setJointId(jointId);
					rotatorBE.setLinkedBlockPos(hingePos);
					hingeBE.setLinkedBlockPos(rotatorPos);

					// Save joint creation data for persistence
					rotatorBE.setJointCreationData(pos1, pos2, rot1, rot2, firstShip, secondShip, "revolute", -Math.PI / 2, Math.PI / 2);

					player.displayClientMessage(Component.literal("§aVertical rotator linked! Joint ID: " + jointId), true);
				});
			} else {
				player.displayClientMessage(Component.literal("§cFailed to create joint!"), true);
			}

			data.reset();
			return;
		}

		// Handle swivel bearing pairing (bearing <-> table)
		if (firstBlockType.equals("swivel_bearing") || firstBlockType.equals("swivel_bearing_table")) {
			SwivelBearingBlockEntity bearingBE;
			SwivelBearingTableBlockEntity tableBE;
			BlockPos bearingPos;
			BlockPos tablePos;

			if (isSwivelBearing) {
				// Second click is the bearing — first click was table
				var raw1 = serverLevel.getBlockEntity(firstBlockPos);
				var raw2 = serverLevel.getBlockEntity(blockPos);
				if (!(raw1 instanceof SwivelBearingTableBlockEntity t)) {
					player.displayClientMessage(Component.literal("§cFirst block entity no longer exists!"), true);
					data.reset();
					return;
				}
				if (!(raw2 instanceof SwivelBearingBlockEntity b)) {
					player.displayClientMessage(Component.literal("§cSecond block entity not found!"), true);
					data.reset();
					return;
				}
				tableBE = t;
				bearingBE = b;
				tablePos = firstBlockPos;
				bearingPos = blockPos;
			} else {
				// Second click is the table — first click was bearing
				var raw1 = serverLevel.getBlockEntity(firstBlockPos);
				var raw2 = serverLevel.getBlockEntity(blockPos);
				if (!(raw1 instanceof SwivelBearingBlockEntity b)) {
					player.displayClientMessage(Component.literal("§cFirst block entity no longer exists!"), true);
					data.reset();
					return;
				}
				if (!(raw2 instanceof SwivelBearingTableBlockEntity t)) {
					player.displayClientMessage(Component.literal("§cSecond block entity not found!"), true);
					data.reset();
					return;
				}
				bearingBE = b;
				tableBE = t;
				bearingPos = firstBlockPos;
				tablePos = blockPos;
			}

			if (bearingBE.hasJoint()) {
				player.displayClientMessage(Component.literal("§cThe Swivel Bearing is already linked!"), true);
				data.reset();
				return;
			}
			if (tableBE.hasJoint()) {
				player.displayClientMessage(Component.literal("§cThe Swivel Bearing Table is already linked!"), true);
				data.reset();
				return;
			}

			// Build the joint with horizontal rotation (Y-axis spin)
			Vec3 pos1 = new Vec3(firstBlockPos.getX() + 0.5, firstBlockPos.getY() + 0.5, firstBlockPos.getZ() + 0.5);
			Vec3 pos2 = new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);

			// Z-axis rotation for horizontal spinning (correct!)
			Vec3 rot1 = new Vec3(0, 0, Math.toRadians(90));
			Vec3 rot2 = new Vec3(0, 0, Math.toRadians(90));

			// CRITICAL: Limits must be between -2*PI and 2*PI (not 0 to 360)
			// Use full rotation range: -PI to PI
			VSJoint joint = JointUtil.makeRevoluteJoint(
					firstShip, secondShip,
					rot1, rot2,
					pos1, pos2,
					-Math.PI, Math.PI // -180° to +180° (full rotation, within valid range)
			);

			if (joint != null) {
				JointUtil.addJoint(serverLevel, joint, (jointId) -> {
					// Bearing BE owns the jointId; table just stores the link back
					bearingBE.setJointId(jointId);
					bearingBE.setLinkedBlockPos(tablePos);
					tableBE.setLinkedBlockPos(bearingPos);

					// Save joint creation data for persistence
					bearingBE.setJointCreationData(pos1, pos2, rot1, rot2, firstShip, secondShip, "revolute", -Math.PI, Math.PI);

					player.displayClientMessage(Component.literal("§aSwivel bearing linked! Joint ID: " + jointId), true);
				});
			} else {
				player.displayClientMessage(Component.literal("§cFailed to create joint!"), true);
			}

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
	 * Checks if two orientations are opposite.
	 * Rules:
	 * - FLOOR <-> CEILING (opposite faces)
	 * - WALL with opposite FACING (NORTH <-> SOUTH, EAST <-> WEST)
	 */
	private static boolean areOppositeOrientations(Direction facing1, AttachFace face1, Direction facing2, AttachFace face2) {
		// If both on floor/ceiling, they must be opposite (floor vs ceiling)
		if ((face1 == AttachFace.FLOOR || face1 == AttachFace.CEILING) &&
				(face2 == AttachFace.FLOOR || face2 == AttachFace.CEILING)) {
			return face1 != face2; // FLOOR <-> CEILING
		}

		// If both on walls, facings must be opposite
		if (face1 == AttachFace.WALL && face2 == AttachFace.WALL) {
			return facing1.getOpposite() == facing2; // NORTH <-> SOUTH, EAST <-> WEST
		}

		// Mixed (one on floor/ceiling, one on wall) - not considered opposite
		return false;
	}

	/**
	 * Gets rotation vector for vertical rotation based on block orientation.
	 * The rotation allows vertical (up/down) pitching motion.
	 */
	private static Vec3 getVerticalRotationFromOrientation(Direction facing, AttachFace face) {
		if (face == AttachFace.FLOOR) {
			// On floor - rotate to allow vertical pitch
			return new Vec3(Math.toRadians(90), 0, 0);
		} else if (face == AttachFace.CEILING) {
			// On ceiling - rotate to allow vertical pitch (inverted)
			return new Vec3(-Math.toRadians(90), 0, 0);
		} else {
			// On wall - rotation depends on which wall
			return switch (facing) {
				case NORTH -> new Vec3(0, 0, 0);
				case SOUTH -> new Vec3(0, Math.PI, 0);
				case EAST  -> new Vec3(0, Math.toRadians(90), 0);
				case WEST  -> new Vec3(0, -Math.toRadians(90), 0);
				default    -> new Vec3(0, 0, 0);
			};
		}
	}

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