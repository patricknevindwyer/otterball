package com.programmish.otterball.ui.helper;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.custom.VerifyKeyListener;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

public class TabToSpace implements VerifyListener, VerifyKeyListener {

	private StyledText editor;
	private int spaces;
	private String spacer;
	
	public TabToSpace(StyledText editor, int spaces) {
		this.editor = editor;
		this.spaces = spaces;
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < spaces; i++) {
			sb.append(" ");
		}
		this.spacer = sb.toString();
		this.editor.addVerifyListener(this);
		this.editor.addVerifyKeyListener(this);
	}

	@Override
	public void verifyText(VerifyEvent event) {
		if (event.end - event.start == 0) {
			if (event.text.equals("\t")) {
				event.text = this.spacer;
			}
		}
		
	}

	@Override
	public void verifyKey(VerifyEvent event) {
		if ( (event.character == '\u0008') || (event.character == '\u007F') ) {
			
			// if we're at the start of a line, and at a caret position a multiple of
			// the tab indenter, we should back out the proper number of spaces
			int curLine = this.editor.getLineAtOffset(this.editor.getCaretOffset());
			int curOffset = this.editor.getOffsetAtLine(curLine);
			int caretOffset = this.editor.getCaretOffset();
			int linePos = caretOffset - curOffset;
			String lineContent = this.editor.getLine(curLine);
			String lead = lineContent.substring(0, linePos);
			
			if (linePos % this.spaces == 0) {
				if (lead.matches("^\\s+$")) {
					this.editor.replaceTextRange(curOffset + linePos - this.spaces, this.spaces - 1, "");
				}
			}
		}		
	}
	
	
	
	
}
