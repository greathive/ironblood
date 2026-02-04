package net.mcreator.ironblood.ships;

import net.minecraft.util.StringRepresentable;

public enum ForceMode implements StringRepresentable {
	POSITION("position"), GLOBAL("global");

	private final String name;

	ForceMode(String name) {
		this.name = name;
	}

	@Override
	public String getSerializedName() {
		return this.name;
	}

	public ForceMode toggle() {
		return this == POSITION ? GLOBAL : POSITION;
	}
}
