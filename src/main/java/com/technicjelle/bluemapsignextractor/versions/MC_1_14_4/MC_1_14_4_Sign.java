package com.technicjelle.bluemapsignextractor.versions.MC_1_14_4;

import com.technicjelle.bluemapsignextractor.versions.MC_1_13_2.MC_1_13_2_Sign;
import de.bluecolored.bluenbt.NBTName;

public class MC_1_14_4_Sign extends MC_1_13_2_Sign {
	@NBTName("Color")
	String colour;

	public String getColour() {
		return colour;
	}
}
