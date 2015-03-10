/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.version;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.acm.seguin.awt.ExceptionPrinter;
import org.acm.seguin.util.FileSettings;
import org.acm.seguin.util.MissingSettingsException;

/**
 *  Creates a process that checks out a file from source safe
 *
 *@author     Chris Seguin
 *@created    June 11, 1999
 */
public class SourceSafe implements VersionControl {
	//  Instance Variables
	private String exeFile;
	private String user;


	/**
	 *  Constructor for the source safe object
	 */
	public SourceSafe() {
		try {
			FileSettings bundle = FileSettings.getSettings("Refactory", "vss");
			exeFile = bundle.getString("vss");
		}
		catch (MissingSettingsException mre) {
			System.out.println("Unable to initialize source safe");
			ExceptionPrinter.print(mre);
		}
		catch (NumberFormatException nfe) {
			System.out.println("Unable to interpret the count property as an integer");
		}
	}


	/**
	 *  Determines if a file is under version control
	 *
	 *@param  fullFilename  The full path of the file
	 *@return               Returns true if the files is under version control
	 */
	public synchronized boolean contains(String fullFilename) {
		try {
			System.out.println("\tChecking for " + fullFilename);
			String project = getProject(Runtime.getRuntime(), fullFilename);
			boolean result = ((project != null) && (project.length() > 0));
			System.out.println("\t" + fullFilename + " is " + (result ? "" : "not ") +
					"in Source Safe");
			return result;
		}
		catch (IOException ioe) {
			ExceptionPrinter.print(ioe);
			return false;
		}
	}


	/**
	 *  Adds a file to version control
	 *
	 *@param  fullFilename  the file to add
	 */
	public synchronized void add(String fullFilename) {
	}


	/**
	 *  Check out the file from source safe
	 *
	 *@param  file  the name of the file
	 */
	public synchronized void checkOut(String file) {
		try {
			//  Get the runtime program
			Runtime processFactory = Runtime.getRuntime();

			System.out.println("\tFinding the project");
			String project = getProject(processFactory, file);
			if (project == null) {
				System.out.println("\tNot in any project");
				return;
			}
			System.out.println("\tChanging to project:  " + project);
			changeProject(processFactory, project);
			System.out.println("\tChecking out the file:  " + file);
			checkout(processFactory, file);
			System.out.println("\tDone:  " + file);
		}
		catch (IOException ioe) {
			ExceptionPrinter.print(ioe);
		}
	}


	/**
	 *  Check out the file from source safe
	 *
	 *@param  file  the name of the file
	 */
	public synchronized void checkIn(String file) {
		try {
			//  Get the runtime program
			Runtime processFactory = Runtime.getRuntime();

			System.out.println("\tFinding the project");
			String project = getProject(processFactory, file);
			if (project == null) {
				System.out.println("\tNot in any project");
				return;
			}
			System.out.println("\tChecking in the file to " + project);
			changeProject(processFactory, project);
			System.out.println("\tChanged to the project");
			checkin(processFactory, file);
			System.out.println("\tDone:  " + file);
		}
		catch (IOException ioe) {
			ExceptionPrinter.print(ioe);
		}
	}


	/**
	 *  Find the project
	 *
	 *@param  factory          the run time factory
	 *@param  file             the name of the file to find
	 *@return                  the name of the project or null if in no project
	 *@exception  IOException  is thrown if the command cannot be executed
	 */
	protected String getProject(Runtime factory, String file) throws IOException {
		System.out.println("DEBUG[SourceSafe.getProject]  #1");
		BufferedReader input = executeLocate(factory, file);

		//  Skip the first line
		System.out.println("DEBUG[SourceSafe.getProject]  #2");
		String firstLine = input.readLine();
		System.out.println("DEBUG[SourceSafe.getProject]  #3 - " + firstLine);

		//  Get the path
		String path = getPath(file);
		System.out.println("DEBUG[SourceSafe.getProject]  #4 - " + path);

		String found = input.readLine();
		if (isNotInSourceSafe(found)) {
			return null;
		}
		System.out.println("DEBUG[SourceSafe.getProject]  #5 - " + found);

		//  Find the project
		int matchCode = -1;
		do {
			System.out.println("DEBUG[SourceSafe.getProject]  #6");
			if (found.indexOf("(Deleted)") == -1) {
				System.out.println("DEBUG[SourceSafe.getProject]  #7");
				String project = extractProjectName(found);
				System.out.println("DEBUG[SourceSafe.getProject]  #8 - " + project);
				if (match(path, project) != -1) {
					System.out.println("DEBUG[SourceSafe.getProject]  #9");
					return project;
				}
			}

			//  Read the next line
			System.out.println("DEBUG[SourceSafe.getProject]  #10");
			found = input.readLine();
			System.out.println("DEBUG[SourceSafe.getProject]  #11 - " + found);
			if ((found.length() == 0) || (found.charAt(0) != '$')) {
				return null;
			}
			System.out.println("DEBUG[SourceSafe.getProject]  #12");
		} while (matchCode == -1);

		//  Not found
		System.out.println("DEBUG[SourceSafe.getProject]  #13");
		return null;
	}


	/**
	 *  Get the filename
	 *
	 *@param  fullFilename  the fully qualified path
	 *@return               the filename
	 */
	protected String getFilename(String fullFilename) {
		File temp = new File(fullFilename);
		return temp.getName();
	}


	/**
	 *  Get the path
	 *
	 *@param  fullFilename  the fully qualified path
	 *@return               the path
	 */
	protected String getPath(String fullFilename) {
		File temp = new File(fullFilename);
		return temp.getParentFile().getPath();
	}


	/**
	 *  Change to the project directory
	 *
	 *@param  factory          the run time factory
	 *@param  project          the name of the project to change to
	 *@exception  IOException  is thrown if the command cannot be executed
	 */
	protected void changeProject(Runtime factory, String project) throws IOException {
		//  Create the executable
		String[] args = new String[3];
		args[0] = exeFile;
		args[1] = "CP";
		args[2] = project;

		Process proc = factory.exec(args);

		try {
			proc.waitFor();
		}
		catch (InterruptedException ie) {
		}
	}


	/**
	 *  Check out the file
	 *
	 *@param  factory          the run time factory
	 *@param  file             the file that we are checking out
	 *@exception  IOException  is thrown if the command cannot be executed
	 */
	protected void checkout(Runtime factory, String file) throws IOException {
		//  Create the executable
		String[] args = new String[3];
		args[0] = exeFile;
		args[1] = "CHECKOUT";
		args[2] = getFilename(file);

		Process proc = factory.exec(args);

		try {
			proc.waitFor();
		}
		catch (InterruptedException ie) {
		}
	}


	/**
	 *  Check in the file
	 *
	 *@param  factory          the run time factory
	 *@param  file             the file that we are checking out
	 *@exception  IOException  is thrown if the command cannot be executed
	 */
	protected void checkin(Runtime factory, String file) throws IOException {
		//  Create the executable
		String[] args = new String[3];
		args[0] = exeFile;
		args[1] = "CHECKIN";
		args[2] = getFilename(file);

		Process proc = factory.exec(args);

		try {
			proc.waitFor();
		}
		catch (InterruptedException ie) {
		}
	}


	/**
	 *  Matches the path to the projects
	 *
	 *@param  path     the path
	 *@param  project  the project
	 *@return          the index of the item in the roots, or -1 if not there
	 */
	protected int match(String path, String project) {
		try {
			FileSettings bundle = FileSettings.getSettings("Refactory", "vss");
			int index = 1;
			while (true) {
				String sourceStart = bundle.getString("source." + index);
				String projectStart = bundle.getString("project." + index);

				if (startSame(path, sourceStart) && startSame(project, projectStart)) {
					//  Check the rest
					String restPath = path.substring(sourceStart.length());
					String restProject = project.substring(projectStart.length());
					System.out.println("    Rest [" + restPath + "] [" + restProject + "]");

					if (compare(restPath, restProject)) {
						System.out.println("\t\tFound!");
						return index;
					}
				}

				System.out.println("Not this pair...  " + index);
				index++;
			}
		}
		catch (MissingSettingsException mre) {
		}

		//  Not found
		return -1;
	}


	/**
	 *  Compares two files
	 *
	 *@param  one  the first name
	 *@param  two  the second name
	 *@return      Description of the Returned Value
	 */
	protected boolean compare(String one, String two) {
		if (one.length() != two.length()) {
			return false;
		}

		int last = one.length();
		for (int ndx = 0; ndx < last; ndx++) {
			char ch1 = one.charAt(ndx);
			char ch2 = two.charAt(ndx);

			if (ch1 == '/') {
				if (!((ch2 == '/') || (ch2 == '\\'))) {
					return false;
				}
			}
			else if (ch1 == '\\') {
				if (!((ch2 == '/') || (ch2 == '\\'))) {
					return false;
				}
			}
			else {
				if (ch1 != ch2) {
					return false;
				}
			}
		}

		return true;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  found  Description of Parameter
	 *@return        Description of the Returned Value
	 */
	private boolean isNotInSourceSafe(String found) {
		if (found == null) {
			return true;
		}

		String trimmed = found.trim();
		if (trimmed == null) {
			return true;
		}

		return trimmed.equals("No matches found.");
	}


	/**
	 *  Description of the Method
	 *
	 *@param  file             Description of Parameter
	 *@param  factory          Description of Parameter
	 *@return                  Description of the Returned Value
	 *@exception  IOException  Description of Exception
	 */
	private BufferedReader executeLocate(Runtime factory, String file) throws IOException {
		//  Create the executable
		String[] args = new String[3];
		args[0] = exeFile;
		args[1] = "LOCATE";
		args[2] = getFilename(file);

		Process proc = factory.exec(args);
		InputStream in = proc.getInputStream();

		//  Read lines
		return new BufferedReader(new InputStreamReader(in));
	}


	/**
	 *  Description of the Method
	 *
	 *@param  found  Description of Parameter
	 *@return        Description of the Returned Value
	 */
	private String extractProjectName(String found) {
		int last = found.lastIndexOf("/");
		if (last < 1) {
			return found;
		}

		return found.substring(0, last);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  fullName  Description of Parameter
	 *@param  prefix    Description of Parameter
	 *@return           Description of the Returned Value
	 */
	private boolean startSame(String fullName, String prefix) {
		System.out.println("    Comparing   prefix:[" + prefix + "]   full:[" + fullName + "]");
		return fullName.startsWith(prefix);
	}


	/**
	 *  Main program
	 *
	 *@param  args  the command line arguments
	 */
	public static void main(String[] args) {
		if (args.length != 1) {
			System.out.println("Syntax:  java org.acm.seguin.version.SourceSafe filename");
			return;
		}

		(new SourceSafe()).checkIn(args[0]);
	}
}
