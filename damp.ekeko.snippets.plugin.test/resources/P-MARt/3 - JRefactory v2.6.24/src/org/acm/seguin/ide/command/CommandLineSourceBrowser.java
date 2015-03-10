/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.command;

import java.io.File;

import org.acm.seguin.ide.common.SourceBrowser;
import org.acm.seguin.util.FileSettings;
import org.acm.seguin.util.MissingSettingsException;

/**
 *  Launch from the source browser
 *
 *@author    Chris Seguin
 */
public class CommandLineSourceBrowser extends SourceBrowser {
	private String pattern;


	/**
	 *  Constructor for the CommandLineSourceBrowser object
	 */
	public CommandLineSourceBrowser()
	{
		try {
			FileSettings umlBundle = FileSettings.getSettings("Refactory", "uml");
			pattern = umlBundle.getString("source.editor");
		}
		catch (MissingSettingsException mse) {
			pattern = null;
		}
	}


	/**
	 *  Determine if we can go to the source code
	 *
	 *@return    Description of the Returned Value
	 */
	public boolean canBrowseSource()
	{
		return pattern != null;
	}


	/**
	 *  Command to go to the source code
	 *
	 *@param  file  Description of Parameter
	 *@param  line  Description of Parameter
	 */
	public void gotoSource(File file, int line)
	{
		try {
			StringBuffer buffer = new StringBuffer(pattern);
			int start = pattern.indexOf("$FILE");
			buffer.replace(start, start + 5, file.getCanonicalPath());
			String temp = buffer.toString();
			start = temp.indexOf("$LINE");
			if (start != -1) {
				buffer.replace(start, start + 5, "" + line);
			}

			String execute = buffer.toString();
			System.out.println("Executing:  " + execute);

			Runtime.getRuntime().exec(execute);
		}
		catch (Exception exc) {
			System.out.println("Unable to launch the editor from the command line");
			exc.printStackTrace();
		}
	}
}
