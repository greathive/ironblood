package net.mcreator.ironblood.ships;

import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages link wrench data for all players
 */
public class LinkWrenchManager {
    private static final Map<UUID, LinkWrenchData> playerData = new HashMap<>();
    
    public static LinkWrenchData getData(Player player) {
        UUID playerId = player.getUUID();
        if (!playerData.containsKey(playerId)) {
            playerData.put(playerId, new LinkWrenchData());
        }
        return playerData.get(playerId);
    }
    
    public static void clearData(Player player) {
        UUID playerId = player.getUUID();
        LinkWrenchData data = playerData.get(playerId);
        if (data != null) {
            data.reset();
        }
    }
    
    public static void resetData(Player player) {
        playerData.remove(player.getUUID());
    }
}
