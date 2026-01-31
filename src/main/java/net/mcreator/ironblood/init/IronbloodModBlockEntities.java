/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.ironblood.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.Block;

import net.mcreator.ironblood.block.entity.MechanicalJointaltBlockEntity;
import net.mcreator.ironblood.block.entity.MechanicalJointBlockEntity;
import net.mcreator.ironblood.IronbloodMod;

public class IronbloodModBlockEntities {
	public static final DeferredRegister<BlockEntityType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, IronbloodMod.MODID);
	public static final RegistryObject<BlockEntityType<MechanicalJointBlockEntity>> MECHANICAL_JOINT = register("mechanical_joint", IronbloodModBlocks.MECHANICAL_JOINT, MechanicalJointBlockEntity::new);
	public static final RegistryObject<BlockEntityType<MechanicalJointaltBlockEntity>> MECHANICAL_JOINTALT = register("mechanical_jointalt", IronbloodModBlocks.MECHANICAL_JOINTALT, MechanicalJointaltBlockEntity::new);

	// Start of user code block custom block entities
	// End of user code block custom block entities
	private static <T extends BlockEntity> RegistryObject<BlockEntityType<T>> register(String registryname, RegistryObject<Block> block, BlockEntityType.BlockEntitySupplier<T> supplier) {
		return REGISTRY.register(registryname, () -> BlockEntityType.Builder.of(supplier, block.get()).build(null));
	}
}