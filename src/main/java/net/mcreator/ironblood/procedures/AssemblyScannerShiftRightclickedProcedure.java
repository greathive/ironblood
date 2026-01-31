package net.mcreator.ironblood.procedures;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

public class AssemblyScannerShiftRightclickedProcedure {
	public static void execute(Entity entity, ItemStack itemstack) {
		if (entity == null)
			return;
		if (entity.isShiftKeyDown()) {
			itemstack.getOrCreateTag().putDouble("state", 0);
			itemstack.getOrCreateTag().putString("selected", "");
			itemstack.getOrCreateTag().putString("finalselection", "");
			if (entity instanceof Player _player && !_player.level().isClientSide())
				_player.displayClientMessage(Component.literal("Selection erased!"), true);
		}
	}
}
