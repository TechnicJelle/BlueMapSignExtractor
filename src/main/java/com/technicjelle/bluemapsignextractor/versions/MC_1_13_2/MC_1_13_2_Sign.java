package com.technicjelle.bluemapsignextractor.versions.MC_1_13_2;

import com.google.gson.Gson;
import com.technicjelle.bluemapsignextractor.common.BlockEntity;
import com.technicjelle.bluemapsignextractor.common.SignColour;
import com.technicjelle.bluemapsignextractor.common.HTMLUtils;
import de.bluecolored.bluenbt.NBTName;

public class MC_1_13_2_Sign extends BlockEntity {
	private static class SignTextLine {
		@NBTName("text")
		private String text;
	}

	@NBTName("Text1")
	protected String text1;
	@NBTName("Text2")
	protected String text2;
	@NBTName("Text3")
	protected String text3;
	@NBTName("Text4")
	protected String text4;

	public static String unJSON(String text) {
		final SignTextLine signTextLine = new Gson().fromJson(text, SignTextLine.class);
		return signTextLine.text;
	}

	@Override
	public String getFormattedHTML() {
		return HTMLUtils.formatSignLineToHTML(unJSON(text1), SignColour.BLACK, false) + "\n" +
				HTMLUtils.formatSignLineToHTML(unJSON(text2), SignColour.BLACK, false) + "\n" +
				HTMLUtils.formatSignLineToHTML(unJSON(text3), SignColour.BLACK, false) + "\n" +
				HTMLUtils.formatSignLineToHTML(unJSON(text4), SignColour.BLACK, false);
	}

	@Override
	public String getLabel() {
		final String t1 = unJSON(text1);
		if (!t1.isBlank()) return t1;

		final String t2 = unJSON(text2);
		if (!t2.isBlank()) return t2;

		final String t3 = unJSON(text3);
		if (!t3.isBlank()) return t3;

		final String t4 = unJSON(text4);
		if (!t4.isBlank()) return t4;

		return "Blank sign at " + getPosition().toString();
	}

	@Override
	public boolean isBlank() {
		return unJSON(text1).isBlank()
			&& unJSON(text2).isBlank()
			&& unJSON(text3).isBlank()
			&& unJSON(text4).isBlank();
	}
}
