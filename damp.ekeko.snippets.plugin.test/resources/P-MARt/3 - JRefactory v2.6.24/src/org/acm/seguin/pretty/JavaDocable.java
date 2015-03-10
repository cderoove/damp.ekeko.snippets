/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.pretty;

/**
 *  Possible to store java doc components 
 *
 *@author     Chris Seguin 
 *@created    April 15, 1999 
 */
public interface JavaDocable {
	/**
	 *  Allows you to add a java doc component 
	 *
	 *@param  component  the component that can be added 
	 */
	public void addJavaDocComponent(JavaDocComponent component);


	/**
	 *  Prints all the java doc components 
	 *
	 *@param  printData  the print data 
	 */
	public void printJavaDocComponents(PrintData printData);


	/**
	 *  Makes sure all the java doc components are present 
	 */
	public void finish();


	/**
	 *  Checks to see if it was printed 
	 *
	 *@return    true if it still needs to be printed 
	 */
	public boolean isRequired();
}
