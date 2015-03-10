package org.acm.seguin.ide.elixir.version;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.acm.seguin.version.VersionControlCache;

/**
 *  Checks a file out of source safe 
 *
 *@author     Chris Seguin 
 *@created    June 17, 1999 
 */
public class CheckOutListener implements ActionListener {
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
	public CheckOutListener(ElixirVersionControl init, String fullName, String name) {
		ess = init;
		this.fullName = fullName;
		this.name = name;
	}


	/**
	 *  The menu item was selected 
	 *
	 *@param  evt  the event 
	 */
	public void actionPerformed(ActionEvent evt) {
		VersionControlCache cache = VersionControlCache.getCache();
		cache.add(fullName, VersionControlCache.CHECK_OUT_PROGRESS);
		ess.checkOut(fullName);
		cache.add(fullName, VersionControlCache.CHECK_IN);
	}
}
