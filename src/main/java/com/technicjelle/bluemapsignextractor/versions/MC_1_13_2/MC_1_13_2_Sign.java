package com.technicjelle.bluemapsignextractor.versions.MC_1_13_2;

import com.google.gson.Gson;
import com.technicjelle.bluemapsignextractor.common.BlockEntity;
import com.technicjelle.bluemapsignextractor.common.HTMLUtils;
import com.technicjelle.bluemapsignextractor.common.SignColour;
import de.bluecolored.bluenbt.NBTName;
import org.jetbrains.annotations.Nullable;

public class MC_1_13_2_Sign extends BlockEntity {
	private static final Gson GSON = new Gson();

	@SuppressWarnings("unused")
	private static class SignTextLine {
		private @Nullable String text;
	}

	@NBTName("Text1")
	protected String text1;
	@NBTName("Text2")
	protected String text2;
	@NBTName("Text3")
	protected String text3;
	@NBTName("Text4")
	protected String text4;

	public static @Nullable String unJSON(String text) {
		final SignTextLine signTextLine = GSON.fromJson(text, SignTextLine.class);
		return signTextLine.text;
		//Can be null if there IS no `text` key in the JSON.
		// This can happen in the case of translated strings.
		// https://minecraft.wiki/w/Raw_JSON_text_format#Translated_Text
		// https://github.com/TechnicJelle/BlueMapSignExtractor/issues/64#issuecomment-2480620906
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
		if (t1 != null && !t1.isBlank()) return t1;

		final String t2 = unJSON(text2);
		if (t2 != null && !t2.isBlank()) return t2;

		final String t3 = unJSON(text3);
		if (t3 != null && !t3.isBlank()) return t3;

		final String t4 = unJSON(text4);
		if (t4 != null && !t4.isBlank()) return t4;

		return "Blank sign at " + getPosition().toString();
	}

	@SuppressWarnings("RedundantIfStatement")
	@Override
	public boolean isBlank() {
		final String t1 = unJSON(text1);
		if (t1 != null && !t1.isBlank()) return false;

		final String t2 = unJSON(text2);
		if (t2 != null && !t2.isBlank()) return false;

		final String t3 = unJSON(text3);
		if (t3 != null && !t3.isBlank()) return false;

		final String t4 = unJSON(text4);
		if (t4 != null && !t4.isBlank()) return false;

		return true;
	}
}
