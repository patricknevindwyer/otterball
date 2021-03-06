package com.programmish.otterball.parsing;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

public class JSONParser implements FingerPrintingParser {
	private String content;
	private static Logger logger = Logger.getLogger("otterball." + JSONParser.class.getSimpleName());

	public JSONParser() {
		this.content = "";
	}
	
	public JSONParser(String blob) {
		this.content = blob;
	}
	
	public void setContent(String s) {
		this.content = s;
	}
	
	public String parserName() {
		return "Vanilla JSON";
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
						if (local.substring(pos, pos + 4).equals("true")) {
							// ok, it's a bool, we jump
							pos += 3;
							continue;
						}
					}
					
					if (c == 'f') {
						if (local.substring(pos, pos + 5).equals("false")) {
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
	
	public List<ParsedElement> parse(String blob) {
		JSONParser.logger.debug(" - parser - starting whole text parse");
		return this.parse(blob, 0);
	}
	
	/**
	 * Run a full parse on the blob, returning a list of parsed
	 * elements suitable for rebuilding the blob, condensing,
	 * highlighting, etc.
	 * 
	 * @param blob
	 * @param offset
	 * @return List<ParsedElement> parse queue
	 */
	public List<ParsedElement> parse(String blob, int offset) {
		
		JSONParser.logger.debug(String.format(" - parser - starting offset parse at %d", offset));
		String local = blob.substring(offset);
		local = local.replaceAll("^\\s+", "");
		
		long start_time = System.currentTimeMillis();
		
		List<ParsedElement> elements = new ArrayList<>();
		
		int trimDiff = blob.length() - local.length() - offset;
		
		int elementAdjustment = trimDiff + offset;
		
		// quick test for our outer marker
		if ( !local.startsWith("{") && !local.startsWith("[")) {
			JSONParser.logger.debug(" - parser - bailing (doesn't start as an object)");
			return elements;
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
		
		// also create a lookup for numerics
		char[] numerics = {
				'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				'-', '.'
		};
		
		HashMap<Character, Boolean> numericmap = new HashMap<Character, Boolean>();
		for (int i = 0; i < numerics.length; i++) {
			numericmap.put(numerics[i], true);
		}
		
		// number matching pattern for validating numerics
		Pattern numericPattern = Pattern.compile("^-?[0-9]+(\\.[0-9]+)?$");
		
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
						
						// we'll also add an element to the elements list for this
						elements.add(new ParsedElement(ElementType.STRING, pos + elementAdjustment));
						
					}
					else {
						// we're poppin!
						if ( (braceStack.size() > 0) && (braceStack.get(braceStack.size() - 1) == '"') ) {
							// whew. it's good
							braceStack.remove(braceStack.size() - 1);
							
							// we also need to mark the end of this string, thankfully it's the last element
							// on the elements list
							elements.get(elements.size() - 1).end = pos + elementAdjustment;

							// make sure we're properly delimited
							if (!this.isDelimited(braceStack, elements.subList(0, elements.size() - 1))) {
								JSONParser.logger.debug(String.format(" - parser - improperly delimited at pos(%d)", pos));
								valid = false;
								
								// pop the last element off of our element list - it's part of the problem
								elements.remove(elements.size() - 1);
								
								break;
							}
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

						// check if we have enough look ahead to even try parsing further
						if (local.length() < (pos + 4)) {
							valid = false;
							break;
						}
						
						if (local.substring(pos, pos + 4).equals("true")) {
							
							// make sure we're properly delimited
							if (!this.isDelimited(braceStack, elements)) {
								valid = false;
								break;
							}
							
							// add to the element list
							elements.add(new ParsedElement(ElementType.BOOLEAN, pos + elementAdjustment, pos + elementAdjustment + 3));

							// ok, it's a bool, we jump
							pos += 3;
							
							continue;
						}
					}
					
					if (c == 'f') {
						
						// check if we have enough look ahead to even try parsing further
						if (local.length() < (pos + 5)) {
							valid = false;
							break;
						}
						
						if (local.substring(pos, pos + 5).equals("false")) {
							
							// make sure we're properly delimited
							if (!this.isDelimited(braceStack, elements)) {
								valid = false;
								break;
							}
							
							// add to the parsed elements
							elements.add(new ParsedElement(ElementType.BOOLEAN, pos + elementAdjustment, pos + elementAdjustment + 4));
							
							// woo
							pos += 4;
							continue;
						}
					}
					
					// check for the null string
					if (c == 'n') {
						
						// check if we have enough look ahead to even try parsing further
						if (local.length() < (pos + 4)) {
							valid = false;
							break;
						}
						
						if (local.substring(pos, pos + 4).equals("null")) {
							
							// make sure we're properly delimited
							if (!this.isDelimited(braceStack, elements)) {
								valid = false;
								break;
							}

							// add to the element list
							elements.add(new ParsedElement(ElementType.NULL, pos + elementAdjustment, pos + elementAdjustment + 3));
							
							// ok, it's the null string, we jump
							pos += 3;
							
							continue;
						}
						
					}
					
					// check the nonbare characters
					if (baremap.containsKey(c)) {
						
						// are we poppin the stack?
						if (stackStarters.containsKey(c)) {
							
							// we're starting a collection. what is it?
							if (c == '{') {
								
								if (elements.size() > 0) {
									// make sure we're properly delimited
									if (!this.isDelimited(braceStack, elements)) {
										valid = false;
										break;
									}
								}
								elements.add(new ParsedElement(ElementType.OBJ_START, pos + elementAdjustment, pos + elementAdjustment));
							}
							else if (c == '[') {
								if (elements.size() > 0) {
									// make sure we're properly delimited
									if (!this.isDelimited(braceStack, elements)) {
										valid = false;
										break;
									}
								}
								elements.add(new ParsedElement(ElementType.LIST_START, pos + elementAdjustment, pos + elementAdjustment));
							}
							
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
							
							// what are we ending?
							if (c == '}') {
								
								// can't end an object with a separator or delimiter
								ElementType tailType = elements.get(elements.size() - 1).type;
								if ( (tailType == ElementType.OBJ_SEPARATOR) || (tailType == ElementType.OBJ_DELIMITER) ) {
									// nope a nope
									valid = false;
									break;
								}

								elements.add(new ParsedElement(ElementType.OBJ_END, pos + elementAdjustment, pos + elementAdjustment));
							}
							else if (c == ']') {
								
								// can't end a list with a delimiter
								if (elements.get(elements.size() - 1).type == ElementType.LIST_DELIMITER) {
									// nope a nope
									valid = false;
									break;
								}
								elements.add(new ParsedElement(ElementType.LIST_END, pos + elementAdjustment, pos + elementAdjustment));
							}
							
							// is our stack empty? If so, we've got a match
							if (braceStack.size() == 0) {
								break;
							}
						}
						
						// time to check our delimiters
						else if (c == ':') {
							
							// make sure the element just before us is a string
							ParsedElement look_behind = elements.get(elements.size() - 1);
							
							if (look_behind.type == ElementType.STRING) {
								elements.add(new ParsedElement(ElementType.OBJ_SEPARATOR, pos + elementAdjustment, pos + elementAdjustment));
							}
							else {
								// nerp.
								valid = false;
								break;
							}
						}
						else if (c == ',') {
							
							// what was the previously parsed element
							ParsedElement peek_back = elements.get(elements.size() - 1);
							
							// make sure the element just before us can support a delimiter
							ElementType pbt = peek_back.type;
							
							if ( 
									(pbt == ElementType.OBJ_START) || (pbt == ElementType.LIST_START) ||
									(pbt == ElementType.OBJ_DELIMITER) || (pbt == ElementType.OBJ_SEPARATOR) ||
									(pbt == ElementType.LIST_DELIMITER) ) {
								// nada
								valid = false;
								break;
							}
							
							// are we in a list or object?
							char look_behind = braceStack.get(braceStack.size() - 1);
							
							if (look_behind == '{') {
								elements.add(new ParsedElement(ElementType.OBJ_DELIMITER, pos + elementAdjustment, pos + elementAdjustment));
							}
							else if (look_behind == '[') {
								elements.add(new ParsedElement(ElementType.LIST_DELIMITER, pos + elementAdjustment, pos + elementAdjustment));
							}
						}
						
						// now for numerics
						else if (numericmap.containsKey(c)) {
							
							// we need to scan ahead and find the whole number, and then validate it
							int look_ahead_idx = pos + 1;
							
							while (numericmap.containsKey(local.charAt(look_ahead_idx))) {
								look_ahead_idx += 1;
							}
							
							// pull out our number and validate it
							String number = local.substring(pos, look_ahead_idx);
							Matcher numberMatcher = numericPattern.matcher(number);
														
							if (numberMatcher.matches()) {

								// make sure we're properly delimited
								if (!this.isDelimited(braceStack, elements)) {
									valid = false;
									break;
								}
								
								// pop in on our element stack
								elements.add(new ParsedElement(ElementType.NUMBER, pos + elementAdjustment, look_ahead_idx - 1 + elementAdjustment));
								
								// jump
								pos = look_ahead_idx - 1;
								continue;
							}
							else {
							
								// we've got a problem. Number isn't sane.
								valid = false;
								break;
							}
							
						}
						
						// Everything else should be white space 
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
		
		long end_time = System.currentTimeMillis();
		JSONParser.logger.debug(String.format(" - parser - Parsed %d elements in %d milliseconds",  elements.size(), end_time - start_time ));
		
		if (valid && (braceStack.size() == 0)) {
			JSONParser.logger.debug(" - parser - successful parse");
			// we've parsed successfully
			return elements;
		}
		else {
			JSONParser.logger.debug(String.format(" - parser - JSON parse marked as invalid. %d elements in parse tree", elements.size()));
			return elements;
		}
		
	}
	
	protected boolean isDelimited(List<Character> braceStack, List<ParsedElement> elements) {
		
		// we're adding a value type to ... something. Determine if we're properly delimited
		boolean proper = false;
		
		char brace_look_behind = braceStack.get(braceStack.size() - 1);
		ParsedElement element_look_behind = elements.get(elements.size() - 1);
		
		//System.out.println("Checking proper delimiting" + "\n\tbrace_behind: " + brace_look_behind + "\n\telement behind: " + element_look_behind.type);
		if (brace_look_behind == '[') {
			
			// if we're in a list, the previous value needs to be start of list or list delimiter
			if ( (element_look_behind.type == ElementType.LIST_START) || (element_look_behind.type == ElementType.LIST_DELIMITER) ) {
				proper = true;
			}
		}
		else if (brace_look_behind == '{') {
			
			// if we're in an object, we need one of: obj_start, delimiter, separator
			if ( 
					(element_look_behind.type == ElementType.OBJ_START) || 
					(element_look_behind.type == ElementType.OBJ_DELIMITER) ||
					(element_look_behind.type == ElementType.OBJ_SEPARATOR)
				) {
				proper = true;
			}
		}
		
		return proper;
	}
}
