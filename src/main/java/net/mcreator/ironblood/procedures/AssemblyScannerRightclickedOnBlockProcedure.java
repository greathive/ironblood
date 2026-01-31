package net.mcreator.ironblood.procedures;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

import net.mcreator.ironblood.IronbloodMod;

public class AssemblyScannerRightclickedOnBlockProcedure {
	public static void execute(double x, double y, double z, Entity entity, ItemStack itemstack) {
		if (entity == null)
			return;
		String requestedblockposition = "";
		if (!((entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == itemstack.getItem())) {
			if (entity.isShiftKeyDown()) {
				AssemblyScannerShiftRightclickedProcedure.execute(entity, itemstack);
			} else {
				requestedblockposition = "[" + x + "," + y + "," + z + "]";
				if (itemstack.getOrCreateTag().getDouble("state") == 0) {
					itemstack.getOrCreateTag().putDouble("state", 1);
					itemstack.getOrCreateTag().putString("selected", requestedblockposition);
				} else {
					itemstack.getOrCreateTag().putDouble("state", 0);
					itemstack.getOrCreateTag().putString("finalselection", (itemstack.getOrCreateTag().getString("finalselection") + "{" + itemstack.getOrCreateTag().getString("selected") + requestedblockposition + "}"));
					itemstack.getOrCreateTag().putString("selected", "");
					if (entity instanceof Player _player && !_player.level().isClientSide())
						_player.displayClientMessage(Component.literal("Selection confirmed!"), true);
				}
				IronbloodMod.LOGGER.info("requestedblockpos" + requestedblockposition);
				IronbloodMod.LOGGER.info("state" + itemstack.getOrCreateTag().getDouble("state"));
				IronbloodMod.LOGGER.info("selected" + itemstack.getOrCreateTag().getString("selected"));
				IronbloodMod.LOGGER.info("finalselection" + itemstack.getOrCreateTag().getString("finalselection"));
			}
		}
	}
}
