package com.programmish.otterball.ui;

import org.eclipse.swt.widgets.Shell;

public interface OBWindow {

	public boolean isActive();
	public boolean isActiveWindow();
	public void setActiveWindow();
	public void setVisible(boolean visibility);
	public boolean getVisible();
		
	// Event controls
	public void dispatchEvent(OBEvent ce);
		
	// get the root elements
	public Shell getWindowShell();
	
	// window control
	public void close();
	
}
