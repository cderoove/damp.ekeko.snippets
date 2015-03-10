/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.common;

import java.io.File;
import java.util.StringTokenizer;

import org.acm.seguin.summary.SummaryTraversal;

/**
 *  Loads all the class settings based on a particular directory
 *
 *@author    Chris Seguin
 */
public class SummaryLoaderThread extends Thread {
	private String base;
	private static int count = 0;


	/**
	 *  Constructor for the SummaryLoaderThread object
	 *
	 *@param  init  The root directory to load
	 */
	public SummaryLoaderThread(String init) {
		base = init;
	}


	/**
	 *  Main processing method for the SummaryLoaderThread object
	 */
	public void run() {
		synchronized (SummaryLoaderThread.class) {
			StringTokenizer tok = new StringTokenizer(base, File.pathSeparator);
			while (tok.hasMoreTokens()) {
				String next = tok.nextToken();
				(new SummaryTraversal(next)).go();
			}

			System.out.println("Completed loading the metadata");
			count = 0;
		}
	}


	/**
	 *  This just confirms that you have loaded the summaries
	 *  into memory.
	 */
	public static synchronized void waitForLoading() {
		count++;
	}
}
