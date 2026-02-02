package net.mcreator.ironblood.ships;

import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import org.valkyrienskies.core.api.ships.ServerShip;

import javax.annotation.Nullable;

/**
 * Stores the link wrench data for each player
 */
public class LinkWrenchData {
    @Nullable
    private ServerShip firstShip;
    @Nullable
    private BlockPos firstBlockPos;
    @Nullable
    private Direction firstBlockFacing;
    @Nullable
    private String firstBlockType; // "mechanical_joint" or "mechanical_joint_alt"
    
    public LinkWrenchData() {
        this.reset();
    }
    
    public void setFirstLink(ServerShip ship, BlockPos pos, Direction facing, String blockType) {
        this.firstShip = ship;
        this.firstBlockPos = pos;
        this.firstBlockFacing = facing;
        this.firstBlockType = blockType;
    }
    
    public boolean hasFirstLink() {
        return this.firstShip != null && this.firstBlockPos != null;
    }
    
    public void reset() {
        this.firstShip = null;
        this.firstBlockPos = null;
        this.firstBlockFacing = null;
        this.firstBlockType = null;
    }
    
    @Nullable
    public ServerShip getFirstShip() {
        return this.firstShip;
    }
    
    @Nullable
    public BlockPos getFirstBlockPos() {
        return this.firstBlockPos;
    }
    
    @Nullable
    public Direction getFirstBlockFacing() {
        return this.firstBlockFacing;
    }
    
    @Nullable
    public String getFirstBlockType() {
        return this.firstBlockType;
    }
}
