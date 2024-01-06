import com.technicjelle.bluemapsignextractor.common.BlockEntity;
import com.technicjelle.bluemapsignextractor.common.MCA;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.stream.Stream;

import static org.junit.Assert.assertEquals;

@SuppressWarnings({"CallToPrintStackTrace", "SameParameterValue"})
public class LoadRegionsTest {
	@Test
	public void test_MC_1_13_2() throws IOException {
		//Chunk [20, 13] in world at (-12, 13)
		testMCAFile("/MC_1_13_2/r.-1.0.mca", 1);
	}

	@Test
	public void test_MC_1_14_4() throws IOException {
		//Chunk [28, 7] in world at (-4, 7)
		testMCAFile("/MC_1_14_4/r.-1.0.mca", 2);
	}

	@Test
	public void test_MC_1_15_2() throws IOException {
		//Chunk [22, 15] in world at (-10, 15)
		testMCAFile("/MC_1_15_2/r.-1.0.mca", 1);
	}

	@Test
	public void test_MC_1_16_5() throws IOException {
		//Chunk [0, 31] in world at (0, -1)
		testMCAFile("/MC_1_16_5/r.0.-1.mca", 1);
	}

	@Test
	public void test_MC_1_17_1() throws IOException {
		//Chunk [16, 16] in world at (-16, -16)
		testMCAFile("/MC_1_17_1/r.-1.-1.mca", 2);
	}

	@Test
	public void test_MC_1_18_2() throws IOException {
		//Chunk [1, 27] in world at (-31, -37)
		testMCAFile("/MC_1_18_2/r.-1.-2.mca", 1);
	}

	@Test
	public void test_MC_1_19_4() throws IOException {
		//Chunk [30, 2] in world at (-2, -2)
		testMCAFile("/MC_1_19_4/r.-1.0.mca", 1);
	}

	@Test
	public void test_MC_1_20_4_Normal() throws IOException {
		//Chunk [1, 5] in world at (1, 5)
		testMCAFile("/MC_1_20_4/r.0.0.mca", 38);
	}

	@Test
	public void test_MC_1_20_4_Upgraded() throws IOException {
		//Chunk files that have been upgraded many times throughout the years.
		// Thanks to GitHub user @bold-gman for providing these files.
		testMCAFile("/MC_1_20_4/r.-1.-1.mca", 5);
		testMCAFile("/MC_1_20_4/r.-19.-11.mca", 1);
	}

	@Test
	public void test_MC_1_20_4_IncompleteChunks() throws IOException {
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

	/// -------------- ///
	/// Helper methods ///
	/// -------------- ///

	/**
	 * @param regionFolderName The name of the folder in src/test/resources to test the region files in
	 */
	private void testRegionFolder(final String regionFolderName) {
		Path regionFolder = Paths.get("").resolve("src/test/resources/" + regionFolderName);
		assert Files.exists(regionFolder);
		try (final Stream<Path> stream = Files.list(regionFolder)) {
			stream.filter(path -> path.toString().endsWith(".mca")).forEach(resourcePath -> testMCAFile(resourcePath, null));
		} catch (IOException e) {
			System.err.println("Error reading region folder");
			e.printStackTrace();
		}
	}

	/**
	 * @param resourcePath          The path to the region file to test
	 * @param expectedAmountOfSigns The amount of signs to expect in the region file. If null, the expected amount of signs will not be checked.
	 */
	private void testMCAFile(String resourcePath, @Nullable Integer expectedAmountOfSigns) throws IOException {
		final Path regionFile = Files.createTempFile(null, null);
		try (final InputStream in = LoadRegionsTest.class.getResourceAsStream(resourcePath)) {
			assert in != null;
			Files.copy(in, regionFile, StandardCopyOption.REPLACE_EXISTING);
		}

		testMCAFile(regionFile, expectedAmountOfSigns);
	}

	/**
	 * @param regionFile            The region file to test
	 * @param expectedAmountOfSigns The amount of signs to expect in the region file. If null, the expected amount of signs will not be checked.
	 */
	private void testMCAFile(@NotNull Path regionFile, @Nullable Integer expectedAmountOfSigns) {
		System.out.println("Processing region " + regionFile.getFileName().toString());
		final MCA mca = new MCA(regionFile);
		int signsFound = 0;

		try {
			for (BlockEntity blockEntity : mca.getBlockEntities()) {
				if (blockEntity.isInvalidSign()) continue;

				signsFound++;

				System.out.println(blockEntity.getClass().getSimpleName() + ":\n" +
						"Key: " + blockEntity.getKey() + "\n" +
						"Label: " + blockEntity.getLabel() + "\n" +
						"Position: " + blockEntity.getPosition() + "\n" +
						blockEntity.getFormattedHTML() +
						"\n\n");
			}
		} catch (IOException e) {
			System.err.println("Error reading region file");
			e.printStackTrace();
		}

		if (expectedAmountOfSigns != null)
			assertEquals(expectedAmountOfSigns.intValue(), signsFound);

		System.out.println("Successfully processed region file, and found " + signsFound + " signs\n");
	}
}
