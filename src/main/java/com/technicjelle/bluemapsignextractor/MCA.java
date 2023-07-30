package com.technicjelle.bluemapsignextractor;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

public class MCA {
	interface Compression {
		InputStream decompress(InputStream in) throws IOException;
	}

	public static InputStream loadChunk(Path regionFile, int chunkX, int chunkZ) throws IOException {
		RandomAccessFile raf = new RandomAccessFile(regionFile.toFile(), "r");

		int xzChunk = Math.floorMod(chunkZ, 32) * 32 + Math.floorMod(chunkX, 32);

		raf.seek(xzChunk * 4L);
		int offset = raf.read() << 16;
		offset |= (raf.read() & 0xFF) << 8;
		offset |= raf.read() & 0xFF;
		offset *= 4096;

		int size = raf.readByte() * 4096;
		if (size == 0) return null;

		raf.seek(offset + 4); // +4 skip chunk size

		byte compressionTypeByte = raf.readByte();
		Compression compressionType;
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
