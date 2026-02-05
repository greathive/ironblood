package net.mcreator.ironblood.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.ChainBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Direction;

import org.joml.Matrix4f;

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

        if (blockEntity.getChainLinkId() == null) {
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

        // IMPORTANT: Get world positions EVERY FRAME for ships (they move!)
        // startWorldPos = this block's world position (the one rendering)
        // endWorldPos = partner block's world position (the target)
        Vec3 startWorldPos = getWorldPosition(blockEntity.getLevel(), thisPos);
        Vec3 endWorldPos = getWorldPosition(blockEntity.getLevel(), partnerPos);

        // Calculate distance - if too far, don't render
        double distance = startWorldPos.distanceTo(endWorldPos);
        if (distance > 256 || distance < 0.1) {
            return; // Chain is too long or invalid
        }

        // Calculate the vector from this block's center to the partner's center (in world space)
        // This direction is ALWAYS correct because both positions are in world-space
        Vec3 directionToPartner = endWorldPos.subtract(startWorldPos);

        // The poseStack is at this block's CORNER (not center)
        // So we need to start at (0.5, 0.5, 0.5) to be at the center
        // And end at (0.5, 0.5, 0.5) + directionToPartner
        Vec3 start = new Vec3(0.5, 0.5, 0.5);
        Vec3 end = start.add(directionToPartner);

        // Debug: Ensure the direction is correct
        // The chain should always render from our center toward the partner
        if (directionToPartner.lengthSqr() < 0.01) {
            return; // Too close, skip rendering
        }

        // Render chain segments
        renderChainSegments(poseStack, bufferSource, start, end, combinedLight);
    }

    /**
     * Gets the world position of a block CENTER, accounting for ships on the CLIENT side
     */
    private Vec3 getWorldPosition(net.minecraft.world.level.Level level, BlockPos blockPos) {
        // Check if this position is on a ship (getShipManagingPos returns polymorphic type on client)
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
                // CRITICAL: Use getRenderTransform() for smooth client-side rendering
                Vector3d worldPos = clientShip.getRenderTransform().getShipToWorld().transformPosition(shipLocal);

                return new Vec3(worldPos.x, worldPos.y, worldPos.z);
            } else {
                // Fallback - shouldn't happen on client
                return Vec3.atCenterOf(blockPos);
            }
        }
    }

    /**
     * Renders a debug line and chain segments between two points
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

        // Render debug line (red)
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());
        Matrix4f matrix = poseStack.last().pose();

        vertexConsumer.vertex(matrix, (float) start.x, (float) start.y, (float) start.z)
                .color(255, 0, 0, 255)
                .normal(0, 1, 0)
                .endVertex();

        vertexConsumer.vertex(matrix, (float) end.x, (float) end.y, (float) end.z)
                .color(255, 0, 0, 255)
                .normal(0, 1, 0)
                .endVertex();

        // Now render chains along the line
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

            // Always apply rotation (even if 0)
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