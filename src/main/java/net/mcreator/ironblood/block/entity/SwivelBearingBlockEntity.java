package net.mcreator.ironblood.block.entity;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import net.mcreator.ironblood.init.IronbloodModBlockEntities;
import net.mcreator.ironblood.ships.JointUtil;

import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import javax.annotation.Nullable;

/**
 * Block entity for SwivelBearingBlock. Owns the joint – stores the joint ID,
 * the partner's position, and is responsible for removing the joint from physics
 * when either side breaks. This is the "parent" block that creates horizontal
 * rotation joints (Y-axis) with SwivelBearingTableBlock.
 */
public class SwivelBearingBlockEntity extends BlockEntity {
    private int jointId = -1;
    @Nullable
    private BlockPos linkedBlockPos = null;
    private boolean needsRecreation = false;
    
    // Joint creation data for recreation
    @Nullable
    private Vec3 positionA = null;
    @Nullable
    private Vec3 positionB = null;
    @Nullable
    private Vec3 rotationA = null;
    @Nullable
    private Vec3 rotationB = null;
    @Nullable
    private Long shipIdA = null;
    @Nullable
    private Long shipIdB = null;
    
    // Joint type and limits - for swivel bearings, we use revolute with Y-axis rotation
    private String jointType = "revolute";
    private double lowerLimit = -Math.PI; // -180 degrees
    private double upperLimit = Math.PI;  // +180 degrees (full rotation within valid range)

    public SwivelBearingBlockEntity(BlockPos pos, BlockState state) {
        super(IronbloodModBlockEntities.SWIVEL_BEARING.get(), pos, state);
    }

    public SwivelBearingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        tag.putInt("jointId", jointId);
        if (linkedBlockPos != null) {
            tag.putInt("linkedX", linkedBlockPos.getX());
            tag.putInt("linkedY", linkedBlockPos.getY());
            tag.putInt("linkedZ", linkedBlockPos.getZ());
        }
        
        // Save joint creation data for recreation
        if (positionA != null) {
            tag.putDouble("posAX", positionA.x);
            tag.putDouble("posAY", positionA.y);
            tag.putDouble("posAZ", positionA.z);
        }
        if (positionB != null) {
            tag.putDouble("posBX", positionB.x);
            tag.putDouble("posBY", positionB.y);
            tag.putDouble("posBZ", positionB.z);
        }
        if (rotationA != null) {
            tag.putDouble("rotAX", rotationA.x);
            tag.putDouble("rotAY", rotationA.y);
            tag.putDouble("rotAZ", rotationA.z);
        }
        if (rotationB != null) {
            tag.putDouble("rotBX", rotationB.x);
            tag.putDouble("rotBY", rotationB.y);
            tag.putDouble("rotBZ", rotationB.z);
        }
        // CRITICAL: Always save whether we have world connections (null ship IDs)
        // Save -1 to indicate "world connection" vs missing data
        tag.putLong("shipIdA", shipIdA != null ? shipIdA : -1L);
        tag.putLong("shipIdB", shipIdB != null ? shipIdB : -1L);
        
        tag.putString("jointType", jointType);
        tag.putDouble("lowerLimit", lowerLimit);
        tag.putDouble("upperLimit", upperLimit);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        jointId = tag.getInt("jointId");
        if (tag.contains("linkedX")) {
            linkedBlockPos = new BlockPos(
                tag.getInt("linkedX"),
                tag.getInt("linkedY"),
                tag.getInt("linkedZ")
            );
        }
        
        // Load joint creation data
        if (tag.contains("posAX")) {
            positionA = new Vec3(tag.getDouble("posAX"), tag.getDouble("posAY"), tag.getDouble("posAZ"));
        }
        if (tag.contains("posBX")) {
            positionB = new Vec3(tag.getDouble("posBX"), tag.getDouble("posBY"), tag.getDouble("posBZ"));
        }
        if (tag.contains("rotAX")) {
            rotationA = new Vec3(tag.getDouble("rotAX"), tag.getDouble("rotAY"), tag.getDouble("rotAZ"));
        }
        if (tag.contains("rotBX")) {
            rotationB = new Vec3(tag.getDouble("rotBX"), tag.getDouble("rotBY"), tag.getDouble("rotBZ"));
        }
        // CRITICAL: Load ship IDs, treating -1 as null (world connection)
        if (tag.contains("shipIdA")) {
            long idA = tag.getLong("shipIdA");
            shipIdA = (idA == -1L) ? null : idA;
        }
        if (tag.contains("shipIdB")) {
            long idB = tag.getLong("shipIdB");
            shipIdB = (idB == -1L) ? null : idB;
        }
        if (tag.contains("jointType")) {
            jointType = tag.getString("jointType");
        }
        if (tag.contains("lowerLimit")) {
            lowerLimit = tag.getDouble("lowerLimit");
        }
        if (tag.contains("upperLimit")) {
            upperLimit = tag.getDouble("upperLimit");
        }
        
        // Mark that we need to recreate the joint if we have the required data
        if (jointId != -1 && linkedBlockPos != null && positionA != null && positionB != null && rotationA != null && rotationB != null) {
            needsRecreation = true;
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        
        // Recreate the joint if we loaded it from NBT
        if (needsRecreation && level != null && !level.isClientSide) {
            recreateJoint();
            needsRecreation = false;
        }
    }

    /**
     * Recreates the joint from saved data after a world reload.
     */
    private void recreateJoint() {
        if (positionA == null || positionB == null || rotationA == null || rotationB == null || level == null) {
            return;
        }
        
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        
        // Get the ships by ID if they exist
        // IMPORTANT: null ship ID means the joint connects to the world (not a ship)
        ServerShip shipA = null;
        ServerShip shipB = null;
        
        if (shipIdA != null) {
            var loadedShip = VSGameUtilsKt.getShipObjectWorld(serverLevel).getLoadedShips().getById(shipIdA);
            if (loadedShip != null) {
                shipA = loadedShip;
            } else {
                // Ship doesn't exist anymore, can't recreate the joint
                return;
            }
        }
        // If shipIdA is null, shipA remains null (connects to world)
        
        if (shipIdB != null) {
            var loadedShip = VSGameUtilsKt.getShipObjectWorld(serverLevel).getLoadedShips().getById(shipIdB);
            if (loadedShip != null) {
                shipB = loadedShip;
            } else {
                // Ship doesn't exist anymore, can't recreate the joint
                return;
            }
        }
        // If shipIdB is null, shipB remains null (connects to world)
        
        // Create the joint
        VSJoint joint;
        switch (jointType.toLowerCase()) {
            case "revolute":
                joint = JointUtil.makeRevoluteJoint(shipA, shipB, rotationA, rotationB, positionA, positionB, lowerLimit, upperLimit);
                break;
            case "distance":
                joint = JointUtil.makeDistanceJoint(shipA, shipB, rotationA, rotationB, positionA, positionB, null, null);
                break;
            case "fixed":
            default:
                joint = JointUtil.makeFixedJoint(shipA, shipB, rotationA, rotationB, positionA, positionB);
                break;
        }
        
        // Add the joint and update our jointId
        JointUtil.addJoint(level, joint, newId -> {
            this.jointId = newId;
            setChanged();
        });
    }

    /**
     * Sets the joint creation data. Call this when creating a joint to ensure it can be recreated on reload.
     */
    public void setJointCreationData(Vec3 posA, Vec3 posB, Vec3 rotA, Vec3 rotB, @Nullable ServerShip shipA, @Nullable ServerShip shipB, String type, double lower, double upper) {
        this.positionA = posA;
        this.positionB = posB;
        this.rotationA = rotA;
        this.rotationB = rotB;
        this.shipIdA = shipA != null ? shipA.getId() : null;
        this.shipIdB = shipB != null ? shipB.getId() : null;
        this.jointType = type;
        this.lowerLimit = lower;
        this.upperLimit = upper;
        setChanged();
    }

    public boolean hasJoint() {
        return jointId != -1;
    }

    public int getJointId() {
        return jointId;
    }

    public void setJointId(int id) {
        this.jointId = id;
        setChanged();
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
     * Removes the joint from VS physics and clears data on both sides.
     * The partner is always a SwivelBearingTableBlockEntity.
     */
    public void removeJoint() {
        if (jointId == -1 || level == null) return;

        // Remove from physics
        JointUtil.removeJointById(level, jointId);

        // Clear the table partner
        if (linkedBlockPos != null) {
            var otherBE = level.getBlockEntity(linkedBlockPos);
            if (otherBE instanceof SwivelBearingTableBlockEntity table) {
                table.clearJointData();
            }
        }

        // Clear ourselves
        jointId = -1;
        linkedBlockPos = null;
        setChanged();
    }
}