package net.mcreator.ironblood.ships;

import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.common.util.GameToPhysicsAdapter;
import org.valkyrienskies.mod.common.ValkyrienSkiesMod;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.core.internal.joints.VSRevoluteJoint;
import org.valkyrienskies.core.internal.joints.VSJointPose;
import org.valkyrienskies.core.internal.joints.VSJointAndId;
import org.valkyrienskies.core.internal.joints.VSJoint;
import org.valkyrienskies.core.internal.joints.VSFixedJoint;
import org.valkyrienskies.core.internal.joints.VSDistanceJoint;
import org.valkyrienskies.core.internal.joints.VSD6Joint;
import org.valkyrienskies.core.impl.shadow.zq;
import org.valkyrienskies.core.impl.shadow.yq;
import org.valkyrienskies.core.impl.shadow.xq;
import org.valkyrienskies.core.impl.shadow.sz;
import org.valkyrienskies.core.impl.shadow.sy;
import org.valkyrienskies.core.impl.shadow.sx;
import org.valkyrienskies.core.impl.shadow.qz;
import org.valkyrienskies.core.impl.shadow.qy;
import org.valkyrienskies.core.impl.shadow.qx;
import org.valkyrienskies.core.impl.shadow.qw;
import org.valkyrienskies.core.impl.shadow.id;
import org.valkyrienskies.core.impl.shadow.cz;
import org.valkyrienskies.core.impl.shadow.cy;
import org.valkyrienskies.core.impl.shadow.cx;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.api.ships.LoadedServerShip;

import org.joml.Vector3dc;
import org.joml.Vector3d;
import org.joml.Quaterniondc;
import org.joml.Quaterniond;

import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;

import java.util.function.Consumer;

public class JointUtil {
	public static VSJoint makeFixedJoint(@Nullable ServerShip shipA, @Nullable ServerShip shipB, Vec3 rotationA, Vec3 rotationB, Vec3 positionA, Vec3 positionB) {
		// Sussy
		Long bodyId1 = shipA != null ? shipA.getId() : null;
		Long bodyId2 = shipB != null ? shipB.getId() : null;
		Quaterniondc rotationAQuat = eulerXYZToQuaternion(rotationA.x(), rotationA.y(), rotationA.z());
		Quaterniondc rotationBQuat = eulerXYZToQuaternion(rotationB.x(), rotationB.y(), rotationB.z());
		VSJointPose poseA = new VSJointPose(VectorConversionsMCKt.toJOML(positionA), rotationAQuat);
		VSJointPose poseB = new VSJointPose(VectorConversionsMCKt.toJOML(positionB), rotationBQuat);
		return new VSFixedJoint(bodyId1, poseA, bodyId2, poseB, null, 1.0E-10);
	}

	public static VSJoint makeDistanceJoint(@Nullable ServerShip shipA, @Nullable ServerShip shipB, Vec3 rotationA, Vec3 rotationB, Vec3 positionA, Vec3 positionB, @Nullable Number minDistance, @Nullable Number maxDistance) {
		// Sussy
		Long bodyId1 = shipA != null ? shipA.getId() : null;
		Long bodyId2 = shipB != null ? shipB.getId() : null;
		Quaterniondc rotationAQuat = eulerXYZToQuaternion(rotationA.x(), rotationA.y(), rotationA.z());
		Quaterniondc rotationBQuat = eulerXYZToQuaternion(rotationB.x(), rotationB.y(), rotationB.z());
		VSJointPose poseA = new VSJointPose(VectorConversionsMCKt.toJOML(positionA), rotationAQuat);
		VSJointPose poseB = new VSJointPose(VectorConversionsMCKt.toJOML(positionB), rotationBQuat);
		Float minDist = minDistance != null ? minDistance.floatValue() : null;
		Float maxDist = maxDistance != null ? maxDistance.floatValue() : null;
		return new VSDistanceJoint(bodyId1, poseA, bodyId2, poseB, null, VSJoint.DEFAULT_COMPLIANCE, minDist, maxDist, null, null, null);
	}

	public static VSJoint makeRevoluteJoint(@Nullable ServerShip shipA, @Nullable ServerShip shipB, Vec3 rotationA, Vec3 rotationB, Vec3 positionA, Vec3 positionB, @Nullable Number lowerLimit, @Nullable Number upperLimit) {
		// Sussy
		Long bodyId1 = shipA != null ? shipA.getId() : null;
		Long bodyId2 = shipB != null ? shipB.getId() : null;
		Quaterniondc rotationAQuat = eulerXYZToQuaternion(rotationA.x(), rotationA.y(), rotationA.z());
		Quaterniondc rotationBQuat = eulerXYZToQuaternion(rotationB.x(), rotationB.y(), rotationB.z());
		VSJointPose poseA = new VSJointPose(VectorConversionsMCKt.toJOML(positionA), rotationAQuat);
		VSJointPose poseB = new VSJointPose(VectorConversionsMCKt.toJOML(positionB), rotationBQuat);
		VSD6Joint.AngularLimitPair limits = new VSD6Joint.AngularLimitPair(lowerLimit != null ? lowerLimit.floatValue() : Float.NEGATIVE_INFINITY, upperLimit != null ? upperLimit.floatValue() : Float.POSITIVE_INFINITY, null, null, null, null);
		VSRevoluteJoint.VSRevoluteDriveVelocity driveVelocity = new VSRevoluteJoint.VSRevoluteDriveVelocity(0.0F, false);
		return new VSRevoluteJoint(bodyId1, poseA, bodyId2, poseB, null, VSJoint.DEFAULT_COMPLIANCE, limits, driveVelocity, null, null, true);
	}

	public static void addJoint(Level level, VSJoint joint, Consumer<Integer> idCallback) {
		GameToPhysicsAdapter gtpa = ValkyrienSkiesMod.getOrCreateGTPA(VSGameUtilsKt.getDimensionId(level));
		gtpa.addJoint(joint, 0, idCallback);
	}

	public static void removeJointById(Level level, int id) {
		GameToPhysicsAdapter gtpa = ValkyrienSkiesMod.getOrCreateGTPA(VSGameUtilsKt.getDimensionId(level));
		gtpa.removeJoint(id);
	}

	public static @Nullable VSJoint getJointById(Level level, int id) {
		GameToPhysicsAdapter gtpa = ValkyrienSkiesMod.getOrCreateGTPA(VSGameUtilsKt.getDimensionId(level));
		return gtpa.getJointById(id);
	}

	public static void updateJoint(Level level, int id, VSJoint joint) {
		GameToPhysicsAdapter gtpa = ValkyrienSkiesMod.getOrCreateGTPA(VSGameUtilsKt.getDimensionId(level));
		gtpa.updateJoint(new VSJointAndId(id, joint));
	}

	public static @Nullable LoadedServerShip getShipFromJoint(Level level, VSJoint joint, boolean firstShip) {
		if (joint == null)
			return null;
		Long id = firstShip ? joint.getShipId0() : joint.getShipId1();
		if (id == null)
			return null;
		if (level instanceof ServerLevel) {
			return VSGameUtilsKt.getShipObjectWorld((ServerLevel) level).getLoadedShips().getById(id);
		}
		return null;
	}

	public static Vec3 getPosFromJoint(VSJoint joint, boolean firstPos) {
		if (joint == null)
			return new Vec3(0, 0, 0);
		Vector3dc pos = firstPos ? joint.getPose0().getPos() : joint.getPose1().getPos();
		return VectorConversionsMCKt.toMinecraft(pos);
	}

	public static Vec3 getRotFromJoint(VSJoint joint, boolean firstRot) {
		if (joint == null)
			return new Vec3(0, 0, 0);
		Quaterniondc rot = firstRot ? joint.getPose0().getRot() : joint.getPose1().getRot();
		return VectorConversionsMCKt.toMinecraft(quaternionToEulerXYZ(rot));
	}

	// ChatGPT slop, seems to work
	private static Quaterniondc eulerXYZToQuaternion(double x, double y, double z) {
		var cx = Math.cos(x * 0.5);
		var sx = Math.sin(x * 0.5);
		var cy = Math.cos(y * 0.5);
		var sy = Math.sin(y * 0.5);
		var cz = Math.cos(z * 0.5);
		var sz = Math.sin(z * 0.5);
		var qw = cx * cy * cz - sx * sy * sz;
		var qx = sx * cy * cz + cx * sy * sz;
		var qy = cx * sy * cz - sx * cy * sz;
		var qz = cx * cy * sz + sx * sy * cz;
		return new Quaterniond(qx, qy, qz, qw);
	}

	private static Vector3d quaternionToEulerXYZ(Quaterniondc quat) {
		// Normalize defensively
		var len = Math.sqrt(quat.x() * quat.x() + quat.y() * quat.y() + quat.z() * quat.z() + quat.w() * quat.w());
		var xq = quat.x() / len;
		var yq = quat.y() / len;
		var zq = quat.z() / len;
		var wq = quat.w() / len;
		var x = Math.atan2(2.0 * (wq * xq - yq * zq), 1.0 - 2.0 * (xq * xq + yq * yq));
		var y = Math.asin(Math.max(-1.0, Math.min((2.0 * (wq * yq + xq * zq)), 1.0)));
		var z = Math.atan2(2.0 * (wq * zq - xq * yq), 1.0 - 2.0 * (yq * yq + zq * zq));
		return new Vector3d(x, y, z);
	}
}
