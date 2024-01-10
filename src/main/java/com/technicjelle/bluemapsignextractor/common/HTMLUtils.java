package com.technicjelle.bluemapsignextractor.common;


public class HTMLUtils {
	private HTMLUtils() {}
	public static String formatSignLineToHTML(String text, SignColour signColour, boolean isGlowing) {
		if (isGlowing) {
			return "<span class='glowing' style='color:" + signColour.glowCenter + ";--sign-glow-colour:" + signColour.glowOutline + ";'>" + text + "</span>";
		} else {
			return "<span style='color:" + signColour.standard + ";'>" + text + "</span>";
		}
	}
}
