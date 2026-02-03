package net.mcreator.ironblood.ships;

import org.valkyrienskies.mod.api.ValkyrienSkies;
import org.valkyrienskies.core.impl.hooks.VSEvents;

import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD, modid = "ironblood")
public class OnShipLoad {
	@SubscribeEvent
	public static void onCommonSetup(FMLCommonSetupEvent event) {
		ValkyrienSkies.api().registerAttachment(ForceInducedShips.class);
		ValkyrienSkies.api().registerAttachment(GravityInducedShips.class);
		ValkyrienSkies.api().registerAttachment(ShipLandingAttachment.class);
		VSEvents.ShipLoadEvent.Companion.on((shipLoadEvent) -> {
			ForceInducedShips.getOrCreate(shipLoadEvent.getShip());
			GravityInducedShips.getOrCreate(shipLoadEvent.getShip());
		});
	}
}
