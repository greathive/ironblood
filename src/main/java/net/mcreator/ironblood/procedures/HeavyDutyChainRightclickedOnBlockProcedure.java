package net.mcreator.ironblood.procedures;

import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.api.ships.ServerShip;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

import net.mcreator.ironblood.ships.JointUtil;
import net.mcreator.ironblood.ships.ChainLinkManager;
import net.mcreator.ironblood.ships.ChainLinkData;
import net.mcreator.ironblood.init.IronbloodModBlocks;
import net.mcreator.ironblood.block.entity.HeavyDutyChainLinkBlockEntity;

import org.joml.Vector3d;

import java.util.Map;
import java.util.UUID;

public class HeavyDutyChainRightclickedOnBlockProcedure {

	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
		if (entity == null || !(entity instanceof Player player)) {
			return;
		}
		if (!(world instanceof ServerLevel serverLevel)) {
			return;
		}

		BlockPos clickedPos = BlockPos.containing(x, y, z);
		BlockState clickedState = serverLevel.getBlockState(clickedPos);

		// Check if clicked block is a HeavyDutyChainLink
		if (clickedState.getBlock() != IronbloodModBlocks.HEAVY_DUTY_CHAIN_LINK.get()) {
			player.displayClientMessage(Component.literal("§cYou must click on a Heavy Duty Chain Link block!"), true);
			return;
		}

		// Get the block entity
		var clickedBE = serverLevel.getBlockEntity(clickedPos);
		if (!(clickedBE instanceof HeavyDutyChainLinkBlockEntity chainLinkBE)) {
			player.displayClientMessage(Component.literal("§cError: Block entity not found!"), true);
			return;
		}

		// Check if this chain link already has a connection
		if (chainLinkBE.hasChainLink()) {
			player.displayClientMessage(Component.literal("§cThis chain link is already connected!"), true);
			return;
		}

		// Check for first or second point selection
		String selectedPosStr = itemstack.getOrCreateTag().getString("chainFirstPoint");

		if (selectedPosStr.isEmpty()) {
			// First point - store it
			ServerShip ship = VSGameUtilsKt.getShipManagingPos(serverLevel, clickedPos);
			String shipIdStr = ship != null ? String.valueOf(ship.getId()) : "world";

			itemstack.getOrCreateTag().putString("chainFirstPoint",
					clickedPos.getX() + "," + clickedPos.getY() + "," + clickedPos.getZ() + "," + shipIdStr);

			if (ship != null) {
				player.displayClientMessage(Component.literal("§aFirst chain link selected on ship! Right-click second chain link."), true);
			} else {
				player.displayClientMessage(Component.literal("§aFirst chain link selected in world! Right-click second chain link."), true);
			}
		} else {
			// Second point - create the chain link
			String[] parts = selectedPosStr.split(",");
			if (parts.length != 4) {
				itemstack.getOrCreateTag().remove("chainFirstPoint");
				player.displayClientMessage(Component.literal("§cError! Chain creation cancelled."), true);
				return;
			}

			BlockPos firstPos = new BlockPos(
					Integer.parseInt(parts[0]),
					Integer.parseInt(parts[1]),
					Integer.parseInt(parts[2])
			);

			// Verify first block still exists and is a chain link
			BlockState firstState = serverLevel.getBlockState(firstPos);
			if (firstState.getBlock() != IronbloodModBlocks.HEAVY_DUTY_CHAIN_LINK.get()) {
				itemstack.getOrCreateTag().remove("chainFirstPoint");
				player.displayClientMessage(Component.literal("§cFirst chain link no longer exists!"), true);
				return;
			}

			var firstBE = serverLevel.getBlockEntity(firstPos);
			if (!(firstBE instanceof HeavyDutyChainLinkBlockEntity firstChainBE)) {
				itemstack.getOrCreateTag().remove("chainFirstPoint");
				player.displayClientMessage(Component.literal("§cFirst chain link error!"), true);
				return;
			}

			if (firstChainBE.hasChainLink()) {
				itemstack.getOrCreateTag().remove("chainFirstPoint");
				player.displayClientMessage(Component.literal("§cFirst chain link is already connected!"), true);
				return;
			}

			String firstShipIdStr = parts[3];
			ServerShip firstShip = null;
			if (!firstShipIdStr.equals("world")) {
				long firstShipId = Long.parseLong(firstShipIdStr);
				firstShip = VSGameUtilsKt.getShipObjectWorld(serverLevel).getAllShips().getById(firstShipId);
			}

			ServerShip secondShip = VSGameUtilsKt.getShipManagingPos(serverLevel, clickedPos);
			BlockPos secondPos = clickedPos;

			// Calculate initial distance between points
			Vec3 firstWorldPos = getWorldPosition(firstShip, firstPos);
			Vec3 secondWorldPos = getWorldPosition(secondShip, secondPos);
			double initialDistance = firstWorldPos.distanceTo(secondWorldPos);

			// Create the chain link data
			ChainLinkData linkData = new ChainLinkData(firstShip, firstPos, secondShip, secondPos, initialDistance);
			UUID linkId = ChainLinkManager.createLink(linkData);

			// Set the chain link ID in both block entities
			firstChainBE.setChainLink(linkId, true);
			chainLinkBE.setChainLink(linkId, false);

			// Set partner positions for client-side rendering
			firstChainBE.setPartnerPosition(secondPos);
			chainLinkBE.setPartnerPosition(firstPos);

			// Force sync to client
			serverLevel.sendBlockUpdated(firstPos, serverLevel.getBlockState(firstPos), serverLevel.getBlockState(firstPos), 3);
			serverLevel.sendBlockUpdated(secondPos, serverLevel.getBlockState(secondPos), serverLevel.getBlockState(secondPos), 3);

			// Create the distance joint
			createDistanceJoint(serverLevel, linkData);

			// CRITICAL: Save joint creation data in both block entities for persistence
			firstChainBE.setJointCreationData(firstWorldPos, secondWorldPos, firstShip, secondShip,
					initialDistance, linkData.getCurrentMaxDistance(), firstPos, secondPos);
			chainLinkBE.setJointCreationData(firstWorldPos, secondWorldPos, firstShip, secondShip,
					initialDistance, linkData.getCurrentMaxDistance(), firstPos, secondPos);

			// Clear selection
			itemstack.getOrCreateTag().remove("chainFirstPoint");

			player.displayClientMessage(Component.literal(
					String.format("§aChain created! Distance: %.1f blocks. Use redstone to adjust.", initialDistance)
			), true);
		}
	}

	/**
	 * Creates a distance joint between two chain link blocks.
	 * Made public so it can be called from HeavyDutyChainLinkBlockEntity.
	 */
	public static void createDistanceJoint(ServerLevel level, ChainLinkData linkData) {
		Vec3 firstWorldPos = getWorldPosition(linkData.getFirstShip(), linkData.getFirstBlockPos());
		Vec3 secondWorldPos = getWorldPosition(linkData.getSecondShip(), linkData.getSecondBlockPos());

		// Convert to ship-local positions for joint
		Vec3 firstLocalPos = linkData.getFirstShip() != null
				? worldToShipLocal(linkData.getFirstShip(), firstWorldPos)
				: firstWorldPos;
		Vec3 secondLocalPos = linkData.getSecondShip() != null
				? worldToShipLocal(linkData.getSecondShip(), secondWorldPos)
				: secondWorldPos;

		// Create distance joint with no rotation constraints
		VSJoint joint = JointUtil.makeDistanceJoint(
				linkData.getFirstShip(),
				linkData.getSecondShip(),
				new Vec3(0, 0, 0), // No rotation
				new Vec3(0, 0, 0), // No rotation
				firstLocalPos,
				secondLocalPos,
				null, // No min distance (allows objects to get closer)
				linkData.getCurrentMaxDistance() // Max distance
		);

		JointUtil.addJoint(level, joint, jointId -> {
			linkData.setJointId(jointId);
		});
	}

	private static Vec3 getWorldPosition(ServerShip ship, BlockPos blockPos) {
		if (ship == null) {
			// World position
			return new Vec3(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5);
		} else {
			// Ship position - transform from ship space to world space
			Vector3d shipLocal = VectorConversionsMCKt.toJOMLD(blockPos).add(0.5, 0.5, 0.5);
			Vector3d worldPos = ship.getShipToWorld().transformPosition(shipLocal);
			return new Vec3(worldPos.x, worldPos.y, worldPos.z);
		}
	}

	private static Vec3 worldToShipLocal(ServerShip ship, Vec3 worldPos) {
		Vector3d worldVec = new Vector3d(worldPos.x, worldPos.y, worldPos.z);
		Vector3d shipLocal = ship.getWorldToShip().transformPosition(worldVec);
		return new Vec3(shipLocal.x, shipLocal.y, shipLocal.z);
	}
}