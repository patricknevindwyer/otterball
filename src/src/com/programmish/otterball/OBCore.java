package com.programmish.otterball;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;


import com.programmish.otterball.ui.OBEditor;
import com.programmish.otterball.ui.OBWindow;
import com.programmish.otterball.sys.DeepFilePointer;

import com.programmish.otterball.ui.JSONShell;
import com.programmish.otterball.ui.DebugShell;
import com.programmish.otterball.ui.MenuManager;

import com.programmish.otterball.sys.DeepFilePointer;

public class OBCore {

	// We keep a static reference to the windows so we can cleanly reference the open
	// shells throughout Civet, without passing around a ref to CivetCore
	protected static List<OBEditor> editors = new ArrayList<OBEditor>();
	
	// Root display for the app
	protected Display civetDisplay;
	
	// Gotta log. This is wired up to DebugShell as an appender
	private static Logger logger = Logger.getLogger("civet." + OBCore.class.getSimpleName());
	
	// We always keep the Debug window around - we can summon it with a keystroke if need be
	public static OBWindow debugWindow;
	
	/**
	 * Launch the application.
	 * @param args
	 */
	public static void main(String[] args) {
		logger.info("Starting CivetCore");
		
		try {
			OBCore core = new OBCore();
			core.start();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static OBEditor getActiveWindow() {
	
		for (OBEditor cw : OBCore.editors) {
			if (cw.isActiveWindow()) {
				return cw;
			}
		}
		return null;
	}
	
	/**
	 * Create a basic search index that can be used to look through any open file
	 * or related project files. This is, right now, just a list of all the files
	 * either open, or if an open file has a related Package.json, all the files
	 * in the related project (excluding node_modules references).
	 * 
	 * @return
	 */
	public static List<DeepFilePointer> getSearchIndex() {
		List<DeepFilePointer> index = new ArrayList<DeepFilePointer>();
				
		OBCore.logger.debug("Building Jumper index");
		
		// get the list of open files
		List<String> openFiles = new ArrayList<>();
		
		for (OBEditor c : OBCore.editors) {
			if (c.getFilePath() != null ) {
				openFiles.add(c.getFilePath());
				OBCore.logger.debug(String.format("Adding [%s] to list of open files", c.getFilePath()));
			}
		}
						
		// return the union of orphaned files and referenced ProjectJson files
		for (String ofile : openFiles) {
			index.add(new DeepFilePointer(ofile));
		}
		
		return index;
	}
	
	public static List<OBEditor> getEditor() {
		return OBCore.editors;
	}
	
	/**
	 * Close the given window.
	 * 
	 * @param window
	 */
	public static void closeWindow(OBWindow window) {
		
		// Find the index for the window
		int idx = -1;
		for (int i = 0 ; i < OBCore.editors.size() ; i++) {
			if ( OBCore.editors.get(i) == window ) {
				idx = i;
			}
		}
		
		// Close the window - if this is the debug window (always index 0), just leave it in
		// place, and call close on it.
		if (idx >= 0) {
			OBCore.editors.remove(idx);
			window.close();
		}
	}
	
	/**
	 * Save the contents of the window, returning true if the window was saved, otherwise false.
	 * @param w
	 * @return
	 */
	public static boolean saveWindow(OBEditor window) {
		
		boolean saved = false;
		
		if (window.isDirty()) {
			OBCore.logger.debug("Trying to save window contents... dispatching.");
			String curPath = window.getFilePath();
			
			if (curPath != null) {
				// window has an existing path, so we can save quickly
				window.saveContents(curPath);
				saved = window.isDirty();
			}
			else {
				// window has no path, we need to pop up a Save As
				// dialog
				OBCore.logger.debug("Window doesn't have a file path - issueing Save As dialog");
				
				// pop an open file dialog, and delegate the results
				FileDialog fd = new FileDialog(Display.getCurrent().getActiveShell(), SWT.SAVE);
		        fd.setText("Save As");
		        fd.setFilterPath(null);
		        fd.setOverwrite(true);
		        String[] filterExtensions = {"*.js"};
		        fd.setFilterExtensions(filterExtensions);
		        
		        String selected = fd.open();
		        
		        OBCore.logger.debug(String.format("SaveMenuListener -> SelectionEvent [%s]", selected));
		        if (selected != null) {
		        	window.saveContents(selected);
		        }
		        
		        saved = window.isDirty();

			}
		}
		else {
			saved = false;
		}
		
		return saved;
	}
	
	/**
	 * Open a blank file in the JavaScriptShell.
	 */
	public static void openNewFile() {
		OBCore.editors.add(new JSONShell(Display.getCurrent()));
	}
	
	public static void openFile(DeepFilePointer dfp) {
		OBCore.openFile(dfp.getAbsoluteFilename());
		
		// if window is not null, we can jump to parts of the file
	}
	
	/**
	 * Open the file at the given path (if it exists and is readable) into the proper Shell type, based
	 * on the file extension. This provides a decently clean separation of the actual heuristics/open
	 * logic and where the trigger came from to open the file.
	 * 
	 * @param filepath File to open in Civet
	 */
	public static OBWindow openFile(String filepath) {
		
		if (filepath == null) {
			return null;
		}
		
		// determine if this file is already open
		for (OBEditor w : OBCore.editors) {
			if ( (w.getFilePath() != null) && w.getFilePath().equals(filepath)) {
				// short circuit and focus the window
				w.setActiveWindow();
				return w;
			}
		}
		
		// determine what type of file we have, and a few attributes
		Path p = Paths.get(filepath);
		File f = p.toFile();
		boolean exists = f.exists();
		boolean canRead = f.canRead();
		String[] bits = p.getFileName().toString().split("\\.");
		String ext = "";
		if (bits.length > 1) {
			ext = bits[bits.length - 1];
		}
		
		OBCore.logger.debug(String.format("Opening file [%s] ext(%s) exists(%s) canRead(%s)", p.getFileName().toString(), ext, exists, canRead));
		
		// short circuit fast
		if (!exists || !canRead) {
			OBCore.logger.debug("Bailing, don't like the looks of this file...");
			return null;
		}
		
		// Delegate to the proper window type
		if (ext.equalsIgnoreCase("json")) {
			// woo
			OBCore.logger.debug("Opening JSON file");
			OBEditor jss = new JSONShell(Display.getCurrent(), filepath);
			OBCore.editors.add(jss);
			return jss;
		}
		else {
			OBCore.logger.debug("Not sure what we're trying to open...");
			return null;
		}
	}
	
	/**
	 * Open the window.
	 */
	public void start() {
		
		// basic bits and pieces
		Display.setAppName("Civet");
		this.civetDisplay = new Display();
		
		// setup the debug
		OBCore.debugWindow = new DebugShell(this.civetDisplay);

		// construct the application menus
		MenuManager mm = MenuManager.getManager();
		mm.constructMenus();
		
		// do some logging!
		OBCore.logger.info("Entering message dispatch");
		
		this.envReport();

		// Sleep away - the program exit is handled by a listening event on the Quit menu option
		while (!this.civetDisplay.isDisposed()) {
			if (!this.civetDisplay.readAndDispatch()) {
				this.civetDisplay.sleep();
			}
		}
	}
	
	/**
	 * Run a report on the system properties, and route the results to the logger
	 */
	private void envReport() {
		
		final Display d = this.civetDisplay;
		
		new Thread("env-report") {
			
			public void run() {
				final List<String> reports = new ArrayList<String>();
				
				Map<String, String> envs = System.getenv();
				for (String k : envs.keySet()) {
					reports.add(String.format("System.getenv(\"%s\") = \"%s\"", k, envs.get(k)));
				}
				
				String [] propsOfInterest = {"os.name", "os.arch", "os.version"};
				
				for (String key : propsOfInterest) {
					reports.add(String.format("System.getProperty(\"%s\") = \"%s\"", key, System.getProperty(key)));
				}				
				
				d.asyncExec(new Runnable() {
					
					public void run() {
						for (String msg : reports) {
							OBCore.logger.info(msg);
						}
					}
					
				});
			}
		
		}.start();
	}
}
