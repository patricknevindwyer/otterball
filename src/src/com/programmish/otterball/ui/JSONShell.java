package com.programmish.otterball.ui;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.widgets.Display;

import com.programmish.otterball.parsing.FingerPrintingParser;
import com.programmish.otterball.parsing.JSONDocument;
import com.programmish.otterball.parsing.JSONParser;
import com.programmish.otterball.parsing.ParsedElement;
import com.programmish.otterball.parsing.PythonUnicodeParser;
import com.programmish.otterball.parsing.SingleQuoteParser;
import com.programmish.otterball.parsing.TextRange;
import com.programmish.otterball.ui.helper.AutoIndenter;
import com.programmish.otterball.ui.helper.BraceMatcher;
import com.programmish.otterball.ui.helper.CaretStatus;
import com.programmish.otterball.ui.helper.CommentToggler;
import com.programmish.otterball.ui.helper.LineHighlight;
import com.programmish.otterball.ui.helper.PairInsert;
import com.programmish.otterball.ui.helper.StyledTextUndo;
import com.programmish.otterball.ui.helper.TabToSpace;
import com.programmish.otterball.ui.highlight.JSONHighlight;

public class JSONShell extends OBEditor implements ModifyListener {

	private static Logger logger = Logger.getLogger("otterball." + JSONShell.class.getSimpleName());
			
	// text helpers/formatters
	private StyledTextUndo undoManager;
	private CommentToggler commentToggler;
	private LineHighlight lineHighlight;
	private CaretStatus statusFooter;
	private TabToSpace autoTabber;
	private PairInsert pairInserter;
	private AutoIndenter autoIndenter;
	//private BraceMatcher braceMatcher;
	
	// parsers
	private List<FingerPrintingParser> fingerPrinters;
	private JSONParser jsonParser;
	
	// Document Control
	private JSONDocument jsonDocument;
	
	// highlight control
	private JSONHighlight highlighter;
		
	public JSONShell(Display d) {
		super(d);
	}
	
	
	public JSONShell(Display d, String p) {
		super(d, p);
	}

	@Override
	public void modifyText(ModifyEvent e) {
		super.modifyText(e);
		
		this.jsonDocument.setParsedElements(this.jsonParser.parse(this.editor.getText()));
		
	}

	public JSONDocument getJSONDocument() {
		return this.jsonDocument;
	}
	
	protected void addEditorFeatures() {
		
		this.jsonParser = new JSONParser();
		this.jsonDocument = new JSONDocument();
		this.jsonDocument.setParsedElements(this.jsonParser.parse(this.editor.getText()));

		// setup language highlighting
		this.highlighter = new JSONHighlight(this, this.editor);
		this.highlighter.highlightAsync(0, this.editor.getText().length());
		
		// Add the undo/redo manager
		this.undoManager = new StyledTextUndo(this.editor);
		this.commentToggler = new CommentToggler(this.editor);
		this.lineHighlight = new LineHighlight(this.editor);
		this.statusFooter = new CaretStatus(this.editor, this.statusLabel);
		this.autoTabber = new TabToSpace(this.editor, 4);
		this.pairInserter = new PairInsert(this.editor);
		this.autoIndenter = new AutoIndenter(this.editor);
		//this.braceMatcher = new BraceMatcher(this, this.editor);
		
		// our finger printers
		this.fingerPrinters = new ArrayList<>();
		this.fingerPrinters.add(new JSONParser());
		this.fingerPrinters.add(new SingleQuoteParser());
		this.fingerPrinters.add(new PythonUnicodeParser());
	}
	
	protected void postOpen() {
		// this is a shim for clearing out the error table
		this.updateWithAnalysis(new ArrayList());
		
		
		// run typing on our content
		this.typeContents();
	}
	
	protected void typeContents() {
		
		String rawText = this.editor.getText();
		
		for (FingerPrintingParser p : this.fingerPrinters) {
			
			List<TextRange> sections = p.findSections(rawText);
			
			if (sections.size() == 1) {
				JSONShell.logger.info("fingerprint of 1 section detected: " + p.parserName());
			}
			else if (sections.size() > 1) {
				JSONShell.logger.info("fingerprint of " + sections.size() + " sections detected: " + p.parserName());
			}
			else {
				JSONShell.logger.info("no fingerprint detected: " + p.parserName());
			}
		}
		
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
			//this.braceMatcher.jumpBraces();
		}
		else if (ce == OBEvent.SelectAll) {
			this.editor.selectAll();
		}
		else if (ce == OBEvent.ToggleCollapse) {
			
			// store our index
			int caretIndex = jsonDocument.getIndexAtCaret(this.editor.getCaretOffset());
			
			if (this.jsonDocument.isExpanded()) {
				// collapse
				this.editor.setText(jsonDocument.collapse(this.editor.getText()));
			}
			else {
				// expand
				this.editor.setText(jsonDocument.expand(this.editor.getText()));	
			}
			
			this.editor.setCaretOffset(this.jsonDocument.getCaretAtIndex(caretIndex));
		}
	}
}
