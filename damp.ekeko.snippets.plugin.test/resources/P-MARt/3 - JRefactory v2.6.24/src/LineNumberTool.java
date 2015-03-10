/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;

import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.parser.factory.FileParserFactory;
import org.acm.seguin.parser.factory.ParserFactory;
import org.acm.seguin.parser.factory.StdInParserFactory;
import org.acm.seguin.pretty.PrettyPrintVisitor;
import org.acm.seguin.pretty.PrintData;
import org.acm.seguin.pretty.line.LineNumberingData;
import org.acm.seguin.tools.install.RefactoryInstaller;

/**
 *  Tool that uses the pretty printer to number lines
 *
 *@author     Chris Seguin
 *@created    May 11, 1999
 */
public class LineNumberTool
{
	//  Instance Variables
	private ArrayList inputList;
	private String dest;
	private OutputStream out;


	/**
	 *  Constructor for the line numbering
	 */
	public LineNumberTool()
	{
		inputList = new ArrayList();
		dest = null;
		out = null;
	}


	/**
	 *  Read command line inputs
	 *
	 *@param  args  Description of Parameter
	 */
	protected void init(String[] args)
	{
		int last = args.length;
		for (int ndx = 0; ndx < last; ndx++)
		{
			if (args[ndx].equals("-help"))
			{
				System.out.println("Pretty Printer Version 1.0.  Has the following inputs");
				System.out.println("         java LineNumberTool [-out filename] (inputfile)*");
				System.out.println("OR");
				System.out.println("         java LineNumberTool [-out filename] < inputfile");
				System.out.println("where");
				System.out.println("         -out filename     Output to the file or directory");
			}
			else if (args[ndx].equals("-out"))
			{
				ndx++;
				dest = args[ndx];
			}
			else
			{
				//  Add another input to the list
				inputList.add(args[ndx]);
			}
		}
	}


	/**
	 *  Run the pretty printer
	 */
	protected void run()
	{
		//  Local Variables
		int last = inputList.size();

		//  Create the visitor
		PrettyPrintVisitor printer = new PrettyPrintVisitor();

		//  Create the appropriate print data
		PrintData data = null;

		for (int index = 0; (index < last) || (index == 0); index++)
		{
			data = getPrintData(index, data);

			//  Create the parser and visit the parse tree
			printer.visit(getRoot(index), data);

			//  Flush the output stream
			data.flush();
		}
		data.close();
	}


	/**
	 *  Create the output stream
	 *
	 *@param  index     the index of the output stream
	 *@param  filename  the name of the file
	 *@return           the output stream
	 */
	private OutputStream getOutputStream(int index, String filename)
	{
		//  Local Variables
		OutputStream out = null;

		//  Check the destination
		if (dest == null)
		{
			out = System.out;
		}
		else
		{
			try
			{
				out = new FileOutputStream(dest);
			}
			catch (IOException ioe)
			{
				//  Hmmm...  Can't create the output stream, then fall back to stdout
				out = System.out;
			}
		}

		//  Return the output stream
		return out;
	}


	/**
	 *  Return the appropriate print data
	 *
	 *@param  oldPrintData  the old print data
	 *@param  index         Description of Parameter
	 *@return               the print data
	 */
	private PrintData getPrintData(int index, PrintData oldPrintData)
	{
		if (oldPrintData == null)
		{
			out = getOutputStream(index, null);
			return new LineNumberingData(out);
		}
		else
		{
			oldPrintData.flush();
			try
			{
				out.write(12);
			}
			catch (IOException ioe)
			{
			}
			return oldPrintData;
		}
	}


	/**
	 *  Create the parser
	 *
	 *@param  index  the index
	 *@return        the java parser
	 */
	private SimpleNode getRoot(int index)
	{
		File in;
		ParserFactory factory;

		if (inputList.size() > index)
		{
			in = new File((String) inputList.get(index));
			factory = new FileParserFactory(in);
		}
		else
		{
			factory = new StdInParserFactory();
		}

		//  Create the parse tree
		return factory.getAbstractSyntaxTree(true);
	}


	/**
	 *  Main program
	 *
	 *@param  args  the command line arguments
	 */
	public static void main(String args[])
	{
		//  Make sure everything is installed properly
		(new RefactoryInstaller(false)).run();

		try
		{
			LineNumberTool pp = new LineNumberTool();
			pp.init(args);
			pp.run();
		}
		catch (Throwable t)
		{
			if (t == null)
			{
				System.out.println("We have caught a null throwable");
			}
			else
			{
				System.out.println("t is a " + t.getClass().getName());
				System.out.println("t has a message " + t.getMessage());
				t.printStackTrace(System.out);
			}
		}
	}
}
