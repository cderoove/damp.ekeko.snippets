/*
 * Copyright 1999
 *
 * Chris Seguin
 * All rights reserved
 */
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.StringTokenizer;

import org.acm.seguin.refactor.RefactoringException;
import org.acm.seguin.refactor.RefactoringFactory;
import org.acm.seguin.refactor.type.MoveClass;
import org.acm.seguin.tools.install.RefactoryInstaller;

/**
 *  Main program for repackaging. This object simply stores the main program
 *  and interprets the command line arguments for repackaging one or more
 *  files.
 *
 *@author     Chris Seguin
 *@created    June 2, 1999
 */
public class Repackage {
	//  Instance Variables
	private MoveClass moveClass;
	private boolean setPackage = false;
	private boolean atLeastOneClass = false;


	/**
	 *  Actual work of the main program occurs here
	 *
	 *@param  args                      the command line arguments
	 *@exception  RefactoringException  Description of Exception
	 */
	public void run(String[] args) throws RefactoringException {
		moveClass = RefactoringFactory.get().moveClass();
		if (init(args)) {
			moveClass.run();
		}
	}


	/**
	 *  Initialize the variables with command line arguments
	 *
	 *@param  args  the command line arguments
	 *@return       true if we should continue processing
	 */
	public boolean init(String[] args) {
		int nCurrentArg = 0;

		while (nCurrentArg < args.length) {
			if (args[nCurrentArg].equals("-dir")) {
				moveClass.setDirectory(args[nCurrentArg + 1]);
				nCurrentArg += 2;
			}
			else if (args[nCurrentArg].equals("-package")) {
				moveClass.setDestinationPackage(args[nCurrentArg + 1]);
				nCurrentArg += 2;
				setPackage = true;
			}
			else if (args[nCurrentArg].equals("-nopackage")) {
				moveClass.setDestinationPackage("");
				nCurrentArg++;
				setPackage = true;
			}
			else if (args[nCurrentArg].equals("-file")) {
				String filename = args[nCurrentArg + 1];
				load(filename);
				nCurrentArg += 2;
				atLeastOneClass = true;
			}
			else if (args[nCurrentArg].equals("-help")) {
				printHelpMessage();
				nCurrentArg++;
				return false;
			}
			else {
				moveClass.add(args[nCurrentArg]);
				nCurrentArg++;
				atLeastOneClass = true;
			}
		}

		return atLeastOneClass && setPackage;
	}


	/**
	 *  Print the help message
	 */
	protected void printHelpMessage() {
		System.out.println("Syntax:  java Repackage \\ ");
		System.out.println("        [-dir <dir>] [-help] ");
		System.out.println("        [-package <packagename> | -nopackage] (<file.java>)*");
		System.out.println("");
		System.out.println("  where:");
		System.out.println("    <dir>        is the name of the directory containing the files");
		System.out.println("    <package>    is the name of the new package");
		System.out.println("    <file.java>  is the name of the java file to be moved");
	}


	/**
	 *  Loads a file listing the names of java files to be moved
	 *
	 *@param  filename  the name of the file
	 */
	private void load(String filename) {
		try {
			BufferedReader input = new BufferedReader(new FileReader(filename));
			String line = input.readLine();
			while (line != null) {
				StringTokenizer tok = new StringTokenizer(line);
				while (tok.hasMoreTokens()) {
					String next = tok.nextToken();
					System.out.println("Adding:  " + next);
					moveClass.add(next);
				}
				line = input.readLine();
			}
			input.close();
		}
		catch (IOException ioe) {
			ioe.printStackTrace();
		}
	}


	/**
	 *  Main program
	 *
	 *@param  args  the command line arguments
	 */
	public static void main(String[] args) {
		try {
			//  Make sure everything is installed properly
			(new RefactoryInstaller(true)).run();

			(new Repackage()).run(args);
		}
		catch (Throwable thrown) {
			thrown.printStackTrace(System.out);
		}

		System.exit(0);
	}
}
