package com.programmish.otterball.ui;



import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import swing2swt.layout.BorderLayout;

import com.programmish.otterball.ui.themes.Theme;

/**
 * Wrap the debug window and controls
 * 
 * @author patricknevindwyer
 *
 */
public class DebugShell implements OBWindow, ModifyListener {

	private Display parentDisplay;
	private Shell debugShell;
	private Label statusLabel;
	private StyledText debugConsole;
	private Logger logger;
	private DebugAppender appender;
	private Theme themeManager;
	private Map<String, Color> colorLevels;
	private int lastHighlight;
	
	public DebugShell(Display parentDisplay) {

		this.themeManager = Theme.getTheme();
	
		this.lastHighlight = -1;
		
		this.parentDisplay = parentDisplay;
		this.debugShell = new Shell(this.parentDisplay, SWT.TITLE | SWT.MIN | SWT.MAX | SWT.RESIZE);
		this.debugShell.setText("Civet Debug");
		debugShell.setLayout(new BorderLayout(0, 0));
			    
		debugConsole = new StyledText(debugShell, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
		debugConsole.setLayoutData(BorderLayout.CENTER);
		debugConsole.setFont(new Font(Display.getCurrent(), "Monaco", 12, 0));
		debugConsole.setLeftMargin(15);
		debugConsole.setForeground(this.themeManager.getColor("base.foreground"));
		debugConsole.setBackground(this.themeManager.getColor("base.background"));
		
		statusLabel = new Label(debugShell, SWT.NONE);
		statusLabel.setLayoutData(BorderLayout.SOUTH);
		statusLabel.setText("Foo");
		this.debugShell.open();
		
		colorLevels = new HashMap<>();
		colorLevels.put("DEBUG", themeManager.getColor("comments.foreground"));
		colorLevels.put("INFO", themeManager.getColor("strings.foreground"));
		colorLevels.put("WARN", themeManager.getColor("braces.foreground"));
		colorLevels.put("ERROR", themeManager.getColor("keywords.foreground"));

		this.debugConsole.addModifyListener(this);
		
		logger = Logger.getLogger("civet");
		this.appender = new DebugAppender(this);
		logger.addAppender(this.appender);
		
	}
	
	public boolean isActive() {
		return !this.debugShell.isDisposed();
	}
	
	public boolean isActiveWindow() {
		return this.parentDisplay.getActiveShell() == this.debugShell;
	}
	
	public void setActiveWindow() {
		this.debugShell.setFocus();
	}
	
	public Shell getWindowShell() {
		return this.debugShell;
	}
	
	protected void createContents() {
		
	}
	
	public void setStatus(String sts) {
		this.statusLabel.setText(sts);
	}
	
	private void highlightTail() {
		
		int lineCount = this.debugConsole.getLineCount();
		int nextLine = this.lastHighlight + 1;
		for (int line = nextLine; line < lineCount; line++) {
			this.highlightLine(line);
		}
		
		this.lastHighlight = lineCount - 2;
	}
	
	private void highlightLine(int line) {
		//[civet.NpmDetector](main) DEBUG - Looking for npm executable at [/usr/bin/npm]
		//[civet.DumbJavaScriptHighlighter](Thread-1) DEBUG - DumbHighlight found 1392 matches against 16470 characters in 191 milliseconds
		int lineOffset = this.debugConsole.getOffsetAtLine(line);
		String content = this.debugConsole.getLine(line);
		
		Pattern p = Pattern.compile("^\\s*\\[(.*?)\\]\\((.*?)\\)\\s+(\\w*)\\s+-");
		Matcher m = p.matcher(content);
		
		if (m.lookingAt()) {
			
			int s_classMatch = m.start(1);
			int e_classMatch = m.end(1);
			
			StyleRange style_classMatch = new StyleRange(lineOffset + s_classMatch, e_classMatch - s_classMatch, themeManager.getColor("strings.foreground"), null, SWT.NORMAL);
			this.debugConsole.setStyleRange(style_classMatch);
			
			int s_threadMatch = m.start(2);
			int e_threadMatch = m.end(2);
			
			StyleRange style_threadMatch = new StyleRange(lineOffset + s_threadMatch, e_threadMatch - s_threadMatch, themeManager.getColor("builtins.foreground"), null, SWT.NORMAL);
			this.debugConsole.setStyleRange(style_threadMatch);
			
			int s_levelMatch = m.start(3);
			int e_levelMatch = m.end(3);
			
			Color levelColor = themeManager.getColor("modules.foreground");
			String level = m.group(3);
			if (colorLevels.containsKey(level)) {
				levelColor = colorLevels.get(level);
			}
			StyleRange style_levelMatch = new StyleRange(lineOffset + s_levelMatch, e_levelMatch - s_levelMatch, levelColor, null, SWT.NORMAL);
			this.debugConsole.setStyleRange(style_levelMatch);
			
//			int s_msgMatch = m.start(4);
//			int e_msgMatch = m.end(4);
//			
//			StyleRange style_msgMatch = new StyleRange(lineOffset + s_msgMatch, e_msgMatch - s_msgMatch, themeManager.getColor("keywords.foreground"), null, SWT.NORMAL);
//			this.debugConsole.setStyleRange(style_msgMatch);
			
		}		
	}
	
	protected void addDebugMessage(final String msg) {
		if (this.parentDisplay.getThread() == Thread.currentThread()) {
			this.debugConsole.append(msg + "\n");
			this.highlightTail();
//			this.highlightLine(this.debugConsole.getLineCount() - 2);
		}
		else {
			final StyledText dc = this.debugConsole;
			
			// make ourselves thread safe - we have no idea where these messages are coming from
			this.parentDisplay.asyncExec(new Runnable() {
				
				public void run() {
					dc.append(msg + "\n");
					highlightTail();
//					highlightLine(dc.getLineCount() - 2);
				}
				
			});
			
		}
	}
	
	/**
	 * Simple log4j appender to redirect log messages to the debug window
	 * @author patricknevindwyer
	 *
	 */
	protected class DebugAppender extends AppenderSkeleton {
		private DebugShell debugShell;
		
		DebugAppender(DebugShell ds) {
			this.debugShell = ds;
		}
		
		@Override
		public void append(LoggingEvent e) {
			
			String msg = e.getRenderedMessage();
			Level lvl = e.getLevel();
			String threadName = e.getThreadName();
			String loggerName = e.getLoggerName();
			String fmtMsg = String.format("[%s](%s) %s - %s", loggerName, threadName, lvl.toString(), msg);
			
			this.debugShell.addDebugMessage(fmtMsg);
		}
		
		public void close() {}
		
		public boolean requiresLayout() {
			return false;
		}
	}
	
	@Override
	public void close() {
		logger.removeAppender(this.appender);
		this.debugShell.setVisible(false);
	}
	
	public void setVisible(boolean visibility) {
		this.debugShell.setVisible(visibility);
	}
	
	public boolean getVisible() {
		return this.debugShell.getVisible();
	}

	@Override
	public void modifyText(ModifyEvent e) {
		
		// auto-scroll to the end of the text
		this.debugConsole.setTopIndex(this.debugConsole.getLineCount() - 1);
		
	}
	
	@Override
	public void dispatchEvent(OBEvent ce) {}
	
}
