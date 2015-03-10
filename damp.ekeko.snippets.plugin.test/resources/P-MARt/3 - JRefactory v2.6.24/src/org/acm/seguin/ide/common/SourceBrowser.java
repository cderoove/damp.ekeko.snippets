/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.common;

import java.io.File;

/**
 *  Base class for source browsing. This is the generic base class.
 *
 *@author    Chris Seguin
 */
public abstract class SourceBrowser {
	private static SourceBrowser singleton = null;


	/**
	 *  Determines if the system is in a state where it can browse the source
	 *  code
	 *
	 *@return    true if the source code browsing is enabled
	 */
	public abstract boolean canBrowseSource();


	/**
	 *  Actually browses to the file
	 *
	 *@param  line      the line in the file
	 *@param  file      Description of Parameter
	 */
	public abstract void gotoSource(File file, int line);


	/**
	 *  Sets the singleton source browser
	 *
	 *@param  value  the new singleton
	 */
	public static void set(SourceBrowser value)
	{
		singleton = value;
	}


	/**
	 *  Gets the singleton source browser
	 *
	 *@return    the current source browser
	 */
	public static SourceBrowser get()
	{
		if (singleton == null) {
			singleton = new NoSourceBrowser();
		}
		return singleton;
	}
}
