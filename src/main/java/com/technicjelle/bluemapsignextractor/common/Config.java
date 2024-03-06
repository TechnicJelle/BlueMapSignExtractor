package com.technicjelle.bluemapsignextractor.common;

public interface Config {
	String MARKER_SET_ID = "signs";

	String getMarkerSetName();

	boolean isToggleable();

	boolean isDefaultHidden();

	double getMaxDistance();
}
