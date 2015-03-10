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
 *  The settings file does not exist 
 *
 *@author     Chris Seguin 
 *@created    October 3, 1999 
 */
public class NoSettingsFileException extends MissingSettingsException {
	/**
	 *  Constructor for the NoSettingsFileException object 
	 *
	 *@param  app   the name of the application 
	 *@param  type  the name of the type 
	 */
	public NoSettingsFileException(String app, String type) {
		super(app, type);
	}


	/**
	 *  Returns a message describing this exception 
	 *
	 *@return    the message 
	 */
	public String getMessage() {
		return "No settings found for the application:  " + getApplication() + 
				" with the name " + getType();
	}
}
