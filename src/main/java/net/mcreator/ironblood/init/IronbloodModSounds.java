
/*
 *    MCreator note: This file will be REGENERATED on each build.
 */
package net.mcreator.ironblood.init;

import net.minecraftforge.registries.RegistryObject;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.DeferredRegister;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.resources.ResourceLocation;

import net.mcreator.ironblood.IronbloodMod;

public class IronbloodModSounds {
	public static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, IronbloodMod.MODID);
	public static final RegistryObject<SoundEvent> SCANNER_ACTIVATE = REGISTRY.register("scanner-activate", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ironblood", "scanner-activate")));
	public static final RegistryObject<SoundEvent> SCANNER_DEACTIVATE = REGISTRY.register("scanner-deactivate", () -> SoundEvent.createVariableRangeEvent(new ResourceLocation("ironblood", "scanner-deactivate")));
}
