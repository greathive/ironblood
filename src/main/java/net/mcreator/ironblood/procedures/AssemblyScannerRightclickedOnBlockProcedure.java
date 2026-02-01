package net.mcreator.ironblood.procedures;

import net.minecraftforge.registries.ForgeRegistries;

import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.Level;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.sounds.SoundSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;

public class AssemblyScannerRightclickedOnBlockProcedure {
	public static void execute(LevelAccessor world, double x, double y, double z, Entity entity, ItemStack itemstack) {
		if (entity == null)
			return;
		String requestedblockposition = "";
		if (!((entity instanceof LivingEntity _livEnt ? _livEnt.getOffhandItem() : ItemStack.EMPTY).getItem() == itemstack.getItem())) {
			if (entity.isShiftKeyDown()) {
				AssemblyScannerShiftRightclickedProcedure.execute(world, x, y, z, entity, itemstack);
			} else {
				requestedblockposition = "[" + x + "," + y + "," + z + "]";
				if (world instanceof Level _level) {
					if (_level.isClientSide()) {
						_level.playLocalSound(x, y, z, ForgeRegistries.SOUND_EVENTS.getValue(new ResourceLocation("ironblood:scanner-activate")), SoundSource.NEUTRAL, 1, 1, false);
					}
				}
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
			}
		}
	}
}
