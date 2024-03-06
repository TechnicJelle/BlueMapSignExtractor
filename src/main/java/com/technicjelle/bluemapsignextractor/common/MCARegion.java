/*
 * This file is part of BlueMap, licensed under the MIT License (MIT).
 *
 * Copyright (c) Blue (Lukas Rieger) <https://bluecolored.de>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package com.technicjelle.bluemapsignextractor.common;

import de.bluecolored.bluenbt.BlueNBT;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MCARegion {
	public static final String FILE_SUFFIX = ".mca";

	private static final BlueNBT nbt = new BlueNBT();

	private final Path regionFile;
	private final Logger logger;

	public MCARegion(Logger logger, Path regionFile) {
		this.regionFile = regionFile;
		this.logger = logger;
	}

	private void logDebugMessage(int x, int z, Level level, String errorMessage) {
		logger.log(level, "Problem in chunk at " + padLeft(x) + ", " + padLeft(z) + " in region file " + regionFile.toAbsolutePath() + "\n" + errorMessage);
	}

	public Collection<BlockEntity> getBlockEntities(Config config) throws IOException {
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
					int xzChunk = (z & 0b11111) << 5 | (x & 0b11111);

					int size = header[xzChunk * 4 + 3] * 4096;
					if (size == 0) continue;

					int i = xzChunk * 4;
					int offset = header[i++] << 16;
					offset |= (header[i++] & 0xFF) << 8;
					offset |= header[i] & 0xFF;
					offset *= 4096;

					if (chunkDataBuffer == null || chunkDataBuffer.length < size)
						chunkDataBuffer = new byte[size];

					channel.position(offset);
					readFully(channel, chunkDataBuffer, 0, size);

					final Compression compression;
					try {
						compression = concludeCompression(chunkDataBuffer);
					} catch (UnsupportedEncodingException e) {
						logDebugMessage(x, z, Level.SEVERE, e.getMessage());
						continue;
					}

					if (chunkClass == null) { //Starts off as null. This is the first chunk we're loading.
						final ChunkWithVersion chunkWithVersion = loadChunk(ChunkWithVersion.class, compression, chunkDataBuffer, size);
						if (chunkWithVersion == null) {
							logDebugMessage(x, z, Level.SEVERE, "Failed to conclude ChunkWithVersion! Skipping...");
							continue;
						}
						try {
							chunkClass = ChunkClass.createFromDataVersion(chunkWithVersion.getDataVersion());
						} catch (UnsupportedEncodingException e) {
							if (config.areWarningsAllowed())
								logDebugMessage(x, z, Level.WARNING, e.getMessage() + " Skipping...");
							continue;
						}
					}

					Chunk chunk = loadChunk(chunkClass.getJavaType(), compression, chunkDataBuffer, size);
					final ChunkClass newChunkClass;
					try {
						newChunkClass = ChunkClass.createFromDataVersion(chunk.getDataVersion());
					} catch (UnsupportedEncodingException e) {
						if (config.areWarningsAllowed())
							logDebugMessage(x, z, Level.WARNING, e.getMessage() + " Skipping...");
						continue;
					}

					//Check if current chunk needs a different loader than the previous chunk
					if (newChunkClass.getJavaType() != chunkClass.getJavaType()) {
						if (config.areWarningsAllowed())
							logDebugMessage(x, z, Level.FINE, "Significantly different data versions between previous and next chunks.\n" +
									"\tPrevious: " + chunkClass + "\n" +
									"\tNext: " + newChunkClass + "\n" +
									"\tSwitching loader...");
						chunkClass = newChunkClass;
						//Load chunk again, with the new class
						chunk = loadChunk(chunkClass.getJavaType(), compression, chunkDataBuffer, size);
					}

					if (config.areWarningsAllowed())
						logger.log(Level.FINEST, "Chunk at " + x + ", " + z + ": " + chunkClass);

					try {
						if (!chunk.isGenerated()) {
							if (config.areWarningsAllowed())
								logDebugMessage(x, z, Level.FINER, "Chunk is not fully generated, yet. Skipping...");
							continue;
						}
					} catch (NullPointerException e) {
						logDebugMessage(x, z, Level.SEVERE, "Failed to conclude ChunkClass due to a NullPointerException in the isGenerated() call! Skipping...\n" +
								"\tChunk class: " + chunkClass);
						continue;
					}

					BlockEntity[] chunkBlockEntities = chunk.getBlockEntities();
					if (chunkBlockEntities == null) {
						logDebugMessage(x, z, Level.SEVERE, "Chunk's BlockEntities was null! Skipping...\n" +
								"\tChunk class: " + chunkClass + "\n" +
								"\tChunk generation status: " + chunk.getStatus() + "\n" +
								"\tChunk is generated: " + chunk.isGenerated());
						continue;
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

	private static Compression concludeCompression(byte[] data) throws UnsupportedEncodingException {
		int compressionTypeId = data[4];
		switch (compressionTypeId) {
			case 0:
			case 3:
				return Compression.NONE;
			case 1:
				return Compression.GZIP;
			case 2:
				return Compression.DEFLATE;
			default:
				throw new UnsupportedEncodingException("Unknown chunk compression-id: " + compressionTypeId);
		}
	}

	private static <T> T loadChunk(Class<T> type, Compression compression, byte[] data, int size) throws IOException {
		try (InputStream in = new BufferedInputStream(compression.decompress(new ByteArrayInputStream(data, 5, size - 5)))) {
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
			if (read < 0)
				// zero out all the remaining data from the buffer
				while (bb.remaining() > 0)
					bb.put((byte) 0);
		} while (bb.remaining() > 0);
	}
}
