package org.acm.seguin.ide.common;

import java.io.File;

/**
 *  Base class for source browsing.  This is the generic
 *  base class.
 *
 *@author    Chris Seguin
 */
public class NoSourceBrowser extends SourceBrowser {
	/**
	 *  Determines if the system is in a state where
	 *  it can browse the source code
	 *
	 *@return    true if the source code browsing is enabled
	 */
	public boolean canBrowseSource() {
		return false;
	}


	/**
	 *  Actually browses to the file
	 *
	 *@param  filename  the file
	 *@param  line      the line in the file
	 */
	public void gotoSource(File file, int line) {
		//  Do nothing
	}
}
