
package net.mcreator.ironblood.item;

import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.Item;

public class DynamicPistonCasingItem extends Item {
	public DynamicPistonCasingItem() {
		super(new Item.Properties().stacksTo(64).rarity(Rarity.COMMON));
	}
}
