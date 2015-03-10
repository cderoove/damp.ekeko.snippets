package org.acm.seguin.ide.elixir.version;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 *  Adds a file to source safe 
 *
 *@author     Chris Seguin 
 *@created    June 17, 1999 
 */
public class AddListener implements ActionListener {
	//  Instance Variables
	private ElixirVersionControl ess;
	private String fullName;
	private String name;


	/**
	 *  Creates an instance of this 
	 *
	 *@param  init      Description of Parameter 
	 *@param  fullName  Description of Parameter 
	 *@param  name      Description of Parameter 
	 */
	public AddListener(ElixirVersionControl init, String fullName, String name) {
		ess = init;
		this.fullName = fullName;
		this.name = name;
	}


	/**
	 *  The menu item was selected 
	 *
	 *@param  evt  Description of Parameter 
	 */
	public void actionPerformed(ActionEvent evt) {
		ess.add(fullName);
	}
}
