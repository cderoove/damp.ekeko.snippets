package org.acm.seguin.tools.build;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.StringTokenizer;

/**
 *  Creates  a JRefactoryVersion object from a command line string
 *  specifying the version.
 *
 *@author    Chris Seguin
 */
public class CreateVersion {
	private String major;
	private String minor;
	private String build;

	private String output;


	/**
	 *  Constructor for the CreateVersion object
	 *
	 *@param  input  the command line argument
	 */
	public CreateVersion(String input, String output) {
		StringTokenizer tok = new StringTokenizer(input, ".");

		major = tok.nextToken();
		minor = tok.nextToken();
		build = tok.nextToken();

		this.output = output;
	}


	/**
	 *  Main processing method for the CreateVersion object
	 */
	public void run() {
try {
		PrintWriter printer = new PrintWriter(new FileWriter(output));

		printer.println("package org.acm.seguin;");
		printer.println("");
		printer.println("/**");
		printer.println(" *  The current version of JRefactory");
		printer.println(" *");
		printer.println(" *@author    Chris Seguin");
		printer.println(" */");
		printer.println("public class JRefactoryVersion {");
		printer.println("	/**");
		printer.println("	 *  Gets the MajorVersion attribute of the JRefactoryVersion object");
		printer.println("	 *");
		printer.println("	 *@return    The MajorVersion value");
		printer.println("	 */");
		printer.println("	public int getMajorVersion() {");
		printer.println("		return " + major + ";");
		printer.println("	}");
		printer.println("");
		printer.println("");
		printer.println("	/**");
		printer.println("	 *  Gets the MinorVersion attribute of the JRefactoryVersion object");
		printer.println("	 *");
		printer.println("	 *@return    The MinorVersion value");
		printer.println("	 */");
		printer.println("	public int getMinorVersion() {");
		printer.println("		return " + minor + ";");
		printer.println("	}");
		printer.println("");
		printer.println("");
		printer.println("	/**");
		printer.println("	 *  Gets the Build attribute of the JRefactoryVersion object");
		printer.println("	 *");
		printer.println("	 *@return    The Build value");
		printer.println("	 */");
		printer.println("	public int getBuild() {");
		printer.println("		return " + build + ";");
		printer.println("	}");
		printer.println("");
		printer.println("");
		printer.println("	/**");
		printer.println("	 *  Converts the JRefactoryVersion to a string");
		printer.println("	 *");
		printer.println("	 *@return    a string representing the version");
		printer.println("	 */");
		printer.println("	public String toString() {");
		printer.println("		StringBuffer buffer = new StringBuffer();");
		printer.println("");
		printer.println("		buffer.append(getMajorVersion());");
		printer.println("		buffer.append('.');");
		printer.println("");
		printer.println("		buffer.append(getMinorVersion());");
		printer.println("		buffer.append('.');");
		printer.println("");
		printer.println("		buffer.append(getBuild());");
		printer.println("");
		printer.println("		return buffer.toString();");
		printer.println("	}");
		printer.println("");
		printer.println("   public static void main(String[] args) {");
		printer.println("       System.out.println(\"Version:  \" + (new JRefactoryVersion()).toString());");
		printer.println("   }");
		printer.println("}");

		printer.flush();
		printer.close();
	}
	catch (IOException ioe) {
	ioe.printStackTrace();
	}
	}


	/**
	 *  The main program for the CreateVersion class
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args) {
		CreateVersion cv = new CreateVersion(args[0], args[1]);
		cv.run();
	}
}
