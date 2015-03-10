/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.elixir;

/*<Imports>*/
import org.acm.seguin.JRefactoryVersion;
/*</Imports>*/

/**
 *  Pretty Printer extension mechanism for Elixir 2.4
 *
 *@author    Chris Seguin
 */
public class PrettyPrinterExtension implements IExtension {
	/**
	 *  Gets the Name attribute of the PrettyPrinterExtension object
	 *
	 *@return    The Name value
	 */
	public String getName() {
		return "Pretty Printer";
	}


	/**
	 *  Gets the CardName attribute of the PrettyPrinterExtension object
	 *
	 *@return    The CardName value
	 */
	public String getCardName() {
		return "Pretty Printer";
	}


	/**
	 *  Gets the Version attribute of the PrettyPrinterExtension object
	 *
	 *@return    The Version value
	 */
	public String getVersion() {
		return (new JRefactoryVersion()).toString();
	}


	/**
	 *  Gets the ReleaseNo attribute of the PrettyPrinterExtension object
	 *
	 *@return    The ReleaseNo value
	 */
	public int getReleaseNo() {
		return (new JRefactoryVersion()).getBuild();
	}


	/**
	 *  Initializes the extension
	 *
	 *@param  args  the arguments
	 *@return       true if installed
	 */
	public boolean init(String[] args) {
		if (FrameManager.current() == null) {
			System.out.println("Not installing " + getName() + " " + getVersion());
			return false;
		}
		System.out.println("Installing " + getName() + " " + getVersion());

		//  Load the objects
		new ElixirPrettyPrinter();
		new ElixirTextPrinter();

		//  Add the menu items
		FrameManager.current().addMenuItem("Script|JRefactory|Refresh=((method \"reload\" \"com.elixirtech.ide.edit.BasicViewManager\") (curr-vm))");
		FrameManager.current().addMenuItem("Script|JRefactory|Pretty Printer=((method \"prettyPrint\" \"org.acm.seguin.ide.elixir.ElixirPrettyPrinter\"))");
		FrameManager.current().addMenuItem("Script|JRefactory|Print=((method \"printCurrent\" \"org.acm.seguin.ide.elixir.ElixirTextPrinter\"))");

		return true;
	}


	/**
	 *  Removes the extension mechanism
	 *
	 *@return    Always returns true
	 */
	public boolean destroy() {
		return true;
	}
}
