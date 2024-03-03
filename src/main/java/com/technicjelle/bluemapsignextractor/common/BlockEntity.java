package com.technicjelle.bluemapsignextractor.common;

import com.flowpowered.math.vector.Vector3d;
import de.bluecolored.bluenbt.NBTName;

public abstract class BlockEntity {
	@NBTName("id")
	private String id;

	@NBTName("x")
	private int x;
	@NBTName("y")
	private int y;
	@NBTName("z")
	private int z;

	public boolean isInvalidSign() {
		return !(id.equals("minecraft:sign") || id.equals("minecraft:hanging_sign"));
	}

	public Vector3d getPosition() {
		return new Vector3d(x + 0.5, y + 0.5, z + 0.5); // center of the block
	}

	public String createKey() {
		return "sign@" + getPosition().toString();
	}

	public abstract String getFormattedHTML();

	public abstract String getLabel();
}
