/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.util;

/**
 *  The settings interface 
 *
 *@author     Chris Seguin 
 *@created    October 3, 1999 
 */
public interface Settings {
	/**
	 *  Gets a string 
	 *
	 *@param  code  The code to look up 
	 *@return       The associated string 
	 */
	public String getString(String code);


	/**
	 *  Gets a integer 
	 *
	 *@param  code  The code to look up 
	 *@return       The associated integer 
	 */
	public int getInteger(String code);


	/**
	 *  Gets a double 
	 *
	 *@param  code  The code to look up 
	 *@return       The associated double 
	 */
	public double getDouble(String code);


	/**
	 *  Gets a boolean 
	 *
	 *@param  code  The code to look up 
	 *@return       The associated boolean 
	 */
	public boolean getBoolean(String code);
}
