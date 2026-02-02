package net.mcreator.ironblood.procedures;

import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.api.ships.LoadedServerShip;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.event.level.BlockEvent;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.core.BlockPos;

import net.mcreator.ironblood.ships.JointUtil;
import net.mcreator.ironblood.ships.JointTrackingAttachment;
import net.mcreator.ironblood.init.IronbloodModBlocks;

@Mod.EventBusSubscriber
public class MechanicalJointBlockBreakProcedure {
	@SubscribeEvent
	public static void onBlockBreak(BlockEvent.BreakEvent event) {
		if (event.getLevel().isClientSide()) {
			return;
		}
		
		BlockPos pos = event.getPos();
		BlockState state = event.getState();
		Level level = (Level) event.getLevel();
		
		// Check if the broken block is a mechanical joint
		if (state.getBlock() == IronbloodModBlocks.MECHANICAL_JOINT.get() || 
		    state.getBlock() == IronbloodModBlocks.MECHANICAL_JOINT_ALT.get()) {
			
			if (level instanceof ServerLevel serverLevel) {
				// Get the ship managing this block position
				LoadedServerShip ship = VSGameUtilsKt.getLoadedShipManagingPos(serverLevel, pos);
				
				if (ship != null) {
					// Get the joint tracking attachment
					JointTrackingAttachment tracking = JointTrackingAttachment.getOrCreate(ship);
					
					// Check if there's a joint at this position
					Integer jointId = tracking.getJointIdAtPosition(pos);
					
					if (jointId != null) {
						// Remove the joint
						JointUtil.removeJointById(level, jointId);
						
						// Remove from tracking
						tracking.removeJoint(jointId);
						
						// Also try to remove from the connected ship's tracking
						// (we don't know which ship it is, but the joint removal handles that)
					}
				}
			}
		}
	}
}