package org.acm.seguin.ide.elixir.version;

import java.io.File;

import javax.swing.JMenuItem;

import org.acm.seguin.util.FileSettings;
import org.acm.seguin.util.MissingSettingsException;
import org.acm.seguin.version.VersionControl;
import org.acm.seguin.version.VersionControlCache;

/**
 *  Interact with version control 
 *
 *@author     Chris Seguin 
 *@created    July 12, 1999 
 */
public class ElixirContainsThread extends Thread {
	//  Instance Variables
	private VersionControl delegate;
	private JMenuItem menu;
	private TNode parent;
	private ElixirVersionControl evc;


	/**
	 *  Constructor 
	 *
	 *@param  initMenuItem  The menu item 
	 *@param  initParent    The initial parent 
	 *@param  initDelegate  The delegate 
	 *@param  initEVC       Description of Parameter 
	 */
	public ElixirContainsThread(JMenuItem initMenuItem, TNode initParent, 
			VersionControl initDelegate, ElixirVersionControl initEVC) {
		menu = initMenuItem;
		parent = initParent;
		delegate = initDelegate;
		evc = initEVC;
	}


	/**
	 *  Actually do the work 
	 */
	public void run() {
		String name = parent.getName();

		if (!isUnderSourceControl(name)) {
			menu.setText("Not under source control");
			menu.setEnabled(false);
			return;
		}

		System.out.println("Full Name:  " + parent.getFullName());
		File file = new File(parent.getFullName());
		if (!file.canWrite()) {
			checkOut();
		}
		else if (contains(parent.getFullName()) == VersionControlCache.ADD) {
			add();
		}
		else {
			checkIn();
		}

		menu.repaint();
	}


	/**
	 *  Is this file contained in visual source safe? 
	 *
	 *@param  filename  The full path of the file in question 
	 *@return           Returns true if it is in source safe 
	 */
	public int contains(String filename) {
		//  Start with the cache
		VersionControlCache cache = VersionControlCache.getCache();
		if (cache.isInCache(filename)) {
			return cache.lookup(filename);
		}

		boolean way = delegate.contains(filename);
		int result;

		if (way) {
			result = VersionControlCache.CHECK_IN;
		}
		else {
			result = VersionControlCache.ADD;
		}

		cache.add(filename, result);
		return result;
	}


	/**
	 *  Determines if the file is under souce control 
	 *
	 *@param  name  the name of the file 
	 *@return       true if it is under source control 
	 */
	private boolean isUnderSourceControl(String name) {
		if (name == null) {
			return false;
		}

		try {
			FileSettings bundle = FileSettings.getSettings("Refactory", "vss");

			int index = 1;
			while (true) {
				String next = bundle.getString("extension." + index);
				System.out.println("\t\tComparing:  [" + name + "] to [" + next + "]");
				if (name.endsWith(next)) {
					System.out.println("\t\tFound it");
					return true;
				}
				index++;
			}
		}
		catch (MissingSettingsException mse) {
			//  Finished
		}

		return false;
	}


	/**
	 *  Sets the menu up to say that it is being checked out 
	 */
	private void checkOut() {
		boolean enabled = true;
		String filename = parent.getFullName();
		VersionControlCache cache = VersionControlCache.getCache();

		if (cache.isInCache(filename)) {
			enabled = (cache.lookup(filename) == VersionControlCache.CHECK_OUT);
		}
		else {
			cache.add(filename, VersionControlCache.CHECK_OUT);
		}

		menu.setText("Check Out");
		menu.setEnabled(enabled);
		menu.addActionListener(new CheckOutListener(evc, filename, parent.getName()));
	}


	/**
	 *  Description of the Method 
	 */
	private void add() {
		menu.setText("Add");
		menu.setEnabled(false);
		menu.addActionListener(new AddListener(evc, parent.getFullName(), parent.getName()));
	}


	/**
	 *  Description of the Method 
	 */
	private void checkIn() {
		VersionControlCache cache = VersionControlCache.getCache();
		String filename = parent.getFullName();
		menu.setText("Check In");
		menu.setEnabled(cache.lookup(filename) == VersionControlCache.CHECK_IN);
		menu.addActionListener(new CheckInListener(evc, filename, parent.getName()));
	}
}
