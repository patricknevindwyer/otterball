package com.programmish.otterball.parsing;

public class TextRange {
	public int start;
	public int end;
	
	TextRange() {
		start = 0;
		end = 0;
	}
	
	TextRange(int s, int e) {
		this.start = s;
		this.end = e;
	}
}
