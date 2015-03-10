/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.metrics;

import java.util.Iterator;

import org.acm.seguin.summary.FieldAccessSummary;
import org.acm.seguin.summary.FieldSummary;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.ImportSummary;
import org.acm.seguin.summary.LocalVariableSummary;
import org.acm.seguin.summary.MessageSendSummary;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.PackageSummary;
import org.acm.seguin.summary.ParameterSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.SummaryTraversal;
import org.acm.seguin.summary.SummaryVisitor;
import org.acm.seguin.summary.TypeDeclSummary;
import org.acm.seguin.summary.TypeSummary;
import org.acm.seguin.summary.VariableSummary;

/**
 *  Gathers metrics data
 *
 *@author     Chris Seguin
 *@created    July 1, 1999
 */
public class GatherData implements SummaryVisitor {
	//  Instance Variables
	private MetricsReport metricsReport;


	/**
	 *  Constructor for the StatementReportVisitor object
	 *
	 *@param  init  Description of Parameter
	 */
	public GatherData(MetricsReport init) {
		metricsReport = init;
	}


	/**
	 *  Visit everything in all packages
	 *
	 *@param  data  a data value
	 *@return       Description of the Returned Value
	 */
	public Object visit(Object data) {
		ProjectMetrics projectData = new ProjectMetrics();

		Iterator iter = PackageSummary.getAllPackages();
		if (iter != null) {
			while (iter.hasNext()) {
				PackageSummary next = (PackageSummary) iter.next();
				next.accept(this, projectData);
			}
		}

		return projectData;
	}


	/**
	 *  Visit a summary node. This is the default method.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(Summary node, Object data) {
		//  Shouldn't have to do anything about one of these nodes
		return data;
	}


	/**
	 *  Visit a package summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(PackageSummary node, Object data) {
		PackageMetrics packageData = new PackageMetrics(node.getName());

		Iterator iter = node.getFileSummaries();
		if (iter != null) {
			while (iter.hasNext()) {
				FileSummary next = (FileSummary) iter.next();
				next.accept(this, packageData);
			}
		}

		//  Add to total
		ProjectMetrics projectData = (ProjectMetrics) data;
		projectData.add(packageData);

		//  Return the metrics gathered at this level
		return packageData;
	}


	/**
	 *  Visit a file summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(FileSummary node, Object data) {
		if (node.getFile() == null) {
			return data;
		}

		//  Over the types
		Iterator iter = node.getTypes();

		if (iter != null) {
			while (iter.hasNext()) {
				TypeSummary next = (TypeSummary) iter.next();
				next.accept(this, data);
			}
		}

		//  Return some value
		return data;
	}


	/**
	 *  Visit a import summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(ImportSummary node, Object data) {
		//  No children so just return
		return data;
	}


	/**
	 *  Visit a type summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(TypeSummary node, Object data) {
		//  Local Variables
		PackageMetrics packageData = (PackageMetrics) data;
		TypeMetrics typeData = new TypeMetrics(packageData.getPackageName(), node.getName());

		//  Over the fields
		Iterator iter = node.getFields();
		if (iter != null) {
			while (iter.hasNext()) {
				FieldSummary next = (FieldSummary) iter.next();
				if (next.getModifiers().isStatic()) {
					typeData.incrClassVariableCount();
				}
				else {
					typeData.incrInstanceVariableCount();
				}
			}
		}

		//  Over the methods
		iter = node.getMethods();
		if (iter != null) {
			while (iter.hasNext()) {
				MethodSummary next = (MethodSummary) iter.next();
				next.accept(this, typeData);

				if (next.getModifiers().isStatic()) {
					typeData.incrClassMethodCount();
				}
				else if (next.getModifiers().isPublic()) {
					typeData.incrPublicMethodCount();
				}
				else {
					typeData.incrOtherMethodCount();
				}
			}
		}

		//  Over the types
		iter = node.getTypes();
		if (iter != null) {
			while (iter.hasNext()) {
				TypeSummary next = (TypeSummary) iter.next();
				next.accept(this, data);
			}
		}

		//  Print the results
		if (metricsReport != null) {
			metricsReport.typeReport(typeData);
		}

		//  Update the totals
		packageData.add(typeData);

		if (node.getModifiers().isAbstract()) {
			packageData.incrAbstractClassCount();
		}

		if (node.isInterface()) {
			packageData.incrInterfaceCount();
		}

		//  Return the metrics gathered at this level
		return typeData;
	}


	/**
	 *  Visit a method summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(MethodSummary node, Object data) {
		//  Local Variables
		TypeMetrics typeData = (TypeMetrics) data;

		//  Gather metrics
		int count = node.getStatementCount();
		int params = node.getParameterCount();

		//  Create method metrics object
		MethodMetrics methodMetrics = new MethodMetrics(typeData.getPackageName(),
				typeData.getTypeName(), node.getName());
		methodMetrics.setStatementCount(count);
		methodMetrics.setParameterCount(params);
		methodMetrics.setLinesOfCode(node.getEndLine() - node.getStartLine());
		methodMetrics.setBlockDepth(node.getMaxBlockDepth());

		//  Report the metrics
		if (metricsReport != null) {
			metricsReport.methodReport(methodMetrics);
		}

		//  Type data
		typeData.add(methodMetrics);

		//  Return the metrics collected
		return methodMetrics;
	}


	/**
	 *  Visit a field summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(FieldSummary node, Object data) {
		return data;
	}


	/**
	 *  Visit a parameter summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(ParameterSummary node, Object data) {
		return data;
	}


	/**
	 *  Visit a local variable summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(LocalVariableSummary node, Object data) {
		return data;
	}


	/**
	 *  Visit a variable summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(VariableSummary node, Object data) {
		return data;
	}


	/**
	 *  Visit a type declaration summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(TypeDeclSummary node, Object data) {
		return data;
	}


	/**
	 *  Visit a message send summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(MessageSendSummary node, Object data) {
		return data;
	}


	/**
	 *  Visit a field access summary.
	 *
	 *@param  node  the summary that we are visiting
	 *@param  data  the data that was passed in
	 *@return       the result
	 */
	public Object visit(FieldAccessSummary node, Object data) {
		return data;
	}


	/**
	 *  Main program
	 *
	 *@param  args  the command line arguments
	 */
	public static void main(String[] args) {
		if (args.length == 0) {
			(new SummaryTraversal(System.getProperty("user.dir"))).go();
		}
		else {
			(new SummaryTraversal(args[0])).go();
		}

		//  Now print everything
		MetricsReport metricsReport = new TextReport();
		GatherData visitor = new GatherData(metricsReport);
		ProjectMetrics projectData = (ProjectMetrics) visitor.visit("");

		metricsReport.finalReport(projectData);
	}
}
