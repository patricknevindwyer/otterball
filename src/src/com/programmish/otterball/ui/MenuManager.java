package com.programmish.otterball.ui;

import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.MessageBox;

import com.programmish.otterball.OBCore;
import com.programmish.otterball.ui.handlers.OBEventDispatcher;
import com.programmish.otterball.ui.handlers.NewFileListener;
import com.programmish.otterball.ui.handlers.OpenMenuListener;
import com.programmish.otterball.ui.handlers.SaveMenuListener;

public class MenuManager {

	private static MenuManager manager;
	private static Logger logger = Logger.getLogger("otterball." + MenuManager.class.getSimpleName());
	
	private MenuManager() {}
	
	public synchronized static MenuManager getManager() {
		
		if (MenuManager.manager == null) {
			MenuManager.manager = new MenuManager ();
		}
		
		return MenuManager.manager;
	}
	
	public void constructMenus() {
		
		Menu bar = Display.getCurrent().getMenuBar();
		boolean hasAppMenuBar = (bar != null);
		
		MenuManager.logger.debug("Has menu bar: " + hasAppMenuBar);
		
		
		// Populate the menu bar once if this is a screen menu bar.
		// Otherwise, we need to make a new menu bar for each shell.
		if (hasAppMenuBar) {
						
			createFileMenu(bar);
			
			createEditMenu(bar);
			
			createFindMenu(bar);
			
			createViewMenu(bar);
						
			
			// View Menu

			if (!hasAppMenuBar) {
				MenuManager.logger.debug("attaching menu to active shell");
				Display.getCurrent().getActiveShell().setMenuBar(bar);
			}
		}
	}
	
	/**
	 * Given the application menu bar, add the File menu.
	 * @param bar
	 */
	private void createFileMenu(Menu bar) {
		
		MenuManager.logger.debug("Adding File menu");
		
		MenuItem item = new MenuItem(bar, SWT.CASCADE);
		item.setText("File");
		Menu menu = new Menu(item);
		item.setMenu(menu);

		MenuItem newFileMenu = new MenuItem(menu, SWT.PUSH);
		newFileMenu.setText("New File");
		newFileMenu.setAccelerator(SWT.MOD1 | 'N');
		newFileMenu.addSelectionListener(new NewFileListener());
		
		MenuItem openFileMenu = new MenuItem(menu, SWT.PUSH);
		openFileMenu.setText("Open File...");
		openFileMenu.setAccelerator(SWT.MOD1 | 'O');
		openFileMenu.addSelectionListener(new OpenMenuListener());
		
		MenuItem saveFileMenu = new MenuItem(menu, SWT.PUSH);
		saveFileMenu.setText("Save");
		saveFileMenu.setAccelerator(SWT.MOD1 | 'S');
		saveFileMenu.addSelectionListener(new SaveMenuListener());

		MenuItem closeWindow = new MenuItem(menu, SWT.PUSH);
		closeWindow.setText("Close Window");
		closeWindow.setAccelerator(SWT.MOD1 | 'W');
		closeWindow.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent (Event e) {
				
				// find the active window and tell it to close
				OBEditor w = OBCore.getActiveWindow();
				
				// some of the modal windows aren't proper windows, so we bail
				// on null (see: Jumper)
				if (w == null) {
					return;
				}
				
				if (w.isDirty()) {
					
					int style = SWT.APPLICATION_MODAL | SWT.SHEET | SWT.YES | SWT.NO | SWT.CANCEL;
					MessageBox messageBox = new MessageBox (w.getWindowShell(), style);
					messageBox.setText ("Information");
					messageBox.setMessage ("Save changes before closing?");
					int res = messageBox.open();
					
					if (res == SWT.YES){
						OBCore.saveWindow(w);
						if (!w.isDirty()) {
							OBCore.closeWindow(w);
						}
					}
					else if (res == SWT.NO) {
						OBCore.closeWindow(w);
					}
				}
				else {
					OBCore.closeWindow(w);
				}
			}
		});
	}
	
	/**
	 * Given the application menu bar, create a Find menu
	 * @param bar
	 */
	private void createFindMenu(Menu bar) {
		
		MenuManager.logger.debug("Adding Find menu");
		
		MenuItem item = new MenuItem(bar, SWT.CASCADE);
		item.setText("Find");
		Menu menu = new Menu(item);
		item.setMenu(menu);

		MenuItem findJumper = new MenuItem(menu, SWT.PUSH);
		findJumper.setText("Jump to type");
		findJumper.setAccelerator(SWT.MOD1 | 'T');
		findJumper.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent (Event e) {
				
				Jumper.getJumper();
				
			}
		});
		
		MenuItem jumpBraces = new MenuItem(menu, SWT.PUSH);
		jumpBraces.setText("Jump to brace");
		jumpBraces.setAccelerator(SWT.MOD1 | '5');
		jumpBraces.addListener(SWT.Selection, new OBEventDispatcher(OBEvent.BraceJump));

	}
	
	/**
	 * Given the application menu bar, add the Edit menu.
	 * @param bar
	 */
	private void createEditMenu(Menu bar) {
		
		MenuManager.logger.debug("Adding Edit menu");
		
		/*
		 * Edit
		 * 	- Copy
		 *  - Paste
		 *  - Select
		 *  	All
		 *  	Surrounding Block
		 *      Line
		 *  - Reflow
		 *  	Expand All
		 *  	Collapse All
		 *  	--
		 *  	Expand Selection
		 *  	Collapse Selection
		 *  	--
		 *  	Toggle Expand/Collapse
		 * - Goto
		 *      Line
		 *      
		 */

		MenuItem editItem = new MenuItem (bar, SWT.CASCADE);
		editItem.setText ("Edit");
		Menu submenu = new Menu (editItem);
		editItem.setMenu (submenu);

		// Select
		MenuItem select = new MenuItem(submenu, SWT.CASCADE);
		select.setText("Select");
		Menu selectMenu = new Menu(select);
		select.setMenu(selectMenu);
		
		// Select All
		MenuItem selectAllItem = new MenuItem (selectMenu, SWT.PUSH);
		selectAllItem.setText ("Select &All\tCtrl+A");
		selectAllItem.setAccelerator (SWT.MOD1 + 'A');
		selectAllItem.addListener(SWT.Selection, new OBEventDispatcher(OBEvent.SelectAll));
		
		MenuItem selectEnclosingBlock = new MenuItem(selectMenu, SWT.PUSH);
		selectEnclosingBlock.setText("Select Enclosing &Block\tCtrl+B");
		selectEnclosingBlock.setAccelerator(SWT.MOD1 + 'B');
		selectEnclosingBlock.addListener(SWT.Selection, new OBEventDispatcher(OBEvent.SelectEnclosingBlock));
		
		MenuItem selectLine = new MenuItem(selectMenu, SWT.PUSH);
		selectLine.setText("Select &Line\tCtrl+l");
		selectLine.setAccelerator(SWT.MOD1 + 'L');
		selectLine.addListener(SWT.Selection, new OBEventDispatcher(OBEvent.SelectLine));
		
		// Reflow
		MenuItem reflow = new MenuItem(submenu, SWT.CASCADE);
		reflow.setText("Reflow");
		Menu reflowMenu = new Menu(reflow);
		reflow.setMenu(reflowMenu);
		
		MenuItem rmExpandAll = new MenuItem(reflowMenu, SWT.PUSH);
		rmExpandAll.setText("Expand All");
		rmExpandAll.addListener(SWT.Selection, new OBEventDispatcher(OBEvent.ReflowExpandAll));
		
		MenuItem rmCollapseAll = new MenuItem(reflowMenu, SWT.PUSH);
		rmCollapseAll.setText("Compact All");
		rmCollapseAll.addListener(SWT.Selection, new OBEventDispatcher(OBEvent.ReflowCollapseAll));
		
		MenuItem rmCSSeparator = new MenuItem(reflowMenu, SWT.SEPARATOR);
		
		MenuItem rmExpandSelection = new MenuItem(reflowMenu, SWT.PUSH);
		rmExpandSelection.setText("Expand Selection");
		rmExpandSelection.addListener(SWT.Selection, new OBEventDispatcher(OBEvent.ReflowExpandSelection));
		
		MenuItem rmCollapseSelection = new MenuItem(reflowMenu, SWT.PUSH);
		rmCollapseSelection.setText("Compact Selection");
		rmCollapseSelection.addListener(SWT.Selection, new OBEventDispatcher(OBEvent.ReflowCollapseSelection));
		
		MenuItem rmSTSeparator = new MenuItem(reflowMenu, SWT.SEPARATOR);
		
		MenuItem toggleCollapse = new MenuItem(reflowMenu, SWT.PUSH);
		toggleCollapse.setText("Toggle &Expand/Compact\tCtrl+E");
		toggleCollapse.setAccelerator(SWT.MOD1 + 'E');
		toggleCollapse.addListener(SWT.Selection, new OBEventDispatcher(OBEvent.ToggleCollapse));
		
		// Goto
		MenuItem gotoMenu = new MenuItem(submenu, SWT.CASCADE);
		gotoMenu.setText("Goto");
		Menu gotoSubmenu = new Menu(gotoMenu);
		gotoMenu.setMenu(gotoSubmenu);
		
		MenuItem gotoLine = new MenuItem(gotoSubmenu, SWT.PUSH);
		gotoLine.setText("&Goto Line\tCtrl+G");
		gotoLine.setAccelerator(SWT.MOD1 + 'G');
		gotoLine.addListener(SWT.Selection, new OBEventDispatcher(OBEvent.GotoLine));
		
		
		
	}
	
	/**
	 * Given the application menu bar, add the View menu.
	 * @param bar
	 */
	private void createViewMenu(Menu bar) {
		
		MenuManager.logger.debug("Adding View menu");
		
		MenuItem viewItem = new MenuItem(bar, SWT.CASCADE);
		viewItem.setText("View");
		Menu subMenu = new Menu(viewItem);
		viewItem.setMenu(subMenu);
		
		// toggle windows
		MenuItem windowToggle = new MenuItem(subMenu, SWT.PUSH);
		windowToggle.addListener(SWT.Selection, new Listener() {
			@Override
			public void handleEvent (Event e) {
				
				// figure out the index of the active window
				List<OBEditor> windows = OBCore.getEditor();
				int active = -1;
				for (int i = 0 ; i < windows.size(); i++) {
					if (windows.get(i).isActiveWindow()) {
						active = i;
					}
				}
				
				// toggle to the next window, or wrap around
				if (active >= 0) {
					
					int next = -1;
					if ( active == (windows.size() - 1) ) {
						next = 0;
					}
					else {
						next = active + 1;
					}
					
					if (next >= 0) {
						windows.get(next).setActiveWindow();
					}
				}
						
			}
		});
		windowToggle.setText("Next Window");
		windowToggle.setAccelerator(SWT.MOD1 + '`');
		
		MenuItem debugWindow = new MenuItem(subMenu, SWT.PUSH);
		debugWindow.setText("Debug Window");
		debugWindow.addListener(SWT.Selection, new Listener() {
			public void handleEvent(Event e) {
				OBWindow d = OBCore.debugWindow;
				d.setVisible(!d.getVisible());
				if (d.getVisible()) {
					d.setActiveWindow();
				}
			}
		});
		
	}
}
