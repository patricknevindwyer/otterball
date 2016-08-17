package com.programmish.otterball.parsing;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.programmish.otterball.ui.JSONShell;

public class JSONDocument {
	
	private static Logger logger = Logger.getLogger("otterball." + JSONDocument.class.getSimpleName());
	
	protected List<ParsedElement> elements;
	protected boolean expanded = true;
	
	public JSONDocument() {
		this.elements = new ArrayList<>();
	}
	
	public JSONDocument(List<ParsedElement> e) {
		this.elements = e;
	}
	
	public boolean containsCursor(int position) {
		
		if (this.elements.size() == 0) {
			return false;
		}
		
		int s = this.elements.get(0).start;
		int e = this.elements.get(this.elements.size() - 1).end;
		
		if ( (position >= s) && (position <= e)) {
			return true;
		}
		else {
			return false;
		}
	}
	
	public void setParsedElements(List<ParsedElement> e) {
		this.elements = e;
	}
	
	public List<ParsedElement> getParsedElements() {
		return this.elements;
	}
		
	public boolean isExpanded() {
		return this.expanded;
	}
	
	public String expand(String text) {
		String expanded = JSONFormat.reflow(text, "    ", this.elements);
		this.expanded = true;
		return expanded;
	}
	
	public String collapse(String text) {
		String compacted = JSONFormat.compact(text, this.elements);
		this.expanded = false;
		return compacted;
	}
	
	public String collapse(String text, int start, int end) {
		String compacted = JSONFormat.compact(text, this, start, end);
		this.expanded = false;
		return compacted;
	}
	
	public String expand(String text, String indent, int start, int end) {
		String expanded = JSONFormat.reflow(text, indent, this, start, end);
		this.expanded = true;
		return expanded;
	}
	
	/**
	 * Find the block enclosing this caret position.
	 * 
	 * @param caret
	 * @return
	 */
	public Block getEnclosingBlock(int caret) {
		
		JSONDocument.logger.debug(String.format(" - ::getEnclosingBlock - start at caret %d", caret));
		
		int pivot_idx = this.getIndexAtCaret(caret);
		ParsedElement pivot_pe = this.elements.get(pivot_idx);
		
		// are we even in an object?
		if (pivot_idx == -1) {
			JSONDocument.logger.debug(" - not in an object");
			return null;
		}
		
		// are we in a string?
		if (pivot_pe.type == ElementType.STRING) {
			JSONDocument.logger.debug(String.format(" - in a string (%d, %d)", pivot_pe.start, pivot_pe.end));
			// return the string
			return new Block(pivot_pe, pivot_pe);
		}
		
		// work our way backwards to an enclosing type
		int block_open_idx = pivot_idx;
		ElementType block_open_type = null;
		while (block_open_idx >= 0) {
			
			ParsedElement open_pe = this.elements.get(block_open_idx);
			if (open_pe.type.isBlockStart()) {
				block_open_type = open_pe.type;
				break;
			}
			block_open_idx--;
		}
		
		// sanity check on having an open block
		if (block_open_type == null) {
			JSONDocument.logger.debug(" - block open is null, bailing");
			return null;
		}
		
		int blockCounter = 1;
		
		int block_close_idx = block_open_idx + 1;
		
		JSONDocument.logger.debug(String.format("- open block is %s", block_open_type));
		
		while (block_close_idx < this.elements.size()) {
			
			ParsedElement close_pe = this.elements.get(block_close_idx);
			
			
			
			if (close_pe.type.isBlockStart()) {
				blockCounter++;
			}
			else if (close_pe.type.isBlockEnd()) {
				blockCounter--;
			}
			JSONDocument.logger.debug(String.format(" - element: %s, blockCounter: %d", close_pe.type, blockCounter));
			
			if ( close_pe.type.closes(block_open_type) && (blockCounter == 0)) {
				break;
			}
			
			block_close_idx++;
		}
		
		// create our return
		if ( (block_open_idx >= 0) && (block_close_idx < this.elements.size()) ) {
			return new Block(this.elements.get(block_open_idx), this.elements.get(block_close_idx));
		}
		else {
			return new Block(this.elements.get(0), this.elements.get(this.elements.size() - 1));
		}
	}
	
	public int getIndexAtCaret(int caret) {
		if (!this.containsCursor(caret)) {
			return -1;
		}
		
		for (int i = 0; i < this.elements.size(); i++) {
			int st = this.elements.get(i).start;
			int ed = this.elements.get(i).end;
			
			if (caret < st) {
				// we're passed the end of the caret already, 
				// let's back track
				if (i == 0) {
					return 0;
				}
				else {
					return i - 1;
				}
			}
			else if ( (caret >= st) && (caret <= ed) ) {
				return i;
			}
		}
		return -1;
		
	}
	
	public int getCaretAtIndex(int index) {
		if (index >= this.elements.size()) {
			return 0;
		}
		else if (index < 0) {
			return 0;
		}
		else {
			return this.elements.get(index).start;
		}
	}
}
