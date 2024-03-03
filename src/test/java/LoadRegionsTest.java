import com.technicjelle.bluemapsignextractor.common.BlockEntity;
import com.technicjelle.bluemapsignextractor.common.Core;
import com.technicjelle.bluemapsignextractor.common.MCA;
import de.bluecolored.bluemap.api.markers.MarkerSet;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@SuppressWarnings("CallToPrintStackTrace")
public class LoadRegionsTest {
	@Test
	public void test_MC_1_13_2() {
		//Chunk [20, 13] in world at (-12, 13)
		testMCAFile("/MC_1_13_2/r.-1.0.mca", 1);
	}

	@Test
	public void test_MC_1_14_4() {
		//Chunk [28, 7] in world at (-4, 7)
		testMCAFile("/MC_1_14_4/r.-1.0.mca", 2);
	}

	@Test
	public void test_MC_1_15_2() {
		//Chunk [22, 15] in world at (-10, 15)
		testMCAFile("/MC_1_15_2/r.-1.0.mca", 1);
	}

	@Test
	public void test_MC_1_16_5() {
		//Chunk [0, 31] in world at (0, -1)
		testMCAFile("/MC_1_16_5/r.0.-1.mca", 1);
	}

	@Test
	public void test_MC_1_17_1() {
		//Chunk [16, 16] in world at (-16, -16)
		testMCAFile("/MC_1_17_1/r.-1.-1.mca", 2);
	}

	@Test
	public void test_MC_1_18_2() {
		//Chunk [1, 27] in world at (-31, -37)
		testMCAFile("/MC_1_18_2/r.-1.-2.mca", 1);
	}

	@Test
	public void test_MC_1_19_4() {
		//Chunk [30, 2] in world at (-2, -2)
		testMCAFile("/MC_1_19_4/r.-1.0.mca", 1);
	}

	@Test
	public void test_MC_1_20_4_Normal() {
		//Chunk [1, 5] in world at (1, 5)
		testMCAFile("/MC_1_20_4/r.0.0.mca", 38);
	}

	@Test
	public void test_MC_1_20_4_Upgraded() {
		//Chunk files that have been upgraded many times throughout the years.
		// Thanks to GitHub user @bold-gman for providing these files.
		testMCAFile("/MC_1_20_4/r.-1.-1.mca", 5);
		testMCAFile("/MC_1_20_4/r.-19.-11.mca", 1);
	}

	@Test
	public void test_MC_1_20_4_IncompleteChunks() {
		//Chunk files with incompletely generated chunks.
		// Thanks to GitHub user @bold-gman for providing these files.
		testMCAFile("/MC_1_20_4/r.-2.5.mca", 0);
		testMCAFile("/MC_1_20_4/r.-98.8.mca", 0);

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
		MarkerSet markerSet = Core.loadMarkerSetFromWorld(Logger.getLogger("test"), regionFolder);

		System.out.println(markerSet);
		markerSet.getMarkers().forEach((key, marker) -> System.out.println(key + " -> " + marker.getLabel()));
		assertEquals(1, markerSet.getMarkers().size());
	}

	@Test
	public void test_MC_1_20_4_ZeroByteRegion() {
		testMCAFile("/MC_1_20_4/r.-15.18.mca", 0);
	}

	@Test
	public void test_MC_1_20_4_DifferentDataVersions() {
		//Chunk file with chunks of different data versions.
		// Thanks to Discord user @alanzucconi for providing this file.

		//Chunks often switch between data version 3117 and 3463 in this region file.
		// This causes the chunk loader to switch between MC_1_20_4_Chunk and MC_1_18_2_Chunk.
		// There are sadly no different sign versions in this region file; only MC_1_17_1 signs (due to MC_1_18_2_Chunk).
		testMCAFile("/MC_1_20_4/r.-1.-2.mca", 4);
	}

	/// -------------- ///
	/// Helper methods ///
	/// -------------- ///

	public static Path getTestResource(String resourcePath) {
		return Paths.get("").resolve("src/test/resources/" + resourcePath);
	}

	/**
	 * @param regionFolderName The name of the folder in src/test/resources to test the region files in
	 */
	public static void testRegionFolder(final String regionFolderName) {
		Path regionFolder = getTestResource(regionFolderName);
		assert Files.exists(regionFolder);
		try (final Stream<Path> stream = Files.list(regionFolder)) {
			stream.filter(path -> path.toString().endsWith(".mca")).forEach(resourcePath -> testMCAFile(resourcePath, null));
		} catch (IOException e) {
			System.err.println("Error reading region folder:");
			e.printStackTrace();
		}
	}

	/**
	 * @param resourcePath          The path to the region file to test
	 * @param expectedAmountOfSigns The amount of signs to expect in the region file. If null, the expected amount of signs will not be checked.
	 */
	public static void testMCAFile(String resourcePath, @Nullable Integer expectedAmountOfSigns) {
		Path regionFile = getTestResource(resourcePath);
		testMCAFile(regionFile, expectedAmountOfSigns);
	}

	/**
	 * @param regionFile            The region file to test
	 * @param expectedAmountOfSigns The amount of signs to expect in the region file. If null, the expected amount of signs will not be checked.
	 */
	public static void testMCAFile(@NotNull Path regionFile, @Nullable Integer expectedAmountOfSigns) {
		System.out.println("Processing region " + regionFile.getFileName().toString());
		final MCA mca = new MCA(regionFile);
		int signsFound = 0;

		try {
			for (BlockEntity blockEntity : mca.getBlockEntities()) {
				if (blockEntity.isInvalidSign()) continue;

				signsFound++;

				System.out.println(blockEntity.getClass().getSimpleName() + ":\n" +
						"Key: " + blockEntity.createKey() + "\n" +
						"Label: " + blockEntity.getLabel() + "\n" +
						"Position: " + blockEntity.getPosition() + "\n" +
						blockEntity.getFormattedHTML() +
						"\n\n");
			}
		} catch (IOException e) {
			System.err.println("Error reading region file:");
			e.printStackTrace();
		}

		if (expectedAmountOfSigns != null)
			assertEquals(expectedAmountOfSigns.intValue(), signsFound);

		System.out.println("Successfully processed region file, and found " + signsFound + " signs\n");
	}
}
