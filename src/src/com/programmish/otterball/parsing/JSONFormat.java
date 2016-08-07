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

	public static String reflow(String text, String indent, List<ParsedElement> elements) {
		return JSONFormat.reflow(text, indent, elements, 0);
	}
	
	public static String reflow(String text, String indent, List<ParsedElement> elements, int offset) {
		StringBuilder sb = new StringBuilder();
		
		int indentSize = 0;
		ParsedElement e;
		for (int pos = 0; pos < elements.size(); pos++) {
			e = elements.get(pos);
			
			if ( (e.type == ElementType.OBJ_START) || (e.type == ElementType.LIST_START) ) {
				indentSize++;
			}
			else if ( (e.type == ElementType.OBJ_END) || (e.type == ElementType.LIST_END) ) {
				indentSize--;
			}
			
			if (e.type == ElementType.OBJ_END) {
				if (elements.get(pos - 1).type != ElementType.OBJ_START) {
					sb.append("\n");
					for (int i = 0; i < indentSize; i++) {
						sb.append(indent);
					}
				}
			}
			
			sb.append(text.substring(e.start + offset, e.end + offset + 1));
			
			if (e.type == ElementType.LIST_DELIMITER) {
				sb.append(" ");
			}
			else if (e.type == ElementType.OBJ_SEPARATOR) {
				sb.append(" ");
			}
			else if (e.type == ElementType.OBJ_DELIMITER) {
				sb.append("\n");
				for (int i = 0; i < indentSize; i++) {
					sb.append(indent);
				}
			}
			else if (e.type == ElementType.OBJ_START) {
				if (elements.get(pos + 1).type != ElementType.OBJ_END) {
					sb.append("\n");
					for (int i = 0; i < indentSize; i++) {
						sb.append(indent);
					}
				}
			}
		}
		
		
		return sb.toString();
	}
}