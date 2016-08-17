package com.programmish.otterball.parsing;

/**
 * A block is defined as the second of the document with an open
 * and close ParsedElement. This could be a String, List, or Object.
 * 
 * @author patricknevindwyer
 *
 */
public class Block {
	ParsedElement open;
	ParsedElement close;
	
	public Block(ParsedElement o, ParsedElement c) {
		this.open = o;
		this.close = c;
	}
	
	public int getCaretStart() {
		return open.start;
	}
	
	public int getCaretEnd() {
		return close.end;
	}
}
