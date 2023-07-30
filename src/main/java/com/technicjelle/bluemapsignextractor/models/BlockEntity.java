package com.technicjelle.bluemapsignextractor.models;

import com.google.gson.Gson;

public class BlockEntity {
	private final Gson gson = new Gson();

	public String id;
	private String text1;
	private String text2;
	private String text3;
	private String text4;
	public int x;
	public int y;
	public int z;

	private String getText(String text) {
		SignTextLine signTextLine = gson.fromJson(text, SignTextLine.class);
		return signTextLine.text;
	}

	public String getText1() {
		return getText(text1);
	}

	public String getText2() {
		return getText(text2);
	}

	public String getText3() {
		return getText(text3);
	}

	public String getText4() {
		return getText(text4);
	}
}
