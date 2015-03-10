/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

import org.acm.seguin.awt.ExceptionPrinter;
import org.acm.seguin.tools.install.RefactoryInstaller;

/**
 *  Settings loaded from a file
 *
 *@author     Chris Seguin
 *@created    October 3, 1999
 */
public class FileSettings implements Settings {
	private String app;
	private String type;
	private File file;
	private long lastModified;
	private Properties props;
	private boolean continuallyReload;
	private boolean reloadNow;
	private FileSettings parent;

	private static Hashtable map = null;
	private static File settingsRoot = null;


	/**
	 *  Constructor for the FileSettings object
	 *
	 *@param  express                       The file to use for loading
	 *@exception  MissingSettingsException  The file is not found
	 */
	public FileSettings(File express) throws MissingSettingsException {
		file = express;
		if (!file.exists()) {
			throw new NoSettingsFileException(app, type);
		}

		load();

		this.app = express.getParent();
		this.type = express.getName();

		continuallyReload = false;
		reloadNow = false;

		parent = null;
	}


	/**
	 *  Constructor for the FileSettings object
	 *
	 *@param  app                           The application name
	 *@param  type                          The application type
	 *@exception  MissingSettingsException  The file is not found
	 */
	protected FileSettings(String app, String type) throws MissingSettingsException {
		File directory = new File(getSettingsRoot(), "." + app);
		if (!directory.exists()) {
			directory.mkdirs();
			throw new NoSettingsFileException(app, type);
		}

		file = new File(directory, type + ".settings");
		if (!file.exists()) {
			throw new NoSettingsFileException(app, type);
		}

		load();

		this.app = app;
		this.type = type;

		continuallyReload = false;
		reloadNow = false;

		parent = null;
	}


	/**
	 *  Sets the ContinuallyReload attribute of the FileSettings object
	 *
	 *@param  way  The new ContinuallyReload value
	 */
	public void setContinuallyReload(boolean way) {
		continuallyReload = way;
	}


	/**
	 *  Sets the ReloadNow attribute of the FileSettings object
	 *
	 *@param  way  The new ReloadNow value
	 */
	public void setReloadNow(boolean way) {
		reloadNow = way;

		if (reloadNow) {
			load();
		}
	}


	/**
	 *  Gets the keys associated with this properties
	 *
	 *@return    the iterator
	 */
	public Enumeration getKeys() {
		if (!isUpToDate()) {
			load();
		}
		reloadNow = false;

		return props.keys();
	}


	/**
	 *  Gets a string
	 *
	 *@param  code  The code to look up
	 *@return       The associated string
	 */
	public String getString(String code) {
		if (!isUpToDate()) {
			load();
		}
		reloadNow = false;

		String result = props.getProperty(code);
		if ((result == null) && (parent != null)) {
			result = parent.getString(code);
		}
		if (result == null) {
			throw new SettingNotFoundException(app, type, code);
		}

		return result;
	}


	/**
	 *  Gets a integer
	 *
	 *@param  code  The code to look up
	 *@return       The associated integer
	 */
	public int getInteger(String code) {
		try {
			return Integer.parseInt(getString(code));
		}
		catch (NumberFormatException mfe) {
			throw new SettingNotFoundException(app, type, code);
		}
	}


	/**
	 *  Gets a double
	 *
	 *@param  code  The code to look up
	 *@return       The associated double
	 */
	public double getDouble(String code) {
		try {
			Double value = new Double(getString(code));
			return value.doubleValue();
		}
		catch (NumberFormatException mfe) {
			throw new SettingNotFoundException(app, type, code);
		}
	}


	/**
	 *  Gets a boolean
	 *
	 *@param  code  The code to look up
	 *@return       The associated boolean
	 */
	public boolean getBoolean(String code) {
		try {
			Boolean value = new Boolean(getString(code));
			return value.booleanValue();
		}
		catch (NumberFormatException mfe) {
			throw new SettingNotFoundException(app, type, code);
		}
	}


	/**
	 *  Sets the Parent attribute of the FileSettings object
	 *
	 *@param  value  The new Parent value
	 */
	protected void setParent(FileSettings value) {
		parent = value;
	}


	/**
	 *  Get the escaped character
	 *
	 *@param  ch  the character
	 *@return     The character it should be replaced with
	 */
	private char getSpecial(char ch) {
		switch (ch) {
			case 'b':
				return (char) 8;
			case 'r':
				return (char) 13;
			case 'n':
				return (char) 10;
			case 'f':
				return (char) 12;
			case 't':
				return (char) 9;
			default:
				return ch;
		}
	}


	/**
	 *  Returns true if the file is up to date. This method is used to determine
	 *  if it is necessary to reload the file.
	 *
	 *@return    true if it is up to date.
	 */
	private boolean isUpToDate() {
		if (continuallyReload || reloadNow) {
			return (lastModified == file.lastModified());
		}

		//  Assume that it is up to date
		return true;
	}


	/**
	 *  Loads all the settings from the file
	 */
	private synchronized void load() {
		//System.out.println("Loading from:  " + file.getPath() + "  " + file.length());

		props = new Properties();

		try {
			BufferedReader input = new BufferedReader(new FileReader(file));
			String line = input.readLine();
			while (line != null) {
				if ((line.length() == 0) || (line.charAt(0) == '#')) {
					//  Comment - skip the line
				}
				else {
					int equalsAt = line.indexOf('=');
					if (equalsAt > 0) {
						String key = line.substring(0, equalsAt);
						String value = unescapeChars(line.substring(equalsAt + 1));
						props.put(key, value);
					}
				}
				line = input.readLine();
			}

			input.close();
		}
		catch (IOException ioe) {
			ExceptionPrinter.print(ioe);
		}

		setReloadNow(false);
		lastModified = file.lastModified();
	}


	/**
	 *  A transformation on the characters in the string
	 *
	 *@param  value  the string we are updating
	 *@return        the updated string
	 */
	private String unescapeChars(String value) {
		StringBuffer buffer = new StringBuffer();
		int last = value.length();

		for (int ndx = 0; ndx < last; ndx++) {
			char ch = value.charAt(ndx);
			if (ch == '\\') {
				char nextChar = value.charAt(ndx + 1);
				char result = ' ';
				if (nextChar == 'u') {
					result = unicode(value, ndx);
					ndx += 5;
				}
				else if (Character.isDigit(nextChar)) {
					result = octal(value, ndx);
					ndx += 3;
				}
				else if (ndx == last - 1) {
					//  Continuation...
				}
				else {
					result = getSpecial(nextChar);
					ndx++;
				}

				buffer.append(result);
			}
			else {
				buffer.append(ch);
			}
		}

		return buffer.toString();
	}


	/**
	 *  Determine the unicode character
	 *
	 *@param  value  Description of Parameter
	 *@param  ndx    Description of Parameter
	 *@return        Description of the Returned Value
	 */
	private char unicode(String value, int ndx) {
		String hex = value.substring(ndx + 2, ndx + 6);
		int result = Integer.parseInt(hex, 16);
		return (char) result;
	}


	/**
	 *  Determine the octal character
	 *
	 *@param  value  Description of Parameter
	 *@param  ndx    Description of Parameter
	 *@return        Description of the Returned Value
	 */
	private char octal(String value, int ndx) {
		String oct = value.substring(ndx + 1, ndx + 4);
		int result = Integer.parseInt(oct, 8);
		return (char) result;
	}


	/**
	 *  Sets the root directory for settings files
	 *
	 *@param  dir  The new SettingsRoot value
	 */
	public static void setSettingsRoot(String dir) {
		settingsRoot = new File(dir);
	}


	/**
	 *  Sets the root directory for settings files
	 *
	 *@param  dir  The new SettingsRoot value
	 */
	public static void setSettingsRoot(File dir) {
		settingsRoot = dir;
	}


	/**
	 *  Factory method to create FileSettings objects
	 *
	 *@param  app   The name of the application
	 *@param  name  The name of the specific settings
	 *@return       A settings object
	 */
	public static FileSettings getSettings(String app, String name) {
		if (map == null) {
			init();
		}

		String key = app + "::" + name;
		FileSettings result = (FileSettings) map.get(key);
		if (result == null) {
			result = new FileSettings(app, name);
			map.put(key, result);
		}

		return result;
	}


	/**
	 *  Gets the SettingsRoot attribute of the FileSettings class
	 *
	 *@return    The SettingsRoot value
	 */
	public static String getSettingsRoot() {
		if (settingsRoot == null) {
			initRootDir();
		}

		return settingsRoot.getPath();
	}


	/**
	 *  Main program to test the FileSettings object
	 *
	 *@param  args  the command line arguments
	 */
	public static void main(String[] args) {
		//  Make sure everything is installed properly
		(new RefactoryInstaller(false)).run();

		String key = "author";
		if (args.length > 0) {
			key = args[0];
		}

		String type = "pretty";
		if (args.length > 1) {
			type = args[1];
		}

		String app = "Refactory";
		if (args.length > 2) {
			app = args[2];
		}

		System.out.println("Found:  " + (new FileSettings(app, type)).getString(key));
	}


	/**
	 *  Initializes static variables
	 */
	private static synchronized void init() {
		if (map == null) {
			map = new Hashtable();
			initRootDir();
		}
	}


	/**
	 *  Initializes the root directory
	 */
	private static void initRootDir() {
		if (settingsRoot != null) {
			return;
		}

		String javaHome = System.getProperty("jrefactory.home");
		if (javaHome != null) {
			//System.out.println("Home:  " + javaHome);
			settingsRoot = new File(javaHome);
			return;
		}

		javaHome = System.getProperty("user.home");
		if (javaHome != null) {
			//System.out.println("Home:  " + javaHome);
			settingsRoot = new File(javaHome);
			return;
		}

		settingsRoot = new File("~/");
		if (settingsRoot.exists()) {
			//System.out.println("Home:  ~/");
			return;
		}

		settingsRoot = new File("C:\\winnt\\profiles");
		if (settingsRoot.exists()) {
			File attempt = new File(settingsRoot, System.getProperty("user.name"));
			if (attempt.exists()) {
				//System.out.println("Home:  C:\\winnt\\profiles\\currentuser");
				settingsRoot = attempt;
				return;
			}
		}

		settingsRoot = new File("c:\\windows");
		//System.out.println("Home:  C:\\windows");
	}
}
