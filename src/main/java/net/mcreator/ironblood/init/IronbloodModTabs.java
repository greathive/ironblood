
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.ironblood.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import net.mcreator.ironblood.IronbloodMod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class IronbloodModTabs {
	public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, IronbloodMod.MODID);
	public static final RegistryObject<CreativeModeTab> IRONBLOOD = REGISTRY.register("ironblood",
			() -> CreativeModeTab.builder().title(Component.translatable("item_group.ironblood.ironblood")).icon(() -> new ItemStack(IronbloodModItems.ASSEMBLY_SCANNER.get())).displayItems((parameters, tabData) -> {
				tabData.accept(IronbloodModItems.ASSEMBLY_SCANNER.get());
				tabData.accept(IronbloodModBlocks.MECHANICAL_JOINT.get().asItem());
				tabData.accept(IronbloodModBlocks.ASSEMBLER.get().asItem());
				tabData.accept(IronbloodModBlocks.MECHANICAL_JOINT_ALT.get().asItem());
				tabData.accept(IronbloodModItems.LINK_WRENCH.get());
				tabData.accept(IronbloodModItems.HEAVY_DUTY_CHAIN.get());
				tabData.accept(IronbloodModBlocks.HEAVY_DUTY_CHAIN_LINK.get().asItem());
				tabData.accept(IronbloodModBlocks.HALFMETAL_BLOCK.get().asItem());
				tabData.accept(IronbloodModBlocks.HALFMETAL_SLAB.get().asItem());
				tabData.accept(IronbloodModBlocks.HALFMETAL_VERTICAL_SLAB.get().asItem());
				tabData.accept(IronbloodModBlocks.MECHANICAL_SWIVEL_JOINT.get().asItem());
				tabData.accept(IronbloodModBlocks.JOINT_CONNECTOR.get().asItem());
				tabData.accept(IronbloodModBlocks.PILOT_SEAT.get().asItem());
				tabData.accept(IronbloodModBlocks.SWIVEL_BEARING.get().asItem());
				tabData.accept(IronbloodModBlocks.SWIVEL_BEARING_TABLE.get().asItem());
				tabData.accept(IronbloodModBlocks.VERTICAL_ROTATOR.get().asItem());
				tabData.accept(IronbloodModBlocks.VERTICAL_HINGE.get().asItem());
				tabData.accept(IronbloodModBlocks.DYNAMIC_PISTON.get().asItem());
				tabData.accept(IronbloodModBlocks.DYNAMIC_PISTON_END.get().asItem());
				tabData.accept(IronbloodModItems.DYNAMIC_PISTON_CASING.get());
			}).build());

	@SubscribeEvent
	public static void buildTabContentsVanilla(BuildCreativeModeTabContentsEvent tabData) {
		if (tabData.getTabKey() == CreativeModeTabs.OP_BLOCKS) {
			if (tabData.hasPermissions()) {
				tabData.accept(IronbloodModBlocks.AHAB_1.get().asItem());
				tabData.accept(IronbloodModBlocks.AHAB_2.get().asItem());
				tabData.accept(IronbloodModBlocks.AHAB_3.get().asItem());
				tabData.accept(IronbloodModBlocks.AHAB_4.get().asItem());
				tabData.accept(IronbloodModBlocks.AHAB_5.get().asItem());
				tabData.accept(IronbloodModBlocks.AHAB_CORE.get().asItem());
				tabData.accept(IronbloodModBlocks.AHAB_VENT_1.get().asItem());
				tabData.accept(IronbloodModBlocks.AHAB_VENT_2.get().asItem());
				tabData.accept(IronbloodModBlocks.AHAB_VENT_3.get().asItem());
			}
		}
	}
}
