package com.technicjelle.BlueMapSignExtractor;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class SignGroupExtractionTest {

	@Test
	void validGroupLowercase() {
		assertEquals(Optional.of("home"), Sign.parseGroupName("[home]"));
	}

	@Test
	void validGroupMixedCase() {
		assertEquals(Optional.of("home"), Sign.parseGroupName("[Home]"));
	}

	@Test
	void validGroupAllCaps() {
		assertEquals(Optional.of("home"), Sign.parseGroupName("[HOME]"));
	}

	@Test
	void validGroupWithHyphen() {
		assertEquals(Optional.of("train-station"), Sign.parseGroupName("[train-station]"));
	}

	@Test
	void validGroupWithNumbers() {
		assertEquals(Optional.of("base-2"), Sign.parseGroupName("[base-2]"));
	}

	@Test
	void plainTextNoBrackets() {
		assertEquals(Optional.empty(), Sign.parseGroupName("Welcome!"));
	}

	@Test
	void emptyBrackets() {
		assertEquals(Optional.empty(), Sign.parseGroupName("[]"));
	}

	@Test
	void spacesInGroupName() {
		assertEquals(Optional.empty(), Sign.parseGroupName("[my station]"));
	}

	@Test
	void blankText() {
		assertEquals(Optional.empty(), Sign.parseGroupName(""));
	}

	@Test
	void whitespaceOnly() {
		assertEquals(Optional.empty(), Sign.parseGroupName("   "));
	}

	@Test
	void nullText() {
		assertEquals(Optional.empty(), Sign.parseGroupName(null));
	}

	@Test
	void leadingTrailingSpaces() {
		assertEquals(Optional.of("home"), Sign.parseGroupName("  [home]  "));
	}

	@Test
	void specialCharactersRejected() {
		assertEquals(Optional.empty(), Sign.parseGroupName("[home!]"));
	}

	@Test
	void underscoreRejected() {
		assertEquals(Optional.empty(), Sign.parseGroupName("[train_station]"));
	}
}
