
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.ironblood.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.network.chat.Component;
import net.minecraft.core.registries.Registries;

import net.mcreator.ironblood.IronbloodMod;

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
			}).build());
}
