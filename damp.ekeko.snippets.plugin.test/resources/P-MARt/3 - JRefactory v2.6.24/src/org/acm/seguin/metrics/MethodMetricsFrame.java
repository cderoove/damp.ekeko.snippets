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

import org.acm.seguin.summary.MethodSummary;

/**
 *  Base class for metrics frame 
 *
 *@author     Chris Seguin 
 *@created    July 26, 1999 
 */
public class MethodMetricsFrame extends MetricsFrame {
	//  Instance Variables
	private MethodSummary method;
	private MethodMetrics metrics;


	/**
	 *  Constructor for the MethodMetricsFrame object 
	 *
	 *@param  initMethod  Description of Parameter 
	 */
	public MethodMetricsFrame(MethodSummary initMethod) {
		method = initMethod;
		TypeMetrics temp = new TypeMetrics("-package-", "-type-");
		GatherData data = new GatherData(null);
		metrics = (MethodMetrics) data.visit(method, temp);

		descriptions = new String[]{"Description", "Statement Count", "Parameter Count"};
		values = new String[3];

        NumberFormat nf = NumberFormat.getInstance();
        nf.setMaximumFractionDigits(2);
		values[0] = "Values";
		values[1] = "" + nf.format(metrics.getStatementCount());
		values[2] = "" + nf.format(metrics.getParameterCount());

		createFrame();
	}


	/**
	 *  Returns the title of this frame 
	 *
	 *@return    Description of the Returned Value 
	 */
	protected String getTitle() {
		return "Metrics for the method " + method.getName();
	}
}
