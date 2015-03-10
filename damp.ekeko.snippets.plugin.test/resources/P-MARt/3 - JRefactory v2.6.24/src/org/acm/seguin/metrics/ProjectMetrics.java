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
 *  Stores the metrics for a particular project 
 *
 *@author     Chris Seguin 
 *@created    July 23, 1999 
 */
public class ProjectMetrics {
	/*<Instance Variables>*/
	private int publicMethodTotal = 0;
	private int otherMethodTotal = 0;
	private int classMethodTotal = 0;
	private int instanceVariableTotal = 0;
	private int classVariableTotal = 0;
	private int classTotal = 0;
	private int abstractClassTotal = 0;
	private int interfaceTotal = 0;
	private int statementTotal = 0;
	private int parameterTotal = 0;


	/*</Instance Variables>*/

	/*<Constructor>*/
	/**
	 *  Constructor for the ProjectMetrics object 
	 */
	public ProjectMetrics() {
	}


	/*</Constructor>*/

	/*<Getters>*/
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
	 *  Return the class method count 
	 *
	 *@return    The class method count 
	 */
	public int getMethodTotal() {
		return publicMethodTotal + classMethodTotal + otherMethodTotal;
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
	public int getAbstractClassTotal() {
		return abstractClassTotal;
	}


	/**
	 *  Return the abstract class count 
	 *
	 *@return    The abstract class count 
	 */
	public int getInterfaceTotal() {
		return interfaceTotal;
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
		double top = abstractClassTotal;
		double bottom = classTotal;
		return 100 * top / bottom;
	}


	/**
	 *  Return the abstract class count 
	 *
	 *@return    The abstract class count 
	 */
	public double getInterfacePercentage() {
		double top = interfaceTotal;
		double bottom = classTotal;
		return 100 * top / bottom;
	}


	/*</Getters>*/

	/*<Setters>*/
	/**
	 *  Add in a package 
	 *
	 *@param  packageData  the package data 
	 */
	void add(PackageMetrics packageData) {
		classTotal += packageData.getClassTotal();
		abstractClassTotal += packageData.getAbstractClassCount();
		interfaceTotal += packageData.getInterfaceCount();
		publicMethodTotal += packageData.getPublicMethodTotal();
		otherMethodTotal += packageData.getOtherMethodTotal();
		classMethodTotal += packageData.getClassMethodTotal();
		instanceVariableTotal += packageData.getInstanceVariableTotal();
		classVariableTotal += packageData.getClassVariableTotal();
		statementTotal += packageData.getStatementTotal();
		parameterTotal += packageData.getParameterTotal();
	}
	/*</Setters>*/
}
