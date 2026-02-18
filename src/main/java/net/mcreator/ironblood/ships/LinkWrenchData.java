package net.mcreator.ironblood.ships;

import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

import org.valkyrienskies.core.api.ships.ServerShip;

import javax.annotation.Nullable;

/**
 * Stores the link wrench selection state for a single player.
 * firstShip is nullable — the first block may be in the world (not on any ship).
 */
public class LinkWrenchData {
    @Nullable
    private ServerShip firstShip;
    @Nullable
    private BlockPos firstBlockPos;
    @Nullable
    private Direction firstBlockFacing;
    @Nullable
    private AttachFace firstFace; // For vertical rotator/hinge
    @Nullable
    private String firstBlockType;

    public LinkWrenchData() {
        this.reset();
    }

    public void setFirstLink(@Nullable ServerShip ship, BlockPos pos, @Nullable Direction facing, String blockType) {
        this.firstShip = ship;
        this.firstBlockPos = pos;
        this.firstBlockFacing = facing;
        this.firstBlockType = blockType;
    }

    public void setFirstFace(@Nullable AttachFace face) {
        this.firstFace = face;
    }

    /** True once the player has selected their first block. Ship may still be null. */
    public boolean hasFirstLink() {
        return this.firstBlockPos != null;
    }

    public void reset() {
        this.firstShip = null;
        this.firstBlockPos = null;
        this.firstBlockFacing = null;
        this.firstFace = null;
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
    public AttachFace getFirstFace() {
        return this.firstFace;
    }

    @Nullable
    public String getFirstBlockType() {
        return this.firstBlockType;
    }
}