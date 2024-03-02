//Adapted a lot from https://github.com/TBlueF/NBTLibraryComparison/blob/f1e0c878ec91bc99c385e32a7b38d8380e02583c/src/main/java/de/bluecolored/nbtlibtest/NBTLibrary.java
package com.technicjelle.bluemapsignextractor.common;

import de.bluecolored.bluenbt.BlueNBT;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public class MCA {
	private static final BlueNBT nbt = new BlueNBT();

	final Path regionFile;

	public MCA(Path regionFile) {
		this.regionFile = regionFile;
	}

	public Collection<BlockEntity> getBlockEntities() throws IOException {
		if (Files.notExists(regionFile)) return Collections.emptyList();

		long fileLength = Files.size(regionFile);
		if (fileLength == 0) return Collections.emptyList();

		final ArrayList<BlockEntity> regionBlockEntities = new ArrayList<>();
		try (FileChannel channel = FileChannel.open(regionFile, StandardOpenOption.READ)) {
			byte[] header = new byte[1024 * 8];
			byte[] chunkDataBuffer = null;

			// read the header
			readFully(channel, header, 0, header.length);

			// iterate over all chunks
			ChunkClass chunkClass = null;
			for (int z = 0; z < 32; z++) {
				for (int x = 0; x < 32; x++) {
					int xzChunk = z * 32 + x;

					int size = header[xzChunk * 4 + 3] * 4096;
					if (size == 0) continue;

					int i = xzChunk * 4;
					int offset = header[i++] << 16;
					offset |= (header[i++] & 0xFF) << 8;
					offset |= header[i] & 0xFF;
					offset *= 4096;

					if (chunkDataBuffer == null || chunkDataBuffer.length < size)
						chunkDataBuffer = new byte[size];

					if (chunkClass == null) { //Starts off as null. This is the first chunk we're loading.
						final ChunkWithVersion chunkWithVersion = loadChunk(ChunkWithVersion.class, channel, offset, size, chunkDataBuffer);
						if (chunkWithVersion == null) {
							throw new IOException("Failed to conclude ChunkClass from chunk at " + padLeft(x) + ", " + padLeft(z) + " in region file " + regionFile.toAbsolutePath());
						}
						chunkClass = ChunkClass.getFromDataVersion(chunkWithVersion.getDataVersion());
					}

					Chunk chunk = loadChunk(chunkClass.getJavaType(), channel, offset, size, chunkDataBuffer);
					final ChunkClass newChunkClass = ChunkClass.getFromDataVersion(chunk.getDataVersion());

					//Check if current chunk needs a different loader than the previous chunk
					if (newChunkClass.getJavaType() != chunkClass.getJavaType()) {
//						System.out.println("Chunk at " + padLeft(x) + ", " + padLeft(z) + " has a significantly different data version (" + newChunkClass.getDataVersion() + ") " +
//								"than the previous chunk (" + chunkClass.getDataVersion() + ") in this region file.\n" +
//								"\tSwitching loader from " + chunkClass.getTypeName() + " to " + newChunkClass.getTypeName() + "...");
						chunkClass = newChunkClass;
						//Load chunk again, with the new class
						chunk = loadChunk(chunkClass.getJavaType(), channel, offset, size, chunkDataBuffer);
					}

//					System.out.println("Chunk at " + x + ", " + z + ": " + chunkClass);

					if (!chunk.isGenerated()) continue;

					BlockEntity[] chunkBlockEntities = chunk.getBlockEntities();
					if (chunkBlockEntities == null) {
						throw new IOException("Chunk's BlockEntities was null in chunk " + padLeft(x) + ", " + padLeft(z) + " in region file " + regionFile.toAbsolutePath() + "\n" +
								"\t\tChunk class: " + chunkClass + "\n" +
								"\t\tChunk generation status: " + chunk.getStatus() + "\n" +
								"\t\tChunk is generated: " + chunk.isGenerated());
					}

					Collections.addAll(regionBlockEntities, chunkBlockEntities);
				}
			}
		}

		return regionBlockEntities;
	}

	private static final String format = "%1$2s";

	public static String padLeft(int i) {
		return String.format(format, i);
	}

	private static <T> T loadChunk(Class<T> type, FileChannel channel, int offset, int size, byte[] dataBuffer) throws IOException {
		channel.position(offset);
		readFully(channel, dataBuffer, 0, size);

		int compressionTypeId = dataBuffer[4];
		Compression compression;
		switch (compressionTypeId) {
			case 0:
			case 3:
				compression = Compression.NONE;
				break;
			case 1:
				compression = Compression.GZIP;
				break;
			case 2:
				compression = Compression.DEFLATE;
				break;
			default:
				throw new IOException("Unknown chunk compression-id: " + compressionTypeId);
		}

		try (InputStream in = new BufferedInputStream(compression.decompress(new ByteArrayInputStream(dataBuffer, 5, size - 5)))) {
			return loadChunk(type, in);
		}
	}

	private static <T> T loadChunk(Class<T> type, InputStream in) throws IOException {
		return nbt.read(in, type);
	}

	@SuppressWarnings("SameParameterValue")
	private static void readFully(ReadableByteChannel src, byte[] dst, int off, int len) throws IOException {
		readFully(src, ByteBuffer.wrap(dst), off, len);
	}

	private static void readFully(ReadableByteChannel src, ByteBuffer bb, int off, int len) throws IOException {
		int limit = off + len;
		if (limit > bb.capacity()) throw new IllegalArgumentException("buffer too small");

		bb.limit(limit);
		bb.position(off);

		do {
			int read = src.read(bb);
			if (read < 0) throw new EOFException();
		} while (bb.remaining() > 0);
	}
}
