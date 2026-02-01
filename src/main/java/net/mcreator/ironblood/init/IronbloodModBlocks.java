
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.ironblood.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;

import net.mcreator.ironblood.block.MechanicalJointBlock;
import net.mcreator.ironblood.block.AssemblerBlock;
import net.mcreator.ironblood.IronbloodMod;

public class IronbloodModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, IronbloodMod.MODID);
	public static final RegistryObject<Block> MECHANICAL_JOINT = REGISTRY.register("mechanical_joint", () -> new MechanicalJointBlock());
	public static final RegistryObject<Block> ASSEMBLER = REGISTRY.register("assembler", () -> new AssemblerBlock());
	// Start of user code block custom blocks
	// End of user code block custom blocks
}
