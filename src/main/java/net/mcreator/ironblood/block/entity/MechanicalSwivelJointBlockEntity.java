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
 * Block entity for MechanicalSwivelJointBlock. Stores the joint ID and partner position.
 * Since swivel joints link with other swivel joints (no alt type), both blocks in a pair
 * will have their own MechanicalSwivelJointBlockEntity that stores the same jointId.
 */
public class MechanicalSwivelJointBlockEntity extends BlockEntity {
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
    
    // Joint type and limits
    private String jointType = "revolute";
    private double lowerLimit = 0.0;
    private double upperLimit = Math.toRadians(90.0);

    public MechanicalSwivelJointBlockEntity(BlockPos pos, BlockState state) {
        super(IronbloodModBlockEntities.MECHANICAL_SWIVEL_JOINT.get(), pos, state);
    }

    public MechanicalSwivelJointBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
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
        if (shipIdA != null) {
            tag.putLong("shipIdA", shipIdA);
        }
        if (shipIdB != null) {
            tag.putLong("shipIdB", shipIdB);
        }
        
        // Save joint type and limits
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
            positionA = new Vec3(
                tag.getDouble("posAX"),
                tag.getDouble("posAY"),
                tag.getDouble("posAZ")
            );
        }
        if (tag.contains("posBX")) {
            positionB = new Vec3(
                tag.getDouble("posBX"),
                tag.getDouble("posBY"),
                tag.getDouble("posBZ")
            );
        }
        if (tag.contains("rotAX")) {
            rotationA = new Vec3(
                tag.getDouble("rotAX"),
                tag.getDouble("rotAY"),
                tag.getDouble("rotAZ")
            );
        }
        if (tag.contains("rotBX")) {
            rotationB = new Vec3(
                tag.getDouble("rotBX"),
                tag.getDouble("rotBY"),
                tag.getDouble("rotBZ")
            );
        }
        if (tag.contains("shipIdA")) {
            shipIdA = tag.getLong("shipIdA");
        }
        if (tag.contains("shipIdB")) {
            shipIdB = tag.getLong("shipIdB");
        }
        
        // Load joint type and limits
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
     * Recreates the VS joint from saved data when the chunk is loaded.
     * Only one of the two swivel blocks should recreate the joint to avoid duplicates.
     * We use position comparison to ensure deterministic behavior.
     */
    private void recreateJoint() {
        if (positionA == null || positionB == null || rotationA == null || rotationB == null || level == null) {
            return;
        }
        
        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }
        
        // CRITICAL: Only ONE block should recreate - use position comparison to decide
        // This ensures deterministic behavior regardless of load order
        if (linkedBlockPos != null) {
            BlockPos myPos = this.getBlockPos();
            
            // If our position is "greater", let the partner handle recreation
            if (myPos.getX() > linkedBlockPos.getX() ||
                (myPos.getX() == linkedBlockPos.getX() && myPos.getY() > linkedBlockPos.getY()) ||
                (myPos.getX() == linkedBlockPos.getX() && myPos.getY() == linkedBlockPos.getY() && myPos.getZ() > linkedBlockPos.getZ())) {
                
                // Check if partner has already recreated the joint
                var partnerBE = serverLevel.getBlockEntity(linkedBlockPos);
                if (partnerBE instanceof MechanicalSwivelJointBlockEntity partner) {
                    int partnerJointId = partner.getJointId();
                    if (partnerJointId != -1) {
                        // Partner already recreated - just copy the ID using setter
                        setJointId(partnerJointId);
                        // Ensure partner knows our position
                        if (partner.getLinkedBlockPos() == null) {
                            partner.setLinkedBlockPos(myPos);
                        }
                    }
                    // Otherwise partner hasn't loaded yet - it will recreate when it loads
                }
                return; // Either way, we don't recreate
            }
        }
        
        // We are the one who should recreate the joint
        // Get the ships by ID if they exist
        ServerShip shipA = null;
        ServerShip shipB = null;
        
        if (shipIdA != null) {
            var loadedShip = VSGameUtilsKt.getShipObjectWorld(serverLevel).getLoadedShips().getById(shipIdA);
            if (loadedShip != null) {
                shipA = loadedShip;
            } else {
                return; // Ship doesn't exist anymore
            }
        }
        
        if (shipIdB != null) {
            var loadedShip = VSGameUtilsKt.getShipObjectWorld(serverLevel).getLoadedShips().getById(shipIdB);
            if (loadedShip != null) {
                shipB = loadedShip;
            } else {
                return; // Ship doesn't exist anymore
            }
        }
        
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
            
            // CRITICAL: Also update partner's jointId AND ensure they have our position
            if (linkedBlockPos != null) {
                var partnerBE = serverLevel.getBlockEntity(linkedBlockPos);
                if (partnerBE instanceof MechanicalSwivelJointBlockEntity partner) {
                    partner.setJointId(newId);
                    // Ensure partner knows our position too
                    if (partner.getLinkedBlockPos() == null) {
                        partner.setLinkedBlockPos(this.getBlockPos());
                    }
                }
            }
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

    /**
     * Called by the partner to wipe this side cleanly (no physics removal).
     */
    public void clearJointData() {
        jointId = -1;
        linkedBlockPos = null;
        // Also clear persistence data
        positionA = null;
        positionB = null;
        rotationA = null;
        rotationB = null;
        shipIdA = null;
        shipIdB = null;
        needsRecreation = false;
        setChanged();
    }

    /**
     * Removes the joint from VS physics and clears data on both sides.
     * The partner is always another MechanicalSwivelJointBlockEntity.
     */
    public void removeJoint() {
        if (jointId == -1 || level == null) return;

        // Remove from physics
        JointUtil.removeJointById(level, jointId);

        // Clear the partner
        if (linkedBlockPos != null) {
            var otherBE = level.getBlockEntity(linkedBlockPos);
            if (otherBE instanceof MechanicalSwivelJointBlockEntity partner) {
                partner.clearJointData();
            }
        }

        // Clear ourselves including all persistence data
        jointId = -1;
        linkedBlockPos = null;
        positionA = null;
        positionB = null;
        rotationA = null;
        rotationB = null;
        shipIdA = null;
        shipIdB = null;
        needsRecreation = false;
        setChanged();
    }
}