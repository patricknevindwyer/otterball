package com.programmish.otterball.validation;

public class SourceAnalysis {

	private int lineNumber;
	private int startCharacter;
	private String message;
	private String source;
	
	public SourceAnalysis(int line, int character, String message, String source) {
		this.lineNumber = line;
		this.startCharacter = character;
		this.message = message;
		this.source = source;
	}

	public int getLineNumber() {
		return lineNumber;
	}

	public int getStartCharacter() {
		return startCharacter;
	}

	public String getMessage() {
		return message;
	}
	
	public String getSource() {
		return source;
	}

}
