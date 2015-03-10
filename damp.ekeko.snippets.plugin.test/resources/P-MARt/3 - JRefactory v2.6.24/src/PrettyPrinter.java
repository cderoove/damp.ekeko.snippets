/*
 * Copyright 1999
 *
 * Chris Seguin
 * All rights reserved
 */
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.acm.seguin.io.AllFileFilter;
import org.acm.seguin.io.DirectoryTreeTraversal;
import org.acm.seguin.io.ExtensionFileFilter;
import org.acm.seguin.pretty.PrettyPrintFile;
import org.acm.seguin.tools.install.RefactoryInstaller;

/**
 *  Traverses a directory structure and performs all refactorings on the
 *  files.
 *
 *@author     Chris Seguin
 *@created    May 12, 1999
 */
public class PrettyPrinter extends DirectoryTreeTraversal {
	//  Instance Variables
	private PrettyPrintFile ppf;


	/**
	 *  The main program
	 *
	 *@param  args  Description of Parameter
	 */
	public static void main(String[] args) {
		try {
			//  Make sure everything is installed properly
			(new RefactoryInstaller(false)).run();

			int lastOption = -1;
			boolean quiet = false;

			for (int ndx = 0; ndx < args.length; ndx++) {
				if (args[ndx].equals("-quiet") || args[ndx].equals("-u")) {
					quiet = true;
					lastOption = ndx;
				}
				else if (args[ndx].equals("-?") || args[ndx].equalsIgnoreCase("-h") || args[ndx].equalsIgnoreCase("-help")) {
					printHelpMessage();
					return;
				}
			}

			if (lastOption + 1 >= args.length) {
				// no more arguments left
				if (quiet) {
					prettyPrinter(quiet);
				}
				else {
					prettyPrinter(System.getProperty("user.dir"), quiet);
				}
			}
			else {
				// process remaining arguments as file / dir names
				for (int ndx = lastOption + 1; ndx < args.length; ++ndx) {
					prettyPrinter(args[ndx], quiet);
				}
			}
		}
		catch (Throwable thrown) {
			thrown.printStackTrace(System.out);
		}

		//  Exit
		System.exit(1);
	}

	/**  Print a help message  */
	private static void printHelpMessage() {
				System.out.println("Syntax:  java PrettyPrinter file   //  means refactor this file");
				System.out.println("   OR    java PrettyPrinter [-quiet|-u] dir   //  means refactor this directory");
				System.out.println("   OR    java PrettyPrinter [-quiet|-u]   //  means refactor the current directory");
				System.out.println("  the -quiet or the -u flag tells the pretty printer not to prompt the user");
	}


	/**
	 *  Refactor the current file
	 *
	 *@param  filename  Description of Parameter
	 */
	public static void prettyPrinter(String filename, boolean quiet) {
		(new PrettyPrinter(filename, quiet)).go();
	}


	/**
	 *  Refactor the current file
	 */
	public static void prettyPrinter(boolean quiet) {
		JFileChooser chooser = new JFileChooser();

		//  Create the java file filter
		ExtensionFileFilter filter = new ExtensionFileFilter();
		filter.addExtension(".java");
		filter.setDescription("Java Source Files (.java)");
		chooser.setFileFilter(filter);

		//  Add other file filters - All
		FileFilter allFilter = new AllFileFilter();
		chooser.addChoosableFileFilter(allFilter);

		//  Set it so that files and directories can be selected
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

		//  Set the directory to the current directory
		chooser.setCurrentDirectory(new File(System.getProperty("user.dir")));

		//  Get the user's selection
		int returnVal = chooser.showOpenDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			(new PrettyPrinter(chooser.getSelectedFile().getAbsolutePath(), quiet)).go();
		}
	}


	/**
	 *  Creates a refactory
	 *
	 *@param  init  the initial directory or file
	 */
	public PrettyPrinter(String init, boolean quiet) {
		super(init);

		if (init == null) {
			return;
		}

		ppf = new PrettyPrintFile();
		ppf.setAsk(!quiet && (new File(init)).isDirectory());
	}


	/**
	 *  Determines if this file should be handled by this traversal
	 *
	 *@param  currentFile  the current file
	 *@return              true if the file should be handled
	 */
	protected boolean isTarget(File currentFile) {
		return (currentFile.getName().endsWith(".java"));
	}


	/**
	 *  Visits the current file
	 *
	 *@param  currentFile  the current file
	 */
	protected void visit(File currentFile) {
		if (ppf.isApplicable(currentFile)) {
			System.out.println("Applying the Pretty Printer:  " + currentFile.getPath());
			ppf.apply(currentFile);
		}
	}
}
