package net.mcreator.ironblood.network;

import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.core.BlockPos;

import net.mcreator.ironblood.block.entity.PilotSeatBlockEntity;
import net.mcreator.ironblood.IronbloodMod;

import java.util.function.Supplier;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class JointManagerButtonMessage {
	private final BlockPos pos;
	private final String jointName;
	private final boolean isAdd; // true for add, false for remove

	public JointManagerButtonMessage(BlockPos pos, String jointName, boolean isAdd) {
		this.pos = pos;
		this.jointName = jointName;
		this.isAdd = isAdd;
	}

	public JointManagerButtonMessage(FriendlyByteBuf buffer) {
		this.pos = buffer.readBlockPos();
		this.jointName = buffer.readUtf();
		this.isAdd = buffer.readBoolean();
	}

	public void encode(FriendlyByteBuf buffer) {
		buffer.writeBlockPos(pos);
		buffer.writeUtf(jointName);
		buffer.writeBoolean(isAdd);
	}

	public void handle(Supplier<NetworkEvent.Context> ctx) {
		ctx.get().enqueueWork(() -> {
			ServerPlayer player = ctx.get().getSender();
			if (player != null) {
				Level world = player.level();
				BlockEntity blockEntity = world.getBlockEntity(pos);

				if (blockEntity instanceof PilotSeatBlockEntity pilotSeat) {
					if (isAdd) {
						pilotSeat.addJoint(jointName);
					} else {
						pilotSeat.removeJoint(jointName);
					}
				}
			}
		});
		ctx.get().setPacketHandled(true);
	}

	@SubscribeEvent
	public static void registerMessage(FMLCommonSetupEvent event) {
		IronbloodMod.addNetworkMessage(JointManagerButtonMessage.class,
				JointManagerButtonMessage::encode,
				JointManagerButtonMessage::new,
				JointManagerButtonMessage::handle);
	}
}