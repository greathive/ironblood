package net.mcreator.ironblood.block.entity;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;

import net.mcreator.ironblood.init.IronbloodModBlockEntities;

import javax.annotation.Nullable;

/**
 * Block entity for SwivelBearingTableBlock. Stores a reference to the partner
 * SwivelBearingBlockEntity's position so that when this block is broken it can
 * delegate joint removal to the main block entity, which owns the joint.
 * This is the "child" block in the swivel bearing joint pair.
 */
public class SwivelBearingTableBlockEntity extends BlockEntity {
    @Nullable
    private BlockPos linkedBlockPos = null;

    public SwivelBearingTableBlockEntity(BlockPos pos, BlockState state) {
        super(IronbloodModBlockEntities.SWIVEL_BEARING_TABLE.get(), pos, state);
    }

    public SwivelBearingTableBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (linkedBlockPos != null) {
            tag.putInt("linkedX", linkedBlockPos.getX());
            tag.putInt("linkedY", linkedBlockPos.getY());
            tag.putInt("linkedZ", linkedBlockPos.getZ());
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("linkedX")) {
            linkedBlockPos = new BlockPos(
                tag.getInt("linkedX"),
                tag.getInt("linkedY"),
                tag.getInt("linkedZ")
            );
        }
    }

    public boolean hasJoint() {
        return linkedBlockPos != null;
    }

    @Nullable
    public BlockPos getLinkedBlockPos() {
        return linkedBlockPos;
    }

    public void setLinkedBlockPos(@Nullable BlockPos pos) {
        this.linkedBlockPos = pos;
        setChanged();
    }

    /**
     * Called by SwivelBearingBlockEntity.removeJoint() to wipe our side cleanly.
     */
    public void clearJointData() {
        linkedBlockPos = null;
        setChanged();
    }

    /**
     * Called when this block is broken. Delegates the actual joint removal
     * (physics + clearing both sides) to the partner SwivelBearingBlockEntity.
     */
    public void removeJoint() {
        if (linkedBlockPos == null || level == null) return;

        var otherBE = level.getBlockEntity(linkedBlockPos);
        if (otherBE instanceof SwivelBearingBlockEntity owner) {
            owner.removeJoint(); // owner handles physics removal and clears both sides
        } else {
            // Partner is already gone – just clear ourselves
            linkedBlockPos = null;
            setChanged();
        }
    }
}