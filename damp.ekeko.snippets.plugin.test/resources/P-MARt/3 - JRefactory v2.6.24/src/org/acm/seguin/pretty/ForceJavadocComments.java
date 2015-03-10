/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.pretty;

import org.acm.seguin.util.FileSettings;
import org.acm.seguin.util.SettingNotFoundException;

/**
 *  Forces javadoc comments only for a certain level of source code. The level 
 *  is based on the permissions associated with the file. 
 *
 *@author     Chris Seguin 
 *@created    December 20, 1999 
 */
public class ForceJavadocComments {
	/**
	 *  Determines if the java doc comment is required for the particular method 
	 *  or field 
	 *
	 *@param  type  the type (method or field) 
	 *@param  mods  the modifiers associated with this object 
	 *@return       true if they are required 
	 */
	public boolean isJavaDocRequired(String type, ModifierHolder mods) {
		//  Get the resource bundle
		FileSettings bundle = FileSettings.getSettings("Refactory", "pretty");

		//  Determine the minimum acceptable level
		String minimumLevel = "none";
		try {
			minimumLevel = bundle.getString(type + ".minimum");
		}
		catch (SettingNotFoundException snfe) {
			//  Use default value
		}

		//	Check the level
		return isAll(minimumLevel) || isPackage(minimumLevel, mods)
				 || isProtected(minimumLevel, mods)
				 || isPublic(minimumLevel, mods);
	}


	/**
	 *  Gets the All attribute of the ForceJavadocComments object 
	 *
	 *@param  level  Description of Parameter 
	 *@return        The All value 
	 */
	private boolean isAll(String level) {
		return level.equalsIgnoreCase("all") || level.equalsIgnoreCase("private");
	}


	/**
	 *  Gets the Package attribute of the ForceJavadocComments object 
	 *
	 *@param  level  Description of Parameter 
	 *@param  mods   Description of Parameter 
	 *@return        The Package value 
	 */
	private boolean isPackage(String level, ModifierHolder mods) {
		return (level.equalsIgnoreCase("package") || level.equalsIgnoreCase("default")) && 
				!mods.isPrivate();
	}


	/**
	 *  Gets the Protected attribute of the ForceJavadocComments object 
	 *
	 *@param  level  Description of Parameter 
	 *@param  mods   Description of Parameter 
	 *@return        The Protected value 
	 */
	private boolean isProtected(String level, ModifierHolder mods) {
		return level.equalsIgnoreCase("protected") && 
				(mods.isProtected() || mods.isPublic());
	}


	/**
	 *  Gets the Public attribute of the ForceJavadocComments object 
	 *
	 *@param  level  Description of Parameter 
	 *@param  mods   Description of Parameter 
	 *@return        The Public value 
	 */
	private boolean isPublic(String level, ModifierHolder mods) {
		return level.equalsIgnoreCase("public") && 
				mods.isPublic();
	}
}
