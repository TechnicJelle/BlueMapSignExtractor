package com.technicjelle.bluemapsignextractor.common;


import java.util.Map;

public class HTMLUtils {
	private static final Map<String, String> mc2css = Map.ofEntries(
			Map.entry("white", "#646464"),
			Map.entry("orange", "#64280c"),
			Map.entry("magenta", "#640064"),
			Map.entry("light_blue", "#3c4b51"),
			Map.entry("yellow", "#646400"),
			Map.entry("lime", "#4b6400"),
			Map.entry("pink", "#642947"),
			Map.entry("gray", "#323232"),
			Map.entry("light_gray", "#535353"),
			Map.entry("cyan", "#006464"),
			Map.entry("purple", "#3f0c5e"),
			Map.entry("blue", "#000064"),
			Map.entry("brown", "#361b07"),
			Map.entry("green", "#006400"),
			Map.entry("red", "#640000"),
			Map.entry("black", "#000000")
	);

	private static final Map<String, String> glowCenter2css = Map.ofEntries(
			Map.entry("white", "#ffffff"),
			Map.entry("orange", "#fc671f"),
			Map.entry("magenta", "#fc00fc"),
			Map.entry("light_blue", "#98becb"),
			Map.entry("yellow", "#fcfc00"),
			Map.entry("lime", "#bdfc00"),
			Map.entry("pink", "#fc68b2"),
			Map.entry("gray", "#7e7e7e"),
			Map.entry("light_gray", "#d0d0d0"),
			Map.entry("cyan", "#00fcfc"),
			Map.entry("purple", "#9e20ed"),
			Map.entry("blue", "#0000fc"),
			Map.entry("brown", "#894413"),
			Map.entry("green", "#00fc00"),
			Map.entry("red", "#fc0000"),
			Map.entry("black", "#000000")
	);

	private static final Map<String, String> glowOutline2css = Map.ofEntries(
			Map.entry("white", "#656565"),
			Map.entry("orange", "#65280c"),
			Map.entry("magenta", "#650065"),
			Map.entry("light_blue", "#3c4b51"),
			Map.entry("yellow", "#656500"),
			Map.entry("lime", "#4b6500"),
			Map.entry("pink", "#652947"),
			Map.entry("gray", "#323232"),
			Map.entry("light_gray", "#535353"),
			Map.entry("cyan", "#006565"),
			Map.entry("purple", "#3f0c5f"),
			Map.entry("blue", "#000065"),
			Map.entry("brown", "#361b07"),
			Map.entry("green", "#006500"),
			Map.entry("red", "#650000"),
			Map.entry("black", "#ede8ca")
	);

	public static String minecraftColourToCSSColour(String minecraftColour) {
		return mc2css.get(minecraftColour);
	}

	public static String minecraftColourToGlowCenterCSSColour(String minecraftColour) {
		return glowCenter2css.get(minecraftColour);
	}

	private static String minecraftColourToGlowOutlineCSSColour(String minecraftColour) {
		return glowOutline2css.get(minecraftColour);
	}

	public static String formatSignLineToHTML(String text, String minecraftColour, boolean isGlowing) {
		if (isGlowing) {
			final String centerColour = minecraftColourToGlowCenterCSSColour(minecraftColour);
			final String outlineColour = minecraftColourToGlowOutlineCSSColour(minecraftColour);
			return "<span class='glowing' style='color:" + centerColour + ";--sign-glow-colour:" + outlineColour + ";'>" + text + "</span>";
		} else {
			final String textColour = minecraftColourToCSSColour(minecraftColour);
			return "<span style='color:" + textColour + ";'>" + text + "</span>";
		}
	}
}
