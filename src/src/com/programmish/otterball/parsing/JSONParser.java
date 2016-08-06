package com.programmish.otterball.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class JSONParser implements FingerPrintingParser {
	private String content;
	
	public JSONParser() {
		this.content = "";
	}
	
	public JSONParser(String blob) {
		this.content = blob;
	}
	
	public void setContent(String s) {
		this.content = s;
	}
	
	/**
	 * For our JSON parser, we're simply going to tear through with
	 * a basic parse. Our conditions for a fingerprint:
	 * 
	 * 		1. Valid outer marker: { or [
	 * 		2. No bare words
	 */
	@Override
	public boolean hasFingerprint(String blob) {
		
		return this.attemptToParse(blob, 0) >= 0;
		
	}
	
	protected int attemptToParse(String blob, int offset) {
		String local = blob.substring(offset);
		local = local.replaceAll("^\\s+", "");
		
		int trimDiff = blob.length() - local.length() - offset;
		
		// quick test for our outer marker
		if ( !local.startsWith("{") && !local.startsWith("[")) {
			return -1;
		}
		
		// fast check for barewords
		boolean inString = false;
		
		// create a quick lookup for nonbareword characters
		char[] nonbare = {
			':', ',', '[', ']', '{', '}', ' ', '\t', '\n',
			'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
			'.', '-'
		};
		
		HashMap<Character, Boolean> baremap = new HashMap<Character, Boolean>();
		for (int i = 0; i < nonbare.length; i++) {
			baremap.put(nonbare[i], true);
		}
		
		// map which items pop which off of our stack - we'll use this for brace
		// counting. Quotes manage themselves on the stack, because they
		// have unique constraints
		HashMap<Character, Character> stackMatches = new HashMap<>();
		stackMatches.put('}', '{');
		stackMatches.put(']',  '[');
		
		// which characters start the stack? - we use this to start the
		// brace counting
		HashMap<Character, Boolean> stackStarters = new HashMap<>();
		stackStarters.put('{', true);
		stackStarters.put('[', true);
		
		// our counting stack
		List<Character> braceStack = new ArrayList<Character>();
				
		// let's begin.
		boolean valid = true;
		int pos = 0;
		for (pos = 0; pos < local.length(); pos++) {
			char c = local.charAt(pos);
			
			if (c == '"') {
				// oh quotes, my old friend.
				
				if (local.charAt(pos - 1) == '\\') {
					// this is an escape
				}
				else {
					
					if (!inString) {
						braceStack.add('"');
					}
					else {
						// we're poppin!
						if ( (braceStack.size() > 0) && (braceStack.get(braceStack.size() - 1) == '"') ) {
							// whew. it's good
							braceStack.remove(braceStack.size() - 1);
						}
						else {
							valid = false;
							break;
						}
					}
					
					inString = !inString;					
				}
			}
			else {
				if (!inString) {
					// is this an acceptable character?
					
					// do a look ahead for being a boolean
					if (c == 't') {
						if (local.substring(pos, pos + 3) == "true") {
							// ok, it's a bool, we jump
							pos += 3;
							continue;
						}
					}
					
					if (c == 'f') {
						if (local.substring(pos, pos + 4) == "false") {
							// woo
							pos += 4;
							continue;
						}
					}
					
					// check the nonbare characters
					if (baremap.containsKey(c)) {
						
						// are we poppin the stack?
						if (stackStarters.containsKey(c)) {
							// push it on the stack
							braceStack.add(c);
						}
						else if (stackMatches.containsKey(c)) {
							
							// pop the stack
							char m = braceStack.get(braceStack.size() - 1);
							braceStack.remove(braceStack.size() - 1);
							
							if (stackMatches.get(c) != m) {
								// we gots a problem
								valid = false;
								break;
							}
							
							// is our stack empty? If so, we've got a match
							if (braceStack.size() == 0) {
								return pos + trimDiff + offset + 1;
							}
						}
						
						// are we 
						// we're good
						continue;
					}
										
					// we've got a bare word
					valid = false;
					break;
					
				}
				else {
					// in a string, don't care
				}
			}
		}
		
		if (valid && (braceStack.size() == 0)) {
			// we've parsed successfully
			return blob.length();
		}
		else {
			return -1;
		}
		
	}
	/**
	 * For our section extractor, we assume that a section of JSON
	 * in a larger text starts at the beginning of a line (or prepended
	 * by spaces) to save ourselves some work.
	 */
	@Override
	public List<TextRange> findSections(String blob) {
		
		// we're going to search for all the newlines in the blob first,
		// so we have some candidates. Then we walk from those, and
		// use our rough brace counter to give us an estimate of
		// if we're in a JSON block
		
		// new lines
		int from_idx = 0;
		List<Integer> line_idxs = new ArrayList<Integer>();
		line_idxs.add(0);
		while (blob.indexOf("\n", from_idx) > -1) {
			int nl_idx = blob.indexOf("\n", from_idx);
			from_idx = nl_idx + 1;
			line_idxs.add(nl_idx + 1);
		}
		
		// now step our counting parser
		List<TextRange> ranges = new ArrayList<>();
		
		int last_match = -1;
		for (int line_idx : line_idxs) {
			if (line_idx < last_match) {
				continue;
			}
			int match = this.attemptToParse(blob, line_idx);
			if (match > -1) {
				// we gots a match
				last_match = match;
				ranges.add(new TextRange(line_idx, match));
			}
		}
		
		return ranges;
	}
	
	/**
	 * Start parsing for a JSON at the given offset, using a string
	 * aware counting parser to see if we have a valid JSON block. If
	 * we do, then return the offset of the end of the JSON block,
	 * otherwise return -1
	 * 
	 * @param blob
	 * @param offset
	 * @return
	 */
	protected int countingParseFromOffset(String blob, int offset) {
		int boundary = -1;
		
		for (int count_idx = offset; count_idx < blob.length(); count_idx++) {
			
			
		}
		return boundary;
	}
	
	
}
