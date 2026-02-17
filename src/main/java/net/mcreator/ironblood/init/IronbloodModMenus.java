
/*
 *	MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.ironblood.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.common.extensions.IForgeMenuType;

import net.minecraft.world.inventory.MenuType;

import net.mcreator.ironblood.world.inventory.JointManagerMenu;
import net.mcreator.ironblood.IronbloodMod;

public class IronbloodModMenus {
	public static final DeferredRegister<MenuType<?>> REGISTRY = DeferredRegister.create(ForgeRegistries.MENU_TYPES, IronbloodMod.MODID);
	public static final RegistryObject<MenuType<JointManagerMenu>> JOINT_MANAGER = REGISTRY.register("joint_manager", () -> IForgeMenuType.create(JointManagerMenu::new));
}
