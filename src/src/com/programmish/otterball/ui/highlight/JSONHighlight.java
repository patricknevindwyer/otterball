package com.programmish.otterball.ui.highlight;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ExtendedModifyEvent;
import org.eclipse.swt.custom.ExtendedModifyListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Color;

import com.programmish.otterball.parsing.ElementType;
import com.programmish.otterball.parsing.ParsedElement;
import com.programmish.otterball.ui.JSONShell;
import com.programmish.otterball.ui.themes.Theme;

public class JSONHighlight implements ExtendedModifyListener {

	private StyledText editor;
	private JSONShell jsEditor;
	
	private static Logger logger = Logger.getLogger("otterball." + JSONHighlight.class.getSimpleName());
	
	Map<ElementType, Color> colorMap;
	
	public JSONHighlight(JSONShell jss, StyledText editor) {
	
		this.jsEditor = jss;
		
		this.editor = editor;
		
		// pull in the current theme info
		this.loadThemeColors();
		
		this.editor.addExtendedModifyListener(this);
	}

	@Override
	public void modifyText(ExtendedModifyEvent arg0) {

		List<ParsedElement> elements = this.jsEditor.getJSONDocument().getParsedElements();
		JSONHighlight.logger.debug(String.format("JSONHighlight got %d parsed elements", elements.size()));
		
		this.highlight(this.editor.getText(), elements);
		
	}

	private void loadThemeColors() {
		this.colorMap = new HashMap<>();
		Theme t = Theme.getTheme();
		
		/*
		 * 	OBJ_START		("{"),
			OBJ_END			("}"),
			OBJ_DELIMITER	(","),
			OBJ_SEPARATOR	(":"),
			
			LIST_START		("["),
			LIST_END		("]"),
			LIST_DELIMITER	(","),
			
			STRING			("<string>"),
			BOOLEAN			("<boolean>"),
			NULL			("<null>"),
			NUMBER			("<number>");

		 */
		this.colorMap.put(ElementType.STRING, t.getColor("strings.foreground"));
		this.colorMap.put(ElementType.BOOLEAN, t.getColor("keywords.foreground"));
		this.colorMap.put(ElementType.NULL, t.getColor("comments.foreground"));
		this.colorMap.put(ElementType.NUMBER, t.getColor("builtins.foreground"));
		
		this.colorMap.put(ElementType.OBJ_START, t.getColor("braces.foreground"));
		this.colorMap.put(ElementType.OBJ_END, t.getColor("braces.foreground"));
		this.colorMap.put(ElementType.OBJ_SEPARATOR, t.getColor("comps.foreground"));
		this.colorMap.put(ElementType.OBJ_DELIMITER, t.getColor("literals.foreground"));
		
		this.colorMap.put(ElementType.LIST_START, t.getColor("modules.foreground"));
		this.colorMap.put(ElementType.LIST_END, t.getColor("modules.foreground"));
		this.colorMap.put(ElementType.LIST_DELIMITER, t.getColor("literals.foreground"));
		
	}
	
	protected void highlight(String text, List<ParsedElement> elements) {
		List<StyleRange> highlights = new ArrayList<>();
		long start = System.currentTimeMillis();
		
		//StyleRange m_sr = new StyleRange(offset + m_start, m_end - m_start, fg, bg, ft_style);
		for (ParsedElement e : elements) {
			highlights.add(new StyleRange(e.start, e.end - e.start + 1, this.colorMap.get(e.type), null, SWT.NORMAL));
		}
		
		long end = System.currentTimeMillis();
		StyleRange[] ranges = new StyleRange[highlights.size()];
		ranges = highlights.toArray(ranges);
		
		this.jsEditor.replaceTextStyles(0, text.length(), ranges);
		
		long ed = System.currentTimeMillis();
		JSONHighlight.logger.debug(String.format("JSONHighlight parsed %d matches against %d characters in %d milliseconds", highlights.size(), text.length(), end - start));

	}
	
	public void highlightAsync(final int start, final int length) {
		
		final String text = this.editor.getText();
		List<ParsedElement> elements = this.jsEditor.getJSONDocument().getParsedElements();
		new Thread("highlight") {
			public void run() {
				highlight(text, elements);
			}
		}.start();
		
	}

}
