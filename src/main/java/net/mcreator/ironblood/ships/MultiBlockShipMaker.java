package net.mcreator.ironblood.ships;

import org.valkyrienskies.mod.util.RelocationUtilKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.internal.ships.VsiServerShip;
import org.valkyrienskies.core.api.ships.Ship;
import org.valkyrienskies.core.api.ships.ServerShip;

import org.joml.Vector3i;
import org.joml.Vector3d;

import net.minecraft.world.level.block.Rotation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.List;

public class MultiBlockShipMaker {
	
	/**
	 * Assembles multiple block regions into a single ship based on the selection string format
	 * @param level The server level
	 * @param selectionText The selection string in format "{[x,y,z][x,y,z]}{[x,y,z][x,y,z]}..."
	 * @param scale The scale of the ship
	 * @return The created ship, or null if failed
	 */
	public static Ship assembleShipFromSelection(ServerLevel level, String selectionText, double scale) {
		if (selectionText == null || selectionText.isEmpty()) {
			return null;
		}
		
		// Parse the selection string to get all block position pairs
		List<BlockRegion> regions = parseSelectionText(selectionText);
		
		if (regions.isEmpty()) {
			return null;
		}
		
		// Collect all block positions from all regions
		List<BlockPos> allBlocks = new ArrayList<>();
		for (BlockRegion region : regions) {
			allBlocks.addAll(getBlocksInRegion(region));
		}
		
		if (allBlocks.isEmpty()) {
			return null;
		}
		
		// Calculate the center position of all blocks
		int sumX = 0, sumY = 0, sumZ = 0;
		for (BlockPos pos : allBlocks) {
			sumX += pos.getX();
			sumY += pos.getY();
			sumZ += pos.getZ();
		}
		BlockPos centerPos = new BlockPos(
			sumX / allBlocks.size(),
			sumY / allBlocks.size(),
			sumZ / allBlocks.size()
		);
		
		// Check if this position is on a parent ship
		ServerShip parentShip = VSGameUtilsKt.getShipManagingPos(level, centerPos);
		
		// Create the new ship at the center position
		ServerShip serverShip = VSGameUtilsKt.getShipObjectWorld(level).createNewShipAtBlock(
			VectorConversionsMCKt.toJOML(centerPos), 
			false, 
			scale, 
			VSGameUtilsKt.getDimensionId(level)
		);
		
		// Get the ship's center position in ship space
		Vector3i shipCenterVec = new Vector3i();
		serverShip.getChunkClaim().getCenterBlockCoordinates(VSGameUtilsKt.getYRange(level), shipCenterVec);
		BlockPos shipCenter = VectorConversionsMCKt.toBlockPos(shipCenterVec);
		
		// Relocate all blocks to the ship, maintaining their relative positions
		for (BlockPos blockPos : allBlocks) {
			// Skip air blocks
			if (level.getBlockState(blockPos).isAir()) {
				continue;
			}
			
			// Calculate relative position from center
			int relX = blockPos.getX() - centerPos.getX();
			int relY = blockPos.getY() - centerPos.getY();
			int relZ = blockPos.getZ() - centerPos.getZ();
			
			// Calculate the target position on the ship (maintain relative position from ship center)
			BlockPos targetPos = new BlockPos(
				shipCenter.getX() + relX,
				shipCenter.getY() + relY,
				shipCenter.getZ() + relZ
			);
			
			// Relocate the block
			RelocationUtilKt.relocateBlock(level, blockPos, targetPos, true, serverShip, Rotation.NONE);
		}
		
		// If the blocks were on a parent ship, set up the transform
		if (parentShip != null) {
			var newShipPosInWorld = parentShip.getShipToWorld().transformPosition(
				VectorConversionsMCKt.toJOMLD(centerPos).add(0.5, 0.5, 0.5)
			);
			var newShipPosInShipyard = VectorConversionsMCKt.toJOMLD(centerPos).add(0.5, 0.5, 0.5);
			var newShipRotation = parentShip.getTransform().getShipToWorldRotation();
			var newTransform = ValkyrienSkiesMod.getVsCore().newBodyTransform(
				newShipPosInWorld, 
				newShipRotation, 
				new Vector3d(scale), 
				newShipPosInShipyard
			);
			((VsiServerShip) serverShip).unsafeSetTransform(newTransform);
		}
		
		return serverShip;
	}
	
	/**
	 * Parses the selection text into a list of block regions
	 */
	private static List<BlockRegion> parseSelectionText(String selectionText) {
		List<BlockRegion> regions = new ArrayList<>();
		
		// Split by }{ to separate each pair
		String[] pairStrings = selectionText.split("\\}\\{");
		
		for (String pairStr : pairStrings) {
			// Remove leading/trailing { and }
			pairStr = pairStr.replace("{", "").replace("}", "");
			
			// Now we should have something like "[x,y,z][x,y,z]"
			// Split into two position strings
			int firstClose = pairStr.indexOf("]");
			if (firstClose == -1) {
				continue;
			}
			
			String pos1Str = pairStr.substring(0, firstClose + 1);
			String pos2Str = pairStr.substring(firstClose + 1);
			
			BlockPos pos1 = parseBlockPos(pos1Str);
			BlockPos pos2 = parseBlockPos(pos2Str);
			
			if (pos1 != null && pos2 != null) {
				regions.add(new BlockRegion(pos1, pos2));
			}
		}
		
		return regions;
	}
	
	/**
	 * Parses a position string like "[x,y,z]" into a BlockPos
	 */
	private static BlockPos parseBlockPos(String posStr) {
		try {
			// Remove brackets and split by comma
			posStr = posStr.replace("[", "").replace("]", "");
			String[] parts = posStr.split(",");
			
			if (parts.length != 3) {
				return null;
			}
			
			int x = (int) Double.parseDouble(parts[0].trim());
			int y = (int) Double.parseDouble(parts[1].trim());
			int z = (int) Double.parseDouble(parts[2].trim());
			
			return new BlockPos(x, y, z);
		} catch (NumberFormatException e) {
			return null;
		}
	}
	
	/**
	 * Gets all block positions within a region (inclusive)
	 */
	private static List<BlockPos> getBlocksInRegion(BlockRegion region) {
		List<BlockPos> blocks = new ArrayList<>();
		
		int minX = Math.min(region.pos1.getX(), region.pos2.getX());
		int minY = Math.min(region.pos1.getY(), region.pos2.getY());
		int minZ = Math.min(region.pos1.getZ(), region.pos2.getZ());
		int maxX = Math.max(region.pos1.getX(), region.pos2.getX());
		int maxY = Math.max(region.pos1.getY(), region.pos2.getY());
		int maxZ = Math.max(region.pos1.getZ(), region.pos2.getZ());
		
		for (int x = minX; x <= maxX; x++) {
			for (int y = minY; y <= maxY; y++) {
				for (int z = minZ; z <= maxZ; z++) {
					blocks.add(new BlockPos(x, y, z));
				}
			}
		}
		
		return blocks;
	}
	
	/**
	 * Validates if the selection is within size limits (16x16x16)
	 * @param selectionText The selection string
	 * @return true if valid, false if too large
	 */
	public static boolean validateSelectionSize(String selectionText) {
		if (selectionText == null || selectionText.isEmpty()) {
			return false;
		}
		
		List<BlockRegion> regions = parseSelectionText(selectionText);
		if (regions.isEmpty()) {
			return false;
		}
		
		// Calculate the overall bounding box
		int globalMinX = Integer.MAX_VALUE;
		int globalMinY = Integer.MAX_VALUE;
		int globalMinZ = Integer.MAX_VALUE;
		int globalMaxX = Integer.MIN_VALUE;
		int globalMaxY = Integer.MIN_VALUE;
		int globalMaxZ = Integer.MIN_VALUE;
		
		for (BlockRegion region : regions) {
			int minX = Math.min(region.pos1.getX(), region.pos2.getX());
			int minY = Math.min(region.pos1.getY(), region.pos2.getY());
			int minZ = Math.min(region.pos1.getZ(), region.pos2.getZ());
			int maxX = Math.max(region.pos1.getX(), region.pos2.getX());
			int maxY = Math.max(region.pos1.getY(), region.pos2.getY());
			int maxZ = Math.max(region.pos1.getZ(), region.pos2.getZ());
			
			globalMinX = Math.min(globalMinX, minX);
			globalMinY = Math.min(globalMinY, minY);
			globalMinZ = Math.min(globalMinZ, minZ);
			globalMaxX = Math.max(globalMaxX, maxX);
			globalMaxY = Math.max(globalMaxY, maxY);
			globalMaxZ = Math.max(globalMaxZ, maxZ);
		}
		
		// Calculate total dimensions
		int width = globalMaxX - globalMinX + 1;
		int height = globalMaxY - globalMinY + 1;
		int length = globalMaxZ - globalMinZ + 1;
		
		// Check if any dimension exceeds 16
		return width <= 16 && height <= 16 && length <= 16;
	}
	
	/**
	 * Validates if the selection is within distance from the assembler
	 * @param selectionText The selection string
	 * @param assemblerPos The position of the assembler block
	 * @param maxDistance Maximum allowed distance (16 blocks)
	 * @return true if valid, false if too far
	 */
	public static boolean validateSelectionDistance(String selectionText, BlockPos assemblerPos, int maxDistance) {
		if (selectionText == null || selectionText.isEmpty()) {
			return false;
		}
		
		List<BlockRegion> regions = parseSelectionText(selectionText);
		if (regions.isEmpty()) {
			return false;
		}
		
		// Check if any block in any region is too far from the assembler
		for (BlockRegion region : regions) {
			int minX = Math.min(region.pos1.getX(), region.pos2.getX());
			int minY = Math.min(region.pos1.getY(), region.pos2.getY());
			int minZ = Math.min(region.pos1.getZ(), region.pos2.getZ());
			int maxX = Math.max(region.pos1.getX(), region.pos2.getX());
			int maxY = Math.max(region.pos1.getY(), region.pos2.getY());
			int maxZ = Math.max(region.pos1.getZ(), region.pos2.getZ());
			
			// Check the closest corner of this region to the assembler
			int closestX = Math.max(minX, Math.min(assemblerPos.getX(), maxX));
			int closestY = Math.max(minY, Math.min(assemblerPos.getY(), maxY));
			int closestZ = Math.max(minZ, Math.min(assemblerPos.getZ(), maxZ));
			
			double distance = Math.sqrt(
				Math.pow(closestX - assemblerPos.getX(), 2) +
				Math.pow(closestY - assemblerPos.getY(), 2) +
				Math.pow(closestZ - assemblerPos.getZ(), 2)
			);
			
			if (distance > maxDistance) {
				return false;
			}
		}
		
		return true;
	}
	
	/**
	 * Helper class to represent a rectangular region between two block positions
	 */
	private static class BlockRegion {
		final BlockPos pos1;
		final BlockPos pos2;
		
		BlockRegion(BlockPos pos1, BlockPos pos2) {
			this.pos1 = pos1;
			this.pos2 = pos2;
		}
	}
}
