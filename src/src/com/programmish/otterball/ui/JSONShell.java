package com.programmish.otterball.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Display;

import com.programmish.otterball.parsing.Block;
import com.programmish.otterball.parsing.FingerPrintingParser;
import com.programmish.otterball.parsing.JSONDocument;
import com.programmish.otterball.parsing.JSONParser;
import com.programmish.otterball.parsing.PythonUnicodeParser;
import com.programmish.otterball.parsing.SingleQuoteParser;
import com.programmish.otterball.parsing.TextRange;
import com.programmish.otterball.ui.helper.AutoIndenter;
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
	
	private String indent = "    ";
	
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
				if (this.hasSelections()) {
					this.collapseSelectedText();
				}
				else {
					this.editor.setText(jsonDocument.collapse(this.editor.getText()));
				}
			}
			else {
				// expand
				if (this.hasSelections()) {
					this.expandSelectedText();
				}
				else {
					this.editor.setText(jsonDocument.expand(this.editor.getText()));
				}
			}
			
			this.editor.setCaretOffset(this.jsonDocument.getCaretAtIndex(caretIndex));
		}
		else if (ce == OBEvent.ReflowCollapseAll) {
			// store our index
			int caretIndex = jsonDocument.getIndexAtCaret(this.editor.getCaretOffset());
			
			this.editor.setText(jsonDocument.collapse(this.editor.getText()));
			
			this.editor.setCaretOffset(this.jsonDocument.getCaretAtIndex(caretIndex));
			
		}
		else if (ce == OBEvent.ReflowExpandAll) {
			// store our index
			int caretIndex = jsonDocument.getIndexAtCaret(this.editor.getCaretOffset());
			
			this.editor.setText(jsonDocument.expand(this.editor.getText()));	
			
			this.editor.setCaretOffset(this.jsonDocument.getCaretAtIndex(caretIndex));
			
		}
		else if (ce == OBEvent.ReflowCollapseSelection) {

			// get our selection - stored in a point, fun.
			this.collapseSelectedText();
		}
		else if (ce == OBEvent.ReflowExpandSelection) {
			
			this.expandSelectedText();
		}
		else if (ce == OBEvent.SelectEnclosingBlock) {
			
			Block enclosing = this.jsonDocument.getEnclosingBlock(this.editor.getCaretOffset());
			
			if (enclosing != null) {
				this.editor.setSelection(enclosing.getCaretStart(), enclosing.getCaretEnd() + 1);
			}
		}
		else if (ce == OBEvent.SelectLine) {
			int line = this.editor.getLineAtOffset(this.editor.getCaretOffset());
			int line_start = this.editor.getOffsetAtLine(line);
			int line_end = this.editor.getLine(line).length() + line_start;
			this.editor.setSelection(line_start, line_end);
		}
		else if (ce == OBEvent.GotoLine) {
			// TODO: Wait... how do we want to actually do this...
		}
		else if (ce == OBEvent.IndentLine) {
			
			// get the offset at the start of our line
			int line = this.editor.getLineAtOffset(this.editor.getCaretOffset());
			
			// insert an indent at the start of our line
			this.addLineIndent(line);
			this.editor.setCaretOffset(this.editor.getOffsetAtLine(line));
			
		}
		else if (ce == OBEvent.OutdentLine) {
			int line = this.editor.getLineAtOffset(this.editor.getCaretOffset());
			
			this.removeLineIndent(line);
			this.editor.setCaretOffset(this.editor.getOffsetAtLine(line));
		}
		else if (ce == OBEvent.IndentSelection) {
			
			Point p = this.editor.getSelection();
			int select_start_idx = p.x;
			int select_end_idx = p.y;
			
			int first_line = this.editor.getLineAtOffset(select_start_idx);
			int last_line = this.editor.getLineAtOffset(select_end_idx);

			JSONShell.logger.info(String.format(" - OutdentSelection - selection(%d, %d) covers lines [%d, %d]", select_start_idx, select_end_idx, first_line, last_line));
			
			for (int i = first_line; i <= last_line; i++) {
				this.addLineIndent(i);
			}
			
			select_start_idx = this.editor.getOffsetAtLine(first_line);
			select_end_idx = this.editor.getOffsetAtLine(last_line) + this.editor.getLine(last_line).length();
			
			this.editor.setSelection(select_start_idx, select_end_idx);
		}
		else if (ce == OBEvent.OutdentSelection) {
			Point p = this.editor.getSelection();
			int select_start_idx = p.x;
			int select_end_idx = p.y - 1;
			
			int first_line = this.editor.getLineAtOffset(select_start_idx);
			int last_line = this.editor.getLineAtOffset(select_end_idx);
			
			JSONShell.logger.info(String.format(" - OutdentSelection - selection(%d, %d) covers lines [%d, %d]", select_start_idx, select_end_idx, first_line, last_line));
			for (int i = first_line; i <= last_line; i++) {
				this.removeLineIndent(i);
			}
			
			select_start_idx = this.editor.getOffsetAtLine(first_line);
			select_end_idx = this.editor.getOffsetAtLine(last_line) + this.editor.getLine(last_line).length();
			
			this.editor.setSelection(select_start_idx, select_end_idx);
			
		}
		else if (ce == OBEvent.ToggleWordWrap) {
			this.editor.setWrapIndent(this.editorGutter.getWidth() + 20);
			this.editor.setWordWrap(!this.editor.getWordWrap());
		}
		
	}
	
	/**
	 * Add an indent to the specified line
	 * 
	 * @param line
	 */
	protected void addLineIndent(int line) {
			
		int line_start = this.editor.getOffsetAtLine(line);
		
		this.editor.replaceTextRange(line_start, 0, this.indent);
	}
	
	protected void removeLineIndent(int line_idx) {
		
		int line_start = this.editor.getOffsetAtLine(line_idx);
		
		String line = this.editor.getLine(line_idx);
		
		if (line.startsWith("\t")) {
			// remove it
			this.editor.replaceTextRange(line_start, 1, "");
		}
		else if (line.matches("^\\s{1,3}.*")) {
			// remove leading spaces
			Pattern p = Pattern.compile("^(\\s{1,3}).*");
			Matcher m = p.matcher(line);
			
			if (m.lookingAt()) {
				String match = m.group(1);
				this.editor.replaceTextRange(line_start, match.length(), "");
			}
		}
		else if (line.startsWith(this.indent)) {
			// remove the indent
			this.editor.replaceTextRange(line_start, this.indent.length(), "");
		}
	}
	
	/**
	 * Determine if text selections have been made.
	 * 
	 * @return
	 */
	public boolean hasSelections() {
		Point p = this.editor.getSelectionRange();
		if (p.y > 1) {
			return true;
		}
		else {
			return false;
		}
	}
	
	/**
	 * Expand the currently selected JSON
	 */
	protected void expandSelectedText() {
		Point p = this.editor.getSelectionRange();
		int caret_start = p.x;
		int caret_end = p.x + p.y;
		
		// store our selection range so we can reselect later
		int caret_startIndex = jsonDocument.getIndexAtCaret(caret_start);
		int caret_endIndex = jsonDocument.getIndexAtCaret(caret_end);
		
		JSONShell.logger.debug(String.format(" - Event::ReflowExpandSelection (%d, %d)", caret_start, caret_end));
		if (caret_end > caret_start) {
			
			// try and do this
			this.editor.setText(this.jsonDocument.expand(this.editor.getText(), "    ", caret_start, caret_end));
			
		}
		
		int select_start_idx = this.jsonDocument.getCaretAtIndex(caret_startIndex);
		int select_end_idx = this.jsonDocument.getCaretAtIndex(caret_endIndex);
		
		// reset our selection
		this.editor.setSelection(select_start_idx, select_end_idx);		
	}
	
	/**
	 * Compact/collapse the selected JSON
	 */
	protected void collapseSelectedText() {
		Point p = this.editor.getSelectionRange();
		int caret_start = p.x;
		int caret_end = p.x + p.y;
		
		// store our selection range so we can reselect later
		int caret_startIndex = jsonDocument.getIndexAtCaret(caret_start);
		int caret_endIndex = jsonDocument.getIndexAtCaret(caret_end);
		
		JSONShell.logger.debug(String.format(" - Event::ReflowCollapseSelection (%d, %d)", caret_start, caret_end));
		if (caret_end > caret_start) {
			
			// try and do this
			this.editor.setText(this.jsonDocument.collapse(this.editor.getText(), caret_start, caret_end));
			
		}
		
		int select_start_idx = this.jsonDocument.getCaretAtIndex(caret_startIndex);
		int select_end_idx = this.jsonDocument.getCaretAtIndex(caret_endIndex);
		
		// reset our selection
		this.editor.setSelection(select_start_idx, select_end_idx);
		
	}
}
