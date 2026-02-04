package net.mcreator.ironblood.ships;

import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.core.api.ships.LoadedServerShip;

import org.joml.Vector3d;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.TickEvent;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

import net.mcreator.ironblood.init.IronbloodModItems;

@Mod.EventBusSubscriber
public class ChainDragTickHandler {
	// Massively increased multipliers to move heavy ships
	private static final double DRAG_FORCE_MULTIPLIER = 50000.0; // Increased from 500
	private static final double MAX_FORCE_RATIO = 5000.0; // Max force as ratio of mass - increased from 50
	private static int tickCounter = 0;
	
	@SubscribeEvent
	public static void onServerTick(TickEvent.ServerTickEvent event) {
		if (event.phase != TickEvent.Phase.END) {
			return;
		}
		
		tickCounter++;
		
		// Process all active chain drags
		for (ServerLevel level : event.getServer().getAllLevels()) {
			for (ServerPlayer player : level.players()) {
				processPlayerDrag(player);
			}
		}
	}
	
	private static void processPlayerDrag(ServerPlayer player) {
		if (!ChainDragManager.isDragging(player)) {
			return;
		}
		
		ItemStack mainHandItem = player.getMainHandItem();
		if (mainHandItem.getItem() != IronbloodModItems.HEAVY_DUTY_CHAIN.get()) {
			ChainDragManager.stopDragging(player);
			player.displayClientMessage(Component.literal("§cChain released!"), true);
			return;
		}
		
		ChainDragManager.ChainDragData dragData = ChainDragManager.getDragData(player);
		if (dragData == null) {
			return;
		}
		
		LoadedServerShip ship = dragData.getShip();
		BlockPos attachmentPoint = dragData.getAttachmentPoint();
		
		if (ship == null) {
			ChainDragManager.stopDragging(player);
			return;
		}
		
		// Ensure ship can move
		if (ship.isStatic()) {
			ship.setStatic(false);
		}
		
		// Get player position
		Vec3 playerPos = player.position().add(0, player.getEyeHeight(), 0);
		
		// Get ship's center of mass position
		Vector3d shipCenterWorld = new Vector3d(ship.getTransform().getPositionInWorld());
		
		// Calculate distance from player to ship center
		double distanceToShip = Math.sqrt(
			Math.pow(playerPos.x - shipCenterWorld.x, 2) +
			Math.pow(playerPos.y - shipCenterWorld.y, 2) +
			Math.pow(playerPos.z - shipCenterWorld.z, 2)
		);
		
		// Break chain if player is too far away (>10 blocks)
		if (distanceToShip > 10.0) {
			ChainDragManager.stopDragging(player);
			player.displayClientMessage(Component.literal("§cChain broke! Too far from ship!"), true);
			return;
		}
		
		// Calculate target position in front of player
		Vec3 lookVec = player.getLookAngle();
		Vec3 targetPos = playerPos.add(lookVec.scale(5.0));
		
		// Calculate direction from ship center to target
		Vector3d direction = new Vector3d(
			targetPos.x - shipCenterWorld.x,
			targetPos.y - shipCenterWorld.y,
			targetPos.z - shipCenterWorld.z
		);
		
		double distance = direction.length();
		
		if (distance > 0.5) {
			direction.normalize();
			
			// Get ship mass and velocity for damping
			double mass = ship.getInertiaData().getMass();
			if (mass < 1.0) mass = 100.0;
			
			// Get ship velocity to add damping (reduces oscillation)
			Vector3d shipVelocity = ship.getVelocity();
			double velocityMagnitude = shipVelocity.length();
			
			// Base force scales with distance and mass
			double baseForceMagnitude = Math.min(
				distance * DRAG_FORCE_MULTIPLIER,
				MAX_FORCE_RATIO * mass
			);
			
			// Add velocity damping to prevent oscillation
			// Reduce force if ship is already moving fast toward target
			Vector3d velocityDirection = new Vector3d(shipVelocity).normalize();
			double dotProduct = direction.dot(velocityDirection);
			double dampingFactor = Math.max(0.1, 1.0 - (velocityMagnitude * 0.1 * Math.max(0, dotProduct)));
			
			double forceMagnitude = baseForceMagnitude * dampingFactor;
			
			direction.mul(forceMagnitude);
			
			// Debug every second
			if (tickCounter % 20 == 0) {
				player.displayClientMessage(Component.literal(
					String.format("§eDrag: dist=%.1fm force=%.0fN mass=%.1f vel=%.1f chain=%.1fm", 
						distance, forceMagnitude, mass, velocityMagnitude, distanceToShip)
				), true);
			}
			
			// Use POSITION mode to apply force at attachment point
			ForceInducedShips forceShip = ForceInducedShips.getOrCreate(ship);
			if (forceShip != null) {
				forceShip.addForce(
					attachmentPoint,
					direction,
					1.0,
					ForceMode.POSITION,
					ForceDirectionMode.WORLD
				);
			}
		} else if (tickCounter % 20 == 0) {
			player.displayClientMessage(Component.literal(String.format("§aChain: Ship at target! (%.1fm)", distanceToShip)), true);
		}
	}
}