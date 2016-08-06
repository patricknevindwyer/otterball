package com.programmish.otterball.ui.handlers;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import com.programmish.otterball.OBCore;

public class NewFileListener implements SelectionListener {

	public NewFileListener() {
		
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) { }

	@Override
	public void widgetSelected(SelectionEvent e) {
		
		OBCore.openNewFile();
		
	}
	
	
}
