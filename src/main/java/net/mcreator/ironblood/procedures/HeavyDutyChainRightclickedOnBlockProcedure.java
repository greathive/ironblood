package net.mcreator.ironblood.procedures;

import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.api.ships.LoadedServerShip;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.core.BlockPos;

import net.mcreator.ironblood.ships.ChainDragManager;
import net.mcreator.ironblood.init.IronbloodModItems;

public class HeavyDutyChainRightclickedOnBlockProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity) {
		if (entity == null || !(entity instanceof Player player)) {
			return;
		}
		if (!(world instanceof ServerLevel serverLevel)) {
			return;
		}

		BlockPos blockPos = BlockPos.containing(x, y, z);
		
		// Check if the clicked block is on a ship
		LoadedServerShip ship = VSGameUtilsKt.getLoadedShipManagingPos(serverLevel, blockPos);
		
		if (ship == null) {
			player.displayClientMessage(Component.literal("§cThis block is not on a ship!"), true);
			return;
		}
		
		// Start dragging the ship
		ChainDragManager.startDragging(player, ship, blockPos);
		player.displayClientMessage(Component.literal("§aGrappled onto ship! Right-click in air to release."), true);
	}
}
