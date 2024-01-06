package com.technicjelle.bluemapsignextractor.versions.MC_1_20_4;

import com.technicjelle.bluemapsignextractor.common.BlockEntity;
import com.technicjelle.bluemapsignextractor.common.Chunk;
import com.technicjelle.bluemapsignextractor.common.ChunkWithVersion;
import de.bluecolored.bluenbt.NBTName;

public class MC_1_20_4_Chunk extends ChunkWithVersion implements Chunk {
	@NBTName("block_entities")
	private MC_1_20_4_Sign[] blockEntities;

	@NBTName("Status")
	private String status;

	@Override
	public BlockEntity[] getBlockEntities() {
		return blockEntities;
	}

	@Override
	public String getStatus() {
		return status;
	}
}
