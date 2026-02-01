package net.mcreator.ironblood.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.mcreator.ironblood.init.IronbloodModItems;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(modid = "ironblood", bus = Mod.EventBusSubscriber.Bus.FORGE, value = Dist.CLIENT)
public class AssemblyScannerOutlineRenderer {

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;

        if (player == null) {
            return;
        }

        // Check if player is holding the Assembly Scanner
        ItemStack heldItem = player.getMainHandItem();
        if (heldItem.getItem() != IronbloodModItems.ASSEMBLY_SCANNER.get()) {
            return;
        }

        double state = heldItem.getOrCreateTag().getDouble("state");

        List<BoundingBox> allBoxes = new ArrayList<>();

        // Always render finalized selections
        String finalSelection = heldItem.getOrCreateTag().getString("finalselection");
        if (!finalSelection.isEmpty()) {
            // Parse and collect all pairs
            java.util.List<BlockPos[]> pairs = parseFinalSelection(finalSelection);
            for (BlockPos[] pair : pairs) {
                if (pair[0] != null && pair[1] != null) {
                    allBoxes.add(new BoundingBox(pair[0], pair[1]));
                }
            }
        }

        // If state is 1, also render preview box from selected to target block
        if (state == 1) {
            String selectedStr = heldItem.getOrCreateTag().getString("selected");
            if (!selectedStr.isEmpty()) {
                // Parse the selected position [x,y,z]
                BlockPos selectedPos = parseBlockPos(selectedStr);

                // Get the block the player is currently looking at
                HitResult hitResult = mc.hitResult;
                if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && selectedPos != null) {
                    BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                    BlockPos targetPos = blockHitResult.getBlockPos();

                    allBoxes.add(new BoundingBox(selectedPos, targetPos));
                }
            }
        }

        if (!allBoxes.isEmpty()) {
            renderAllBoxes(event.getPoseStack(), mc, allBoxes);
        }
    }

    private static void renderAllBoxes(PoseStack poseStack, Minecraft mc, List<BoundingBox> boxes) {
        Vec3 cameraPos = mc.gameRenderer.getMainCamera().getPosition();

        poseStack.pushPose();
        poseStack.translate(-cameraPos.x, -cameraPos.y, -cameraPos.z);

        // Render all the outlines
        MultiBufferSource.BufferSource bufferSource = mc.renderBuffers().bufferSource();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.lines());

        float r = 45 / 255.0f;
        float g = 218 / 255.0f;
        float b = 50 / 255.0f;

        for (BoundingBox box : boxes) {
            LevelRenderer.renderLineBox(
                    poseStack,
                    vertexConsumer,
                    box.minX, box.minY, box.minZ,
                    box.maxX, box.maxY, box.maxZ,
                    r, g, b, 1.0f
            );
        }

        bufferSource.endBatch();

        poseStack.popPose();
    }

    private static BlockPos parseBlockPos(String posStr) {
        try {
            // Remove brackets and split by comma
            posStr = posStr.replace("[", "").replace("]", "");
            String[] parts = posStr.split(",");

            if (parts.length != 3) {
                return null;
            }

            int x = (int) Double.parseDouble(parts[0].trim());
            int y = (int) Double.parseDouble(parts[1].trim());
            int z = (int) Double.parseDouble(parts[2].trim());

            return new BlockPos(x, y, z);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private static java.util.List<BlockPos[]> parseFinalSelection(String finalSelection) {
        java.util.List<BlockPos[]> pairs = new java.util.ArrayList<>();

        if (finalSelection == null || finalSelection.isEmpty()) {
            return pairs;
        }

        // Split by pairs: {[x,y,z][x,y,z]}
        // First, split by }{ to separate each pair
        String[] pairStrings = finalSelection.split("\\}\\{");

        for (String pairStr : pairStrings) {
            // Remove leading/trailing { and }
            pairStr = pairStr.replace("{", "").replace("}", "");

            // Now we should have something like "[x,y,z][x,y,z]"
            // Split into two position strings
            int firstClose = pairStr.indexOf("]");
            if (firstClose == -1) {
                continue;
            }

            String pos1Str = pairStr.substring(0, firstClose + 1);
            String pos2Str = pairStr.substring(firstClose + 1);

            BlockPos pos1 = parseBlockPos(pos1Str);
            BlockPos pos2 = parseBlockPos(pos2Str);

            if (pos1 != null && pos2 != null) {
                pairs.add(new BlockPos[]{pos1, pos2});
            }
        }

        return pairs;
    }

    private static class BoundingBox {
        double minX, minY, minZ, maxX, maxY, maxZ;

        BoundingBox(BlockPos pos1, BlockPos pos2) {
            double offset = 0.005;
            this.minX = Math.min(pos1.getX(), pos2.getX()) - offset;
            this.minY = Math.min(pos1.getY(), pos2.getY()) - offset;
            this.minZ = Math.min(pos1.getZ(), pos2.getZ()) - offset;
            this.maxX = Math.max(pos1.getX(), pos2.getX()) + 1 + offset;
            this.maxY = Math.max(pos1.getY(), pos2.getY()) + 1 + offset;
            this.maxZ = Math.max(pos1.getZ(), pos2.getZ()) + 1 + offset;
        }
    }
}
