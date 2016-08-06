package com.programmish.otterball.ui.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.Bullet;
import org.eclipse.swt.custom.PaintObjectEvent;
import org.eclipse.swt.custom.PaintObjectListener;
import org.eclipse.swt.custom.ST;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.GlyphMetrics;
import org.eclipse.swt.graphics.TextLayout;
import org.eclipse.swt.widgets.Display;

import com.programmish.otterball.validation.SourceAnalysis;
import com.programmish.otterball.ui.themes.Theme;

public class Gutter implements ModifyListener, PaintObjectListener {

	private StyledText editor;
	private int oldLineCount;
	private Color gutterForeground;
	private Color gutterBackground;
	private Color gutterWarning;
	private Color gutterWarningStripe;
//	private Color gutterChannelDark;
//	private Color gutterChannelLight;
	private int fontWidth;
	private int flagWidth;
	private Map<Integer, Boolean> flagMap;
	
	public Gutter (StyledText editor) {
	
		Theme t = Theme.getTheme();
		
		this.gutterBackground = t.getColor("gutter.background");
		this.gutterForeground = t.getColor("gutter.foreground");
		this.gutterWarning = t.getColor("gutter.warning");
		this.gutterWarningStripe = t.getColor("gutter.warningStripe");
//		this.gutterChannelDark = t.getColor("gutter.channel.dark");
//		this.gutterChannelLight = t.getColor("gutter.channel.light");
		
		this.fontWidth = 10;
		this.oldLineCount = 0;
		this.flagWidth = 8;
	
		this.flagMap = new HashMap<Integer, Boolean>();
		
		this.editor = editor;
		this.editor.addPaintObjectListener(this);
		this.editor.addModifyListener(this);
		this.updateGutter();
		
	}

	public void setGutterFlags(List<SourceAnalysis> analysis) {
	
		// clear the old flags
		this.flagMap.clear();
		
		for (SourceAnalysis a : analysis) {
			// we keep the line numbering starting at 1
			flagMap.put(a.getLineNumber(), Boolean.TRUE);
		}
	}
	
	@Override
	public void paintObject(PaintObjectEvent event) {
		int charWidth = Integer.toString(this.editor.getLineCount()).length();
		
		Display display = event.display;
		StyleRange style = event.style;
		
		// set the actual draw colors of the current GC
		event.gc.setForeground(this.gutterForeground);
		event.gc.setBackground(this.gutterBackground);
		
		Font font = style.font;
		if (font == null) font = editor.getFont();
		TextLayout layout = new TextLayout(display);
		layout.setAscent(event.ascent);
		layout.setDescent(event.descent);
		layout.setFont(font);
		layout.setText(this.padNumberToSize(event.bulletIndex + 1, charWidth));
		
		
		// paint the background
		event.gc.fillRectangle(event.x, event.y, event.style.metrics.width, layout.getBounds().height);
		
		if (this.flagMap.containsKey(event.bulletIndex + 1)) {
			// paint any status symbols
			
			// flag?
			int flagHeight = layout.getBounds().height / 8 * 3;
			int right = event.x + event.style.metrics.width;
			int left = right - flagWidth;
			int top = event.y + (layout.getBounds().height - flagHeight) / 2;
			int bottom = top + flagHeight;
			int notchMiddle = top + (flagHeight / 2);
//			int notchRight = left + 3;
			int notchRight = left - 3;
			
			int[] fp =        {right, top, left, top, notchRight, notchMiddle, left, bottom, right, bottom};
			
			// Draw the flag body
			event.gc.setBackground(this.gutterWarning);
			event.gc.fillPolygon(fp);
			event.gc.setForeground(this.gutterWarning);
			event.gc.drawPolygon(fp);
			
			// round out the flag
			event.gc.fillArc(right - 1, top, 4, bottom - top, 90, -180);
			event.gc.drawArc(right - 1, top, 4, bottom - top, 90, -180);
			event.gc.drawLine(right + 1, top, right + 1, bottom);
			
			event.gc.setForeground(this.gutterWarningStripe);
//			int [] lineoffsets = {1, 2, 3, 4, 5, 6};
			int [] lineoffsets = {5, 6, 7, 8, 9, 10};
			
			int oldAA = event.gc.getAntialias();
			event.gc.setAntialias(SWT.OFF);
			for (int lineoffset : lineoffsets) {
				event.gc.drawLine(notchRight + lineoffset, top + 2, notchRight + lineoffset, bottom - 2);
			}
			event.gc.setAntialias(oldAA);
			
		}
		
		// paint the text
		event.gc.setForeground(this.gutterForeground);
		layout.draw(event.gc, event.x + 4, event.y);
		layout.dispose();
	}

	private void updateGutter() {
		
		// we primarily want a line number
		if (this.editor.getLineCount() != this.oldLineCount) {
			
			// setup the bullet styling - the paintObject delegate of the PaintObjectListener will handle the actual
			// layout and colorizing
			int charWidth = this.editor.getLineCount();
			StyleRange sr = new StyleRange();
			sr.metrics = new GlyphMetrics(0, 0, Integer.toString(charWidth).length() * this.fontWidth + (flagWidth / 2 * 3));
			Bullet b = new Bullet(ST.BULLET_CUSTOM, sr);

			this.editor.setLineBullet(0, this.editor.getLineCount(), null);
			this.editor.setLineBullet(0, this.editor.getLineCount(), b);
			
			this.oldLineCount = this.editor.getLineCount();
		}
	}
	
	private String padNumberToSize(int num, int size) {
		StringBuilder sb = new StringBuilder();
		String numStr = Integer.toString(num);
		for (int i = 0; i < (size - numStr.length()); i++) {
			sb.append(" ");
		}
		sb.append(numStr);
		return sb.toString();
	}

	@Override
	public void modifyText(ModifyEvent event) {
		this.updateGutter();
	}
	
	
}
