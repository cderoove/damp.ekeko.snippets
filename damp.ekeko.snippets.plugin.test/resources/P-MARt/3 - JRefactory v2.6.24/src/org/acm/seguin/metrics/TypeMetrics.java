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
 *  Stores the metrics for a particular type
 *
 *@author     Chris Seguin
 *@created    July 23, 1999
 */
public class TypeMetrics {
	/*<Instance Variables>*/
	private String packageName;
	private String typeName;
	private int publicMethodCount = 0;
	private int otherMethodCount = 0;
	private int classMethodCount = 0;
	private int instanceVariableCount = 0;
	private int classVariableCount = 0;
	private int statementTotal = 0;
	private int parameterTotal = 0;
	private int blockDepthTotal = 0;
	private int lineCountTotal = 0;


	/*</Instance Variables>*/

	/*<Constructor>*/
	/**
	 *  Constructor for the TypeMetrics object
	 *
	 *@param  initPackage  The package
	 *@param  initType     The type
	 */
	public TypeMetrics(String initPackage, String initType) {
		packageName = initPackage;
		typeName = initType;
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
	 *  Return the public method count
	 *
	 *@return    The public method count
	 */
	public int getPublicMethodCount() {
		return publicMethodCount;
	}


	/**
	 *  Return the other method count
	 *
	 *@return    The other method count
	 */
	public int getOtherMethodCount() {
		return otherMethodCount;
	}


	/**
	 *  Return the class method count
	 *
	 *@return    The class method count
	 */
	public int getClassMethodCount() {
		return classMethodCount;
	}


	/**
	 *  Return the instance variable count
	 *
	 *@return    The instance variable count
	 */
	public int getInstanceVariableCount() {
		return instanceVariableCount;
	}


	/**
	 *  Return the class variable count
	 *
	 *@return    The class variable count
	 */
	public int getClassVariableCount() {
		return classVariableCount;
	}


	/**
	 *  Return the statement total
	 *
	 *@return    The statement total
	 */
	public int getStatementTotal() {
		return statementTotal;
	}


	/**
	 *  Return the parameter total
	 *
	 *@return    The parameter total
	 */
	public int getParameterTotal() {
		return parameterTotal;
	}


	/**
	 *  Return the block depth for use in computing system wide averages
	 *
	 *@return    The block depth total
	 */
	public int getBlockDepthTotal() {
		return blockDepthTotal;
	}


	/**
	 *  Return the lines of code for use in computing system wide averages
	 *
	 *@return    The block depth total
	 */
	public int getLinesOfCodeTotal() {
		return lineCountTotal;
	}


	/**
	 *  Return the statement average
	 *
	 *@return    The statement average
	 */
	public double getStatementAverage() {
		double top = statementTotal;
		double bottom = publicMethodCount + otherMethodCount + classMethodCount;
		return top / bottom;
	}


	/**
	 *  Return the parameter average
	 *
	 *@return    The parameter average
	 */
	public double getParameterAverage() {
		double top = parameterTotal;
		double bottom = publicMethodCount + otherMethodCount + classMethodCount;
		return top / bottom;
	}


	/**
	 *  Return the average block depth
	 *
	 *@return    The parameter average
	 */
	public double getBlockDepthAverage() {
		double top = blockDepthTotal;
		double bottom = publicMethodCount + otherMethodCount + classMethodCount;
		return top / bottom;
	}


	/**
	 *  Return the average lines of code
	 *
	 *@return    The parameter average
	 */
	public double getLinesOfCodeAverage() {
		double top = lineCountTotal;
		double bottom = publicMethodCount + otherMethodCount + classMethodCount;
		return top / bottom;
	}
	/*</Getters>*/

	/*<Setters>*/
	/**
	 *  Increment the public method count
	 */
	void incrPublicMethodCount() {
		publicMethodCount++;
	}


	/**
	 *  Increment the other method count
	 */
	void incrOtherMethodCount() {
		otherMethodCount++;
	}


	/**
	 *  Increment the class method count
	 */
	void incrClassMethodCount() {
		classMethodCount++;
	}


	/**
	 *  Increment the instance variable count
	 */
	void incrInstanceVariableCount() {
		instanceVariableCount++;
	}


	/**
	 *  Increment the class variable count
	 */
	void incrClassVariableCount() {
		classVariableCount++;
	}


	/**
	 *  Add in a method
	 *
	 *@param  methodData  the method data
	 */
	void add(MethodMetrics methodData) {
		statementTotal += methodData.getStatementCount();
		parameterTotal += methodData.getParameterCount();
		blockDepthTotal += methodData.getBlockDepth();
		lineCountTotal += methodData.getLinesOfCode();
	}
	/*</Setters>*/
}
