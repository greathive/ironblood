package net.mcreator.ironblood.ships;

import org.valkyrienskies.core.api.ships.LoadedServerShip;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import javax.annotation.Nullable;

/**
 * Manages chain dragging state for all players
 */
public class ChainDragManager {
    private static final Map<UUID, ChainDragData> activeDrags = new HashMap<>();
    
    /**
     * Starts dragging a ship for a player
     */
    public static void startDragging(Player player, LoadedServerShip ship, BlockPos attachmentPoint) {
        UUID playerId = player.getUUID();
        activeDrags.put(playerId, new ChainDragData(ship, attachmentPoint));
    }
    
    /**
     * Stops dragging for a player
     */
    public static void stopDragging(Player player) {
        activeDrags.remove(player.getUUID());
    }
    
    /**
     * Checks if a player is currently dragging a ship
     */
    public static boolean isDragging(Player player) {
        return activeDrags.containsKey(player.getUUID());
    }
    
    /**
     * Gets the drag data for a player, or null if not dragging
     */
    @Nullable
    public static ChainDragData getDragData(Player player) {
        return activeDrags.get(player.getUUID());
    }
    
    /**
     * Clears all drag data (useful for cleanup)
     */
    public static void clearAll() {
        activeDrags.clear();
    }
    
    /**
     * Data class to store information about an active chain drag
     */
    public static class ChainDragData {
        private final LoadedServerShip ship;
        private final BlockPos attachmentPoint;
        
        public ChainDragData(LoadedServerShip ship, BlockPos attachmentPoint) {
            this.ship = ship;
            this.attachmentPoint = attachmentPoint;
        }
        
        public LoadedServerShip getShip() {
            return ship;
        }
        
        public BlockPos getAttachmentPoint() {
            return attachmentPoint;
        }
    }
}
