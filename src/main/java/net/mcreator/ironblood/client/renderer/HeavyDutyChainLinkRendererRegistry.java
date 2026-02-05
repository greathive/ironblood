package net.mcreator.ironblood.client;

import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.client.renderer.blockentity.BlockEntityRenderers;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.mcreator.ironblood.init.IronbloodModBlockEntities;
import net.mcreator.ironblood.client.renderer.HeavyDutyChainLinkRenderer;
import net.mcreator.ironblood.block.entity.HeavyDutyChainLinkBlockEntity;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class HeavyDutyChainLinkRendererRegistry {

	@SubscribeEvent
	public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
		event.registerBlockEntityRenderer(
			(BlockEntityType<HeavyDutyChainLinkBlockEntity>) IronbloodModBlockEntities.HEAVY_DUTY_CHAIN_LINK.get(),
			HeavyDutyChainLinkRenderer::new
		);
	}
}