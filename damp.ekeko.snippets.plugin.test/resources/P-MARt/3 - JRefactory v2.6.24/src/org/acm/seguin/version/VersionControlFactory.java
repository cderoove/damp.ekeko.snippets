/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.version;

import org.acm.seguin.util.FileSettings;

/**
 *  A factory for the version control system
 *
 *@author    Chris Seguin
 */
public class VersionControlFactory {
	private static VersionControl singleton = null;


	/**
	 *  Constructor for the VersionControlFactory object
	 */
	private VersionControlFactory() { }


	/**
	 *  Gets the current version control system
	 *
	 *@return    the system
	 */
	public static VersionControl get()
	{
		if (singleton == null) {
			init();
		}

		return singleton;
	}


	/**
	 *  Description of the Method
	 */
	private static synchronized void init()
	{
		if (singleton == null) {
			try {
				FileSettings bundle = FileSettings.getSettings("Refactory", "vss");
				String className = bundle.getString("version.control");
				singleton = (VersionControl) Class.forName(className).newInstance();
			}
			catch (Exception exc) {
				singleton = new UserDirectedVersionControl();
			}
		}
	}
}
