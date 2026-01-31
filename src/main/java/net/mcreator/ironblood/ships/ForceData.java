package net.mcreator.ironblood.ships;

import org.joml.Vector3d;

public class ForceData {
	public final Vector3d dir;
	public volatile double throttle;
	public volatile ForceMode mode;
	public volatile ForceDirectionMode dirMode;

	public ForceData(Vector3d dir, double throttle, ForceMode mode, ForceDirectionMode dirMode) {
		this.dir = dir;
		this.throttle = throttle;
		this.mode = mode;
		this.dirMode = dirMode;
	}

	public String toString() {
		return "Direction: " + this.dir + " Throttle: " + this.throttle + " Mode: " + this.mode + "Direction mode: " + this.dirMode;
	}
}
