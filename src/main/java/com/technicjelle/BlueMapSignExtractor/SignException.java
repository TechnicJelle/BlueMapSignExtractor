package com.technicjelle.BlueMapSignExtractor;


//TODO: Make this an IOException instead of a RuntimeException
public class SignException extends RuntimeException {
	private static final String pleaseReport = """
			
			\t\tPlease report this as a bug on GitHub, so support for this edge-case can be added!
			\t\t → https://github.com/TechnicJelle/BlueMapSignExtractor/issues/new
			\t\tInclude this whole error log, and also the region file in which this happened.""";

	public SignException(String message) {
		super(message + pleaseReport);
	}

	public SignException(String message, Object value) {
		super(message + "\n\t\t\t" + value + pleaseReport);
	}
}
