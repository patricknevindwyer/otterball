package com.programmish.otterball.ui.helper;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

public class AutoIndenter implements VerifyListener, ExtendedModifyListener {

	private StyledText editor;
	private int tabSize = 4;
	private Map<Integer, String> tabCache;
	private int modifyCaret = 0;
	
	public AutoIndenter(StyledText editor) {
		this.editor = editor;
		this.tabCache = new HashMap<>();
		
		this.editor.addVerifyListener(this);
		this.editor.addExtendedModifyListener(this);
	}

	@Override
	public void verifyText(VerifyEvent e) {
		
		if (e.end - e.start == 0) {
			// Simple text insert
			
			modifyCaret = 0;
			
			if ( e.text.equals("\n") ) {
				
				// do the calculations to get the actual previous and next characters from where
				// we are typing enter
				String prevLine = this.editor.getLine(this.editor.getLineAtOffset(this.editor.getCaretOffset()));
				int lineOffset = this.editor.getOffsetAtLine(this.editor.getLineAtOffset(this.editor.getCaretOffset()));
				int offsetInLine = this.editor.getCaretOffset() - lineOffset;
				
				String tailingCharacter = "";
				String leadingCharacter = "";
				
				if (prevLine.length() > 1) {
					if (offsetInLine - 1 >= 0) {
						tailingCharacter = prevLine.substring(offsetInLine - 1, offsetInLine);
					}
				}
				if (offsetInLine < prevLine.length()) {
					leadingCharacter = prevLine.substring(offsetInLine, offsetInLine + 1);
				}
				
				System.out.println("Tail/Lead = [" + tailingCharacter + "," + leadingCharacter + "]");
				int currentIndent = this.getIndentOfString(prevLine);
				
				if ( (tailingCharacter.equals("{") ) && (leadingCharacter.equals("}") ) ) {
					// special case - we're hitting enter between two block brackets, and we should
					// insert a proper set of indents
					e.text = "\n" + this.buildIndent(currentIndent + 1) + "\n" + this.buildIndent(currentIndent);
					
					// TODO: we also need to adjust the caret on this one...
					modifyCaret = -1 * currentIndent * this.tabSize - 1;
				}
				else if (tailingCharacter.equals("{")) {
					e.text = "\n" + this.buildIndent(currentIndent + 1);
				}
				else {
					e.text = "\n" + this.buildIndent(currentIndent);
				}
				
			}
			else if (e.text.equals("}")) {
				// we need to drop the indent if the line starts with spaces...
				System.out.println("-- de-indent");
			}
			
		}
		
	}
	
	
	
	@Override
	public void modifyText(ExtendedModifyEvent e) {
		if (this.modifyCaret != 0) {
			this.editor.setCaretOffset(this.editor.getCaretOffset() + this.modifyCaret);
		}
	}

	/**
	 * Determine the indent at the given line. The indent is calculated as the number of spaces
	 * at the start of the line divided by the tab size.
	 * 
	 * @param line Line number
	 * @return Number of indents
	 */
	private int getIndentOfString(String line) {
		int spaces = 0;
		
		if (line.length() == 0) {
			return 0;
		}
		
		while ( (spaces < line.length()) && ( line.charAt(spaces) == ' ') ) {
			spaces += 1;
		}
		
		return spaces / this.tabSize;
	}
	
	/**
	 * Construct the string representing the given indent size.
	 * 
	 * @param size Size of the indent
	 * @return A String with the proper number of indent spaces
	 */
	private String buildIndent(int size) {
		
		if (this.tabCache.containsKey(size)) {
			return this.tabCache.get(size);
		}
		
		StringBuilder sb = new StringBuilder();
		
		for (int i = 0 ; i < (size * this.tabSize) ; i ++) {
			sb.append(" ");
		}
		
		tabCache.put(size, sb.toString());
		return sb.toString();
	}
	
}
