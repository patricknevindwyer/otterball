package com.programmish.otterball.parsing;

import java.util.List;

public class JSONFormat {
	
	public static String compact(String text, List<ParsedElement> elements) {
		return JSONFormat.compact(text, elements, 0);
	}
	public static String compact(String text, List<ParsedElement> elements, int offset) {
		StringBuilder sb = new StringBuilder();
		
		// we are literally ripping through this text to rebuild
		for (ParsedElement e : elements) {
			sb.append(text.substring(e.start + offset, e.end + offset + 1));
		}
		
		return sb.toString();
	}
}
