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
 *  Stores the metrics for a particular package
 *
 *@author     Chris Seguin
 *@created    July 23, 1999
 */
public class PackageMetrics {
	/*<Instance Variables>*/
	private String packageName;
	private int publicMethodTotal = 0;
	private int otherMethodTotal = 0;
	private int classMethodTotal = 0;
	private int instanceVariableTotal = 0;
	private int classVariableTotal = 0;
	private int classTotal = 0;
	private int abstractClassCount = 0;
	private int interfaceCount = 0;
	private int statementTotal = 0;
	private int parameterTotal = 0;
	private int blockDepthTotal = 0;
	private int lineCountTotal = 0;


	/*</Instance Variables>*/

	/*<Constructor>*/
	/**
	 *  Constructor for the PackageMetrics object
	 *
	 *@param  initPackage  Description of Parameter
	 */
	public PackageMetrics(String initPackage) {
		packageName = initPackage;
	}


	/*</Constructor>*/

	/*<Getters>*/
	/**
	 *  Get the package name
	 *
	 *@return    the package name
	 */
	public String getPackageName() {
		return packageName;
	}


	/**
	 *  Return the public method count
	 *
	 *@return    The public method count
	 */
	public int getPublicMethodTotal() {
		return publicMethodTotal;
	}


	/**
	 *  Return the other method count
	 *
	 *@return    The other method count
	 */
	public int getOtherMethodTotal() {
		return otherMethodTotal;
	}


	/**
	 *  Return the class method count
	 *
	 *@return    The class method count
	 */
	public int getClassMethodTotal() {
		return classMethodTotal;
	}


	/**
	 *  Return the instance variable count
	 *
	 *@return    The instance variable count
	 */
	public int getInstanceVariableTotal() {
		return instanceVariableTotal;
	}


	/**
	 *  Return the class variable count
	 *
	 *@return    The class variable count
	 */
	public int getClassVariableTotal() {
		return classVariableTotal;
	}


	/**
	 *  Return the class count
	 *
	 *@return    The class count
	 */
	public int getClassTotal() {
		return classTotal;
	}


	/**
	 *  Return the abstract class count
	 *
	 *@return    The abstract class count
	 */
	public int getAbstractClassCount() {
		return abstractClassCount;
	}


	/**
	 *  Return the abstract class count
	 *
	 *@return    The abstract class count
	 */
	public int getInterfaceCount() {
		return interfaceCount;
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
	 *  Return the statement average
	 *
	 *@return    The statement average
	 */
	public double getStatementAverage() {
		double top = statementTotal;
		double bottom = publicMethodTotal + otherMethodTotal + classMethodTotal;
		return top / bottom;
	}


	/**
	 *  Return the parameter average
	 *
	 *@return    The parameter average
	 */
	public double getParameterAverage() {
		double top = parameterTotal;
		double bottom = publicMethodTotal + otherMethodTotal + classMethodTotal;
		return top / bottom;
	}



	/**
	 *  Return the block depth average
	 *
	 *@return    The block depth average
	 */
	public double getBlockDepthAverage() {
		double top = blockDepthTotal;
		double bottom = publicMethodTotal + otherMethodTotal + classMethodTotal;
		return top / bottom;
	}



	/**
	 *  Return the lines of code average
	 *
	 *@return    The lines of code average
	 */
	public double getLinesOfCodeAverage() {
		double top = lineCountTotal;
		double bottom = publicMethodTotal + otherMethodTotal + classMethodTotal;
		return top / bottom;
	}

	/**
	 *  Return the public method count
	 *
	 *@return    The public method count
	 */
	public double getPublicMethodAverage() {
		double top = publicMethodTotal;
		double bottom = classTotal;
		return top / bottom;
	}


	/**
	 *  Return the other method count
	 *
	 *@return    The other method count
	 */
	public double getOtherMethodAverage() {
		double top = otherMethodTotal;
		double bottom = classTotal;
		return top / bottom;
	}


	/**
	 *  Return the class method count
	 *
	 *@return    The class method count
	 */
	public double getClassMethodAverage() {
		double top = classMethodTotal;
		double bottom = classTotal;
		return top / bottom;
	}


	/**
	 *  Return the instance variable count
	 *
	 *@return    The instance variable count
	 */
	public double getInstanceVariableAverage() {
		double top = instanceVariableTotal;
		double bottom = classTotal;
		return top / bottom;
	}


	/**
	 *  Return the class variable count
	 *
	 *@return    The class variable count
	 */
	public double getClassVariableAverage() {
		double top = classVariableTotal;
		double bottom = classTotal;
		return top / bottom;
	}


	/**
	 *  Return the abstract class count
	 *
	 *@return    The abstract class count
	 */
	public double getAbstractClassPercentage() {
		double top = abstractClassCount;
		double bottom = classTotal;
		return 100 * top / bottom;
	}


	/**
	 *  Return the abstract class count
	 *
	 *@return    The abstract class count
	 */
	public double getInterfacePercentage() {
		double top = interfaceCount;
		double bottom = classTotal;
		return 100 * top / bottom;
	}


	/*</Getters>*/

	/*<Setters>*/
	/**
	 *  Increment the abstract class count
	 */
	void incrAbstractClassCount() {
		abstractClassCount++;
	}


	/**
	 *  Increment the interface count
	 */
	void incrInterfaceCount() {
		interfaceCount++;
	}


	/**
	 *  Add in a type
	 *
	 *@param  typeData  the type data
	 */
	void add(TypeMetrics typeData) {
		publicMethodTotal += typeData.getPublicMethodCount();
		otherMethodTotal += typeData.getOtherMethodCount();
		classMethodTotal += typeData.getClassMethodCount();
		instanceVariableTotal += typeData.getInstanceVariableCount();
		classVariableTotal += typeData.getClassVariableCount();
		statementTotal += typeData.getStatementTotal();
		parameterTotal += typeData.getParameterTotal();
		lineCountTotal += typeData.getLinesOfCodeTotal();
		blockDepthTotal += typeData.getBlockDepthTotal();
		classTotal++;
	}
	/*</Setters>*/
}
