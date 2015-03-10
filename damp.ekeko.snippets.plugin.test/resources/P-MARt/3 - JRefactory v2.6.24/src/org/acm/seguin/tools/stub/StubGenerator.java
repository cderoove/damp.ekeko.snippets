/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.tools.stub;

import java.io.File;

import org.acm.seguin.tools.install.RefactoryInstaller;

/**
 *  Generates a stub set from a file or a directory
 *
 *@author    Chris Seguin
 */
public class StubGenerator extends Thread
{
	private String filename;
	private String key;
	private File file;


	/**
	 *  Constructor for the StubGenerator object
	 *
	 *@param  name     The name of the zip file
	 *@param  stubKey  The key associated with this stub
	 */
	public StubGenerator(String name, String stubKey)
	{
		filename = name;
		key = stubKey;
		file = null;
	}


	/**
	 *  Constructor for the StubGenerator object
	 *
	 *@param  name     The name of the zip file
	 *@param  output   Description of Parameter
	 */
	public StubGenerator(String name, File output)
	{
		filename = name;
		key = null;
		file = output;
	}


	/**
	 *  Main processing method for the StubGenerator object
	 */
	public void run()
	{
		synchronized (StubGenerator.class)
		{
			File sourceFile = new File(filename);
			if (sourceFile.isDirectory())
			{
				(new StubGenTraversal(filename, key, file)).run();
			}
			else
			{
				(new StubGenFromZip(filename, key, file)).run();
			}
		}
	}


	/**
	 *  Waits until it is appropriate to allow the stub files to be loaded
	 */
	public static synchronized void waitForLoaded()
	{
		//System.out.println("OK to load");
	}


	/**
	 *  The main program
	 *
	 *@param  args  the command line arguments
	 */
	public static void main(String[] args)
	{
		//  Make sure everything is installed properly
		(new RefactoryInstaller(false)).run();

		if (args.length != 2)
		{
			System.out.println("Syntax:  java org.acm.seguin.tools.stub.StubGenerator <name> <file>   ");
			System.out.println("   OR    java org.acm.seguin.tools.stub.StubGenerator <name> <dir>   ");
			System.out.println("   where <name> is the name of the stub file to generate");
			System.out.println("   where <file> is the jar or zip file");
			System.out.println("   where <dir> is the directory for one or more source files source file");
			return;
		}
		generateStubs(args[0], args[1]);

		//  Exit
		System.exit(1);
	}


	/**
	 *  Generate a stub for the current file or directory
	 *
	 *@param  filename  the name of the directory
	 *@param  stubname  the name of the stub
	 */
	public static void generateStubs(String stubname, String filename)
	{
		(new StubGenerator(filename, stubname)).run();
	}
}
