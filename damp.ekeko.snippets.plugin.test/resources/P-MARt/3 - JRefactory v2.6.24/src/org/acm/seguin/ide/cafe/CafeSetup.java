package org.acm.seguin.ide.cafe;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.io.InputStream;
import java.io.OutputStream;

import org.acm.seguin.tools.install.RefactoryInstaller;
import org.acm.seguin.util.FileSettings;

/**
 *  This class sets up the pretty printer and all the
 *  associated menu items.
 *
 *@author     Chris Seguin
 *@created    August 26, 2000
 */
public class CafeSetup implements Plugin {
	/**
	 *  Gets the PluginInfo attribute of the CafePrettyPrinter object
	 */
	public void getPluginInfo() {
		System.out.println("CafePrettyPrinter::getInfo()");
	}


	/**
	 *  Initializes Visual Cafe settings
	 */
	public void init() {
		String root = System.getProperty("user.home");
		FileSettings.setSettingsRoot(root);

		//  Make sure everything is installed properly
		(new RefactoryInstaller(false)).run();

		VisualCafe vc = VisualCafe.getVisualCafe();

		// Add sample submenus to Visual Cafe MenuBar
		MenuBar mb = vc.getMenuBar();
		Menu subMenu = getSubMenu();
		mb.add(subMenu);
	}


	/**
	 *  Used to close out this object
	 */
	public void destroy() {
	}


	/**
	 *  Used to save this object
	 *
	 *@param  os  the output stream
	 *@param  b   boolean if it needs to be saved
	 */
	public void save(OutputStream os, boolean b) {
	}


	/**
	 *  Restores the state
	 *
	 *@param  is  The input stream
	 *@param  b   a boolean if anything has changed
	 */
	public void restore(InputStream is, boolean b) {
	}


	/**
	 *  Gets the SubMenu attribute of the CafePrettyPrinter object
	 *
	 *@return    The SubMenu value
	 */
	private Menu getSubMenu() {
		Menu jrefactoryMenu = new Menu("JRefactory");

		MenuItem prettyPrinterMenuItem = new MenuItem("P&retty Printer");
		prettyPrinterMenuItem.addActionListener(new CafePrettyPrinter());
		jrefactoryMenu.add(prettyPrinterMenuItem);

        try {
		    MenuItem loadMenuItem = new MenuItem("Extract Method");
		    loadMenuItem.addActionListener(new CafeExtractMethod());
		    jrefactoryMenu.add(loadMenuItem);
		}
		catch (Throwable re) {
		}

		MenuItem extractMenuItem = new MenuItem("Load Metadata");
		extractMenuItem.addActionListener(new ReloadActionAdapter());
		jrefactoryMenu.add(extractMenuItem);

		/*
		MenuItem viewDiagramMenuItem = new MenuItem("View Class Diagram");
		viewDiagramMenuItem.setEnabled(false);
		jrefactoryMenu.add(viewDiagramMenuItem);

		MenuItem printMenuItem = new MenuItem("Print");
		printMenuItem.setEnabled(false);
		jrefactoryMenu.add(printMenuItem);

		Menu zoomMenu = new Menu("Zoom");
		jrefactoryMenu.add(zoomMenu);

		MenuItem tenMenuItem = new MenuItem("10%");
		tenMenuItem.setEnabled(false);
		zoomMenu.add(tenMenuItem);

		MenuItem twentyfiveMenuItem = new MenuItem("25%");
		twentyfiveMenuItem.setEnabled(false);
		zoomMenu.add(twentyfiveMenuItem);

		MenuItem fiftyMenuItem = new MenuItem("50%");
		fiftyMenuItem.setEnabled(false);
		zoomMenu.add(fiftyMenuItem);

		MenuItem fullMenuItem = new MenuItem("100%");
		fullMenuItem.setEnabled(false);
		zoomMenu.add(fullMenuItem);
		*/

		return jrefactoryMenu;
	}
}
