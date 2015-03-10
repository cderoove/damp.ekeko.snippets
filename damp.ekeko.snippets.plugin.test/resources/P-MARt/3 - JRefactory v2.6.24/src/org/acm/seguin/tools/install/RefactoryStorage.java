/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.tools.install;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;

import org.acm.seguin.awt.ExceptionPrinter;
import org.acm.seguin.util.FileSettings;
import org.acm.seguin.util.MissingSettingsException;

/**
 *  Description of the Class
 *
 *@author    Chris Seguin
 */
public class RefactoryStorage {
	private HashMap map;


	/**
	 *  Constructor for the RefactoryStorage object
	 */
	public RefactoryStorage() {
		map = new HashMap();
		load();
	}


	/**
	 *  Gets the Value attribute of the RefactoryStorage object
	 *
	 *@param  key  Description of Parameter
	 *@return      The Value value
	 */
	public int getValue(String key) {
		Object obj = map.get(normalize(key));
		if (obj == null) {
			return 1000;
		}

		return ((Integer) obj).intValue();
	}


	/**
	 *  Adds a feature to the Key attribute of the RefactoryStorage object
	 *
	 *@param  key    The feature to be added to the Key attribute
	 *@param  value  The feature to be added to the Key attribute
	 */
	public void addKey(String key, int value) {
		map.put(normalize(key), new Integer(value));
	}


	/**
	 *  Description of the Method
	 */
	public void store() {
		try {
			String dir = FileSettings.getSettingsRoot() + File.separator + ".Refactory";
			String filename = dir + File.separator + "refactory.settings";

			PrintWriter output = new PrintWriter(new FileWriter(filename));
			Iterator iter = map.keySet().iterator();
			while (iter.hasNext()) {
				String next = (String) iter.next();
				output.println(next + "=" + map.get(next));
			}
			output.close();
		}
		catch (IOException ioe) {
			ExceptionPrinter.print(ioe);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  input  Description of Parameter
	 *@return        Description of the Returned Value
	 */
	private String normalize(String input) {
		StringBuffer buffer = new StringBuffer();

		for (int ndx = 0; ndx < input.length(); ndx++) {
			char ch = input.charAt(ndx);
			if (Character.isJavaIdentifierPart(ch) || (ch == '.')) {
				buffer.append(ch);
			}
			else {
				buffer.append('_');
			}
		}

		return buffer.toString();
	}


	/**
	 *  Description of the Method
	 */
	private void load() {
		try {
			FileSettings settings = FileSettings.getSettings("Refactory", "refactory");
			Enumeration enum_ = settings.getKeys();
			while (enum_.hasMoreElements()) {
				String next = (String) enum_.nextElement();
				map.put(next, new Integer(settings.getInteger(next)));
			}
		}
		catch (MissingSettingsException mse) {
			//  Reasonable
		}
	}
}
