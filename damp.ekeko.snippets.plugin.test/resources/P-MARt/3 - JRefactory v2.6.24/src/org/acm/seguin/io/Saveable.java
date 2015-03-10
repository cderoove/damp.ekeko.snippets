/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.io;

import java.io.IOException;

/**
 *  Notifies something that this object can be saved autonomically. 
 *
 *@author     Chris Seguin 
 *@created    August 12, 1999 
 */
public interface Saveable {
	/**
	 *  Saves the object 
	 *
	 *@exception  IOException  thrown if trouble saving the file 
	 */
	public void save() throws IOException;
}
