package com.technicjelle.bluemapsignextractor.versions.MC_1_18_2;

import com.technicjelle.bluemapsignextractor.common.BlockEntity;
import com.technicjelle.bluemapsignextractor.common.Chunk;
import com.technicjelle.bluemapsignextractor.common.ChunkWithVersion;
import com.technicjelle.bluemapsignextractor.versions.MC_1_17_1.MC_1_17_1_Sign;
import de.bluecolored.bluenbt.NBTName;

public class MC_1_18_2_Chunk extends ChunkWithVersion implements Chunk {
	@NBTName("block_entities")
	private MC_1_17_1_Sign[] blockEntities; //The actual sign format hasn't changed, only the chunk format has

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
