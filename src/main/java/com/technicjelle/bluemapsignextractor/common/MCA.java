package com.technicjelle.bluemapsignextractor.common;

import com.technicjelle.bluemapsignextractor.versions.MC_1_13_2.MC_1_13_2_Chunk;
import com.technicjelle.bluemapsignextractor.versions.MC_1_14_4.MC_1_14_4_Chunk;
import com.technicjelle.bluemapsignextractor.versions.MC_1_17_1.MC_1_17_1_Chunk;
import com.technicjelle.bluemapsignextractor.versions.MC_1_18_2.MC_1_18_2_Chunk;
import com.technicjelle.bluemapsignextractor.versions.MC_1_20_4.MC_1_20_4_Chunk;
import de.bluecolored.bluenbt.BlueNBT;
import de.bluecolored.bluenbt.NBTReader;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class MCA {
	private static final BlueNBT nbt = new BlueNBT();

	interface Compression {
		InputStream decompress(InputStream in) throws IOException;
	}

	final Path regionFile;

	public MCA(Path regionFile) {
		this.regionFile = regionFile;
	}

	public ArrayList<BlockEntity> getBlockEntities() throws IOException {
		final ArrayList<BlockEntity> blockEntities = new ArrayList<>();
		Class<? extends Chunk> chunkClass = null;
		for (int z = 0; z < 32; z++) {
			for (int x = 0; x < 32; x++) {
				final InputStream in = loadChunk(x, z);
				if (in == null) continue;
				final NBTReader reader = new NBTReader(in);

				if (chunkClass == null) {
					final ChunkWithVersion chunkWithVersion = getChunkClassFromChunk(x, z);
					if (chunkWithVersion == null) {
						throw new IOException("Failed to conclude ChunkClass from chunk at " + x + ", " + z);
					}
					chunkClass = getChunkClassFromDataVersion(chunkWithVersion.getDataVersion());
				}

				Chunk chunk = nbt.read(reader, chunkClass);
				final Class<? extends Chunk> newChunkClass = getChunkClassFromDataVersion(chunk.getDataVersion());

				if (newChunkClass != chunkClass) {
					System.err.println("Chunk at " + x + ", " + z + " has a different data version than the first chunk in this region file. Switching...");
					chunkClass = newChunkClass;
					//Load chunk again, with the new class
					//TODO: This is a bit ugly, but it's the easiest way to do it for now.
					final InputStream in2 = loadChunk(x, z);
					if (in2 == null) continue;
					final NBTReader reader2 = new NBTReader(in2);
					chunk = nbt.read(reader2, chunkClass);
				}

				Collections.addAll(blockEntities, chunk.getBlockEntities());
			}
		}

		return blockEntities;
	}

	private ChunkWithVersion getChunkClassFromChunk(int x, int z) throws IOException {
		final InputStream in = loadChunk(x, z);
		if (in == null) return null;
		final NBTReader reader = new NBTReader(in);

		return nbt.read(reader, ChunkWithVersion.class);
	}

	private Class<? extends Chunk> getChunkClassFromDataVersion(int dataVersion) throws IOException {
		final Class<? extends Chunk> chunkClass;
		if (dataVersion >= 3463) {
			chunkClass = MC_1_20_4_Chunk.class;
		} else if (intInRange(dataVersion, 2825, 3337)) {
			//For versions:
			// - 1.18.2
			// - 1.19.4
			chunkClass = MC_1_18_2_Chunk.class;
		} else if (intInRange(dataVersion, 2724, 2730)) {
			chunkClass = MC_1_17_1_Chunk.class;
		} else if (intInRange(dataVersion, 1901, 2586)) {
			//For versions:
			// - 1.14.4
			// - 1.15.2
			// - 1.16.5
			chunkClass = MC_1_14_4_Chunk.class;
		} else if (intInRange(dataVersion, 1444, 1631)) {
			chunkClass = MC_1_13_2_Chunk.class;
		} else {
			throw new IOException("Unknown data version: " + dataVersion);
		}

		return chunkClass;
	}

	private static boolean intInRange(int value, int min, int max) {
		return value >= min && value <= max;
	}

	private InputStream loadChunk(int chunkX, int chunkZ) throws IOException {
		@SuppressWarnings("resource")
		// Resource reference is returned, so it cannot be closed at the end of this method. It should be closed later on.
		final RandomAccessFile raf = new RandomAccessFile(regionFile.toFile(), "r");

		final int xzChunk = Math.floorMod(chunkZ, 32) * 32 + Math.floorMod(chunkX, 32);

		raf.seek(xzChunk * 4L);
		int offset = raf.read() << 16;
		offset |= (raf.read() & 0xFF) << 8;
		offset |= raf.read() & 0xFF;
		offset *= 4096;

		final int size = raf.readByte() * 4096;
		if (size == 0) return null;

		raf.seek(offset + 4); // +4 skip chunk size

		final byte compressionTypeByte = raf.readByte();
		final Compression compressionType;
		switch (compressionTypeByte) {
			case 1:
				compressionType = GZIPInputStream::new;
				break;
			case 2:
				compressionType = InflaterInputStream::new;
				break;
			default:
				throw new IOException("Unknown compression type: " + compressionTypeByte);
		}

		return compressionType.decompress(new FileInputStream(raf.getFD()));
	}
}
