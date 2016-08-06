package com.programmish.otterball.ui.handlers;

import org.eclipse.swt.widgets.Event;

import org.eclipse.swt.widgets.Listener;

import com.programmish.otterball.OBCore;
import com.programmish.otterball.ui.OBEvent;
import com.programmish.otterball.ui.OBWindow;

public class CivetEventDispatcher implements Listener {

	private OBEvent event;
	
	public CivetEventDispatcher(OBEvent ce) {
		this.event = ce;
	}
	
	public void handleEvent(Event e) {
		OBWindow window = OBCore.getActiveWindow();
		if (window != null) {
			window.dispatchEvent(this.event);
		}
	}
}
