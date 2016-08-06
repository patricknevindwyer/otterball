package com.programmish.otterball.ui.helper;

import org.eclipse.swt.SWT;

public class UIUtils {

	public static int getControlMetaKey() {
		if (System.getProperty("os.name").startsWith("Mac")) {
			return SWT.COMMAND;
		}
		else {
			return SWT.CTRL;
		}
	}
}
