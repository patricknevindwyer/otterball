package com.programmish.otterball.ui.helper;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.graphics.Point;

public class CommentToggler implements KeyListener {

    private StyledText editor;
    private int metaKey;
    
    public CommentToggler(StyledText editor) {
    	this.editor = editor;
    	this.metaKey = UIUtils.getControlMetaKey();
    	
        editor.addKeyListener(this);

    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.events.KeyListener#keyPressed(org.eclipse.swt.events.
     * KeyEvent)
     */
    public void keyPressed(KeyEvent e) {
    	
    	// listen for MOD+/ for comment toggle
    	boolean isMod = (e.stateMask & this.metaKey) > 0;
    	boolean isAlt = (e.stateMask & SWT.ALT) > 0;
        boolean isShift = (e.stateMask & SWT.SHIFT) > 0;

        
        if (isMod && !isAlt) {
        	
        	if (!isShift && e.keyCode == '/') {
        		
        		// calculate the current selection range and mode
        		Point selection = this.editor.getSelection();
        		boolean hasSelection = (selection.y - selection.x) > 0;
        		
        		if (hasSelection) {
        		
        			// we need to indent a series of lines, and determine how to change
        			// the indent based upon the indent of the first line...
        			int offsetStart = selection.x;
        			int offsetEnd = selection.y;
        			
        			int lineStart = this.editor.getLineAtOffset(offsetStart);
        			int lineEnd = this.editor.getLineAtOffset(offsetEnd);
        		
        			for (int x = lineStart; x <= lineEnd; x++) {
        				this.toggleLine(x);
        			}
        			
        		}
        		else {
        			
        			/*
        			 * Toggle only the current line
        			 */
        			
            		int offset = this.editor.getCaretOffset();
            		int lineNumber = this.editor.getLineAtOffset(offset);
            		        		
        			this.toggleLine(lineNumber);
        		}
        		
        	}
        }
    }

    private void toggleLine(int lineNumber) {
		// character position at start of current line
		int lineOffset = this.editor.getOffsetAtLine(lineNumber);
		
		// text of current line
		String line = this.editor.getLine(lineNumber);
		
		if (line.matches("^\\s*//.*$")) {
			// already have a comment, strip it
			int toggleStart = line.indexOf("//");
			this.editor.replaceTextRange(lineOffset + toggleStart, 2, "");
		}
		else {
			this.editor.replaceTextRange(lineOffset, 0, "//");	
		}
    	
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.swt.events.KeyListener#keyReleased(org.eclipse.swt.events
     * .KeyEvent)
     */
    public void keyReleased(KeyEvent e) {
        // ignore
    }

    
}
