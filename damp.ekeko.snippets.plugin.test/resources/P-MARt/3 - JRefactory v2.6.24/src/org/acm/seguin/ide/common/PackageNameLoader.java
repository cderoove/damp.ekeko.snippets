package org.acm.seguin.ide.common;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

/**
 *  Loads the name of the package from the package.uml file
 *
 *@author    Chris Seguin
 */
public class PackageNameLoader {
	/**
	 *  Loads the package name from a package.uml file
	 *
	 *@param  filename  the name of the file
	 *@return           the package name
	 */
	public String load(String filename) {
		String packageName = "Unknown";

		try {
			BufferedReader input = new BufferedReader(new FileReader(filename));

			String line = input.readLine();
			packageName = parseLine(line);

			input.close();
		}
		catch (IOException ioe) {
		}

		return packageName;
	}


	/**
	 *  Parses the line
	 *
	 *@param  line  the line to parse
	 *@return       the package name
	 */
	private String parseLine(String line) {
		if (line.charAt(0) == 'V') {
			StringTokenizer tok = new StringTokenizer(line, "[:]");
			if (tok.hasMoreTokens()) {
				// Skip the first - it is the letter v
				tok.nextToken();
				if (tok.hasMoreTokens()) {
					// Skip the second - it is the version (1.1)
					tok.nextToken();
					if (tok.hasMoreTokens()) {
						//  Third item is the package name
						return tok.nextToken();
					}
				}
			}
		}

		return "Unknown";
	}
}
