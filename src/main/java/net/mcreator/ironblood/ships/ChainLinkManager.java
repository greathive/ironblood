package net.mcreator.ironblood.ships;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.*;

/**
 * Manages all active chain links in the world
 */
public class ChainLinkManager {
    private static final Map<UUID, ChainLinkData> activeLinks = new HashMap<>();
    
    /**
     * Creates a new chain link and returns its UUID
     */
    public static UUID createLink(ChainLinkData linkData) {
        UUID linkId = UUID.randomUUID();
        activeLinks.put(linkId, linkData);
        return linkId;
    }
    
    /**
     * Registers an existing chain link with a specific UUID (used for loading from NBT)
     */
    public static void registerLink(UUID linkId, ChainLinkData linkData) {
        activeLinks.put(linkId, linkData);
    }
    
    /**
     * Gets a chain link by its UUID
     */
    @Nullable
    public static ChainLinkData getLink(UUID linkId) {
        return activeLinks.get(linkId);
    }
    
    /**
     * Removes a chain link
     */
    public static void removeLink(UUID linkId) {
        activeLinks.remove(linkId);
    }
    
    /**
     * Finds a chain link that has an endpoint at the given position
     */
    @Nullable
    public static Map.Entry<UUID, ChainLinkData> findLinkAtPosition(BlockPos pos) {
        for (Map.Entry<UUID, ChainLinkData> entry : activeLinks.entrySet()) {
            if (entry.getValue().hasEndpoint(pos)) {
                return entry;
            }
        }
        return null;
    }
    
    /**
     * Gets all active chain links
     */
    public static Collection<ChainLinkData> getAllLinks() {
        return activeLinks.values();
    }
    
    /**
     * Clears all chain links (for cleanup)
     */
    public static void clearAll() {
        activeLinks.clear();
    }
}
