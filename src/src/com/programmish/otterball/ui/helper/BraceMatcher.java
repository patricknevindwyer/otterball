package com.programmish.otterball.ui.helper;

import java.util.HashMap;

import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;

import com.programmish.otterball.ui.JSONShell;

public class BraceMatcher implements CaretListener {

	private static Logger logger = Logger.getLogger("civet." + BraceMatcher.class.getSimpleName());
	
	private StyledText editor;
	private int lastCloseBraceOffset;
	private int lastOpenBraceOffset;
	private Map<String,String> openToClose;
	private Map<String,String> closeToOpen;
	private JSONShell jsShell;
	
	public BraceMatcher(JSONShell jss, StyledText editor) {
		this.editor = editor;
		this.jsShell = jss;
		this.lastCloseBraceOffset = -1;
		this.lastOpenBraceOffset = -1;
		this.openToClose = new HashMap<>();
		this.closeToOpen = new HashMap<>();
		
		this.setupMatches();
		
		this.editor.addCaretListener(this);
	}
	
	private void setupMatches() {
		this.openToClose.put("(", ")");
		this.closeToOpen.put(")", "(");
		
		this.openToClose.put("{", "}");
		this.closeToOpen.put("}", "{");
		
		this.openToClose.put("[", "]");
		this.closeToOpen.put("]", "[");
	}

	/**
	 * If we have values for the last open and close braces, then we're currently on (caret
	 * location) one or the other. If we can, jump between the braces.
	 */
	public void jumpBraces() {
	
		int caretPos = this.editor.getCaretOffset();
		
		if ( (lastCloseBraceOffset != -1) && (lastOpenBraceOffset != -1) ) {
			int newPos = -1;
			
			if ( ( caretPos - 1) == lastOpenBraceOffset ) {
				newPos = lastCloseBraceOffset;
			}
			else if ( caretPos == lastCloseBraceOffset) {
				newPos = lastOpenBraceOffset + 1;
			}
			
			if (newPos != -1) {
				this.editor.setCaretOffset(newPos);
				
				this.jsShell.scrollToOffset(newPos);
				// we may need to also move the new caret position into view
//				int visibleLines = editor.getBounds().height / (editor.getLinePixel(2) - editor.getLinePixel(1));
//				int visibleLines = this.jsShell.getVisibleLines();
//				BraceMatcher.logger.debug(String.format("There are %d visible lines", visibleLines));
			}
		}
	}
	
	@Override
	public void caretMoved(CaretEvent e) {

		// setup our raw data
		int caretPos = e.caretOffset;
		String text = this.editor.getText();
		
		String prevChar = "";
		String nextChar = "";
		
		if (caretPos > 0) {
			prevChar = text.substring(caretPos - 1, caretPos);
		}
		
		if (caretPos < text.length()) {
			nextChar = text.substring(caretPos, caretPos + 1);
		}

		// turn off the old braces
		if (lastOpenBraceOffset != -1) {
			toggleHighlight(lastOpenBraceOffset, false);
			lastOpenBraceOffset = -1;
		}
		
		if (lastCloseBraceOffset != -1) {
			toggleHighlight(lastCloseBraceOffset, false);
			lastCloseBraceOffset = -1;
		}
		
		// start figuring a few bits out
		if ( openToClose.containsKey(prevChar) ) {
			String openChar = prevChar;
			String closeChar = openToClose.get(openChar);
			
			BraceMatcher.logger.debug("open brace on left");
			
			lastOpenBraceOffset = caretPos - 1;
			
			// now seek out the close brace for this open brace
			int openCount = 0;
			boolean found = false;
			int seekPos = caretPos;
			
			while ( !found && (seekPos < text.length()) ) {
				
				String seekChar = text.substring(seekPos, seekPos + 1);
				
				if (seekChar.equals(openChar)) {
					openCount += 1;
				}
				else if (seekChar.equals(closeChar)) {
					
					if (openCount == 0) {
						found = true;
						lastCloseBraceOffset = seekPos;
					}
					else {
						openCount -= 1;
					}
				}
				seekPos += 1;
			}
			
			if (!found) {
				lastCloseBraceOffset = -1;
			}
			
		}
		else if ( closeToOpen.containsKey(nextChar) ) {
			String closeChar = nextChar;
			String openChar = closeToOpen.get(closeChar);
			
			BraceMatcher.logger.debug("close brace on right");
			
			lastCloseBraceOffset = caretPos;
			
			// now seek out the open brace for this close brace
			int closeCount = 0;
			boolean found = false;
			int seekPos = caretPos - 1;
			
			while ( !found && (seekPos >= 0)) {
				
				String seekChar = text.substring(seekPos, seekPos + 1);
				
				if (seekChar.equals(closeChar)) {
					closeCount += 1;
				}
				else if ( seekChar.equals(openChar)) {
					
					if (closeCount == 0) {
						found = true;
						lastOpenBraceOffset = seekPos;
					}
					else {
						closeCount -= 1;
					}
				}
				seekPos -= 1;
			}
			
			if (!found) {
				lastOpenBraceOffset = -1;
			}
		}
		
		if (lastOpenBraceOffset != -1) {
			toggleHighlight(lastOpenBraceOffset, true);
		}
		
		if (lastCloseBraceOffset != -1) {
			toggleHighlight(lastCloseBraceOffset, true);
		}
	}
	
	private void toggleHighlight(int offset, boolean toggle) {
		
		// Find the current style used on the brace
		StyleRange style = this.editor.getStyleRangeAtOffset(offset);

		if (style != null) {
			
			// toggle the style setting
			if (toggle) {
				style.borderStyle = SWT.BORDER_SOLID;
			}
			else {
				style.borderStyle = SWT.NONE;
			}
			this.editor.setStyleRange(style);
		}
		else {
			
			// setup a style for this space - it doesn't have a style yet...
			if (toggle) {
				style = new StyleRange(offset, 1, null, null, SWT.NORMAL);
				style.borderStyle = SWT.BORDER_SOLID;
				this.editor.setStyleRange(style);
				
			}
		}
	}
	
}
