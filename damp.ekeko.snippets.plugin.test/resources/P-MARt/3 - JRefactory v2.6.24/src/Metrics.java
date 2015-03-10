/*
 * Copyright 1999
 *
 * Chris Seguin
 * All rights reserved
 */
import org.acm.seguin.metrics.CommaDelimitedReport;
import org.acm.seguin.metrics.GatherData;
import org.acm.seguin.metrics.MetricsReport;
import org.acm.seguin.metrics.ProjectMetrics;
import org.acm.seguin.metrics.TextReport;
import org.acm.seguin.summary.SummaryTraversal;
import org.acm.seguin.summary.load.LoadStatus;
import org.acm.seguin.summary.load.SilentLoadStatus;
import org.acm.seguin.summary.load.TextLoadStatus;
import org.acm.seguin.tools.install.RefactoryInstaller;

/**
 *  Gathers metrics data
 *
 *@author     Chris Seguin
 *@created    July 25, 1999
 */
public class Metrics {
	/**
	 *  Main program
	 *
	 *@param  args  the command line arguments
	 */
	public static void main(String[] args) {
		//  Make sure everything is installed properly
		(new RefactoryInstaller(false)).run();

		//  Local Variables
		String startDir = System.getProperty("user.dir");
		int type = 0;
		LoadStatus status = new TextLoadStatus();

		//  Process command line arguments
		int argIndex = 0;
		while (argIndex < args.length) {
			if (args[argIndex].equals("-help")) {
				System.out.println("Syntax:  java Metrics [-text|-comma] [dir]");
				return;
			}
			else if (args[argIndex].equals("-text")) {
				type = 0;
				argIndex++;
			}
			else if (args[argIndex].equals("-comma")) {
				type = 1;
				argIndex++;
			}
			else if (args[argIndex].equals("-quiet")) {
				status = new SilentLoadStatus();
				argIndex++;
			}
			else {
				startDir = args[argIndex];
				argIndex++;
			}
		}

		//  Gather the summary
		(new SummaryTraversal(startDir, status)).go();

		//  Now print everything
		MetricsReport metricsReport;
		if (type == 0) {
			metricsReport = new TextReport();
		}
		else {
			metricsReport = new CommaDelimitedReport();
		}
		GatherData visitor = new GatherData(metricsReport);
		ProjectMetrics projectData = (ProjectMetrics) visitor.visit("");

		metricsReport.finalReport(projectData);
	}
}
