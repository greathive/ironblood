package net.mcreator.ironblood.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;

import net.mcreator.ironblood.block.entity.HeavyDutyChainLinkBlockEntity;

import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.core.api.ships.ClientShip;

import org.joml.Vector3d;

/**
 * Renders chain segments between two HeavyDutyChainLink blocks.
 * Uses the vanilla chain block model and texture.
 */
public class HeavyDutyChainLinkRenderer implements BlockEntityRenderer<HeavyDutyChainLinkBlockEntity> {

    public HeavyDutyChainLinkRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(HeavyDutyChainLinkBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int combinedLight, int combinedOverlay) {

        // CRITICAL: Check if we have a valid chain link AND partner position
        if (blockEntity.getChainLinkId() == null || blockEntity.getPartnerBlockPos() == null) {
            return;
        }

        // Only render from the first endpoint to avoid rendering twice
        if (!blockEntity.isFirstEndpoint()) {
            return;
        }

        // Get partner position directly from block entity (synced to client)
        BlockPos partnerPos = blockEntity.getPartnerBlockPos();
        if (partnerPos == null || blockEntity.getLevel() == null) {
            return;
        }

        BlockPos thisPos = blockEntity.getBlockPos();

        // Safety check: Don't render if positions are the same
        if (thisPos.equals(partnerPos)) {
            return;
        }

        // Get world positions for both endpoints
        Vec3 startWorldPos = getWorldPosition(blockEntity.getLevel(), thisPos);
        Vec3 endWorldPos = getWorldPosition(blockEntity.getLevel(), partnerPos);

        // Calculate distance - if too far, don't render
        double distance = startWorldPos.distanceTo(endWorldPos);
        if (distance > 256 || distance < 0.1) {
            return; // Chain is too long or invalid
        }

        // Calculate the direction vector in WORLD space
        Vec3 worldDirection = endWorldPos.subtract(startWorldPos);

        // Now we need to transform this world direction into the LOCAL coordinate system
        // of the rendering block (which might be on a ship)
        Vec3 localDirection = worldToLocalDirection(blockEntity.getLevel(), thisPos, worldDirection);

        if (localDirection.lengthSqr() < 0.01) {
            return; // Invalid direction
        }

        // The poseStack is positioned at this block's corner in its local space
        // Start at block center in local coords
        Vec3 start = new Vec3(0.5, 0.5, 0.5);
        // End at start + local direction
        Vec3 end = start.add(localDirection);

        // Render chain segments
        renderChainSegments(poseStack, bufferSource, start, end, combinedLight);
    }

    /**
     * Gets the world position of a block CENTER, accounting for ships on the CLIENT side
     */
    private Vec3 getWorldPosition(net.minecraft.world.level.Level level, BlockPos blockPos) {
        var ship = VSGameUtilsKt.getShipManagingPos(level, blockPos);

        if (ship == null) {
            // World position - center of block
            return Vec3.atCenterOf(blockPos);
        } else {
            // Ship position - transform from ship-space to world-space
            if (ship instanceof ClientShip clientShip) {
                // Convert block position to ship-local coordinates (at block center)
                Vector3d shipLocal = VectorConversionsMCKt.toJOMLD(blockPos).add(0.5, 0.5, 0.5);

                // Transform to world coordinates using the ship's render transform
                Vector3d worldPos = clientShip.getRenderTransform().getShipToWorld().transformPosition(shipLocal);

                return new Vec3(worldPos.x, worldPos.y, worldPos.z);
            } else {
                // Fallback
                return Vec3.atCenterOf(blockPos);
            }
        }
    }

    /**
     * Transforms a world-space direction vector into the local coordinate system of a block.
     * If the block is on a ship, this applies the inverse ship rotation.
     */
    private Vec3 worldToLocalDirection(net.minecraft.world.level.Level level, BlockPos blockPos, Vec3 worldDirection) {
        var ship = VSGameUtilsKt.getShipManagingPos(level, blockPos);

        if (ship == null) {
            // Block is in world - no transformation needed
            return worldDirection;
        } else {
            // Block is on a ship - transform direction from world to ship-local
            if (ship instanceof ClientShip clientShip) {
                Vector3d worldDir = new Vector3d(worldDirection.x, worldDirection.y, worldDirection.z);

                // Use the inverse rotation to transform from world to ship-local
                Vector3d localDir = clientShip.getRenderTransform().getWorldToShip().transformDirection(worldDir);

                return new Vec3(localDir.x, localDir.y, localDir.z);
            } else {
                // Fallback
                return worldDirection;
            }
        }
    }

    /**
     * Renders chain segments between two points (in local coordinates)
     */
    private void renderChainSegments(PoseStack poseStack, MultiBufferSource bufferSource,
                                     Vec3 start, Vec3 end, int combinedLight) {

        // Calculate direction and distance
        Vec3 direction = end.subtract(start);
        double distance = direction.length();

        if (distance < 0.1) {
            return;
        }

        // Safety limit: max 256 blocks
        if (distance > 256) {
            return;
        }

        // Render chains along the line
        double segmentLength = 1.0;
        int segmentCount = Math.max(1, Math.min(256, (int) Math.ceil(distance / segmentLength)));

        // Normalize direction for rotation calculation
        Vec3 normalized = direction.normalize();

        // Safety check: make sure normalized is valid
        if (Double.isNaN(normalized.x) || Double.isNaN(normalized.y) || Double.isNaN(normalized.z)) {
            return; // Skip if direction is invalid
        }

        // Always use Y-axis chains (pointing up by default)
        BlockState chainState = Blocks.CHAIN.defaultBlockState().setValue(ChainBlock.AXIS, Direction.Axis.Y);

        // Get block renderer
        Minecraft mc = Minecraft.getInstance();

        // Calculate rotation to point the Y-axis chain along our direction
        // Yaw: horizontal rotation (around Y axis)
        double yaw = Math.atan2(normalized.x, normalized.z);

        // Pitch: vertical rotation (around X axis)
        // acos(y) gives us the angle from vertical (Y-axis)
        double pitch = Math.acos(Math.max(-1.0, Math.min(1.0, normalized.y))); // Clamp to prevent NaN

        // Safety check: ensure angles are valid
        if (Double.isNaN(yaw) || Double.isNaN(pitch) || Double.isInfinite(yaw) || Double.isInfinite(pitch)) {
            // Fallback to no rotation if calculations fail
            yaw = 0;
            pitch = 0;
        }

        // Render each chain segment
        for (int i = 0; i < segmentCount; i++) {
            poseStack.pushPose();

            // Calculate position along the line
            double t = (i + 0.5) / segmentCount;
            Vec3 segmentPos = start.add(direction.scale(t));

            // Translate to segment position
            poseStack.translate(segmentPos.x, segmentPos.y, segmentPos.z);

            // Apply rotation to align chain with direction
            poseStack.mulPose(Axis.YP.rotation((float) yaw));
            poseStack.mulPose(Axis.XP.rotation((float) pitch));

            // Center the block at origin
            poseStack.translate(-0.5, -0.5, -0.5);

            // Render the chain block model
            mc.getBlockRenderer().renderSingleBlock(
                    chainState,
                    poseStack,
                    bufferSource,
                    combinedLight,
                    OverlayTexture.NO_OVERLAY
            );

            poseStack.popPose();
        }
    }

    @Override
    public int getViewDistance() {
        return 256; // Render chains from far away
    }

    @Override
    public boolean shouldRenderOffScreen(HeavyDutyChainLinkBlockEntity blockEntity) {
        return true; // Always try to render even if the block entity is off screen
    }
}