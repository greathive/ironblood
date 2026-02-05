package net.mcreator.ironblood.block.entity;

import net.minecraftforge.items.wrapper.SidedInvWrapper;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.Capability;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.ContainerHelper;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import net.mcreator.ironblood.init.IronbloodModBlockEntities;
import net.mcreator.ironblood.ships.ChainLinkManager;
import net.mcreator.ironblood.ships.ChainLinkData;
import net.mcreator.ironblood.ships.JointUtil;
import net.mcreator.ironblood.procedures.HeavyDutyChainRightclickedOnBlockProcedure;

import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.mod.common.VSGameUtilsKt;

import java.util.UUID;
import java.util.stream.IntStream;

import javax.annotation.Nullable;

/**
 * Block entity for HeavyDutyChainLinkBlock.
 * Stores the chain link UUID and handles redstone-based distance changes.
 * Saves joint creation data for persistence across world reloads.
 */
public class HeavyDutyChainLinkBlockEntity extends RandomizableContainerBlockEntity implements WorldlyContainer {
    private NonNullList<ItemStack> stacks = NonNullList.<ItemStack>withSize(0, ItemStack.EMPTY);
    private final LazyOptional<? extends IItemHandler>[] handlers = SidedInvWrapper.create(this, Direction.values());

    @Nullable
    private UUID chainLinkId = null;
    private boolean isFirstEndpoint = false;

    // Store partner position directly for client-side rendering
    @Nullable
    private BlockPos partnerBlockPos = null;

    // Redstone state tracking to prevent rapid toggles
    private boolean lastLeftState = false;
    private boolean lastRightState = false;

    // Joint creation data for persistence
    private boolean needsRecreation = false;
    @Nullable
    private Vec3 positionA = null;
    @Nullable
    private Vec3 positionB = null;
    @Nullable
    private Long shipIdA = null;
    @Nullable
    private Long shipIdB = null;
    private double currentMaxDistance = 0;
    private double initialDistance = 0;
    @Nullable
    private BlockPos firstBlockPos = null;
    @Nullable
    private BlockPos secondBlockPos = null;

    public HeavyDutyChainLinkBlockEntity(BlockPos pos, BlockState state) {
        super(IronbloodModBlockEntities.HEAVY_DUTY_CHAIN_LINK.get(), pos, state);
    }

    @Override
    public void load(CompoundTag compound) {
        super.load(compound);
        if (!this.tryLoadLootTable(compound))
            this.stacks = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        ContainerHelper.loadAllItems(compound, this.stacks);

        // Load chain link data
        if (compound.hasUUID("chainLinkId")) {
            chainLinkId = compound.getUUID("chainLinkId");
            isFirstEndpoint = compound.getBoolean("isFirstEndpoint");

            // Load partner position for client-side rendering
            if (compound.contains("partnerX")) {
                partnerBlockPos = new BlockPos(
                        compound.getInt("partnerX"),
                        compound.getInt("partnerY"),
                        compound.getInt("partnerZ")
                );
            }

            // Load joint creation data
            if (compound.contains("posAX")) {
                positionA = new Vec3(
                        compound.getDouble("posAX"),
                        compound.getDouble("posAY"),
                        compound.getDouble("posAZ")
                );
            }
            if (compound.contains("posBX")) {
                positionB = new Vec3(
                        compound.getDouble("posBX"),
                        compound.getDouble("posBY"),
                        compound.getDouble("posBZ")
                );
            }
            if (compound.contains("shipIdA")) {
                shipIdA = compound.getLong("shipIdA");
            }
            if (compound.contains("shipIdB")) {
                shipIdB = compound.getLong("shipIdB");
            }
            if (compound.contains("currentMaxDistance")) {
                currentMaxDistance = compound.getDouble("currentMaxDistance");
            }
            if (compound.contains("initialDistance")) {
                initialDistance = compound.getDouble("initialDistance");
            }

            if (compound.contains("firstX")) {
                firstBlockPos = new BlockPos(
                        compound.getInt("firstX"),
                        compound.getInt("firstY"),
                        compound.getInt("firstZ")
                );
            }
            if (compound.contains("secondX")) {
                secondBlockPos = new BlockPos(
                        compound.getInt("secondX"),
                        compound.getInt("secondY"),
                        compound.getInt("secondZ")
                );
            }

            // Mark that we need to recreate the joint and ChainLinkData
            if (positionA != null && positionB != null && firstBlockPos != null && secondBlockPos != null) {
                needsRecreation = true;
            }
        }
    }

    @Override
    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        if (!this.trySaveLootTable(compound)) {
            ContainerHelper.saveAllItems(compound, this.stacks);
        }

        // Save chain link data
        if (chainLinkId != null) {
            compound.putUUID("chainLinkId", chainLinkId);
            compound.putBoolean("isFirstEndpoint", isFirstEndpoint);

            // Save partner position for client-side rendering
            if (partnerBlockPos != null) {
                compound.putInt("partnerX", partnerBlockPos.getX());
                compound.putInt("partnerY", partnerBlockPos.getY());
                compound.putInt("partnerZ", partnerBlockPos.getZ());
            }

            // Save joint creation data for recreation
            if (positionA != null) {
                compound.putDouble("posAX", positionA.x);
                compound.putDouble("posAY", positionA.y);
                compound.putDouble("posAZ", positionA.z);
            }
            if (positionB != null) {
                compound.putDouble("posBX", positionB.x);
                compound.putDouble("posBY", positionB.y);
                compound.putDouble("posBZ", positionB.z);
            }
            if (shipIdA != null) {
                compound.putLong("shipIdA", shipIdA);
            }
            if (shipIdB != null) {
                compound.putLong("shipIdB", shipIdB);
            }
            compound.putDouble("currentMaxDistance", currentMaxDistance);
            compound.putDouble("initialDistance", initialDistance);

            if (firstBlockPos != null) {
                compound.putInt("firstX", firstBlockPos.getX());
                compound.putInt("firstY", firstBlockPos.getY());
                compound.putInt("firstZ", firstBlockPos.getZ());
            }
            if (secondBlockPos != null) {
                compound.putInt("secondX", secondBlockPos.getX());
                compound.putInt("secondY", secondBlockPos.getY());
                compound.putInt("secondZ", secondBlockPos.getZ());
            }
        }
    }

    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return this.saveWithFullMetadata();
    }

    @Override
    public int getContainerSize() {
        return stacks.size();
    }

    @Override
    public boolean isEmpty() {
        for (ItemStack itemstack : this.stacks)
            if (!itemstack.isEmpty())
                return false;
        return true;
    }

    @Override
    public Component getDefaultName() {
        return Component.literal("heavy_duty_chain_link");
    }

    @Override
    public int getMaxStackSize() {
        return 1;
    }

    @Override
    public AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return ChestMenu.threeRows(id, inventory);
    }

    @Override
    public Component getDisplayName() {
        return Component.literal("Heavy Duty Chain Link");
    }

    @Override
    protected NonNullList<ItemStack> getItems() {
        return this.stacks;
    }

    @Override
    protected void setItems(NonNullList<ItemStack> stacks) {
        this.stacks = stacks;
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int[] getSlotsForFace(Direction side) {
        return IntStream.range(0, this.getContainerSize()).toArray();
    }

    @Override
    public boolean canPlaceItemThroughFace(int index, ItemStack stack, @Nullable Direction direction) {
        return this.canPlaceItem(index, stack);
    }

    @Override
    public boolean canTakeItemThroughFace(int index, ItemStack stack, Direction direction) {
        return true;
    }

    @Override
    public <T> LazyOptional<T> getCapability(Capability<T> capability, @Nullable Direction facing) {
        if (!this.remove && facing != null && capability == ForgeCapabilities.ITEM_HANDLER)
            return handlers[facing.ordinal()].cast();
        return super.getCapability(capability, facing);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        for (LazyOptional<? extends IItemHandler> handler : handlers)
            handler.invalidate();
    }

    @Override
    public void onLoad() {
        super.onLoad();

        // Recreate the chain link and joint if we loaded it from NBT
        if (needsRecreation && level != null && !level.isClientSide) {
            recreateChainLink();
            needsRecreation = false;
        }
    }

    /**
     * Recreates the ChainLinkData and joint from saved data when the chunk is loaded.
     */
    private void recreateChainLink() {
        if (positionA == null || positionB == null || level == null) {
            return;
        }

        if (!(level instanceof ServerLevel serverLevel)) {
            return;
        }

        // Get the ships by ID if they exist
        ServerShip shipA = null;
        ServerShip shipB = null;

        if (shipIdA != null) {
            var loadedShip = VSGameUtilsKt.getShipObjectWorld(serverLevel).getLoadedShips().getById(shipIdA);
            if (loadedShip != null) {
                shipA = loadedShip;
            }
        }

        if (shipIdB != null) {
            var loadedShip = VSGameUtilsKt.getShipObjectWorld(serverLevel).getLoadedShips().getById(shipIdB);
            if (loadedShip != null) {
                shipB = loadedShip;
            }
        }

        // Recreate the ChainLinkData in the manager
        ChainLinkData linkData = new ChainLinkData(shipA, firstBlockPos, shipB, secondBlockPos, initialDistance);
        linkData.setCurrentMaxDistance(currentMaxDistance); // Restore adjusted distance
        ChainLinkManager.registerLink(chainLinkId, linkData);

        // Recreate the joint
        HeavyDutyChainRightclickedOnBlockProcedure.createDistanceJoint(serverLevel, linkData);
    }

    /**
     * Sets the joint creation data when a chain is created.
     */
    public void setJointCreationData(Vec3 posA, Vec3 posB, @Nullable ServerShip shipA, @Nullable ServerShip shipB,
                                     double initial, double current, BlockPos first, BlockPos second) {
        this.positionA = posA;
        this.positionB = posB;
        this.shipIdA = shipA != null ? shipA.getId() : null;
        this.shipIdB = shipB != null ? shipB.getId() : null;
        this.initialDistance = initial;
        this.currentMaxDistance = current;
        this.firstBlockPos = first;
        this.secondBlockPos = second;
        setChanged();
    }

    public boolean hasChainLink() {
        return chainLinkId != null;
    }

    @Nullable
    public UUID getChainLinkId() {
        return chainLinkId;
    }

    public void setChainLink(UUID linkId, boolean isFirst) {
        this.chainLinkId = linkId;
        this.isFirstEndpoint = isFirst;
        setChanged();
    }

    public void setPartnerPosition(BlockPos pos) {
        this.partnerBlockPos = pos;
        setChanged();
    }

    @Nullable
    public BlockPos getPartnerBlockPos() {
        return partnerBlockPos;
    }

    public boolean isFirstEndpoint() {
        return isFirstEndpoint;
    }

    /**
     * Handles redstone signal changes from left and right sides
     */
    public void handleRedstoneChange(boolean leftPowered, boolean rightPowered) {
        if (chainLinkId == null || level == null || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        ChainLinkData linkData = ChainLinkManager.getLink(chainLinkId);
        if (linkData == null) {
            return;
        }

        boolean changed = false;

        // Left powered: extend chain (only on rising edge)
        if (leftPowered && !lastLeftState && !rightPowered) {
            linkData.increaseDistance(0.5);
            changed = true;
        }

        // Right powered: shorten chain (only on rising edge)
        if (rightPowered && !lastRightState && !leftPowered) {
            linkData.decreaseDistance(0.5);
            changed = true;
        }

        // Update last states
        lastLeftState = leftPowered;
        lastRightState = rightPowered;

        // If changed, update the joint and save new distance
        if (changed) {
            currentMaxDistance = linkData.getCurrentMaxDistance();
            updateJoint(serverLevel, linkData);
            setChanged(); // Mark for saving
        }
    }

    /**
     * Updates the joint with new distance
     */
    private void updateJoint(ServerLevel level, ChainLinkData linkData) {
        if (!linkData.hasJoint()) {
            return;
        }

        // Remove old joint
        JointUtil.removeJointById(level, linkData.getJointId());

        // Create new joint with updated distance
        HeavyDutyChainRightclickedOnBlockProcedure.createDistanceJoint(level, linkData);
    }

    /**
     * Called when this block is broken. Removes the joint and clears partner.
     */
    public void removeChain() {
        if (chainLinkId == null || level == null) {
            return;
        }

        ChainLinkData linkData = ChainLinkManager.getLink(chainLinkId);
        if (linkData == null) {
            return;
        }

        // Remove joint from physics
        if (linkData.hasJoint() && level instanceof ServerLevel serverLevel) {
            JointUtil.removeJointById(serverLevel, linkData.getJointId());
        }

        // Clear the partner block entity
        BlockPos partnerPos = isFirstEndpoint ? linkData.getSecondBlockPos() : linkData.getFirstBlockPos();
        var partnerBE = level.getBlockEntity(partnerPos);
        if (partnerBE instanceof HeavyDutyChainLinkBlockEntity partnerChain) {
            partnerChain.clearChainData();
        }

        // Remove from global manager
        ChainLinkManager.removeLink(chainLinkId);

        // Clear ourselves
        clearChainData();
    }

    /**
     * Clears chain data without removing joint (called by partner)
     */
    public void clearChainData() {
        chainLinkId = null;
        isFirstEndpoint = false;
        partnerBlockPos = null;
        lastLeftState = false;
        lastRightState = false;
        positionA = null;
        positionB = null;
        shipIdA = null;
        shipIdB = null;
        currentMaxDistance = 0;
        initialDistance = 0;
        firstBlockPos = null;
        secondBlockPos = null;
        setChanged();
    }
}