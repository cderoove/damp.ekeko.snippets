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
 *  Gathers metrics data
 *
 *@author     Chris Seguin
 *@created    July 1, 1999
 */
public abstract class MetricsReport {
	/**
	 *  Make a final report on totals
	 *
	 *@param  projectData  Description of Parameter
	 */
	public void finalReport(ProjectMetrics projectData) {
		reportAverageStatements(projectData);
		reportAveragePublicMethods(projectData);
		reportAverageOtherMethods(projectData);
		reportAverageClassMethods(projectData);
		reportAverageInstanceVariables(projectData);
		reportAverageClassVariables(projectData);
		reportAbstractClasses(projectData);
		reportInterfaces(projectData);
		reportAverageParameters(projectData);
	}


	/**
	 *  Method report shows all the metrics associated with a particular type.
	 *
	 *@param  typeData  the metrics for a particular type
	 */
	public void typeReport(TypeMetrics typeData) {
		reportPublicMethods(typeData.getPackageName(), typeData.getTypeName(),
				typeData.getPublicMethodCount());
		reportOtherMethods(typeData.getPackageName(), typeData.getTypeName(),
				typeData.getOtherMethodCount());
		reportClassMethods(typeData.getPackageName(), typeData.getTypeName(),
				typeData.getClassMethodCount());
		reportInstanceVariables(typeData.getPackageName(), typeData.getTypeName(),
				typeData.getInstanceVariableCount());
		reportClassVariables(typeData.getPackageName(), typeData.getTypeName(),
				typeData.getClassVariableCount());
	}


	/**
	 *  Method report shows all the metrics associated with a particular method.
	 *
	 *@param  methodData  the metrics associated with a particular method
	 */
	public void methodReport(MethodMetrics methodData) {
		reportStatement(methodData.getPackageName(), methodData.getTypeName(),
				methodData.getMethodName(), methodData.getStatementCount());
		reportParameters(methodData.getPackageName(), methodData.getTypeName(),
				methodData.getMethodName(), methodData.getParameterCount());
		reportLinesOfCode(methodData.getPackageName(), methodData.getTypeName(),
				methodData.getMethodName(), methodData.getLinesOfCode());
		reportBlockDepth(methodData.getPackageName(), methodData.getTypeName(),
				methodData.getMethodName(), methodData.getBlockDepth());
	}


	/**
	 *  Reports on the number of statements
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  name   the name of the method
	 *@param  count  the number of statements
	 */
	protected abstract void reportStatement(String pack, String type, String name, int count);


	/**
	 *  Reports on the number of parameters
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  name   the name of the method
	 *@param  count  the number of parameters
	 */
	protected abstract void reportParameters(String pack, String type, String name, int count);


	/**
	 *  Reports on the number of lines of code
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  name   the name of the method
	 *@param  count  the number of parameters
	 */
	protected abstract void reportLinesOfCode(String pack, String type, String name, int count);


	/**
	 *  Reports on the block depth of code
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  name   the name of the method
	 *@param  count  the number of parameters
	 */
	protected abstract void reportBlockDepth(String pack, String type, String name, int count);


	/**
	 *  Reports on the average number of statements
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected abstract void reportAverageStatements(ProjectMetrics projectData);


	/**
	 *  Reports on the average number of parameters
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected abstract void reportAverageParameters(ProjectMetrics projectData);


	/**
	 *  Reports on the number of public methods
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  count  the number of public methods
	 */
	protected abstract void reportPublicMethods(String pack, String type, int count);


	/**
	 *  Reports on the number of other methods
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  count  the number of other methods
	 */
	protected abstract void reportOtherMethods(String pack, String type, int count);


	/**
	 *  Reports on the number of class methods
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  count  the number of class methods
	 */
	protected abstract void reportClassMethods(String pack, String type, int count);


	/**
	 *  Reports on the number of instance variables
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  count  the number of instance variables
	 */
	protected abstract void reportInstanceVariables(String pack, String type, int count);


	/**
	 *  Reports on the number of class variables
	 *
	 *@param  pack   the name of the package
	 *@param  type   the name of the class or interface
	 *@param  count  the number of class variables
	 */
	protected abstract void reportClassVariables(String pack, String type, int count);


	/**
	 *  Reports on the number of abstract classes
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected abstract void reportAbstractClasses(ProjectMetrics projectData);


	/**
	 *  Reports on the number of interfaces
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected abstract void reportInterfaces(ProjectMetrics projectData);


	/**
	 *  Reports on the number of classes
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected abstract void reportClasses(ProjectMetrics projectData);


	/**
	 *  Reports on the average number of public methods
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected abstract void reportAveragePublicMethods(ProjectMetrics projectData);


	/**
	 *  Reports on the average number of other methods
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected abstract void reportAverageOtherMethods(ProjectMetrics projectData);


	/**
	 *  Reports on the average number of class methods
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected abstract void reportAverageClassMethods(ProjectMetrics projectData);


	/**
	 *  Reports on the average number of instance variables
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected abstract void reportAverageInstanceVariables(ProjectMetrics projectData);


	/**
	 *  Reports on the average number of class variables
	 *
	 *@param  projectData  Description of Parameter
	 */
	protected abstract void reportAverageClassVariables(ProjectMetrics projectData);
}
