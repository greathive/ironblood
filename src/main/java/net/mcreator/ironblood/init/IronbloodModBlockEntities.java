
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.ironblood.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.Block;

import net.mcreator.ironblood.block.entity.VerticalRotatorBlockEntity;
import net.mcreator.ironblood.block.entity.VerticalHingeBlockEntity;
import net.mcreator.ironblood.block.entity.SwivelBearingTableBlockEntity;
import net.mcreator.ironblood.block.entity.SwivelBearingBlockEntity;
import net.mcreator.ironblood.block.entity.PilotSeatBlockEntity;
import net.mcreator.ironblood.block.entity.MechanicalSwivelJointBlockEntity;
import net.mcreator.ironblood.block.entity.MechanicalJointBlockEntity;
import net.mcreator.ironblood.block.entity.MechanicalJointAltBlockEntity;
import net.mcreator.ironblood.block.entity.JointConnectorBlockEntity;
import net.mcreator.ironblood.block.entity.HeavyDutyChainLinkBlockEntity;
import net.mcreator.ironblood.block.entity.AssemblerBlockEntity;
import net.mcreator.ironblood.IronbloodMod;

public class IronbloodModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, IronbloodMod.MODID);
	public static final RegistryObject<BlockEntityType<?>> MECHANICAL_JOINT = register("mechanical_joint", IronbloodModBlocks.MECHANICAL_JOINT, MechanicalJointBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> ASSEMBLER = register("assembler", IronbloodModBlocks.ASSEMBLER, AssemblerBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> MECHANICAL_JOINT_ALT = register("mechanical_joint_alt", IronbloodModBlocks.MECHANICAL_JOINT_ALT, MechanicalJointAltBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> HEAVY_DUTY_CHAIN_LINK = register("heavy_duty_chain_link", IronbloodModBlocks.HEAVY_DUTY_CHAIN_LINK, HeavyDutyChainLinkBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> MECHANICAL_SWIVEL_JOINT = register("mechanical_swivel_joint", IronbloodModBlocks.MECHANICAL_SWIVEL_JOINT, MechanicalSwivelJointBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> JOINT_CONNECTOR = register("joint_connector", IronbloodModBlocks.JOINT_CONNECTOR, JointConnectorBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> PILOT_SEAT = register("pilot_seat", IronbloodModBlocks.PILOT_SEAT, PilotSeatBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> SWIVEL_BEARING = register("swivel_bearing", IronbloodModBlocks.SWIVEL_BEARING, SwivelBearingBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> SWIVEL_BEARING_TABLE = register("swivel_bearing_table", IronbloodModBlocks.SWIVEL_BEARING_TABLE, SwivelBearingTableBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> VERTICAL_ROTATOR = register("vertical_rotator", IronbloodModBlocks.VERTICAL_ROTATOR, VerticalRotatorBlockEntity::new);
	public static final RegistryObject<BlockEntityType<?>> VERTICAL_HINGE = register("vertical_hinge", IronbloodModBlocks.VERTICAL_HINGE, VerticalHingeBlockEntity::new);

	// Start of user code block custom block entities
	// End of user code block custom block entities
	private static RegistryObject<BlockEntityType<?>> register(String registryname, RegistryObject<Block> block, BlockEntityType.BlockEntitySupplier<?> supplier) {
		return REGISTRY.register(registryname, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
	}
}
