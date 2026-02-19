package com.aisupport.analysis.exception;

public class AnalysisException extends RuntimeException {

	private static final long serialVersionUID = -8504338908249297485L;

	public AnalysisException(String message) {
		super(message);
	}

	public AnalysisException(String message, Throwable cause) {
		super(message, cause);
	}

}
