package com.technicjelle.bluemapsignextractor.versions.MC_1_13_2;

import com.google.gson.Gson;
import com.technicjelle.bluemapsignextractor.common.BlockEntity;
import de.bluecolored.bluenbt.NBTName;

public class MC_1_13_2_Sign extends BlockEntity {
	private static class SignTextLine {
		@NBTName("text")
		public String text;
	}

	@NBTName("Text1")
	private String text1;
	@NBTName("Text2")
	private String text2;
	@NBTName("Text3")
	private String text3;
	@NBTName("Text4")
	private String text4;

	private String getText(String text) {
		SignTextLine signTextLine = new Gson().fromJson(text, SignTextLine.class);
		return signTextLine.text;
	}

	@Override
	public String getAllSignMessages() {
		return getText(text1) + "\n" + getText(text2) + "\n" + getText(text3) + "\n" + getText(text4);
	}
}
