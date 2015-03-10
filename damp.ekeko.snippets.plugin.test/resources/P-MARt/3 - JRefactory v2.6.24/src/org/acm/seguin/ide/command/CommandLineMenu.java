/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.command;

import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.acm.seguin.ide.common.UndoAdapter;
import org.acm.seguin.io.Saveable;
import org.acm.seguin.uml.SaveMenuSelection;
import org.acm.seguin.uml.UMLPackage;
import org.acm.seguin.uml.jpg.SaveAdapter;
import org.acm.seguin.uml.line.LinedPanel;
import org.acm.seguin.uml.print.PrintAdapter;
import org.acm.seguin.uml.print.PrintSetupAdapter;

/**
 *  Creates the menubar for the command line program
 *
 *@author    Chris Seguin
 */
class CommandLineMenu {
	/**
	 *  Gets the MenuBar attribute of the CommandLineMenu object
	 *
	 *@param  panel  Description of Parameter
	 *@return        The MenuBar value
	 */
	public JMenuBar getMenuBar(JPanel panel)
	{
		JMenuBar menubar = new JMenuBar();
		menubar.add(createFileMenu(panel));
		menubar.add(createEditMenu());

		if (panel instanceof LinedPanel) {
			menubar.add(createZoomMenu(panel));
		}

		return menubar;
	}


	/**
	 *  Creates the file menu
	 *
	 *@param  panel  the panel
	 *@return        the file menu
	 */
	private JMenu createFileMenu(JPanel panel)
	{
		JMenu fileMenu = new JMenu("File");

		JMenuItem saveMenuItem = new JMenuItem("Save");
		if (panel instanceof Saveable) {
			saveMenuItem.addActionListener(new SaveMenuSelection((Saveable) panel));
		}
		else {
			saveMenuItem.setEnabled(false);
		}
		fileMenu.add(saveMenuItem);

		JMenuItem jpgMenuItem = new JMenuItem("JPG");
		if (panel instanceof UMLPackage) {
			jpgMenuItem.addActionListener(new SaveAdapter((UMLPackage) panel));
		}
		else {
			jpgMenuItem.setEnabled(false);
		}
		fileMenu.add(jpgMenuItem);

		fileMenu.addSeparator();

		JMenuItem printSetupMenuItem = new JMenuItem("Print Setup");
		printSetupMenuItem.addActionListener(new PrintSetupAdapter());
		fileMenu.add(printSetupMenuItem);

		JMenuItem printMenuItem = new JMenuItem("Print");
		if (panel instanceof UMLPackage) {
			printMenuItem.addActionListener(new PrintAdapter((UMLPackage) panel));
		}
		else {
			printMenuItem.setEnabled(false);
		}
		fileMenu.add(printMenuItem);

		fileMenu.addSeparator();

		JMenuItem exitMenuItem = new JMenuItem("Exit");
		exitMenuItem.addActionListener(new ExitMenuSelection());
		fileMenu.add(exitMenuItem);
		return fileMenu;
	}


	/**
	 *  Creates edit menu
	 *
	 *@return    returns the edit menu
	 */
	private JMenu createEditMenu()
	{
		JMenu editMenu = new JMenu("Edit");

		JMenuItem undoMenuItem = new JMenuItem("Undo Refactoring");
		undoMenuItem.addActionListener(new UndoAdapter());
		editMenu.add(undoMenuItem);
		return editMenu;
	}


	/**
	 *  Creates the zoom menu
	 *
	 *@param  panel  the panel
	 *@return        the zoom menu
	 */
	private JMenu createZoomMenu(JPanel panel)
	{
		LinedPanel linedPanel = (LinedPanel) panel;
		JMenu zoomMenu = new JMenu("Zoom");
		JMenuItem tenPercent = new JMenuItem("10%");
		tenPercent.addActionListener(new ZoomAdapter(linedPanel, 0.1));
		zoomMenu.add(tenPercent);
		JMenuItem twentyFivePercent = new JMenuItem("25%");
		twentyFivePercent.addActionListener(new ZoomAdapter(linedPanel, 0.25));
		zoomMenu.add(twentyFivePercent);
		JMenuItem fiftyPercent = new JMenuItem("50%");
		fiftyPercent.addActionListener(new ZoomAdapter(linedPanel, 0.5));
		zoomMenu.add(fiftyPercent);
		JMenuItem normal = new JMenuItem("100%");
		normal.addActionListener(new ZoomAdapter(linedPanel, 1.0));
		zoomMenu.add(normal);
		JMenuItem twoHunderd = new JMenuItem("200%");
		twoHunderd.addActionListener(new ZoomAdapter(linedPanel, 2.0));
		zoomMenu.add(twoHunderd);
		return zoomMenu;
	}
}
