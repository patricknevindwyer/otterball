package com.programmish.otterball.parsing;

import java.util.List;

import org.apache.log4j.Logger;

public class JSONFormat {
	
	private static Logger logger = Logger.getLogger("otterball." + JSONFormat.class.getSimpleName());
	
	public static String compact(String text, List<ParsedElement> elements) {
		return JSONFormat.compact(text, elements, 0);
	}
	
	public static String compact(String text, List<ParsedElement> elements, int offset) {
		long st = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		
		// we are literally ripping through this text to rebuild
		for (ParsedElement e : elements) {
			sb.append(text.substring(e.start + offset, e.end + offset + 1));
		}
		long ed = System.currentTimeMillis();
		JSONFormat.logger.debug(String.format(" - JSONFormat::compact took %d ms", ed - st));
		return sb.toString();
	}

	public static String reflow(String text, String indent, List<ParsedElement> elements) {
		return JSONFormat.reflow(text, indent, elements, 0);
	}
	
	public static String reflow(String text, String indent, List<ParsedElement> elements, int offset) {
		long st = System.currentTimeMillis();
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
		
		long ed = System.currentTimeMillis();
		JSONFormat.logger.debug(String.format(" - JSONFormat::reflow took %d ms", ed - st));
		
		return sb.toString();
	}
}