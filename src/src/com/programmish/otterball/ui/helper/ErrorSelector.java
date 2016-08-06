package com.programmish.otterball.ui.helper;


import java.util.List;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Table;

import com.programmish.otterball.validation.SourceAnalysis;
import com.programmish.otterball.ui.OBEditor;

public class ErrorSelector implements SelectionListener {

	private StyledText editor;
	private Table errorTable;
	private List<SourceAnalysis> analysis;
	private OBEditor obEditor;
	
	public ErrorSelector(OBEditor ce, StyledText editor, Table table) {
		this.editor = editor;
		this.errorTable = table;
		this.obEditor = ce;
		errorTable.addSelectionListener(this);
		
	}
	
	public void setAnalysis(List<SourceAnalysis> analysis) {
		this.analysis = analysis;
	}
	
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		// nop
	}

	@Override
	public void widgetSelected(SelectionEvent e) {

		// get the selected line
		int idx = errorTable.getSelectionIndex();
		SourceAnalysis a = analysis.get(idx);
		int line = a.getLineNumber() - 1;
		int offset = editor.getOffsetAtLine(line);
		editor.setSelection(offset);
		obEditor.centerLine(line);
		
	}
}
