package com.programmish.otterball.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import com.programmish.otterball.OBCore;
import com.programmish.otterball.sys.DeepFilePointer;

public class Jumper implements ModifyListener, SelectionListener, KeyListener {
	
	private static Jumper jumper;

	// UI and context layout
//	private Display parent;
	private Shell jumperShell;
	private List results;
	private Text search;
	
	// data control for the list element
	private ArrayList<DeepFilePointer> rawValues;
	private ArrayList<Integer> filteredIndexes;
	private String filter;
	
	private Jumper (Display parent) {
		
//		this.parent = parent;
		this.jumperShell = new Shell(parent, SWT.ON_TOP);
		
		// setup the basic data controls
		rawValues = new ArrayList<DeepFilePointer>();
		filteredIndexes = new ArrayList<Integer>();
		filter = "";
		
		this.layoutJumper();
		
		
		this.search.addModifyListener(this);
		this.search.addSelectionListener(this);
		this.search.addKeyListener(this);
		
		this.results.addKeyListener(this);
		
		this.jumperShell.open();
	}
	
	/**
	 * Load the values that comprise the search index used by jumper.
	 */
	private void loadIndex() {
		rawValues.clear();
		
		for (DeepFilePointer dfp : OBCore.getSearchIndex()) {
			rawValues.add(dfp);
		}
		Collections.sort(rawValues, new Comparator<DeepFilePointer>() {
			@Override
			public final int compare(DeepFilePointer a, DeepFilePointer b) {
				return a.getReadableString().compareTo(b.getReadableString());
			}
		});
	}
	
	/**
	 * Use the rawValues indexes to build a list of indexes that match the
	 * current search criteria.
	 */
	private void filterValues() {
		filteredIndexes.clear();
		
		String lcfilter = filter.toLowerCase();
		for (int i = 0 ; i < rawValues.size(); i++) {
			if (filter.equals("") || rawValues.get(i).matches(lcfilter)) {
				filteredIndexes.add(i);
			}
		}
	}
	
	/**
	 * Update the results list to use the readable string value of the matched
	 * rawValues.
	 */
	private void updateResults() {
		results.removeAll();
		for (int idx : filteredIndexes) {
			results.add(rawValues.get(idx).getReadableString());
		}
		
		System.out.println("item height: " + results.getItemHeight());
		updateJumperSize();
	}
	
	private void updateJumperSize() {
	
		int maxHeight = 300;
		int searchHeight = search.getSize().y;
		int resultsHeight = results.getItemCount() * results.getItemHeight();
		int padding = 5;
		int newHeight = Math.min(maxHeight, searchHeight + resultsHeight + padding);
		jumperShell.setSize(jumperShell.getSize().x, newHeight);
	}
	
	public static synchronized Jumper getJumper() {
		if (Jumper.jumper == null) {
			Jumper.jumper = new Jumper(Display.getCurrent());
		}
		
		Jumper.jumper.loadIndex();
		Jumper.jumper.filterValues();
		Jumper.jumper.updateResults();
		Jumper.jumper.setVisible(true);
		
		return Jumper.jumper;
	}
	
	public void setVisible(boolean state) {
	
		this.jumperShell.setVisible(state);
		if (state) {
			this.results.setSelection(0);
			this.search.setFocus();
			this.search.selectAll();
		}
	}
	
	private void layoutJumper() {
		jumperShell.setSize(200, 300);
		
		// setup the layout on the jumper shell
		FormLayout baseLayout = new FormLayout();
		baseLayout.marginWidth = 1;
		baseLayout.marginHeight = 1;
		
		jumperShell.setLayout(baseLayout);
		
		// setup the search area
		search = new Text(jumperShell, SWT.SEARCH | SWT.ICON_CANCEL | SWT.ICON_SEARCH);
		FormData searchData = new FormData();
		searchData.top = new FormAttachment(0, 0);
		searchData.left = new FormAttachment(0, 0);
		searchData.right = new FormAttachment(100, 0);
		search.setLayoutData(searchData);
		
		// setup the results area
		results = new List(jumperShell, SWT.SINGLE | SWT.V_SCROLL);
		FormData listData = new FormData();
		listData.top = new FormAttachment(search);
		listData.left = new FormAttachment(0, 0);
		listData.right = new FormAttachment(100, 0);
		listData.bottom = new FormAttachment(100, 0);
		results.setLayoutData(listData);
		
		for (int i = 0 ; i < 10; i++) {
			results.add("Item " + i);
		}
	}

	@Override
	public void modifyText(ModifyEvent e) {
		this.filter = search.getText();
		
		this.filterValues();
		this.updateResults();
		
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		if (e.detail != SWT.ICON_CANCEL) {
			// peek into the search results to figure out what happened
			if (this.filteredIndexes.size() != 0) {
				// pick out the selection
				OBCore.openFile(this.getSelectedFilePointer(0));
				this.setVisible(false);
			}
		}		
	}

	private DeepFilePointer getSelectedFilePointer(int index) {
	
		int idx = this.filteredIndexes.get(index);
		return this.rawValues.get(idx);
	}
		
	@Override
	public void widgetSelected(SelectionEvent e) {}

	@Override
	public void keyPressed(KeyEvent e) {}

	@Override
	public void keyReleased(KeyEvent e) {
		if (e.character == SWT.ESC) {
			this.setVisible(false);
		}
		else if ( (e.widget == this.results) && ( ( e.character == SWT.CR ) ||  ( e.character == SWT.LF ) ) ) {
//			System.out.println("enter from the table");
			int selectionIdx = this.results.getSelectionIndex();
			
			if (selectionIdx >= 0) {
				OBCore.openFile(this.getSelectedFilePointer(this.results.getSelectionIndex()));
				this.setVisible(false);
			}
		}
		
	}
	
	
	
}
