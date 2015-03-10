/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.metrics;

import java.text.NumberFormat;

import org.acm.seguin.summary.TypeSummary;

/**
 *  Type metrics frame 
 *
 *@author     Chris Seguin 
 *@created    July 26, 1999 
 */
public class TypeMetricsFrame extends MetricsFrame {
	//  Instance Variables
	private TypeSummary type;
	private TypeMetrics metrics;


	/**
	 *  Constructor for the TypeMetricsFrame object 
	 *
	 *@param  initType  Description of Parameter 
	 */
	public TypeMetricsFrame(TypeSummary initType) {
		type = initType;
		PackageMetrics temp = new PackageMetrics("-package-");
		GatherData data = new GatherData(null);
		metrics = (TypeMetrics) data.visit(type, temp);

		//  Fill in the description array
		descriptions = new String[]
				{"Description", 
				"Statement Total", "Statement Average", 
				"Parameter Total", "Parameter Average", 
				"Public Method Count", "Other Method Count", "Class Metric Count", 
				"Instance Variable Count", "Class Variable Count"};

		//  Fill in the value array
        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);

		values = new String[10];
		values[0] = "Value";
		values[1] = nf.format(metrics.getStatementTotal());
		values[2] = nf.format(metrics.getStatementAverage());
		values[3] = nf.format(metrics.getParameterTotal());
		values[4] = nf.format(metrics.getParameterAverage());
		values[5] = nf.format(metrics.getPublicMethodCount());
		values[6] = nf.format(metrics.getOtherMethodCount());
		values[7] = nf.format(metrics.getClassMethodCount());
		values[8] = nf.format(metrics.getInstanceVariableCount());
		values[9] = nf.format(metrics.getClassVariableCount());

		//  Create the frame
		createFrame();
	}


	/**
	 *  Returns the title of this frame 
	 *
	 *@return    Description of the Returned Value 
	 */
	protected String getTitle() {
		return "Metrics for the class " + type.getName();
	}
}
