package com.programmish.otterball.ui.helper;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

public class PairInsert implements VerifyListener, ExtendedModifyListener {

	private StyledText editor;
	private Map<String,String> pairs;
	private Map<String, Boolean> tailPairs;
	
	private int modifyCaret;
	
	public PairInsert(StyledText editor) {
		this.editor = editor;
		this.pairs = new HashMap<String,String>();
		this.tailPairs = new HashMap<String, Boolean>();
		
		this.addPair("{", "}");
		this.addPair("[", "]");
		this.addPair("(", ")");
		this.addPair("'", "'");
		this.addPair("\"", "\"");
		
		this.editor.addVerifyListener(this);
		this.editor.addExtendedModifyListener(this);
	}
	
	private void addPair(String s, String e) {
		this.pairs.put(s,  e);
		this.tailPairs.put(e, Boolean.TRUE);
	}
	
	@Override
	public void verifyText(VerifyEvent event) {
		this.modifyCaret = 0;
		
		if (event.end - event.start == 0) {
			// insert only. Test for the tail pair in the next character first
			String nextCharacter = "";
			if (this.editor.getCaretOffset() < this.editor.getCharCount()) {
				nextCharacter = this.editor.getText(this.editor.getCaretOffset(), this.editor.getCaretOffset());
			}
			
			if (event.text.equals(nextCharacter) && this.tailPairs.containsKey(nextCharacter)) {
				// skip the content, move the cursor
				this.modifyCaret = 1;
				event.text = "";
			}
			if (this.pairs.containsKey(event.text)) {
				event.text = event.text + this.pairs.get(event.text);
				this.modifyCaret = -1;
			}
			
		}
		else {
			
			if (this.pairs.containsKey(event.text)) {
				// modify the selection instead of deleting
				String pairWrappedText = event.text + this.editor.getSelectionText() + this.pairs.get(event.text);
				event.text = pairWrappedText;
			}
		}
	}

	@Override
	public void modifyText(ExtendedModifyEvent event) {
		
		// if this was a pairing event, move the cursor
		if (this.modifyCaret != 0) {
			this.editor.setCaretOffset(this.editor.getCaretOffset() + this.modifyCaret);
		}
	}
	
}
