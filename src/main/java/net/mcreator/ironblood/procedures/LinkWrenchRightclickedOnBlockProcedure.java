package net.mcreator.ironblood.procedures;

import org.valkyrienskies.mod.common.VSGameUtilsKt;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import net.mcreator.ironblood.init.IronbloodModBlocks;
import net.mcreator.ironblood.IronbloodMod;

public class LinkWrenchRightclickedOnBlockProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z) {
		if ((world.getBlockState(BlockPos.containing(x, y, z))).getBlock() == IronbloodModBlocks.MECHANICAL_JOINT.get()) {
			IronbloodMod.LOGGER.info("(" + ((world instanceof ServerLevel) ? (VSGameUtilsKt.getLoadedShipManagingPos((ServerLevel) world, (BlockPos.containing(x, y, z)))) : null) + ")" + BlockPos.containing(x, y, z));
		}
	}
}
