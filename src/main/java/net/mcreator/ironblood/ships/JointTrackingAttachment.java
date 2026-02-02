package net.mcreator.ironblood.ships;

import org.valkyrienskies.core.api.ships.LoadedServerShip;

import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonAutoDetect;

/**
 * Tracks joints associated with specific block positions on a ship
 * This allows us to remove joints when blocks are broken
 */
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
public class JointTrackingAttachment {
	@JsonProperty
	private final Map<String, Integer> blockPosToJointId = new HashMap<>();
	
	@JsonProperty
	private final Map<Integer, Set<String>> jointIdToBlockPos = new HashMap<>();
	
	public JointTrackingAttachment() {
	}
	
	/**
	 * Register a joint at a specific block position
	 */
	public void registerJoint(BlockPos pos, int jointId) {
		String posKey = blockPosToString(pos);
		blockPosToJointId.put(posKey, jointId);
		
		jointIdToBlockPos.computeIfAbsent(jointId, k -> new HashSet<>()).add(posKey);
	}
	
	/**
	 * Get the joint ID associated with a block position
	 */
	public Integer getJointIdAtPosition(BlockPos pos) {
		return blockPosToJointId.get(blockPosToString(pos));
	}
	
	/**
	 * Remove a joint registration
	 */
	public void removeJoint(int jointId) {
		Set<String> positions = jointIdToBlockPos.remove(jointId);
		if (positions != null) {
			for (String pos : positions) {
				blockPosToJointId.remove(pos);
			}
		}
	}
	
	/**
	 * Remove a joint by block position
	 */
	public Integer removeJointAtPosition(BlockPos pos) {
		String posKey = blockPosToString(pos);
		Integer jointId = blockPosToJointId.remove(posKey);
		
		if (jointId != null) {
			Set<String> positions = jointIdToBlockPos.get(jointId);
			if (positions != null) {
				positions.remove(posKey);
				if (positions.isEmpty()) {
					jointIdToBlockPos.remove(jointId);
				}
			}
		}
		
		return jointId;
	}
	
	/**
	 * Check if a block position has a joint
	 */
	public boolean hasJointAtPosition(BlockPos pos) {
		return blockPosToJointId.containsKey(blockPosToString(pos));
	}
	
	/**
	 * Get all registered joints
	 */
	public Set<Integer> getAllJointIds() {
		return new HashSet<>(jointIdToBlockPos.keySet());
	}
	
	private String blockPosToString(BlockPos pos) {
		return pos.getX() + "," + pos.getY() + "," + pos.getZ();
	}
	
	public static JointTrackingAttachment getOrCreate(LoadedServerShip ship) {
		JointTrackingAttachment attachment = ship.getAttachment(JointTrackingAttachment.class);
		if (attachment == null) {
			attachment = new JointTrackingAttachment();
			ship.setAttachment(JointTrackingAttachment.class, attachment);
		}
		return attachment;
	}
}