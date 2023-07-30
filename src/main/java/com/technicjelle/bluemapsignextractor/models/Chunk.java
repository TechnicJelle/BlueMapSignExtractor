package com.technicjelle.bluemapsignextractor.models;

import de.bluecolored.bluenbt.NBTName;

public class Chunk {
	@NBTName("block_entities")
	public BlockEntity[] blockEntities;
}
