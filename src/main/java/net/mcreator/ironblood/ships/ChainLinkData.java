package net.mcreator.ironblood.ships;

import org.valkyrienskies.core.api.ships.ServerShip;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

/**
 * Stores information about a chain link created by the Heavy Duty Chain item.
 * Each link connects two points - either ship-to-world or ship-to-ship.
 */
public class ChainLinkData {
    @Nullable
    private final ServerShip firstShip;
    private final BlockPos firstBlockPos;
    
    @Nullable
    private final ServerShip secondShip;
    private final BlockPos secondBlockPos;
    
    private int jointId = -1;
    private double currentMaxDistance;
    private final double initialDistance;
    
    public ChainLinkData(@Nullable ServerShip firstShip, BlockPos firstBlockPos,
                         @Nullable ServerShip secondShip, BlockPos secondBlockPos,
                         double initialDistance) {
        this.firstShip = firstShip;
        this.firstBlockPos = firstBlockPos;
        this.secondShip = secondShip;
        this.secondBlockPos = secondBlockPos;
        this.initialDistance = initialDistance;
        this.currentMaxDistance = initialDistance;
    }
    
    public void setJointId(int id) {
        this.jointId = id;
    }
    
    public int getJointId() {
        return jointId;
    }
    
    public boolean hasJoint() {
        return jointId != -1;
    }
    
    @Nullable
    public ServerShip getFirstShip() {
        return firstShip;
    }
    
    public BlockPos getFirstBlockPos() {
        return firstBlockPos;
    }
    
    @Nullable
    public ServerShip getSecondShip() {
        return secondShip;
    }
    
    public BlockPos getSecondBlockPos() {
        return secondBlockPos;
    }
    
    public double getCurrentMaxDistance() {
        return currentMaxDistance;
    }
    
    public double getInitialDistance() {
        return initialDistance;
    }
    
    public void decreaseDistance(double amount) {
        currentMaxDistance = Math.max(1.0, currentMaxDistance - amount);
    }
    
    public void increaseDistance(double amount) {
        currentMaxDistance = currentMaxDistance + amount;
    }
    
    public void setCurrentMaxDistance(double distance) {
        currentMaxDistance = Math.max(1.0, distance);
    }
    
    /**
     * Checks if a block position matches either end of this chain link
     */
    public boolean hasEndpoint(BlockPos pos) {
        return firstBlockPos.equals(pos) || secondBlockPos.equals(pos);
    }
}
