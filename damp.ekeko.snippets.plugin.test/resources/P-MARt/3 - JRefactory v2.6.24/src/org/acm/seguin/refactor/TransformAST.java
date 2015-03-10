/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor;

import org.acm.seguin.parser.ast.SimpleNode;

/**
 *  This is the base class for any algorithm that updates the syntax tree. 
 *  Each of these objects contains one update to a syntax tree. 
 *
 *@author     Chris Seguin 
 *@created    October 23, 1999 
 */
public abstract class TransformAST {
	/**
	 *  Update the syntax tree 
	 *
	 *@param  root  the root of the syntax tree 
	 */
	public abstract void update(SimpleNode root);
}
