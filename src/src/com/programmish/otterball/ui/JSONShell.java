package com.programmish.otterball.ui;

import java.util.ArrayList;

import org.apache.log4j.Logger;
import org.eclipse.swt.widgets.Display;

import com.programmish.otterball.ui.helper.AutoIndenter;
import com.programmish.otterball.ui.helper.BraceMatcher;
import com.programmish.otterball.ui.helper.CaretStatus;
import com.programmish.otterball.ui.helper.CommentToggler;
import com.programmish.otterball.ui.helper.LineHighlight;
import com.programmish.otterball.ui.helper.PairInsert;
import com.programmish.otterball.ui.helper.StyledTextUndo;
import com.programmish.otterball.ui.helper.TabToSpace;
import com.programmish.otterball.ui.highlight.DumbJavaScriptHighlighter;

public class JSONShell extends OBEditor {

	private static Logger logger = Logger.getLogger("otterball." + JSONShell.class.getSimpleName());
			
	// text helpers/formatters
	private StyledTextUndo undoManager;
	private CommentToggler commentToggler;
	private LineHighlight lineHighlight;
	private CaretStatus statusFooter;
	private TabToSpace autoTabber;
	private PairInsert pairInserter;
	private AutoIndenter autoIndenter;
	private BraceMatcher braceMatcher;
	
	// highlight control
	private DumbJavaScriptHighlighter highlighter;
		
	public JSONShell(Display d) {
		super(d);
	}
	
	public JSONShell(Display d, String p) {
		super(d, p);
	}
	
	protected void addEditorFeatures() {

		// setup language highlighting
		this.highlighter = new DumbJavaScriptHighlighter(this, this.editor);
		this.highlighter.highlightAsync(0, this.editor.getText().length());
		
		// Add the undo/redo manager
		this.undoManager = new StyledTextUndo(this.editor);
		this.commentToggler = new CommentToggler(this.editor);
		this.lineHighlight = new LineHighlight(this.editor);
		this.statusFooter = new CaretStatus(this.editor, this.statusLabel);
		this.autoTabber = new TabToSpace(this.editor, 4);
		this.pairInserter = new PairInsert(this.editor);
		this.autoIndenter = new AutoIndenter(this.editor);
		this.braceMatcher = new BraceMatcher(this, this.editor);
		
	}
	
	protected void postOpen() {
		this.updateWithAnalysis(new ArrayList());
	}
	
	public boolean preSave(String path) {
		return true;
	}
	
	public void postSave(String path) {

	}
	
	protected String getTitleFragment() {
		StringBuilder sb = new StringBuilder();
		
		return sb.toString();
	}
	
	@Override
	public void dispatchEvent(OBEvent ce) {
		
		if (ce == OBEvent.BraceJump) {
			this.braceMatcher.jumpBraces();
		}
		else if (ce == OBEvent.SelectAll) {
			this.editor.selectAll();
		}
	}
}
