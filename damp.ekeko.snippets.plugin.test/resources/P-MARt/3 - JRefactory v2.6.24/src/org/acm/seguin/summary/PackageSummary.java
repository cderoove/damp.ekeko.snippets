/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.summary;

import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

/**
 *  Creates a summary of a package
 *
 *@author     Chris Seguin
 *@created    May 5, 1999
 */
public class PackageSummary extends Summary {
	//  Instance Variables
	private String name;
	private LinkedList fileList;

	//  Class Variables
	private static HashMap packageMap;


	/**
	 *  Constructor for the package summary
	 *
	 *@param  packageName  the name of the package
	 */
	protected PackageSummary(String packageName) {
		//  Initialize package summary - packages have no parents
		super(null);

		name = packageName.intern();
		fileList = null;
	}


	/**
	 *  Get the name of the package
	 *
	 *@return    the package name
	 */
	public String getName() {
		return name;
	}


	/**
	 *  Get a file summary by file name
	 *
	 *@param  name  the name of the file summary
	 *@return       the file summary if it is found and null otherwise
	 */
	public FileSummary getFileSummary(String name) {
		//  Check for null pointers
		if (name == null) {
			return null;
		}

		//  Local Variables
		if (fileList != null) {
			Iterator iter = fileList.iterator();

			//  Check for it
			while (iter.hasNext()) {
				FileSummary next = (FileSummary) iter.next();
				if (name.equals(next.getName())) {
					return next;
				}
			}
		}

		//  Hmm...  not found
		return null;
	}


	/**
	 *  Return an iterator of the files
	 *
	 *@return    the iterator
	 */
	public Iterator getFileSummaries() {
		if (fileList == null) {
			return null;
		}

		return fileList.iterator();
	}


	/**
	 *  Get the directory associated with this package
	 *
	 *@return    a file or null if none
	 */
	public File getDirectory() {
		Iterator iter = getFileSummaries();
		if (iter == null) {
			return null;
		}

		while (iter.hasNext()) {
			FileSummary next = (FileSummary) iter.next();
			File result = next.getFile();
			if (result != null) {
				result = result.getParentFile();
				if (result != null) {
					return result;
				}
			}
		}

		return null;
	}


	/**
	 *  Determines if it is the top level package
	 *
	 *@return    true if it is the top level
	 */
	public boolean isTopLevel() {
		return ((name == null) || (name.length() == 0));
	}


	/**
	 *  Delete a file summary
	 *
	 *@param  fileSummary  the file summary object that we are removing
	 */
	public void deleteFileSummary(FileSummary fileSummary) {
		if (fileSummary != null) {
			if (fileList == null) {
				initFileList();
			}

			fileList.remove(fileSummary);
		}
	}


	/**
	 *  Converts this object to a string
	 *
	 *@return    the string
	 */
	public String toString() {
		if (!isTopLevel()) {
			return name;
		}
		else {
			return "<Top Level Package>";
		}
	}


	/**
	 *  Provide method to visit a node
	 *
	 *@param  visitor  the visitor
	 *@param  data     the data for the visit
	 *@return          some new data
	 */
	public Object accept(SummaryVisitor visitor, Object data) {
		return visitor.visit(this, data);
	}


	/**
	 *  Add a file summary
	 *
	 *@param  fileSummary  the file summary that we are adding
	 */
	protected void addFileSummary(FileSummary fileSummary) {
		if (fileSummary != null) {
			if (fileList == null) {
				initFileList();
			}

			fileList.add(fileSummary);
		}
	}


	/**
	 *  Initialize the file list
	 */
	private void initFileList() {
		fileList = new LinkedList();
	}


	/**
	 *  Get a package summary object
	 *
	 *@param  name  the name of the package that we are creating
	 *@return       The PackageSummary value
	 */
	public static PackageSummary getPackageSummary(String name) {
		if (packageMap == null) {
			init();
		}

		PackageSummary result = (PackageSummary) packageMap.get(name);
		if (result == null) {
			result = new PackageSummary(name);
			packageMap.put(name, result);
		}

		return result;
	}


	/**
	 *  Get a package summary object
	 *
	 *@return    all package summaries
	 */
	public static Iterator getAllPackages() {
		if (packageMap == null) {
			init();
		}

		return packageMap.values().iterator();
	}


	/**
	 *  Saves all the packages to an object output stream
	 *
	 *@param  out              Description of Parameter
	 *@exception  IOException  Description of Exception
	 */
	public static void saveAll(ObjectOutputStream out) throws IOException {
		out.writeObject(packageMap);
	}


	/**
	 *  Loads all the packages from the object input stream
	 *
	 *@param  in               Description of Parameter
	 *@exception  IOException  Description of Exception
	 */
	public static void loadAll(ObjectInputStream in) throws IOException {
		try {
			packageMap = (HashMap) in.readObject();

			if ((packageMap == null) || (packageMap.values() == null)) {
				return;
			}

			Iterator iter = packageMap.values().iterator();
			while (iter.hasNext()) {
				System.out.print("*");
				PackageSummary nextPackage = (PackageSummary) iter.next();
				Iterator iter2 = nextPackage.getFileSummaries();
				while ((iter2 != null) && iter2.hasNext()) {
					System.out.print(".");
					FileSummary nextFile = (FileSummary) iter2.next();
					FileSummary.register(nextFile);
				}
			}

			System.out.println(" ");
		}
		catch (ClassNotFoundException cnfe) {
			packageMap = null;
			cnfe.printStackTrace(System.out);
		}
	}


	/**
	 *  Initialization method
	 */
	private static void init() {
		if (packageMap == null) {
			packageMap = new HashMap();
		}
	}
}
