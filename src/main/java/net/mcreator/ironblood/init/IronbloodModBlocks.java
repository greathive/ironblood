
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.ironblood.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.Block;

import net.mcreator.ironblood.block.VerticalRotatorBlock;
import net.mcreator.ironblood.block.VerticalHingeBlock;
import net.mcreator.ironblood.block.SwivelBearingTableBlock;
import net.mcreator.ironblood.block.SwivelBearingBlock;
import net.mcreator.ironblood.block.PistonRodRenderBlock;
import net.mcreator.ironblood.block.PistonCasingRenderBlock;
import net.mcreator.ironblood.block.PilotSeatBlock;
import net.mcreator.ironblood.block.MechanicalSwivelJointBlock;
import net.mcreator.ironblood.block.MechanicalJointBlock;
import net.mcreator.ironblood.block.MechanicalJointAltBlock;
import net.mcreator.ironblood.block.JointConnectorBlock;
import net.mcreator.ironblood.block.HeavyDutyChainLinkBlock;
import net.mcreator.ironblood.block.HalfmetalVerticalSlabBlock;
import net.mcreator.ironblood.block.HalfmetalSlabBlock;
import net.mcreator.ironblood.block.HalfmetalBlockBlock;
import net.mcreator.ironblood.block.DynamicPistonEndBlock;
import net.mcreator.ironblood.block.DynamicPistonBlock;
import net.mcreator.ironblood.block.AssemblerBlock;
import net.mcreator.ironblood.block.AhabVent3Block;
import net.mcreator.ironblood.block.AhabVent2Block;
import net.mcreator.ironblood.block.AhabVent1Block;
import net.mcreator.ironblood.block.AhabCoreBlock;
import net.mcreator.ironblood.block.Ahab5Block;
import net.mcreator.ironblood.block.Ahab4Block;
import net.mcreator.ironblood.block.Ahab3Block;
import net.mcreator.ironblood.block.Ahab2Block;
import net.mcreator.ironblood.block.Ahab1Block;
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
	public static final RegistryObject<Block> MECHANICAL_SWIVEL_JOINT = REGISTRY.register("mechanical_swivel_joint", () -> new MechanicalSwivelJointBlock());
	public static final RegistryObject<Block> JOINT_CONNECTOR = REGISTRY.register("joint_connector", () -> new JointConnectorBlock());
	public static final RegistryObject<Block> PILOT_SEAT = REGISTRY.register("pilot_seat", () -> new PilotSeatBlock());
	public static final RegistryObject<Block> SWIVEL_BEARING = REGISTRY.register("swivel_bearing", () -> new SwivelBearingBlock());
	public static final RegistryObject<Block> SWIVEL_BEARING_TABLE = REGISTRY.register("swivel_bearing_table", () -> new SwivelBearingTableBlock());
	public static final RegistryObject<Block> AHAB_1 = REGISTRY.register("ahab_1", () -> new Ahab1Block());
	public static final RegistryObject<Block> AHAB_2 = REGISTRY.register("ahab_2", () -> new Ahab2Block());
	public static final RegistryObject<Block> AHAB_3 = REGISTRY.register("ahab_3", () -> new Ahab3Block());
	public static final RegistryObject<Block> AHAB_4 = REGISTRY.register("ahab_4", () -> new Ahab4Block());
	public static final RegistryObject<Block> AHAB_5 = REGISTRY.register("ahab_5", () -> new Ahab5Block());
	public static final RegistryObject<Block> AHAB_CORE = REGISTRY.register("ahab_core", () -> new AhabCoreBlock());
	public static final RegistryObject<Block> AHAB_VENT_1 = REGISTRY.register("ahab_vent_1", () -> new AhabVent1Block());
	public static final RegistryObject<Block> AHAB_VENT_2 = REGISTRY.register("ahab_vent_2", () -> new AhabVent2Block());
	public static final RegistryObject<Block> AHAB_VENT_3 = REGISTRY.register("ahab_vent_3", () -> new AhabVent3Block());
	public static final RegistryObject<Block> VERTICAL_ROTATOR = REGISTRY.register("vertical_rotator", () -> new VerticalRotatorBlock());
	public static final RegistryObject<Block> VERTICAL_HINGE = REGISTRY.register("vertical_hinge", () -> new VerticalHingeBlock());
	public static final RegistryObject<Block> DYNAMIC_PISTON = REGISTRY.register("dynamic_piston", () -> new DynamicPistonBlock());
	public static final RegistryObject<Block> DYNAMIC_PISTON_END = REGISTRY.register("dynamic_piston_end", () -> new DynamicPistonEndBlock());
	public static final RegistryObject<Block> PISTON_CASING_RENDER = REGISTRY.register("piston_casing_render", () -> new PistonCasingRenderBlock());
	public static final RegistryObject<Block> PISTON_ROD_RENDER = REGISTRY.register("piston_rod_render", () -> new PistonRodRenderBlock());
	// Start of user code block custom blocks
	// End of user code block custom blocks
}
