package com.technicjelle.bluemapsignextractor.common;

import de.bluecolored.bluenbt.NBTName;

public class ChunkWithVersion {
	@NBTName("DataVersion")
	private int dataVersion;

	public int getDataVersion() {
		return dataVersion;
	}
}
