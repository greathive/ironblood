
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.ironblood.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;

import net.mcreator.ironblood.block.MechanicalJointBlock;
import net.mcreator.ironblood.block.MechanicalJointAltBlock;
import net.mcreator.ironblood.block.HeavyDutyChainLinkBlock;
import net.mcreator.ironblood.block.HalfmetalVerticalSlabBlock;
import net.mcreator.ironblood.block.HalfmetalSlabBlock;
import net.mcreator.ironblood.block.HalfmetalBlockBlock;
import net.mcreator.ironblood.block.AssemblerBlock;
import net.mcreator.ironblood.IronbloodMod;

public class IronbloodModBlocks {
	public static final DeferredRegister<Block> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCKS, IronbloodMod.MODID);
	public static final RegistryObject<Block> MECHANICAL_JOINT = REGISTRY.register("mechanical_joint", () -> new MechanicalJointBlock());
	public static final RegistryObject<Block> ASSEMBLER = REGISTRY.register("assembler", () -> new AssemblerBlock());
	public static final RegistryObject<Block> MECHANICAL_JOINT_ALT = REGISTRY.register("mechanical_joint_alt", () -> new MechanicalJointAltBlock());
	public static final RegistryObject<Block> HEAVY_DUTY_CHAIN_LINK = REGISTRY.register("heavy_duty_chain_link", () -> new HeavyDutyChainLinkBlock());
	public static final RegistryObject<Block> HALFMETAL_BLOCK = REGISTRY.register("halfmetal_block", () -> new HalfmetalBlockBlock());
	public static final RegistryObject<Block> HALFMETAL_SLAB = REGISTRY.register("halfmetal_slab", () -> new HalfmetalSlabBlock());
	public static final RegistryObject<Block> HALFMETAL_VERTICAL_SLAB = REGISTRY.register("halfmetal_vertical_slab", () -> new HalfmetalVerticalSlabBlock());
	// Start of user code block custom blocks
	// End of user code block custom blocks
}
