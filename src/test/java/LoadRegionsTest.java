import com.technicjelle.bluemapsignextractor.common.BlockEntity;
import com.technicjelle.bluemapsignextractor.common.Config;
import com.technicjelle.bluemapsignextractor.common.Core;
import com.technicjelle.bluemapsignextractor.common.MCARegion;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import mockery.ConsoleLogger;
import mockery.MockConfig;
import org.jetbrains.annotations.Nullable;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

public class LoadRegionsTest {
	@Test
	public void test_MC_1_13_2() {
		//Chunk [20, 13] in world at (-12, 13)
		testMCAFile("/MC_1_13_2/r.-1.0.mca", 1, 0);
	}

	@Test
	public void test_MC_1_14_4() {
		//Chunk [28, 7] in world at (-4, 7)
		testMCAFile("/MC_1_14_4/r.-1.0.mca", 2, 0);
	}

	@Test
	public void test_MC_1_15_2() {
		//Chunk [22, 15] in world at (-10, 15)
		testMCAFile("/MC_1_15_2/r.-1.0.mca", 1, 0);
	}

	@Test
	public void test_MC_1_16_5() {
		//Chunk [0, 31] in world at (0, -1)
		testMCAFile("/MC_1_16_5/r.0.-1.mca", 1, 0);
	}

	@Test
	public void test_MC_1_17_1() {
		//Chunk [16, 16] in world at (-16, -16)
		testMCAFile("/MC_1_17_1/r.-1.-1.mca", 2, 0);

		//Chunk file with chunks of DataVersion 2840 in it.
		// Thanks to GitHub user @ShDis for providing this file.
		testMCAFile("/MC_1_20_4/r.282.253.mca", 0, 0);
	}

	@Test
	public void test_MC_1_18_2() {
		//Chunk [1, 27] in world at (-31, -37)
		testMCAFile("/MC_1_18_2/r.-1.-2.mca", 1, 0);
	}

	@Test
	public void test_MC_1_19_4() {
		//Chunk [30, 2] in world at (-2, -2)
		testMCAFile("/MC_1_19_4/r.-1.0.mca", 1, 0);
	}

	@Test
	public void test_MC_1_20_4_Normal() {
		//Chunk [1, 5] in world at (1, 5)
		testMCAFile("/MC_1_20_4/r.0.0.mca", 38, 0);
	}

	@Test
	public void test_MC_1_20_4_Upgraded() {
		//Chunk files that have been upgraded many times throughout the years.
		// Thanks to GitHub user @bold-gman for providing these files.
		testMCAFile("/MC_1_20_4/r.-1.-1.mca", 5, 0);
		testMCAFile("/MC_1_20_4/r.-19.-11.mca", 1, 0);
	}

	@Test
	public void test_MC_1_20_4_IncompleteChunks() {
		//Chunk files with incompletely generated chunks.
		// Thanks to GitHub user @bold-gman for providing these files.
		testMCAFile("/MC_1_20_4/r.-2.5.mca", 0, 0);
		testMCAFile("/MC_1_20_4/r.-98.8.mca", 0, 0);

		//Chunk files from the Nether with incompletely generated chunks.
		testRegionFolder("MC_1_20_4/nether");
	}

	@Test
	public void test_MC_1_20_4_SignWithCustomJSON() {
		//Chunk file with a sign that has custom JSON in it.
		//It is currently just being handled like a 1.13 sign, which ignores the special formatting that custom JSON signs can provide.
		// Thanks to Discord user @poweroffapt for providing this file.

		//Chunk [31, 31] in world at (-1, -1)
		testRegionFolder("MC_1_20_4/region_flat_world");
	}

	@Test
	public void test_MC_1_20_4_LoadFullMarkerSet() {
		Path regionFolder = getTestResource("MC_1_20_4/region_flat_world");
		Logger logger = ConsoleLogger.createLogger(regionFolder.toAbsolutePath().toString(), Level.FINE);
		MarkerSet markerSet = Core.loadMarkerSetFromWorld(logger, new MockConfig(), regionFolder);

		logger.log(Level.INFO, "MarkerSet \"" + markerSet.getLabel() + "\" contains " + markerSet.getMarkers().size() + " markers:");
		markerSet.getMarkers().forEach((key, marker) -> logger.log(Level.INFO, key + " -> " + marker.getLabel()));
		Assert.assertEquals(1, markerSet.getMarkers().size());
	}

	@Test
	public void test_MC_1_20_4_ZeroByteRegion() {
		testMCAFile("/MC_1_20_4/r.-15.18.mca", 0, 0);
	}

	@Test
	public void test_MC_1_20_4_DifferentDataVersions() {
		//Chunk file with chunks of different data versions.
		// Thanks to Discord user @alanzucconi for providing this file.

		//Chunks often switch between data version 3117 and 3463 in this region file.
		// This causes the chunk loader to switch between MC_1_20_4_Chunk and MC_1_18_2_Chunk.
		// There are sadly no different sign versions in this region file; only MC_1_17_1 signs (due to MC_1_18_2_Chunk).
		testMCAFile("/MC_1_20_4/r.-1.-2.mca", 4, 2);
	}

	/// -------------- ///
	/// Helper methods ///
	/// -------------- ///


	// --- Public --- //

	public static Path getTestResource(String resourcePath) {
		return Paths.get("").resolve("src/test/resources/" + resourcePath);
	}

	/**
	 * @param regionFolderName The name of the folder in src/test/resources to test the region files in
	 */
	public static void testRegionFolder(final String regionFolderName) {
		Logger logger = ConsoleLogger.createLogger(regionFolderName, Level.FINE);
		Config config = new MockConfig();
		Path regionFolder = getTestResource(regionFolderName);
		assert Files.exists(regionFolder);
		try (final Stream<Path> stream = Files.list(regionFolder)) {
			stream.filter(path -> path.toString().endsWith(MCARegion.FILE_SUFFIX)).forEach(resourcePath -> testMCAFile(logger, config, resourcePath, null, null));
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error reading region folder", e);
		}
	}

	/**
	 * @param resourcePath                 The path to the region file to test
	 * @param expectedAmountOfSigns        The amount of signs to expect in the region file. If null, the expected amount of signs will not be checked.
	 * @param expectedAmountOfBlankSigns   The amount of blank signs to expect in the region file. If null, the expected amount of blank signs will not be checked.
	 */
	public static void testMCAFile(String resourcePath, @Nullable Integer expectedAmountOfSigns, @Nullable Integer expectedAmountOfBlankSigns) {
		Logger logger = ConsoleLogger.createLogger(resourcePath, Level.FINE);
		Config config = new MockConfig();
		Path regionFile = getTestResource(resourcePath);
		testMCAFile(logger, config, regionFile, expectedAmountOfSigns, expectedAmountOfBlankSigns);
	}

	// --- Private --- //

	/**
	 * @param regionFile                 The region file to test
	 * @param expectedAmountOfSigns      The amount of signs to expect in the region file. If null, the expected amount of signs will not be checked.
	 * @param expectedAmountOfBlankSigns The amount of blank signs to expect in the region file. If null, the expected amount of blank signs will not be checked.
	 */
	private static void testMCAFile(Logger logger, Config config, Path regionFile, @Nullable Integer expectedAmountOfSigns, @Nullable Integer expectedAmountOfBlankSigns) {
		logger.log(Level.INFO, "Processing region " + regionFile.getFileName().toString());
		final MCARegion mcaRegion = new MCARegion(logger, regionFile);
		int signsFound = 0;
		int blankSignsFound = 0;

		try {
			for (BlockEntity blockEntity : mcaRegion.getBlockEntities(config)) {
				if (blockEntity.isInvalidSign()) continue;
				if (blockEntity.isBlank()) blankSignsFound++;

				signsFound++;

				logger.log(Level.CONFIG, blockEntity.getClass().getSimpleName() + ":\n" +
						"Key: " + blockEntity.createKey() + "\n" +
						"Label: " + blockEntity.getLabel() + "\n" +
						"Position: " + blockEntity.getPosition() + "\n" +
						blockEntity.getFormattedHTML() +
						"\n\n");
			}
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Error reading region file", e);
		}

		if (expectedAmountOfSigns != null)
			Assert.assertEquals(expectedAmountOfSigns.intValue(), signsFound);

		if (expectedAmountOfBlankSigns != null)
			Assert.assertEquals(expectedAmountOfBlankSigns.intValue(), blankSignsFound);

		logger.log(Level.INFO, "Successfully processed region file, and found " + signsFound + " signs\n");
	}
}
