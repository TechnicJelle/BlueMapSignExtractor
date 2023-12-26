package com.technicjelle.bluemapsignextractor.versions.MC_1_17_1;

import com.technicjelle.bluemapsignextractor.common.HTMLUtils;
import com.technicjelle.bluemapsignextractor.versions.MC_1_14_4.MC_1_14_4_Sign;
import de.bluecolored.bluenbt.NBTName;

public class MC_1_17_1_Sign extends MC_1_14_4_Sign {
	@NBTName("GlowingText")
	private boolean isGlowing;

	@Override
	public String getFormattedHTML() {
		return HTMLUtils.formatSignLineToHTML(unJSON(text1), colour, isGlowing) + "\n" +
				HTMLUtils.formatSignLineToHTML(unJSON(text2), colour, isGlowing) + "\n" +
				HTMLUtils.formatSignLineToHTML(unJSON(text3), colour, isGlowing) + "\n" +
				HTMLUtils.formatSignLineToHTML(unJSON(text4), colour, isGlowing);
	}
}
