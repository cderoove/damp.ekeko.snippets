package org.acm.seguin.pretty.line;

import java.io.PrintWriter;

import org.acm.seguin.pretty.LineQueue;

class NumberedLineQueue extends LineQueue {
	NumberedLineQueue(PrintWriter output) {
		super(output);
	}

	/**  Writes the line to the output stream */
	protected void writeln(String value) {
		PrintWriter out = getOutput();
		//  Insert initial spaces
		if (lineNumber < 10) {
			out.print("    " + lineNumber + "  ");
		}
		else if (lineNumber < 100) {
			out.print("   " + lineNumber + "  ");
		}
		else if (lineNumber < 1000) {
			out.print("  " + lineNumber + "  ");
		}
		else if (lineNumber < 10000) {
			out.print(" " + lineNumber + "  ");
		}
		else if (lineNumber < 100000) {
			out.print(lineNumber + "  ");
		}

		//  Print the line
		super.writeln(value);
	}
}
