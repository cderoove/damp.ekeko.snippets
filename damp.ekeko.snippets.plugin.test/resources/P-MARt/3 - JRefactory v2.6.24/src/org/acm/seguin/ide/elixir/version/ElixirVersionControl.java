package org.acm.seguin.ide.elixir.version;
import javax.swing.JMenuItem;

import org.acm.seguin.tools.install.RefactoryInstaller;
import org.acm.seguin.version.SourceSafe;
import org.acm.seguin.version.VersionControl;
import org.acm.seguin.version.VersionControlCache;

/**
 *  Interact with version control 
 *
 *@author     Chris Seguin 
 *@created    June 29, 1999 
 */
public class ElixirVersionControl implements IVersionControl {
	//  Instance Variables
	private VersionControl delegate = null;


	/**
	 *  Creates a menu item 
	 *
	 *@param  parent  Node that describes the file 
	 *@return         The menu item 
	 */
	public JMenuItem getMenu(TNode parent) {
		String name = parent.getName();

		JMenuItem jmi = new JMenuItem("Querying source control...");
		jmi.setEnabled(false);

		if (delegate == null) {
			init();
		}
		ElixirContainsThread ect = new ElixirContainsThread(jmi, parent, delegate, this);
		ect.start();

		return jmi;
	}


	/**
	 *  Is this file contained in visual source safe? 
	 *
	 *@param  filename  The full path of the file in question 
	 *@return           Returns true if it is in source safe 
	 */
	public boolean contains(String filename) {
		VersionControlCache cache = VersionControlCache.getCache();
		return cache.lookup(filename) != VersionControlCache.ADD;
	}


	/**
	 *  Adds a file to visual source safe 
	 *
	 *@param  filename  The full path to the file 
	 */
	public void add(String filename) {
		System.out.println("Add:  " + filename);
		VersionControlCache cache = VersionControlCache.getCache();
		cache.add(filename, VersionControlCache.ADD_PROGRESS);
		if (delegate == null) {
			init();
		}

		Thread evct = new ElixirVersionControlThread(delegate, filename, ElixirVersionControlThread.ADD);
		evct.start();
	}


	/**
	 *  Checks in a file to visual source safe 
	 *
	 *@param  filename  The full pathname of the file 
	 */
	public void checkIn(String filename) {
		System.out.println("Check In:  " + filename);
		if (delegate == null) {
			init();
		}

		Thread evct = new ElixirVersionControlThread(delegate, filename, ElixirVersionControlThread.CHECK_IN);
		evct.start();
	}


	/**
	 *  Checks out a file from visual source safe 
	 *
	 *@param  filename  The full path name of the file 
	 */
	public void checkOut(String filename) {
		System.out.println("Check Out:  " + filename);
		if (delegate == null) {
			init();
		}

		Thread evct = new ElixirVersionControlThread(delegate, filename, ElixirVersionControlThread.CHECK_OUT);
		evct.start();
	}


	/**
	 *  Adds an array of files 
	 *
	 *@param  filenames  The array of files to add 
	 */
	public void add(String[] filenames) {
		System.out.println("Multiple Add");
		for (int ndx = 0; ndx < filenames.length; ndx++) {
			add(filenames[ndx]);
		}
	}


	/**
	 *  Checks in multiple files 
	 *
	 *@param  filenames  Multiple files to check in 
	 */
	public void checkIn(String[] filenames) {
		System.out.println("Multiple Check In");
		for (int ndx = 0; ndx < filenames.length; ndx++) {
			checkIn(filenames[ndx]);
		}
	}


	/**
	 *  Constructor for ElixirVersionControl object 
	 */
	private synchronized void init() {
		if (delegate == null) {
			//  Make sure everything is installed properly
			(new RefactoryInstaller(false)).run();

			delegate = new SourceSafe();
		}
	}
}
