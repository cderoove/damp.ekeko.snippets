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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.parser.factory.FileParserFactory;
import org.acm.seguin.parser.factory.InputStreamParserFactory;
import org.acm.seguin.parser.factory.ParserFactory;
import org.acm.seguin.pretty.PrintData;

/**
 *  Holds a refactoring. Default version just pretty prints the file.
 *
 *@author     Chris Seguin
 *@created    May 12, 1999
 */
public class StubFile
{
	//  Instance Variables
	private ParserFactory factory;
	private OutputStream out;
	private String name;
	private File outputFile;

	private static boolean creating = false;


	/**
	 *  Refactors java code.
	 *
	 *@param  init  Description of Parameter
	 *@param  file  Description of Parameter
	 */
	public StubFile(String init, File file)
	{
		factory = null;
		out = null;
		name = init;
		outputFile = file;

		StubFile.creating = true;
	}


	/**
	 *  Set the parser factory
	 *
	 *@param  factory  Description of Parameter
	 */
	public void setParserFactory(ParserFactory factory)
	{
		this.factory = factory;
	}


	/**
	 *  Return the factory that gets the abstract syntax trees
	 *
	 *@return    the parser factory
	 */
	public ParserFactory getParserFactory()
	{
		return factory;
	}


	/**
	 *  Create the stub for this file
	 *
	 *@param  inputFile  the input file
	 */
	public void apply(File inputFile)
	{
		setParserFactory(new FileParserFactory(inputFile));
		apply();
	}


	/**
	 *  Create the stub for this file
	 *
	 *@param  inputStream  the input stream
	 *@param  filename     the name of the file contained by the input stream
	 */
	public void apply(InputStream inputStream, String filename)
	{
		setParserFactory(new InputStreamParserFactory(inputStream, filename));
		apply();
	}


	/**
	 *  Close the file and note that we have completed this operation
	 */
	public void done()
	{
		if (out != null)
		{
			try
			{
				out.close();
			}
			catch (IOException ioe)
			{
			}
		}

		StubFile.creating = false;
		StubFile.resume();
	}


	/**
	 *  Create the output stream
	 *
	 *@param  file  the name of the file
	 *@return       the output stream
	 */
	protected OutputStream getOutputStream(File file)
	{
		if (out != null)
		{
			return out;
		}

		if (name != null)
		{
			String home = System.getProperty("user.home");
			File directory = new File(home + File.separator + ".Refactory");
			if (!directory.exists())
			{
				directory.mkdirs();
			}

			try
			{
				File outFile = new File(directory, name + ".stub");
				//System.out.println("Creating output stream:  " + outFile.getPath());
				out = new FileOutputStream(outFile.getPath(), true);
			}
			catch (IOException ioe)
			{
				out = System.out;
			}
		}
		else
		{
			try
			{
				//System.out.println("Creating output stream:  " + outputFile.getPath());
				out = new FileOutputStream(outputFile.getPath(), true);
			}
			catch (IOException ioe)
			{
				out = System.out;
			}
		}

		//  Return the output stream
		return out;
	}


	/**
	 *  Return the appropriate print data
	 *
	 *@param  input  Description of Parameter
	 *@return        the print data
	 */
	private PrintData getPrintData(File input)
	{
		//  Create the new stream
		return new PrintData(getOutputStream(input));
	}


	/**
	 *  Create the stub for this file
	 */
	private void apply()
	{
		//  Create the visitor
		StubPrintVisitor printer = new StubPrintVisitor();

		//  Create the appropriate print data
		PrintData data = getPrintData(null);
		SimpleNode root = factory.getAbstractSyntaxTree(false);

		if (root != null)
		{
			printer.visit(root, data);
		}

		//  Flush the output stream
		data.flush();
		try
		{
			out.write("\n\n|\n".getBytes());
		}
		catch (IOException ioe)
		{
			ioe.printStackTrace(System.out);
		}
	}


	/**
	 *  Wait while this is being created
	 */
	public static synchronized void waitForCreation()
	{
		if (creating)
		{
			try
			{
				StubFile.class.wait();
			}
			catch (InterruptedException ie)
			{
			}
		}
	}


	/**
	 *  Resume all processing
	 */
	private static synchronized void resume()
	{
		StubFile.class.notifyAll();
	}
}
