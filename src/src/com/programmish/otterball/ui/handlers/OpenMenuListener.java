package com.programmish.otterball.ui.handlers;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.FileDialog;

import com.programmish.otterball.OBCore;

/**
 * A fairly straight-forward SelectionListener. We want to intercept menu
 * events from the OpenFile menu, provide an Open File dialog, and route 
 * the results to CivetCore to handle actually opening the file. 
 * @author patricknevindwyer
 *
 */
public class OpenMenuListener implements SelectionListener {

	private static Logger logger = Logger.getLogger("civet." + OpenMenuListener.class.getSimpleName());
	
	// What extensions/types are we opening?
	private List<String> types;
	
	public void addTypes(String... t) {
		for (String tt : t) {
			this.types.add(tt);
		}
	}
	
	/**
	 * The default OpenMenuListener filters for *.js and *.json files.
	 */
	public OpenMenuListener() {
		
		this.types = new ArrayList<String>();
		
		// set our default types
		this.addTypes("*.js", "*.json");
		
	}
	
	/**
	 * Create an Open file dialog for a custom set of file types.
	 * @param atypes
	 */
	public OpenMenuListener(String... atypes) {
		this.types = new ArrayList<String>();
		this.addTypes(atypes);
	}
	
	@Override
	public void widgetSelected(SelectionEvent e) {

		// pop an open file dialog, and delegate the results
		FileDialog fd = new FileDialog(OBCore.debugWindow.getWindowShell(), SWT.OPEN | SWT.MULTI);
        fd.setText("Open");
        fd.setFilterPath(null);
        String [] ftypes = new String[this.types.size()];
        this.types.toArray(ftypes);
        fd.setFilterExtensions(ftypes);
        String selected = fd.open();
        String filePath = fd.getFilterPath();
        
        OpenMenuListener.logger.debug(String.format("OpenMenuListener -> SelectionEvent [%s]", selected));
        
        for (String filename : fd.getFileNames()) {
        	OBCore.openFile(Paths.get(filePath, filename).toString());
        }
        
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {}
	
	
}