package net.mcreator.ironblood.procedures;

import net.minecraft.world.item.ItemStack;

public class AssemblyScannerPropertyValueProviderProcedure {
	public static double execute(ItemStack itemstack) {
		return itemstack.getOrCreateTag().getDouble("state");
	}
}
