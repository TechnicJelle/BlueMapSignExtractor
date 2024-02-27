package com.technicjelle.bluemapsignextractor.common;

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
		final ArrayList<BlockEntity> regionBlockEntities = new ArrayList<>();
		ChunkClass chunkClass = null;
		for (int z = 0; z < 32; z++) {
			for (int x = 0; x < 32; x++) {
				final InputStream in = loadChunk(x, z);
				if (in == null) continue;
				final NBTReader reader = new NBTReader(in);

				if (chunkClass == null) { //Starts off as null. This is the first chunk we're loading.
					final ChunkWithVersion chunkWithVersion = getChunkClassFromChunk(x, z);
					if (chunkWithVersion == null) {
						throw new IOException("Failed to conclude ChunkClass from chunk at " + x + ", " + z + " in region file " + regionFile.toAbsolutePath());
					}
					chunkClass = ChunkClass.getFromDataVersion(chunkWithVersion.getDataVersion());
				}

				Chunk chunk = nbt.read(reader, chunkClass.getJavaType());
				final ChunkClass newChunkClass = ChunkClass.getFromDataVersion(chunk.getDataVersion());

				//Check if current chunk needs a different loader than the previous chunk
				if (newChunkClass.getJavaType() != chunkClass.getJavaType()) {
//					System.out.println("Chunk at " + x + ", " + z + " has a significantly different data version (" + newChunkClass.getDataVersion() + ") " +
//							"than the previous chunk (" + chunkClass.getDataVersion() + ") in this region file.\n" +
//							"\tSwitching loader from " + chunkClass.getTypeName() + " to " + newChunkClass.getTypeName() + "...");
					chunkClass = newChunkClass;
					//Load chunk again, with the new class
					//TODO: This is a bit ugly, but it's the easiest way to do it for now.
					final InputStream in2 = loadChunk(x, z);
					if (in2 == null) continue;
					final NBTReader reader2 = new NBTReader(in2);
					chunk = nbt.read(reader2, chunkClass.getJavaType());
				}

//				System.out.println("Chunk at " + x + ", " + z + ": " + chunkClass);

				if (!chunk.isGenerated()) continue;

				BlockEntity[] chunkBlockEntities = chunk.getBlockEntities();
				if (chunkBlockEntities == null) {
					throw new IOException("Chunk's BlockEntities was null in chunk " + x + ", " + z + " in region file " + regionFile.toAbsolutePath() + "\n" +
							"\t\tChunk class: " + chunkClass + "\n" +
							"\t\tChunk generation status: " + chunk.getStatus() + "\n" +
							"\t\tChunk is generated: " + chunk.isGenerated());
				}

				Collections.addAll(regionBlockEntities, chunkBlockEntities);
			}
		}

		return regionBlockEntities;
	}

	private ChunkWithVersion getChunkClassFromChunk(int x, int z) throws IOException {
		final InputStream in = loadChunk(x, z);
		if (in == null) return null;
		final NBTReader reader = new NBTReader(in);

		return nbt.read(reader, ChunkWithVersion.class);
	}

	private InputStream loadChunk(int chunkX, int chunkZ) throws IOException {
		@SuppressWarnings("resource")
		// Resource reference is returned, so it cannot be closed at the end of this method. It should be closed later on.
		final RandomAccessFile raf = new RandomAccessFile(regionFile.toFile(), "r");

		if (raf.length() == 0) return null; // Skip empty files

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
