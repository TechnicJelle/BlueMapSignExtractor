import com.technicjelle.bluemapsignextractor.common.BlockEntity;
import com.technicjelle.bluemapsignextractor.common.MCA;
import com.technicjelle.bluemapsignextractor.versions.MC_1_13_2.MC_1_13_2_Sign;
import com.technicjelle.bluemapsignextractor.versions.MC_1_14_4.MC_1_14_4_Sign;
import com.technicjelle.bluemapsignextractor.versions.MC_1_17_1.MC_1_17_1_Sign;
import com.technicjelle.bluemapsignextractor.versions.MC_1_20_4.MC_1_20_4_Sign;
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
		testMCAFile("/MC_1_20_4/r.0.0.mca", 6);
	}

	private void testMCAFile(String resourcePath, int amountOfSignsToExpect) throws IOException {
		Path regionFile = Files.createTempFile(null, null);
		try (InputStream in = LoadRegionsTest.class.getResourceAsStream(resourcePath)) {
			assert in != null;
			Files.copy(in, regionFile, StandardCopyOption.REPLACE_EXISTING);
		}

		MCA mca = new MCA(regionFile);
		int signsFound = 0;
		for (BlockEntity blockEntity : mca.getBlockEntities()) {
			if (blockEntity.isInvalidSign()) continue;

			signsFound++;

			if (blockEntity instanceof MC_1_20_4_Sign) {
				MC_1_20_4_Sign sign = (MC_1_20_4_Sign) blockEntity;
				System.out.println("1.20.4 Sign:\nPosition:" + sign.getPosition() + "\n" + sign.getAllSignMessages());
			} else if (blockEntity instanceof MC_1_17_1_Sign) {
				MC_1_17_1_Sign sign = (MC_1_17_1_Sign) blockEntity;
				System.out.println("1.17.1 Sign:\nPosition: " + sign.getPosition() + "\nColour: " + sign.getColour() + "\nGlowing: " + sign.isGlowing() + "\n---\n" + sign.getAllSignMessages() + "\n===");
			} else if (blockEntity instanceof MC_1_14_4_Sign) {
				MC_1_14_4_Sign sign = (MC_1_14_4_Sign) blockEntity;
				System.out.println("1.14.4 Sign:\nPosition: " + sign.getPosition() + "\nColour: " + sign.getColour() + "\n---\n" + sign.getAllSignMessages() + "\n===");
			} else if (blockEntity instanceof MC_1_13_2_Sign) {
				MC_1_13_2_Sign sign = (MC_1_13_2_Sign) blockEntity;
				System.out.println("1.13.2 Sign:\nPosition: " + sign.getPosition() + "\n---\n" + sign.getAllSignMessages() + "\n===");
			}
		}

		assertEquals(amountOfSignsToExpect, signsFound);
	}
}
