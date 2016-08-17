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

	public static String compact(String text, JSONDocument jsd, int start, int end) {
		long st = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		
		int idx_st = jsd.getIndexAtCaret(start);
		int idx_ed = jsd.getIndexAtCaret(end);
		
		if ( (idx_st == -1) && (idx_ed == -1) ) {
			return text;
		}
		
		// get the beginning of our text before our compaction area
		int idx_compactStart = jsd.elements.get(idx_st).start;
		sb.append(text.substring(0, idx_compactStart));
		
		// rip through the elements to build our compacted area
		for (int idx = idx_st; idx <= idx_ed; idx++) {
			ParsedElement e = jsd.elements.get(idx);
			sb.append(text.substring(e.start, e.end + 1));
		}

		// get the end of the document post-selection
		if (idx_ed >= 0) {
			int idx_compactEnd = jsd.elements.get(idx_ed).end;
			sb.append(text.substring(idx_compactEnd + 1));
		}
		
		long ed = System.currentTimeMillis();
		JSONFormat.logger.debug(String.format(" - JSONFormat::compact took %d ms", ed - st));
		return sb.toString();
	}
	
	public static String reflow(String text, List<ParsedElement> elements) {
		return JSONFormat.reflow(text, "    ", elements, 0);
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

			if (e.type == ElementType.LIST_END) {
				if (elements.get(pos - 1).type != ElementType.LIST_START) {
					sb.append("\n");
					for (int i = 0; i < indentSize; i++) {
						sb.append(indent);
					}
				}
			}

			sb.append(text.substring(e.start + offset, e.end + offset + 1));
			
			if (e.type == ElementType.LIST_DELIMITER) {
				sb.append("\n");
				for (int i = 0; i < indentSize; i++) {
					sb.append(indent);
				}
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
			else if ( (e.type == ElementType.OBJ_START) || (e.type == ElementType.LIST_START) ){
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
	
	public static String reflow(String text, String indent, JSONDocument jsd, int start, int end) {
		long st = System.currentTimeMillis();
		StringBuilder sb = new StringBuilder();
		
		int idx_st = jsd.getIndexAtCaret(start);
		int idx_ed = jsd.getIndexAtCaret(end);
		
		if ( (idx_st == -1) && (idx_ed == -1) ) {
			return text;
		}
	
		// get the beginning of our text before our compaction area
		int idx_compactStart = jsd.elements.get(idx_st).start;
		sb.append(text.substring(0, idx_compactStart));

		int indentSize = 0;
		ParsedElement e;
		for (int pos = idx_st; pos <= idx_ed; pos++) {
			e = jsd.elements.get(pos);
			
			if ( (e.type == ElementType.OBJ_START) || (e.type == ElementType.LIST_START) ) {
				indentSize++;
			}
			else if ( (e.type == ElementType.OBJ_END) || (e.type == ElementType.LIST_END) ) {
				indentSize--;
			}
			
			if (e.type == ElementType.OBJ_END) {
				if (jsd.elements.get(pos - 1).type != ElementType.OBJ_START) {
					sb.append("\n");
					for (int i = 0; i < indentSize; i++) {
						sb.append(indent);
					}
				}
			}

			if (e.type == ElementType.LIST_END) {
				if (jsd.elements.get(pos - 1).type != ElementType.LIST_START) {
					sb.append("\n");
					for (int i = 0; i < indentSize; i++) {
						sb.append(indent);
					}
				}
			}

			sb.append(text.substring(e.start, e.end + 1));
			
			if (e.type == ElementType.LIST_DELIMITER) {
				sb.append("\n");
				for (int i = 0; i < indentSize; i++) {
					sb.append(indent);
				}
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
			else if ( (e.type == ElementType.OBJ_START) || (e.type == ElementType.LIST_START) ){
				if (jsd.elements.get(pos + 1).type != ElementType.OBJ_END) {
					sb.append("\n");
					for (int i = 0; i < indentSize; i++) {
						sb.append(indent);
					}
				}
			}
			
		}
		
		// get the end of the document - post selection
		if (idx_ed >= 0) {
			int idx_compactEnd = jsd.elements.get(idx_ed).end;
			sb.append(text.substring(idx_compactEnd + 1));
		}

		long ed = System.currentTimeMillis();
		JSONFormat.logger.debug(String.format(" - JSONFormat::compact took %d ms", ed - st));
		return sb.toString();

	}
}