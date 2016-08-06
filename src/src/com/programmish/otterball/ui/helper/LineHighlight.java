package com.programmish.otterball.ui.helper;

import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;

import com.programmish.otterball.ui.themes.Theme;

public class LineHighlight implements CaretListener {

	private StyledText editor;
	private Color lineBackground;
	private Color defaultBackground;
	
	private int lastLine = -1;
	
	public LineHighlight(StyledText editor) {
		this.editor = editor;
		Theme t = Theme.getTheme();
		
		this.lineBackground = t.getColor("currentline.background");
		this.defaultBackground = t.getColor("base.background");
		
		this.editor.addCaretListener(this);
	}

	@Override
	public void caretMoved(CaretEvent event) {
		
		int lineNumber = this.editor.getLineAtOffset(event.caretOffset);
		
		// don't re-highlight the line if we're on the same time
		if (lineNumber != this.lastLine) {

			if ( (this.lastLine >= 0) && (this.lastLine < this.editor.getLineCount()) ) {
				// reset old backgrounds, reset line background
				this.editor.setLineBackground(this.lastLine, 1, this.defaultBackground);
			}
			
			// read the styles of the line, and reset the backgrounds
			
			// set the new background for the line
			this.editor.setLineBackground(lineNumber, 1, this.lineBackground);
			
			this.lastLine = lineNumber;
		}
	}
	
	
}
