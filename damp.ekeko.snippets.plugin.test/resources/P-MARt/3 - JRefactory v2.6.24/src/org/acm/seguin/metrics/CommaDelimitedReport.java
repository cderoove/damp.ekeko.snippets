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
 *  Reports metrics in a comma delimited format
 *
 *@author     Chris Seguin
 *@created    July 1, 1999
 */
public class CommaDelimitedReport extends MetricsReport {
	/**
	 *  Constructor for the CommaDelimitedReport object
	 */
	public CommaDelimitedReport() {
		System.out.println("Metric Code,Value,Package,Type,Method,Raw/Avg");
	}


	/**
	 *  Make a final report on totals
	 *
	 *@param  projectData  Description of Parameter
	 */
	public void finalReport(ProjectMetrics projectData) {
		super.finalReport(projectData);
		printKey();
	}

	/**
	 *  Prints out the key
	 */
	private void printKey() {
		System.out.println(" ");
		System.out.println(" ");
		System.out.println("Key");
		System.out.println(",Metric Code,Description");
		System.out.println(",000,Statement Count");
		System.out.println(",001,Number of Public Methods");
		System.out.println(",002,Number of Other Methods");
		System.out.println(",003,Number of Class Methods");
		System.out.println(",004,Number of Instance Variables");
		System.out.println(",005,Number of Class Variables");
		System.out.println(",006,Number of Abstract Classes");
		System.out.println(",007,Number of Interfaces");
		System.out.println(",008,Parameter Count");
		System.out.println(",009,Total Classes");
		System.out.println(",010,Lines of Code");
		System.out.println(",011,Block Depth");
	}


	/**
	 *  Reports on the number of statements
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  name   the name of the method
	 *@param  count  the number of statements
	 */
	protected void reportStatement(String pack, String type, String name, int count) {
		System.out.println("000," + count + "," + pack + "," + type + "," + name + ",raw");
	}


	/**
	 *  Reports on the number of parameters
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  name   the name of the method
	 *@param  count  the number of parameters
	 */
	protected void reportParameters(String pack, String type, String name, int count) {
		System.out.println("008," + count + "," + pack + "," + type + "," + name + ",raw");
	}


	/**
	 *  Reports on the number of lines of code
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  name   the name of the method
	 *@param  count  the number of lines of code
	 */
	protected void reportLinesOfCode(String pack, String type, String name, int count) {
		System.out.println("010," + count + "," + pack + "," + type + "," + name + ",raw");
	}


	/**
	 *  Reports on the block depth of code
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  name   the name of the method
	 *@param  count  the number of blocks deep
	 */
	protected void reportBlockDepth(String pack, String type, String name, int count) {
		System.out.println("011," + count + "," + pack + "," + type + "," + name + ",raw");
	}


	/**
	 *  Reports on the number of public methods
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  count  the number of public methods
	 */
	protected void reportPublicMethods(String pack, String type, int count) {
		System.out.println("001," + count + "," + pack + "," + type + ",---,raw");
	}


	/**
	 *  Reports on the number of other methods
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  count  the number of other methods
	 */
	protected void reportOtherMethods(String pack, String type, int count) {
		System.out.println("002," + count + "," + pack + "," + type + ",---,raw");
	}


	/**
	 *  Reports on the number of class methods
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  count  the number of class methods
	 */
	protected void reportClassMethods(String pack, String type, int count) {
		System.out.println("003," + count + "," + pack + "," + type + ",---,raw");
	}


	/**
	 *  Reports on the number of instance variables
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  count  the number of instance variables
	 */
	protected void reportInstanceVariables(String pack, String type, int count) {
		System.out.println("004," + count + "," + pack + "," + type + ",---,raw");
	}


	/**
	 *  Reports on the number of class variables
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  count  the number of class variables
	 */
	protected void reportClassVariables(String pack, String type, int count) {
		System.out.println("005," + count + "," + pack + "," + type + ",---,raw");
	}


	/**
	 *  Reports on the number of abstract classes
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected void reportAbstractClasses(ProjectMetrics projectData) {
		//  Abstract Classes
		double top = projectData.getAbstractClassTotal();
		double bottom = projectData.getClassTotal();
		System.out.println("006," + projectData.getAbstractClassTotal() + ",---,---,---,raw");
		System.out.println("006," + (100 * top / bottom) + ",---,---,---,percent");
	}


	/**
	 *  Reports on the number of interfaces
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected void reportInterfaces(ProjectMetrics projectData) {
		//  Interfaces
		double top = projectData.getInterfaceTotal();
		double bottom = projectData.getClassTotal();
		System.out.println("007," + projectData.getInterfaceTotal() + ",---,---,---,raw");
		System.out.println("007," + (100 * top / bottom) + ",---,---,---,percent");
	}


	/**
	 *  Reports on the number of classes
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected void reportClasses(ProjectMetrics projectData) {
		System.out.println("009," + projectData.getClassTotal() + ",---,---,---,raw");
	}


	/**
	 *  Reports on the average number of statements
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected void reportAverageStatements(ProjectMetrics projectData) {
		//  Public Methods
		double top = projectData.getStatementTotal();
		double bottom = projectData.getMethodTotal();
		System.out.println("000," + projectData.getStatementTotal() + ",---,---,---,total");
		System.out.println("000," + (top / bottom) + ",---,---,---,avg");
	}


	/**
	 *  Reports on the average number of parameters
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected void reportAverageParameters(ProjectMetrics projectData) {
		//  Public Methods
		double top = projectData.getParameterTotal();
		double bottom = projectData.getMethodTotal();
		System.out.println("008," + projectData.getParameterTotal() + ",---,---,---,total");
		System.out.println("008," + (top / bottom) + ",---,---,---,avg");
	}


	/**
	 *  Reports on the average number of public methods
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected void reportAveragePublicMethods(ProjectMetrics projectData) {
		//  Public Methods
		double top = projectData.getPublicMethodTotal();
		double bottom = projectData.getClassTotal();
		System.out.println("001," + projectData.getPublicMethodTotal() + ",---,---,---,total");
		System.out.println("001," + (top / bottom) + ",---,---,---,avg");
	}


	/**
	 *  Reports on the average number of other methods
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected void reportAverageOtherMethods(ProjectMetrics projectData) {
		//  Other Methods
		double top = projectData.getOtherMethodTotal();
		double bottom = projectData.getClassTotal();
		System.out.println("002," + projectData.getOtherMethodTotal() + ",---,---,---,total");
		System.out.println("002," + (top / bottom) + ",---,---,---,avg");
	}


	/**
	 *  Reports on the average number of class methods
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected void reportAverageClassMethods(ProjectMetrics projectData) {
		//  Class Methods
		double top = projectData.getClassMethodTotal();
		double bottom = projectData.getClassTotal();
		System.out.println("003," + projectData.getClassMethodTotal() + ",---,---,---,total");
		System.out.println("003," + (top / bottom) + ",---,---,---,avg");
	}


	/**
	 *  Reports on the average number of instance variables
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected void reportAverageInstanceVariables(ProjectMetrics projectData) {
		//  Instance Variables
		double top = projectData.getInstanceVariableTotal();
		double bottom = projectData.getClassTotal();
		System.out.println("004," + projectData.getInstanceVariableTotal() + ",---,---,---,total");
		System.out.println("004," + (top / bottom) + ",---,---,---,avg");
	}


	/**
	 *  Reports on the average number of class variables
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected void reportAverageClassVariables(ProjectMetrics projectData) {
		//  Class Variables
		double top = projectData.getClassVariableTotal();
		double bottom = projectData.getClassTotal();
		System.out.println("005," + projectData.getClassVariableTotal() + ",---,---,---,total");
		System.out.println("005," + (top / bottom) + ",---,---,---,avg");
	}
}
