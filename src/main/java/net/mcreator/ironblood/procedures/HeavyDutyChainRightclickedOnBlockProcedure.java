package net.mcreator.ironblood.procedures;

import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.api.ships.ServerShip;

import net.minecraft.world.phys.Vec3;
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

import org.joml.Vector3d;

import java.util.Map;
import java.util.UUID;

public class HeavyDutyChainRightclickedOnBlockProcedure {
	private static final double DISTANCE_CHANGE_AMOUNT = 0.5; // Change distance by 0.5 blocks per click
	
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
		if (entity == null || !(entity instanceof Player player)) {
			return;
		}
		if (!(world instanceof ServerLevel serverLevel)) {
			return;
		}
		
		BlockPos clickedPos = BlockPos.containing(x, y, z);
		
		// Check if player clicked on an existing chain endpoint
		Map.Entry<UUID, ChainLinkData> existingLink = ChainLinkManager.findLinkAtPosition(clickedPos);
		
		if (existingLink != null) {
			// Clicked on an existing chain endpoint
			ChainLinkData linkData = existingLink.getValue();
			UUID linkId = existingLink.getKey();
			
			if (player.isShiftKeyDown()) {
				// Shift + right-click: Increase max distance
				linkData.increaseDistance(DISTANCE_CHANGE_AMOUNT);
				updateJoint(serverLevel, linkData);
				player.displayClientMessage(Component.literal(
					String.format("§aChain extended! Max distance: %.1f blocks", linkData.getCurrentMaxDistance())
				), true);
			} else {
				// Normal right-click: Decrease max distance (pulls objects together)
				linkData.decreaseDistance(DISTANCE_CHANGE_AMOUNT);
				updateJoint(serverLevel, linkData);
				player.displayClientMessage(Component.literal(
					String.format("§eChain shortened! Max distance: %.1f blocks", linkData.getCurrentMaxDistance())
				), true);
			}
			return;
		}
		
		// Not clicking on existing endpoint - check for first or second point selection
		String selectedPosStr = itemstack.getOrCreateTag().getString("chainFirstPoint");
		
		if (selectedPosStr.isEmpty()) {
			// First point - store it
			ServerShip ship = VSGameUtilsKt.getShipManagingPos(serverLevel, clickedPos);
			String shipIdStr = ship != null ? String.valueOf(ship.getId()) : "world";
			
			itemstack.getOrCreateTag().putString("chainFirstPoint", 
				clickedPos.getX() + "," + clickedPos.getY() + "," + clickedPos.getZ() + "," + shipIdStr);
			
			if (ship != null) {
				player.displayClientMessage(Component.literal("§aFirst point selected on ship! Right-click second point."), true);
			} else {
				player.displayClientMessage(Component.literal("§aFirst point selected in world! Right-click second point."), true);
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
			
			// Create the distance joint
			createDistanceJoint(serverLevel, linkData);
			
			// Clear selection
			itemstack.getOrCreateTag().remove("chainFirstPoint");
			
			player.displayClientMessage(Component.literal(
				String.format("§aChain created! Distance: %.1f blocks. Click endpoints to adjust.", initialDistance)
			), true);
		}
	}
	
	private static void createDistanceJoint(ServerLevel level, ChainLinkData linkData) {
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
	
	private static void updateJoint(ServerLevel level, ChainLinkData linkData) {
		if (!linkData.hasJoint()) {
			return;
		}
		
		// Remove old joint
		JointUtil.removeJointById(level, linkData.getJointId());
		
		// Create new joint with updated distance
		createDistanceJoint(level, linkData);
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
