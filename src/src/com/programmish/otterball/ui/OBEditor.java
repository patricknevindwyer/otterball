package com.programmish.otterball.ui;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import com.programmish.otterball.sys.FileUtils;
import com.programmish.otterball.ui.helper.ErrorSelector;
import com.programmish.otterball.ui.helper.Gutter;
import com.programmish.otterball.ui.themes.Theme;
import com.programmish.otterball.validation.SourceAnalysis;

import swing2swt.layout.BorderLayout;

public abstract class OBEditor implements OBWindow, ModifyListener {

	protected static Logger logger = Logger.getLogger("otterball." + OBEditor.class.getSimpleName());
	
	// SWT components used throughout the editor
	protected Display parentDisplay;
	protected StyledText editor;
	protected Shell editorShell;
	protected SashForm editorSash;
	protected Table errorTable;
	protected Label statusLabel;
	protected Gutter editorGutter;
	
	// UI/Feature control
	protected ErrorSelector errorSelector;
	
	// File control
	protected boolean isUnsaved;
	protected String filepath;
		
	/**
	 * Open a blank, unsaved editor.
	 * 
	 * @param parent
	 */
	public OBEditor(Display parent) {
		
		this.parentDisplay = parent;
		this.isUnsaved = true;
		
		this.layoutEditor();
		
		this.editor.setText("");
		
		this.addEditorFeatures();
		this.openEditor();
		this.postOpen();
		
		// ping the window title
		this.updateTitle();

	}

	/**
	 * Open a file into the editor.
	 * 
	 * @param parent
	 * @param jsFilePath
	 */
	public OBEditor(Display parent, String filePath) {

		this.parentDisplay = parent;

		// Basic control data for the editor
		this.filepath = filePath;
		this.isUnsaved = false;
				
		// build the UI
		this.layoutEditor();
		
		OBEditor.logger.debug("Using charset: " + Charset.defaultCharset().name());
		
		// open the file and display it
		this.editor.setText(FileUtils.getFileContents(this.filepath, Charset.defaultCharset()));
		
		// add editor features and listeners
		this.addEditorFeatures();
		this.openEditor();
		this.postOpen();

		// ping the window title
		this.updateTitle();

	}

	/**
	 * Called after all of the editor layout and link stages, regardless of how the window
	 * was opened. It is up to the editor implementation to check necessary file paths and
	 * contents before preceding with any further file inspection/knock on steps.
	 */
	protected abstract void postOpen();
	
	/**
	 * Called after the editor is finished with layout, but before the editor is opened, 
	 * to add any extra features required by the specific editor.
	 */
	protected abstract void addEditorFeatures();
	
	/**
	 * Provides layout and basic implementation of all of the root editor features.
	 */
	protected void layoutEditor() {
		
		// wrapper for this window
		this.editorShell = new Shell(this.parentDisplay);
		
		// Setup a layout for a fairly clean, bare-bones editor
		editorShell.setLayout(new BorderLayout(0, 0));
			    
		// create a basic sashform to contain the primary editor and info table
		this.editorSash = new SashForm(editorShell, SWT.BORDER | SWT.VERTICAL);
		this.editorSash.setLayout(new FillLayout());
		
		// Create the styled text
		editor = new StyledText(editorSash, SWT.V_SCROLL | SWT.H_SCROLL);
		
		// get the theme properties we need
		Theme theme = Theme.getTheme();
		editor.setBackground(theme.getColor("base.background"));
		editor.setForeground(theme.getColor("base.foreground"));
		editor.setSelectionBackground(theme.getColor("selection.background"));
		
		// Font and Typography/text layout
		Font editorFont = new Font(Display.getCurrent(), "Menlo", 12, 0); 
		editor.setFont(editorFont);
		editor.setIndent(10);
		
		// Setup the info table
		errorTable = new Table(editorSash, SWT.VIRTUAL);
		errorTable.setFont(editorFont);
		errorTable.setLayout(new FillLayout());
		errorTable.setLinesVisible(true);
		errorTable.setHeaderVisible(true);
		
		TableColumn col1 = new TableColumn(errorTable, SWT.NONE);
		col1.setText("Source");
		col1.setWidth(100);
		
		TableColumn col2 = new TableColumn(errorTable, SWT.NONE);
		col2.setText("Line");
		col2.setWidth(75);
		
		TableColumn col3 = new TableColumn(errorTable, SWT.NONE);
		col3.setText("Error");
		col3.setWidth(400);
		
		int[] weights = {80, 20};
		this.editorSash.setWeights(weights);
		
		// Status area setup
		statusLabel = new Label(editorShell, SWT.NONE);
		statusLabel.setLayoutData(BorderLayout.SOUTH);
		statusLabel.setText("Foo");
		
		// core features (gutter, selector for editor table)
		this.editorGutter = new Gutter(this.editor);
		this.errorSelector = new ErrorSelector(this, this.editor, this.errorTable);


	}
	
	/**
	 * Open the editor, attaching monitors for file changes (dirty flag)
	 */
	protected void openEditor() {
		this.editor.addModifyListener(this);		
		this.editorShell.open();
	}
	
	/**
	 * Bulk update the entire analysis of the current file.
	 * 
	 * @param analysis
	 */
	protected void updateWithAnalysis(List<SourceAnalysis> analysis) {
		// send the source analysis to the gutter
		this.editorGutter.setGutterFlags(analysis);
		
		// set the table data for errors
		this.errorTable.removeAll();
		for (SourceAnalysis error : analysis) {
			TableItem t = new TableItem(this.errorTable, SWT.NONE);
			t.setText(0, error.getSource());
			t.setText(1, "" + error.getLineNumber());
			t.setText(2, error.getMessage());
		}
		
		this.errorSelector.setAnalysis(analysis);
		
		int[] currentWeights = this.editorSash.getWeights();
		if (analysis.size() == 0) {
			// hide it
			int[] hideWeights = {100, 0};
			this.editorSash.setWeights(hideWeights);
		}
		else {
			// show it
			if (currentWeights[1] < 5) {
				int[] showWeights = {80, 20};
				this.editorSash.setWeights(showWeights);
			}
		}
	}
	
	/**
	 * Replace Styles on the editor, in a selected range.
	 * 
	 * @param start Start of the style range
	 * @param length Length of the style range
	 * @param styles Array of StyleRange objects.
	 */
	public void replaceTextStyles(final int start, final int length, final StyleRange[] styles) {
		
		if (this.parentDisplay.getThread() == Thread.currentThread()) {
			this.editor.replaceStyleRanges(start, length, styles);
		}
		else {
			
			final Display display = this.parentDisplay;
			final StyledText editor = this.editor;
			
			this.parentDisplay.asyncExec(new Runnable() {

				public void run() {
					if (!display.isDisposed()) {
						editor.replaceStyleRanges(start, length, styles);
					}
				}
				
			});
		}
	}
	
	public boolean isActiveWindow() {
		return this.parentDisplay.getActiveShell() == this.editorShell;
	}
	
	public void setActiveWindow() {
		this.editorShell.setFocus();
	}
	
	public Shell getWindowShell() {
		return this.editorShell;
	}
	
	public boolean isActive() {
		return !this.editorShell.isDisposed();
	}

	public String getFilePath() {
		return this.filepath;
	}

	public boolean isDirty() {
		return this.isUnsaved;
	}
	
	public List<String> getLines() {
		List<String> lines = new ArrayList<>();
		for (int i = 0; i < this.editor.getLineCount(); i++) {
			lines.add(this.editor.getLine(i));
		}
		return lines;
	}
	
	/**
	 * Order of operations for the Saving workflow:
	 * 
	 * 		1. preSave(String path) -> boolean (continue with save?)
	 * 		2. saveContents(String path) -> boolean (did save?)
	 * 		3. postSave(String path)
	 * 
	 * 	At each stage, if the previous method returns false, the remaining methods are skipped.
	 */
	public void saveContents(String path) {
		
		// save ourselves some nesting pain and short circuit fast
		if (!this.preSave(path)) {
			return;
		}
		
		// write the contents
		try {
			// write the file
			
			//byte[] encoded = Charset.defaultCharset().encode(this.editor.getText()).array();
			//Files.write(FileSystems.getDefault().getPath(path), encoded);
			Files.write(FileSystems.getDefault().getPath(path), this.getLines(), Charset.defaultCharset());
			
			// update the unsaved flag
			this.isUnsaved = false;
						
			// update the file path we use
			this.filepath = path;
			
			this.postSave(this.filepath);
			
			this.updateTitle();
			
		}
		catch (IOException ioe) {
			OBEditor.logger.error("Couldn't write the file contents: " + ioe);
		}		
	}
	
	public abstract boolean preSave(String path);
	
	public abstract void postSave(String path);
	
	@Override
	public void modifyText(ModifyEvent e) {
		
		// we need to mark the window as dirty
		this.isUnsaved = true;
		
		this.updateTitle();
	}
	
	protected void updateTitle() {
		
		StringBuilder sb = new StringBuilder();
		
		// saved/unsaved state
		if (this.isUnsaved) {
			sb.append("* ");
		}
		
		// file base name for the current source file (if it exists...)
		if (filepath != null) {
			String fileBasename = Paths.get(filepath).getFileName().toString();
			sb.append(fileBasename);
		}
		else {
			sb.append("Untitled");
		}
		
		sb.append(this.getTitleFragment());
		
		// set it
		this.editorShell.setText(sb.toString());
		
	}
	
	protected abstract String getTitleFragment();

	@Override
	public void setVisible(boolean visibility) {
		this.editorShell.setVisible(visibility);
	}
	
	@Override
	public boolean getVisible() {
		return this.editorShell.getVisible();
	}
	
	@Override
	public void close() {
		this.editorShell.close();
		this.editorShell.dispose();
	}
	
	/**
	 * Make sure the given offset is visible, if it isn't already in view.
	 * 
	 * @param offset
	 */
	public void scrollToOffset(int offset) {
	
		// figure out the line of the offset
		int line = this.editor.getLineAtOffset(offset);
		int top = this.editor.getTopIndex();
		int bottom = this.getVisibleLines() + top;
		
		if (line < top) {
			this.editor.setTopIndex(line);
		}
		else if (line > bottom) {
			this.editor.setTopIndex(line - this.getVisibleLines() + 1);
		}
	}
	
	/**
	 * Center the given line in the display, if possible.
	 * 
	 * @param line
	 */
	public void centerLine(int line) {
		
		// determine the best bounds for the line to center
		int visibleLines = this.getVisibleLines();
		
		this.editor.setTopIndex(line - (visibleLines / 2));
	}
	
	/**
	 * Determine an integer number of lines currently visible in the editor
	 * @return
	 */
	public int getVisibleLines() {
		int lineTwo = editor.getLinePixel(2);
		int lineOne = editor.getLinePixel(1);
		int lineHeight = lineOne;
		
		if (lineOne != lineTwo) {
			lineHeight = lineTwo - lineOne;
		}
		int visibleLines = editor.getBounds().height / lineHeight;
		return visibleLines;
	}

}
