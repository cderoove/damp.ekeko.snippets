/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.parser.factory;

import java.util.StringTokenizer;

import org.acm.seguin.parser.ast.ASTName;

/**
 *  Creates Name objects from strings 
 *
 *@author     Chris Seguin 
 *@created    November 23, 1999 
 */
public class NameFactory {
	/**
	 *  Constructor for the NameFactory object 
	 */
	private NameFactory() {
	}


	/**
	 *  Creates a ASTName object based on the packageName and the className. If 
	 *  the className is null, it is without a class name. 
	 *
	 *@param  packageName  the package name 
	 *@param  className    the class name or null 
	 *@return              The ASTName value 
	 */
	public static ASTName getName(String packageName, String className) {
		StringTokenizer tok = new StringTokenizer(packageName, ".");

		ASTName result = new ASTName(0);

		while (tok.hasMoreTokens()) {
			result.addNamePart(tok.nextToken());
		}

		if (className != null) {
			result.addNamePart(className);
		}

		return result;
	}
}
