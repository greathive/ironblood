package net.mcreator.ironblood.procedures;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import net.mcreator.ironblood.ships.ShipMaker;

public class CreateShipFromAssemblyScannerProcedure {
	public static void execute(LevelAccessor world) {
		BlockPos shipblockpos = new BlockPos(0, 0, 0);
		{
			if (world instanceof ServerLevel) {
				ShipMaker.assembleShip((ServerLevel) world, shipblockpos, 1);
			}
		}
	}
}
