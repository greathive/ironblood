package net.mcreator.ironblood.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

public class HeavyDutyChainRightclickedProcedure {
	public static void execute(LevelAccessor world, Entity entity, ItemStack itemstack) {
		if (entity == null || !(entity instanceof Player player)) {
			return;
		}
		
		// Only execute on server side
		if (world.isClientSide()) {
			return;
		}
		
		// Check if this is actually the heavy duty chain in the main hand
		if (!(entity instanceof LivingEntity living) || living.getMainHandItem() != itemstack) {
			return;
		}
		
		// Cancel any pending chain selection
		if (itemstack.getOrCreateTag().contains("chainFirstPoint")) {
			itemstack.getOrCreateTag().remove("chainFirstPoint");
			player.displayClientMessage(Component.literal("§eChain selection cancelled!"), true);
		}
	}
}
