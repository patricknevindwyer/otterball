package com.programmish.otterball.parsing;

import java.util.ArrayList;
import java.util.List;

public class JSONDocument {
	
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
			return -1;
		}
		else {
			return this.elements.get(index).start;
		}
	}
}
