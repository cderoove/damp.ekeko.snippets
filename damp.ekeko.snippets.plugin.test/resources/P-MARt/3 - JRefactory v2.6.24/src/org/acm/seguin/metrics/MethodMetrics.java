/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.metrics;

/**
 *  Stores the metrics for a particular method
 *
 *@author     Chris Seguin
 *@created    July 23, 1999
 */
public class MethodMetrics {
	/*<Instance Variables>*/
	private String packageName;
	private String typeName;
	private String methodName;
	private int statementCount;
	private int parameterCount;
	private int blockDepth;
	private int lines;
	/*</Instance Variables>*/

	/*<Constructor>*/
	/**
	 *  Constructor for the MethodMetrics object
	 *
	 *@param  initPackage  The package
	 *@param  initType     The type
	 *@param  initMethod   The method
	 */
	public MethodMetrics(String initPackage, String initType, String initMethod) {
		packageName = initPackage;
		typeName = initType;
		methodName = initMethod;
		statementCount = 0;
		parameterCount = 0;
		blockDepth = 0;
		lines = 0;
	}


	/*</Constructor>*/

	/*<Getters>*/
	/**
	 *  Return the package name
	 *
	 *@return    the package name
	 */
	public String getPackageName() {
		return packageName;
	}


	/**
	 *  Return the type name
	 *
	 *@return    The type name
	 */
	public String getTypeName() {
		return typeName;
	}


	/**
	 *  Return the method name
	 *
	 *@return    The method name
	 */
	public String getMethodName() {
		return methodName;
	}


	/**
	 *  Return the statement count
	 *
	 *@return    The statement count
	 */
	public int getStatementCount() {
		return statementCount;
	}


	/**
	 *  Return the parameter count
	 *
	 *@return    The parameter count
	 */
	public int getParameterCount() {
		return parameterCount;
	}

	public int getLinesOfCode() {
		return lines;
	}

	public int getBlockDepth() {
		return blockDepth;
	}
	/*</Getters>*/

	/*<Setters>*/
	/**
	 *  Set the statement count
	 *
	 *@param  count  The statement count
	 */
	void setStatementCount(int count) {
		statementCount = count;
	}


	/**
	 *  Set the parameter count
	 *
	 *@param  count  The parameter count
	 */
	void setParameterCount(int count) {
		parameterCount = count;
	}

	void setBlockDepth(int count) {
		blockDepth = count;
	}

	void setLinesOfCode(int count) {
		lines = count;
	}
	/*</Setters>*/
}
