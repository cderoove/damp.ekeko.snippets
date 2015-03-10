package org.acm.seguin.ide.elixir.version;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.acm.seguin.version.VersionControlCache;

/**
 *  Checks a file into source safe 
 *
 *@author     Chris Seguin 
 *@created    June 17, 1999 
 */
public class CheckInListener implements ActionListener {
	//  Instance Variables
	private ElixirVersionControl ess;
	private String fullName;
	private String name;


	/**
	 *  Creates an instance of this 
	 *
	 *@param  init      The elixir version control unit 
	 *@param  fullName  the file's full name 
	 *@param  name      the file's name 
	 */
	public CheckInListener(ElixirVersionControl init, String fullName, String name) {
		ess = init;
		this.fullName = fullName;
		this.name = name;
	}


	/**
	 *  The menu item was selected 
	 *
	 *@param  evt  the menu selection event 
	 */
	public void actionPerformed(ActionEvent evt) {
		VersionControlCache cache = VersionControlCache.getCache();
		cache.add(fullName, VersionControlCache.CHECK_IN_PROGRESS);
		ess.checkIn(fullName);
		cache.add(fullName, VersionControlCache.CHECK_OUT);
	}
}
