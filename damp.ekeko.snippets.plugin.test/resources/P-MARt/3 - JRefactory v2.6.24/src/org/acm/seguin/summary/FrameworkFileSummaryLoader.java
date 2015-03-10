/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.summary;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;

import org.acm.seguin.awt.ExceptionPrinter;
import org.acm.seguin.summary.load.LoadStatus;
import org.acm.seguin.summary.load.TextLoadStatus;
import org.acm.seguin.tools.stub.StubFile;
import org.acm.seguin.tools.stub.StubGenerator;
import org.acm.seguin.util.FileSettings;
import org.acm.seguin.util.MissingSettingsException;

/**
 *  Loads the file summaries for the framework files
 *
 *@author     Chris Seguin
 *@created    August 23, 1999
 */
public class FrameworkFileSummaryLoader extends FrameworkLoader {
	private String directory;
	private boolean loaded;
	private LoadStatus status;


	/**
	 *  Constructor for the FrameworkFileSummaryLoader object
	 *
	 *@param  init  Description of Parameter
	 */
	public FrameworkFileSummaryLoader(LoadStatus init) {
		status = init;
		String home;
		try {
				FileSettings umlBundle = FileSettings.getSettings("Refactory", "uml");
				home = umlBundle.getString("stub.dir");
			}
			catch (MissingSettingsException mse) {
				home = System.getProperty("user.home");
			}

		directory = home + File.separator + ".Refactory";
		loaded = false;
	}


	/**
	 *  Main processing method for the FrameworkFileSummaryLoader object
	 */
	public void run() {
		if (loaded) {
			return;
		}

		StubGenerator.waitForLoaded();

		PackageSummary.getPackageSummary("java.lang");

		StubFile.waitForCreation();

		loaded = true;
		File dir = new File(directory);
		String[] filenames = dir.list();
		if (filenames == null) {
			return;
		}

		for (int ndx = 0; ndx < filenames.length; ndx++) {
			if (filenames[ndx].endsWith(".stub")) {
				loadFile(filenames[ndx]);
			}
		}
	}


	/**
	 *  Gets the InputReader attribute of the FrameworkFileSummaryLoader object
	 *
	 *@param  filename         Description of Parameter
	 *@return                  The InputReader value
	 *@exception  IOException  Description of Exception
	 */
	private Reader getInputReader(String filename) throws IOException {
		return new BufferedReader(new FileReader(new File(directory, filename)));
	}


	/**
	 *  Gets the TypeName attribute of the FrameworkFileSummaryLoader object
	 *
	 *@param  summary  Description of Parameter
	 *@return          The TypeName value
	 */
	private String getTypeName(FileSummary summary) {
		if (summary == null) {
			return "No summary";
		}

		Iterator iter = summary.getTypes();
		if (iter == null) {
			return "No types";
		}

		TypeSummary first = (TypeSummary) iter.next();
		String name = first.getName();
		if (name == null) {
			return "No name";
		}

		return name;
	}


	/**
	 *  Loads a stub file
	 *
	 *@param  filename  The name of the file to load
	 */
	private void loadFile(String filename) {
		try {
			status.setRoot(filename);
			Reader input = getInputReader(filename);
			String buffer = loadBuffer(input);

			while (buffer.length() > 0) {
				FileSummary summary = FileSummary.getFileSummary(buffer);
				status.setCurrentFile(getTypeName(summary));
				buffer = loadBuffer(input);

				Thread.currentThread().yield();
			}
		}
		catch (IOException ioe) {
			ExceptionPrinter.print(ioe);
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  input            Description of Parameter
	 *@return                  Description of the Returned Value
	 *@exception  IOException  Description of Exception
	 */
	private String loadBuffer(Reader input) throws IOException {
		StringBuffer buffer = new StringBuffer();
		int next = input.read();
		while ((next >= 0) && (next != '|') && input.ready()) {
			buffer.append((char) next);
			next = input.read();
		}

		return buffer.toString().trim();
	}


	/**
	 *  The main program for the FrameworkFileSummaryLoader class
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args) {
		(new FrameworkFileSummaryLoader(new TextLoadStatus())).run();
	}
}
