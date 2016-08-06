package com.programmish.otterball.ui.themes;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;

/**
 * The Theme loader in OtterBall is a global singleton - whichever theme is currently loaded is used for all windows.
 * 
 * @author patricknevindwyer
 *
 */
public class Theme {

	private static Theme themeSingleton;
	private static Logger logger;
	private String themeName;
	private Map<String,Color> themeColors;
	
	private Theme() {
		Theme.logger = Logger.getLogger("otterball." + Theme.class.getSimpleName());
		themeName = "dark";
		
		this.themeColors = new HashMap<String,Color>();
	}
	
	public static synchronized Theme getTheme() {
		
		if (themeSingleton == null) {
			themeSingleton = new Theme();
			themeSingleton.readThemeFile();
		}
		return themeSingleton;
	}
	
	private void readThemeFile() {
		Properties p = new Properties();
		try {
			p.load(Theme.class.getResourceAsStream(this.themeName + ".properties"));
		}
		catch (IOException ioe) {
			Theme.logger.error(String.format("Trouble loading the %s theme file", this.themeName));
		}
				
		for (String colorName : p.stringPropertyNames()) {
			if (colorName.startsWith("color.")) {
				String storedName = colorName.replace("color.", "");
				this.themeColors.put(storedName, this.getColorFromRGB(p.getProperty(colorName)));
			}
		}
	}
	
	private Color getColorFromRGB(String rgb) {
		
		// convert this string to a valid color
		String[] bits = rgb.replace("\"", "").split(",");
		return new Color(Display.getCurrent(), Integer.parseInt( bits[0] ), Integer.parseInt( bits[1] ), Integer.parseInt( bits[2] ));
	}
	
	public String getFontFamily() {
		return "";
	}
	
	public Color getColor(String name) {
		return this.themeColors.get(name);
	}
}
