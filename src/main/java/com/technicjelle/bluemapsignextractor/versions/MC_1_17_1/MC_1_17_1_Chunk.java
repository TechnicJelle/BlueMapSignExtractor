package com.technicjelle.bluemapsignextractor.versions.MC_1_17_1;

import com.technicjelle.bluemapsignextractor.common.BlockEntity;
import com.technicjelle.bluemapsignextractor.common.Chunk;
import com.technicjelle.bluemapsignextractor.common.ChunkWithVersion;
import de.bluecolored.bluenbt.NBTName;

public class MC_1_17_1_Chunk extends ChunkWithVersion implements Chunk {
	static class Level {
		@NBTName("TileEntities")
		public MC_1_17_1_Sign[] tileEntities;
	}

	@NBTName("Level")
	private Level level;

	@Override
	public BlockEntity[] getBlockEntities() {
		return level.tileEntities;
	}
}
