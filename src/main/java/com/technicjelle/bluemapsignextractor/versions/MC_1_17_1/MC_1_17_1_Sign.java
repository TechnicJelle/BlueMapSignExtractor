package com.technicjelle.bluemapsignextractor.versions.MC_1_17_1;

import com.technicjelle.bluemapsignextractor.versions.MC_1_14_4.MC_1_14_4_Sign;
import de.bluecolored.bluenbt.NBTName;

public class MC_1_17_1_Sign extends MC_1_14_4_Sign {
	@NBTName("GlowingText")
	private boolean glowingText;

	public boolean isGlowing() {
		return glowingText;
	}
}
