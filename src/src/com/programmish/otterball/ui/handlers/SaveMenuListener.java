package com.programmish.otterball.ui.handlers;

import org.apache.log4j.Logger;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

import com.programmish.otterball.OBCore;
import com.programmish.otterball.ui.OBEditor;

public class SaveMenuListener implements SelectionListener {
	static Logger logger = Logger.getLogger("otterball." + SaveMenuListener.class.getSimpleName());
	
	public SaveMenuListener() {
		
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {}

	@Override
	public void widgetSelected(SelectionEvent e) {
		SaveMenuListener.logger.debug("Trying to save file contents");
		
		// find the active window
		OBEditor window = OBCore.getActiveWindow();
		OBCore.saveWindow(window);
		
	}
	
}
