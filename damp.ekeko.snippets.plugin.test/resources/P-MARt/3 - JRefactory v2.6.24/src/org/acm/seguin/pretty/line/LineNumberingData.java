/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.pretty.line;

import java.io.OutputStream;
import java.io.PrintWriter;

import org.acm.seguin.pretty.LineQueue;
import org.acm.seguin.pretty.PrintData;

/**
 *  The line number data
 *
 *@author     Chris Seguin
 *@created    October 14, 1999
 */
public class LineNumberingData extends PrintData {
	/**
	 *  Create a print data object
	 */
	public LineNumberingData() {
		super();
	}


	/**
	 *  Create a print data object
	 *
	 *@param  out  the output stream
	 */
	public LineNumberingData(OutputStream out) {
		super(out);
	}

	/**  Creates a line queue object
	*@param output the output stream
	*@return the queue
	*/
	protected LineQueue lineQueueFactory(PrintWriter output) {
		return new NumberedLineQueue(output);
	}
}
