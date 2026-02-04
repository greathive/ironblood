package net.mcreator.ironblood.item;

import net.minecraft.world.level.Level;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Item;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;

import net.mcreator.ironblood.procedures.HeavyDutyChainRightclickedProcedure;
import net.mcreator.ironblood.procedures.HeavyDutyChainRightclickedOnBlockProcedure;

import java.util.List;

public class HeavyDutyChainItem extends Item {
	public HeavyDutyChainItem() {
		super(new Item.Properties().stacksTo(1).rarity(Rarity.COMMON));
	}

	@Override
	public void appendHoverText(ItemStack itemstack, Level level, List<Component> list, TooltipFlag flag) {
		super.appendHoverText(itemstack, level, list, flag);
		list.add(Component.translatable("item.ironblood.heavy_duty_chain.description_0"));
		list.add(Component.literal("§7Right-click two points to create chain"));
		list.add(Component.literal("§7Click endpoint to shorten, shift-click to extend"));
		list.add(Component.literal("§7Right-click in air to cancel selection"));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player entity, InteractionHand hand) {
		InteractionResultHolder<ItemStack> ar = super.use(world, entity, hand);
		HeavyDutyChainRightclickedProcedure.execute(world, entity, ar.getObject());
		return ar;
	}

	@Override
	public InteractionResult useOn(UseOnContext context) {
		super.useOn(context);
		HeavyDutyChainRightclickedOnBlockProcedure.execute(
			context.getLevel(), 
			context.getClickedPos().getX(), 
			context.getClickedPos().getY(), 
			context.getClickedPos().getZ(), 
			context.getPlayer(),
			context.getItemInHand()
		);
		return InteractionResult.SUCCESS;
	}
}