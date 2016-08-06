package com.programmish.otterball.ui.highlight;


import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;

import com.programmish.otterball.ui.JSONShell;
import com.programmish.otterball.ui.themes.Theme;

public class DumbJavaScriptHighlighter implements ExtendedModifyListener {

	private StyledText editor;
	private JSONShell jsEditor;
	
	private static Logger logger = Logger.getLogger("civet." + DumbJavaScriptHighlighter.class.getSimpleName());
	
	Color fg_var;
	Color fg_keywords;
	Color fg_comps;
	Color fg_modules;
	Color fg_braces;
	Color fg_builtins;
	Color fg_literals;
	Color fg_comments;
	Color fg_strings;

	public DumbJavaScriptHighlighter(JSONShell jss, StyledText editor) {
	
		this.jsEditor = jss;
		
		this.editor = editor;
		
		// pull in the current theme info
		this.loadThemeColors();
		
		this.editor.addExtendedModifyListener(this);
	}
	
	public void highlightAsync(final int start, final int length) {
		
		final String text = this.editor.getText();
		new Thread("highlight") {
			public void run() {
				dumbHighlight(start, length, text);
			}
		}.start();
		
	}
	
	private void loadThemeColors() {
		Theme t = Theme.getTheme();
		this.fg_comments = t.getColor("comments.foreground");
		this.fg_keywords = t.getColor("keywords.foreground");
		this.fg_var = t.getColor("keywords.foreground");
		this.fg_braces = t.getColor("braces.foreground");
		this.fg_modules = t.getColor("modules.foreground");
		this.fg_strings = t.getColor("strings.foreground");
		this.fg_literals = t.getColor("literals.foreground");
		this.fg_comps = t.getColor("comps.foreground");
		this.fg_builtins = t.getColor("builtins.foreground");
	}
	
	@Override
	public void modifyText(ExtendedModifyEvent event) {
		// highlight the lines of the modify event
		int startLine = this.editor.getLineAtOffset(event.start);
		int endLine = this.editor.getLineAtOffset(event.start + event.length);
		
		int startOffset = this.editor.getOffsetAtLine(startLine);
		int endOffset = this.editor.getOffsetAtLine(endLine) + this.editor.getLine(endLine).length();
		
		this.dumbHighlight(startOffset, endOffset, this.editor.getText());
		
	}


	/**
	 * This highlighter might run from any thread, so cannot call out to any part of the editor, save
	 * for the replaceTextStyles method of JavaScriptShell, which is thread safe for updating
	 * the editor after styles have been calculated.
	 * 
	 * @param start Start of the highlight range, as a character offset from the beginning of the text
	 * @param end End of the highlight range, as a character offset from the beginning of the text
	 * @param text The entire text to be highlighted
	 */
	private void dumbHighlight(int start, int end, String text) {

		// clamp the start and end so we don't overflow the text
		int charCount = text.length();
		
		// bail on empty text
		if (charCount == 0) {
			return;
		}
		
		if (start > (charCount - 1)) {
			start = charCount - 1;
		}
		
		if (end > (charCount - 1)) {
			end = charCount - 1;
		}
		else if (end < 0) {
			end = 0;
		}
				
		long st = System.currentTimeMillis();
		
		// Setup the current text against which to highlight
		String current = text.substring(start, end);
		
		// track our highlight ranges
		ArrayList<StyleRange> highlights = new ArrayList<StyleRange>();
		
		// Variable decls.
		String[] vars = {"var", "function", "="};
		highlights.addAll(this.findRanges(start, current, vars, fg_var, null, SWT.NORMAL));
		
		// Control flow
		String[] keywords = {"if", "else", "return", "\\?", ":" };
		highlights.addAll(this.findRanges(start, current, keywords, fg_keywords, null, SWT.NORMAL));
		
		// Comparators
		String[] comps = {"!", "\\&\\&", "\\|\\|", "===", "==", "!==", "!=", ">=", "<=", ">", "<"};
		highlights.addAll(this.findRanges(start, current, comps, fg_comps, null, SWT.BOLD));

		// value literals
		String[] literals = {"\\d+", "true", "false", "null", "\\d+\\.\\d+"};
		highlights.addAll(this.findRanges(start, current, literals, fg_literals, null, SWT.BOLD));

		// Built-in/implied objects and functions
		String[] builtins = {"require", "exports\\.(?:[a-zA-Z_$][a-zA-Z0-9_$]*)", "setTimeout", "setInterval", "decodeURIComponent"};
		highlights.addAll(this.findRanges(start, current, builtins, fg_builtins, null, SWT.ITALIC));

		// modules and barewords
		String[] modules = {"process", "console", "_", "(?:[a-zA-Z_$][a-zA-Z0-9_$]*)(?:\\.[a-zA-Z_$][a-zA-Z0-9_$]*)*"};
		highlights.addAll(this.findRanges(start, current, modules, fg_modules, null, SWT.BOLD));
		
		
		// braces and brackets
		String[] braces = {"[\\(\\{\\[]+[\\)\\}\\]]+;?", "\\{+", "\\}+;?", "\\(+", "\\)+;?", "\\[", "\\];?", "\\{\\};?", "\\[\\];?", "\\(\\);?", "[\\(\\{\\[]+;?", "[\\)\\}\\]]+;?"};
		highlights.addAll(this.findRanges(start, current, braces, fg_braces, null, SWT.NORMAL));
		
		
		// Single line comments
		highlights.addAll(this.findComments(start, current, fg_comments, null, SWT.ITALIC));
		
		// Double quoted comments?
		highlights.addAll(this.findDoubleQuotedStrings(start, current, fg_strings, null, SWT.ITALIC));
		highlights.addAll(this.findSingleQuotedStrings(start, current, fg_strings, null, SWT.ITALIC));

		// try for regex?
		highlights.addAll(this.findRegexBlock(start, current, fg_builtins, null, SWT.NORMAL));
		
		// quick sanity check
		if (highlights.size() == 0) {
			return;
		}
		
		// Ok, these are fairly dumb highlighters, just regex objects mostly, so we want to sort, and
		// then remove overlaps. We can get overlaps when two patterns match against the same
		// portion of a string. Most likely this means something like a Number in a Comment. We
		// want the comment styling, not the number styling, in that case.
		//
		// General idea -
		//		1. Sort so that if one stylerange contains the other, the larger range comes first
		//		2. Walk the sorted ranges and mark for removal
		//		3. Apply resulting ranges
		
		// apply the ranges. Fun times - they need to be in order...
		Collections.sort(highlights, new StyleRangeComparator());
		
		// mark for deletion by maintaining the most recent largest
		// matching range that hasn't been marked for deletion
		int rangeStart = highlights.get(0).start;
		int rangeEnd = rangeStart + highlights.get(0).length;
		ArrayList<Integer> removals = new ArrayList<Integer>();
		for (int i = 1; i < highlights.size(); i++) {
			
			// if this block is in the rangeStart/End, mark for deletion, otherwise
			// update the range
			StyleRange md = highlights.get(i);
			int sm = md.start;
			int em = md.start + md.length;
			
			if ( (rangeStart <= sm) && (em <= rangeEnd) ) {
				// contained
				removals.add(i);
			}
			else if ( (rangeStart <= sm) && (sm < rangeEnd) && (em > rangeEnd) ) {
				// overlap, keep the first?
				removals.add(i);
			}
			else {
				rangeStart = sm;
				rangeEnd = em;
			}
		}

		// walk backwards to delete
		for (int i = removals.size() - 1; i >= 0; i--) {
			highlights.remove(removals.get(i).intValue());
		}
		
		// bulk with comments: 2288 matches, ~ 221 millis
		// bulk apply with setStyleRanges: 2209 matches, ~ 293 millis
		// brute for apply with iterator + setStyleRange: 2209 matches, ~579 millis
		StyleRange[] ranges = new StyleRange[highlights.size()];
		ranges = highlights.toArray(ranges);
		
		this.jsEditor.replaceTextStyles(start, end - start, ranges);
		
		long ed = System.currentTimeMillis();
		DumbJavaScriptHighlighter.logger.debug(String.format("DumbHighlight found %d matches against %d characters in %d milliseconds", highlights.size(), current.length(), ed - st));

	}
	
	/**
	 * Returns true if and only style B is contained in the range of style A.
	 * @param a Possible larger style range
	 * @param b Possible contained style range
	 * @return True if first StyleRange contains second StyleRange
	 */
	public boolean styleRangeContainsStyleRange(StyleRange a, StyleRange b) {
		
		int sa = a.start;
		int ea = a.start + a.length;
		
		int sb = b.start;
		int eb = b.start + b.length;
		
		if ( (sa <= sb) && (eb <= ea) ) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Compare ranges so that non-overlapping ranges sort normally, but ranges that might contain one
	 * another are sorted differently... maybe?
	 * 
	 * @author patricknevindwyer
	 *
	 */
	private class StyleRangeComparator implements Comparator<StyleRange> {

		@Override
		public int compare(StyleRange s1, StyleRange s2) {
			return Integer.compare(s1.start, s2.start);
		}
		
	}
	
	/**
	 * Walk over a text segment and, using the provided set of Regex strings, find all matches. With those
	 * matches, construct StyleRanges for syntax highlighting using the provided foreground color, background
	 * color, and text style (SWT.(NORMAL | BOLD | ITALIC)).
	 * @param text Text to parse for highlighting
	 * @param terms Regex terms to use in the search
	 * @param fg Foreground color to apply to all matches
	 * @param bg Background color to apply to all matches
	 * @param ft_style Font style to apply to all matches
	 * @return A list (ordered) of all StyleRange objects for the matches
	 */
	public List<StyleRange> findRanges(int offset, String text, String[] terms, Color fg, Color bg, int ft_style) {
	
		List<StyleRange> ranges = new ArrayList<StyleRange>();

		// build up the terms
		StringBuilder sb = new StringBuilder();
		for (String term : terms) {
			sb.append(term + "|");
		}
		sb.deleteCharAt(sb.length() - 1);
		
		// Generate the pattern
		Pattern p_var = Pattern.compile("(\\b+|\\s+|\"|'|\\$|,)(" + sb.toString() +")(\\b+|\\s+|\"|'|\\$|,)");
		Matcher m_var = p_var.matcher(text);
		int last = 0;
		
		while (m_var.find(last)) {
			int m_start = m_var.start(2);
			int m_end = m_var.end(2);
			last = m_end;
			
			// apply a dumb highlight
			StyleRange m_sr = new StyleRange(offset + m_start, m_end - m_start, fg, bg, ft_style);
			ranges.add(m_sr);
		}

		return ranges;
		
	}
	
	public List<StyleRange> findDoubleQuotedStrings(int offset, String text, Color fg, Color bg, int ft_style) {
		/*
		 * 	
		 */
		List<StyleRange> ranges = new ArrayList<StyleRange>();

		// Generate the pattern
		Pattern p_var = Pattern.compile("(\"(?:[^\"\\\\]|\\\\.)*\")");
		Matcher m_var = p_var.matcher(text);
		int last = 0;
		
		while (m_var.find(last)) {
			int m_start = m_var.start(1);
			int m_end = m_var.end(1);
			last = m_end;
			
			// apply a dumb highlight
			StyleRange m_sr = new StyleRange(offset + m_start, m_end - m_start, fg, bg, ft_style);
			ranges.add(m_sr);
		}

		return ranges;
	}
	
	/**
	 * The single quoted string lookup is rather limited - if both the single quoted string and
	 * double quoted string matchers use the same regex (with different delimiters) we get an infinite
	 * recursion of the regex in apostrophes.
	 * 
	 * So, the Single Quote String lookup covers a very general case - find single quoted identifiers
	 * as they often occur in "requires" and object keys.
	 * 
	 * @param text Text to search for single quoted strings
	 * @param fg Foreground color of strings
	 * @param bg Background color of strings
	 * @param ft_style Font style for the quoted string
	 * @return
	 */
	public List<StyleRange> findSingleQuotedStrings(int offset, String text, Color fg, Color bg, int ft_style) {
		/*
		 * 	
		 */
		List<StyleRange> ranges = new ArrayList<StyleRange>();

		// Generate the pattern
		Pattern p_var = Pattern.compile("('(?:[ a-zA-Z0-9-:/\\._\\-\\(\\)&\\[\\]=])*')");
		Matcher m_var = p_var.matcher(text);
		int last = 0;
		
		while (m_var.find(last)) {
			int m_start = m_var.start(1);
			int m_end = m_var.end(1);
			last = m_end;
			
			// apply a dumb highlight
			StyleRange m_sr = new StyleRange(offset + m_start, m_end - m_start, fg, bg, ft_style);
			ranges.add(m_sr);
		}

		return ranges;
	}

	public List<StyleRange> findRegexBlock(int offset, String text, Color fg, Color bg, int ft_style) {
		/*
		 * 	
		 */
		List<StyleRange> ranges = new ArrayList<StyleRange>();

		// Generate the pattern
		Pattern p_var = Pattern.compile("(/.*?[^\\\\]/[gimy]*)");
//		Pattern p_var = Pattern.compile("('(?:[ a-zA-Z0-9-:/\\._\\-\\(\\)&\\[\\]=])*')");
		Matcher m_var = p_var.matcher(text);
		int last = 0;
		
		while (m_var.find(last)) {
			int m_start = m_var.start(1);
			int m_end = m_var.end(1);
			last = m_end;
			
			// apply a dumb highlight
			StyleRange m_sr = new StyleRange(offset + m_start, m_end - m_start, fg, bg, ft_style);
			ranges.add(m_sr);
		}

		return ranges;
	}
	public List<StyleRange> findComments(int offset, String text, Color fg, Color bg, int ft_style) {
		
		List<StyleRange> ranges = new ArrayList<StyleRange>();

		// Lookup for single line comments
		// Generate the pattern
		Pattern p_var = Pattern.compile("(//.*?)$", Pattern.MULTILINE);
		Matcher m_var = p_var.matcher(text);
		int last = 0;
		
		while (m_var.find(last)) {
			int m_start = m_var.start(1);
			int m_end = m_var.end(1);
			last = m_end;
			
			// apply a dumb highlight
			StyleRange m_sr = new StyleRange(offset + m_start, m_end - m_start, fg, bg, ft_style);
			ranges.add(m_sr);
		}
		
		// lookup for multiline comments
		//    /\\*.*?\\*/
		p_var = Pattern.compile("(/\\*.*?\\*/)", Pattern.MULTILINE | Pattern.DOTALL);
		m_var = p_var.matcher(text);
		last = 0;
		
		while (m_var.find(last)) {
			int m_start = m_var.start(1);
			int m_end = m_var.end(1);
			last = m_end;
			
			// apply a dumb highlight
			StyleRange m_sr = new StyleRange(offset + m_start, m_end - m_start, fg, bg, ft_style);
			ranges.add(m_sr);
		}
		return ranges;
		
	}
}
