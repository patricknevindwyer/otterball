package com.programmish.otterball.parsing;

public class ParsedElement {
	public int start;
	public int end;
	
	public ElementType type;
	
	public ParsedElement() {
		
	}
	
	public ParsedElement(ElementType t, int s) {
		this.type = t;
		this.start = s;
	}
	
	public ParsedElement(ElementType t, int s, int e) {
		this.type = t;
		this.start = s;
		this.end = e;
	}
}
