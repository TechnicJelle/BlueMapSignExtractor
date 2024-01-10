package com.technicjelle.bluemapsignextractor.versions.MC_1_14_4;

import com.technicjelle.bluemapsignextractor.common.SignColour;
import com.technicjelle.bluemapsignextractor.common.HTMLUtils;
import com.technicjelle.bluemapsignextractor.versions.MC_1_13_2.MC_1_13_2_Sign;
import de.bluecolored.bluenbt.NBTName;

public class MC_1_14_4_Sign extends MC_1_13_2_Sign {
	@NBTName("Color")
	protected String colour;

	@Override
	public String getFormattedHTML() {
		return HTMLUtils.formatSignLineToHTML(unJSON(text1), SignColour.get(colour), false) + "\n" +
				HTMLUtils.formatSignLineToHTML(unJSON(text2), SignColour.get(colour), false) + "\n" +
				HTMLUtils.formatSignLineToHTML(unJSON(text3), SignColour.get(colour), false) + "\n" +
				HTMLUtils.formatSignLineToHTML(unJSON(text4), SignColour.get(colour), false);
	}
}
