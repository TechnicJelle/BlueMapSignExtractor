package com.technicjelle.BlueMapSignExtractor;

import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

/**
 * NOT TO BE USED FOR ACTUAL TEXT COLOUR!<br>
 * ONLY FOR SIGN(SIDE)S.
 * <p>
 * Use Adventure for Text Colours.
 */
public enum SignSideColour {
	WHITE("white", "#646464", "#ffffff", "#656565"),
	ORANGE("orange", "#64280c", "#fc671f", "#65280c"),
	MAGENTA("magenta", "#640064", "#fc00fc", "#650065"),
	LIGHT_BLUE("light_blue", "#3c4b51", "#98becb", "#3c4b51"),
	YELLOW("yellow", "#646400", "#fcfc00", "#656500"),
	LIME("lime", "#4b6400", "#bdfc00", "#4b6500"),
	PINK("pink", "#642947", "#fc68b2", "#652947"),
	GRAY("gray", "#323232", "#7e7e7e", "#323232"),
	LIGHT_GRAY("light_gray", "#535353", "#d0d0d0", "#535353"),
	CYAN("cyan", "#006464", "#00fcfc", "#006565"),
	PURPLE("purple", "#3f0c5e", "#9e20ed", "#3f0c5f"),
	BLUE("blue", "#000064", "#0000fc", "#000065"),
	BROWN("brown", "#361b07", "#894413", "#361b07"),
	GREEN("green", "#006400", "#00fc00", "#006500"),
	RED("red", "#640000", "#fc0000", "#650000"),
	BLACK("black", "#000000", "#000000", "#ede8ca");

	private static final Map<String, SignSideColour> BY_NAME = new HashMap<>();

	static {
		for (SignSideColour c : values()) {
			BY_NAME.put(c.minecraftName, c);
		}
	}

	private final String minecraftName;
	private final String standard;
	private final String glowCenter;
	private final String glowOutline;

	SignSideColour(String minecraftName, String standard, String glowCenter, String glowOutline) {
		this.minecraftName = minecraftName;
		this.standard = standard;
		this.glowCenter = glowCenter;
		this.glowOutline = glowOutline;
	}

	public static @Nullable SignSideColour get(String name) {
		return BY_NAME.get(name);
	}

	public String getHTMLAttributes(boolean isGlowing, String additionalClasses) {
		if (isGlowing) {
			return "class='" + additionalClasses + " glowing' style='color:" + glowCenter + ";--sign-glow-colour:" + glowOutline + ";'";
		} else {
			return "class='" + additionalClasses + "' style='color:" + standard + ";'";
		}
	}
}
