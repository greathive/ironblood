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
import net.mcreator.ironblood.block.entity.SwivelBearingBlockEntity;
import net.mcreator.ironblood.block.entity.SwivelBearingTableBlockEntity;
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
		boolean isSwivelBearing         = blockState.getBlock() == IronbloodModBlocks.SWIVEL_BEARING.get();
		boolean isSwivelBearingTable    = blockState.getBlock() == IronbloodModBlocks.SWIVEL_BEARING_TABLE.get();

		if (!isMechanicalJoint && !isMechanicalJointAlt && !isMechanicalSwivelJoint && !isSwivelBearing && !isSwivelBearingTable) {
			return;
		}

		// Only get facing for blocks that have the FACING property
		Direction facing = null;
		if (!isSwivelBearing && !isSwivelBearingTable) {
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
		} else {
			blockType = "swivel_bearing_table";
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
			} else { // swivel bearing table
				var be = serverLevel.getBlockEntity(blockPos);
				if (!(be instanceof SwivelBearingTableBlockEntity tableBE)) {
					player.displayClientMessage(Component.literal("§cNo block entity found!"), true);
					return;
				}
				if (tableBE.hasJoint()) {
					player.displayClientMessage(Component.literal("§cThis block is already linked!"), true);
					return;
				}
			}

			ServerShip ship = VSGameUtilsKt.getShipManagingPos(serverLevel, blockPos);
			data.setFirstLink(ship, blockPos, facing, blockType);

			if (isMechanicalSwivelJoint) {
				player.displayClientMessage(Component.literal("§aFirst swivel joint selected! Now click another swivel joint on a different ship."), true);
			} else if (isSwivelBearing || isSwivelBearingTable) {
				player.displayClientMessage(Component.literal("§aFirst block selected! Now click the opposite swivel bearing type on a different ship."), true);
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
		}

		if (!validPair) {
			if (firstBlockType.equals("mechanical_swivel_joint")) {
				player.displayClientMessage(Component.literal("§cSwivel joints can only link with other swivel joints!"), true);
			} else if (firstBlockType.equals("swivel_bearing") || firstBlockType.equals("swivel_bearing_table")) {
				player.displayClientMessage(Component.literal("§cYou must link a Swivel Bearing with a Swivel Bearing Table!"), true);
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

	@Nullable
	private static Direction getBlockFacing(BlockState blockState) {
		if (blockState.hasProperty(BlockStateProperties.FACING)) {
			return blockState.getValue(BlockStateProperties.FACING);
		} else if (blockState.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) {
			return blockState.getValue(BlockStateProperties.HORIZONTAL_FACING);
		}
		return null;
	}

	private static Vec3 getRotationFromFacing(Direction facing) {
		return switch (facing) {
			case UP    -> new Vec3(0, 0, 0);
			case DOWN  -> new Vec3(Math.PI, 0, 0);
			case NORTH -> new Vec3(Math.PI / 2, 0, 0);
			case SOUTH -> new Vec3(Math.PI / 2, Math.PI, 0);
			case WEST  -> new Vec3(Math.PI / 2, Math.PI / 2, 0);
			case EAST  -> new Vec3(Math.PI / 2, -Math.PI / 2, 0);
		};
	}

	private static Vec3 getSwivelRotationFromFacing(Direction facing) {
		return switch (facing) {
			case UP    -> new Vec3(0, 0, 0);
			case DOWN  -> new Vec3(Math.PI, 0, 0);
			case NORTH -> new Vec3(Math.PI / 2, 0, 0);
			case SOUTH -> new Vec3(-Math.PI / 2, 0, 0);
			case WEST  -> new Vec3(0, 0, -Math.PI / 2);
			case EAST  -> new Vec3(0, 0, Math.PI / 2);
		};
	}

	private static Vec3 getOppositeSwivelRotation(Vec3 rotation) {
		// Flip the rotation by 180 degrees around the axis
		return new Vec3(rotation.x, rotation.y + Math.PI, rotation.z);
	}
}