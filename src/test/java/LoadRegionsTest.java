import com.technicjelle.bluemapsignextractor.common.BlockEntity;
import com.technicjelle.bluemapsignextractor.common.MCA;
import org.junit.Test;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

import static org.junit.Assert.assertEquals;

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
	public void test_MC_1_20_4() throws IOException {
		//Chunk [1, 5] in world at (1, 5)
		testMCAFile("/MC_1_20_4/r.0.0.mca", 38);
	}

	private void testMCAFile(String resourcePath, int amountOfSignsToExpect) throws IOException {
		final Path regionFile = Files.createTempFile(null, null);
		try (final InputStream in = LoadRegionsTest.class.getResourceAsStream(resourcePath)) {
			assert in != null;
			Files.copy(in, regionFile, StandardCopyOption.REPLACE_EXISTING);
		}

		final MCA mca = new MCA(regionFile);
		int signsFound = 0;
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

		assertEquals(amountOfSignsToExpect, signsFound);
	}
}
