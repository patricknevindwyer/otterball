package com.programmish.otterball.ui.helper;

import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Label;

public class CaretStatus implements CaretListener {

	private StyledText editor;
	private Label statusFooter;
	
	public CaretStatus(StyledText editor, Label footer) {
		this.editor = editor;
		this.statusFooter = footer;
		
		this.editor.addCaretListener(this);
		this.updateFooter();
	}
	
	private void updateFooter() {
		int lineCount = this.editor.getLineCount();
		int curLine = this.editor.getLineAtOffset(this.editor.getCaretOffset()) + 1;
		
		this.statusFooter.setText(String.format("Line %d / %d", curLine, lineCount));
	}
	
	@Override
	public void caretMoved(CaretEvent arg0) {
		updateFooter();		
	}


}
