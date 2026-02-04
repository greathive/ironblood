
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.ironblood.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.api.distmarker.Dist;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.renderer.item.ItemProperties;

import net.mcreator.ironblood.procedures.AssemblyScannerPropertyValueProviderProcedure;
import net.mcreator.ironblood.item.LinkWrenchItem;
import net.mcreator.ironblood.item.HeavyDutyChainItem;
import net.mcreator.ironblood.item.AssemblyScannerItem;
import net.mcreator.ironblood.IronbloodMod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class IronbloodModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, IronbloodMod.MODID);
	public static final RegistryObject<Item> MECHANICAL_JOINT = block(IronbloodModBlocks.MECHANICAL_JOINT);
	public static final RegistryObject<Item> ASSEMBLY_SCANNER = REGISTRY.register("assembly_scanner", () -> new AssemblyScannerItem());
	public static final RegistryObject<Item> ASSEMBLER = block(IronbloodModBlocks.ASSEMBLER);
	public static final RegistryObject<Item> MECHANICAL_JOINT_ALT = block(IronbloodModBlocks.MECHANICAL_JOINT_ALT);
	public static final RegistryObject<Item> LINK_WRENCH = REGISTRY.register("link_wrench", () -> new LinkWrenchItem());
	public static final RegistryObject<Item> HEAVY_DUTY_CHAIN = REGISTRY.register("heavy_duty_chain", () -> new HeavyDutyChainItem());
	public static final RegistryObject<Item> HEAVY_DUTY_CHAIN_LINK = block(IronbloodModBlocks.HEAVY_DUTY_CHAIN_LINK);

	// Start of user code block custom items
	// End of user code block custom items
	private static RegistryObject<Item> block(RegistryObject<Block> block) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
	}

	@SubscribeEvent
	public static void clientLoad(FMLClientSetupEvent event) {
		event.enqueueWork(() -> {
			ItemProperties.register(ASSEMBLY_SCANNER.get(), new ResourceLocation("ironblood:assembly_scanner_state"),
					(itemStackToRender, clientWorld, entity, itemEntityId) -> (float) AssemblyScannerPropertyValueProviderProcedure.execute(itemStackToRender));
		});
	}
}
