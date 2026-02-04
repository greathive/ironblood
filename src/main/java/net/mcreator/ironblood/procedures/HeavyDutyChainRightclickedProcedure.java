package net.mcreator.ironblood.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.network.chat.Component;

import net.mcreator.ironblood.ships.ChainDragManager;
import net.mcreator.ironblood.init.IronbloodModItems;

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
		
		// Stop dragging if currently active
		if (ChainDragManager.isDragging(player)) {
			ChainDragManager.stopDragging(player);
			player.displayClientMessage(Component.literal("§eReleased grapple!"), true);
		}
	}
}
