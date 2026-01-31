/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.ironblood.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.BlockItem;

import net.mcreator.ironblood.IronbloodMod;

public class IronbloodModItems {
	public static final DeferredRegister<Item> REGISTRY = DeferredRegister.create(ForgeRegistries.ITEMS, IronbloodMod.MODID);
	public static final RegistryObject<Item> MECHANICAL_JOINT;
	public static final RegistryObject<Item> MECHANICAL_JOINTALT;
	static {
		MECHANICAL_JOINT = block(IronbloodModBlocks.MECHANICAL_JOINT);
		MECHANICAL_JOINTALT = block(IronbloodModBlocks.MECHANICAL_JOINTALT);
	}

	// Start of user code block custom items
	// End of user code block custom items
	private static RegistryObject<Item> block(RegistryObject<Block> block) {
		return block(block, new Item.Properties());
	}

	private static RegistryObject<Item> block(RegistryObject<Block> block, Item.Properties properties) {
		return REGISTRY.register(block.getId().getPath(), () -> new BlockItem(block.get(), properties));
	}
}