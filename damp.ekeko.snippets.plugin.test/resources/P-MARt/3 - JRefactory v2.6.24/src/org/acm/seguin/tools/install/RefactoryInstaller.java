/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.tools.install;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;
import java.util.GregorianCalendar;

import javax.swing.JOptionPane;

import org.acm.seguin.awt.ExceptionPrinter;
import org.acm.seguin.print.PrintingSettings;
import org.acm.seguin.tools.stub.StubPrompter;
import org.acm.seguin.util.FileSettings;
import org.acm.seguin.util.MissingSettingsException;

/**
 *  Installs the refactory
 *
 *@author     Chris Seguin
 *@created    October 1, 1999
 */
public class RefactoryInstaller {
	private boolean refactory;
	private final static double PRETTY_CURRENT_VERSION = 3.7;
	private final static double UML_CURRENT_VERSION = 1.2;


	/**
	 *  Constructor for the RefactoryInstaller object
	 *
	 *@param  forRefactory  is true when we are installing software for a
	 *      refactory
	 */
	public RefactoryInstaller(boolean forRefactory)
	{
		refactory = forRefactory;
	}


	/**
	 *  Main procedure - actually does the work of installing the various
	 *  settings files if they are not present.
	 */
	public void run()
	{
		try {
			String home = FileSettings.getSettingsRoot();
			File homeDir = new File(home);
			String dir = home + File.separator + ".Refactory";
			File directory = new File(dir);
			if (!directory.exists()) {
				directory.mkdirs();
			}

			String filename = dir + File.separator + "pretty.settings";
			File file = new File(filename);
			FileWriter output;
			PrintWriter printer;
			if (!file.exists()) {
				output = new FileWriter(file);
				printer = new PrintWriter(output);
				prettySettings(printer, 0.0);
				printer.flush();
				output.flush();
				printer.close();
				output.close();

				//System.out.println("Length:  " + file.length());
				FileSettings bundle = FileSettings.getSettings("Refactory", "pretty");
				bundle.setReloadNow(true);
			}
			else {
				FileSettings bundle = FileSettings.getSettings("Refactory", "pretty");
				bundle.setReloadNow(true);

				double version = 1.0;
				try {
					String temp = bundle.getString("indent");
				}
				catch (MissingSettingsException mse) {
					version = 0.5;
				}
				try {
					version = bundle.getDouble("version");
				}
				catch (MissingSettingsException mse) {
				}

				if (version < (PRETTY_CURRENT_VERSION - 0.05)) {
					output = new FileWriter(file.getPath(), true);
					printer = new PrintWriter(output);
					prettySettings(printer, version);
					printer.flush();
					output.flush();
					printer.close();
					output.close();

					//System.out.println("Length:  " + file.length());
				}

				bundle.setReloadNow(true);
			}

			filename = dir + File.separator + "uml.settings";
			file = new File(filename);
			if (!file.exists()) {
				output = new FileWriter(file);
				printer = new PrintWriter(output);
				umlSettings(printer, 0.0, home);
				printer.close();
				output.close();
			}
			else {
				FileSettings bundle = FileSettings.getSettings("Refactory", "uml");
				bundle.setReloadNow(true);

				double version = 1.0;
				try {
					String temp = bundle.getString("stub.dir");
				}
				catch (MissingSettingsException mse) {
					version = 0.5;
				}
				try {
					version = bundle.getDouble("version");
				}
				catch (MissingSettingsException mse) {
				}

				if (version < (UML_CURRENT_VERSION - 0.05)) {
					output = new FileWriter(file.getPath(), true);
					printer = new PrintWriter(output);
					umlSettings(printer, version, home);
					printer.flush();
					printer.close();
				}
			}

			filename = dir + File.separator + "printing.settings";
			file = new File(filename);
			if (!file.exists()) {
				(new PrintingSettings()).save();
			}

			filename = dir + File.separator + "vss.settings";
			file = new File(filename);
			if (!file.exists()) {
				output = new FileWriter(file);
				printer = new PrintWriter(output);
				vssSettings(printer);
				printer.close();
				output.close();
			}

			filename = dir + File.separator + "process.settings";
			file = new File(filename);
			if (!file.exists()) {
				output = new FileWriter(file);
				printer = new PrintWriter(output);
				processSettings(printer);
				printer.close();
				output.close();
			}

			filename = dir + File.separator + "creation.txt";
			file = new File(filename);
			if (!file.exists()) {
				generateCreationText(file);
			}
			else {
				GregorianCalendar created = new GregorianCalendar();
				created.setTime(new Date(file.lastModified()));
				GregorianCalendar lastMonth = new GregorianCalendar();
				lastMonth.add(GregorianCalendar.MONTH, -1);

				File logFile = new File(homeDir, "log.txt");
				if (lastMonth.after(created) && logFile.exists() && (logFile.length() > 0)) {
					generateCreationText(file);

					String message =
							"Chris Seguin wrote JRefactory to discover\n" +
							"how people use refactorings.  While you have\n" +
							"used this tool, it has created a log of which\n" +
							"refactorings you used.  This log contains a\n" +
							"number representing the refactoring and a date.\n" +
							"\n" +
							"I would really appreciate it if you could e-mail\n" +
							"the following file to seguin@acm.org.\n" +
							"\n" +
							FileSettings.getSettingsRoot() + File.separator + "log.txt\n" +
							"\n" +
							"Thank you for taking the time to do this.\n";

					JOptionPane.showMessageDialog(null, message,
							"Research request",
							JOptionPane.QUESTION_MESSAGE);
				}
			}
		}
		catch (IOException ioe) {
			ExceptionPrinter.print(ioe);
		}

		if (refactory) {
			jsdkStubInstall();
		}
	}


	/**
	 *  Installs properties for the pretty printer
	 *
	 *@param  printer  The pretty printer
	 *@param  version  Description of Parameter
	 */
	private void prettySettings(PrintWriter printer, double version)
	{
		String username = System.getProperty("user.name");
		FileSettings bundle = null;
		try {
			bundle = FileSettings.getSettings("Refactory", "pretty");
			bundle.setContinuallyReload(false);
		}
		catch (Exception exc) {
			//  Good enough
		}

		if (version < (PRETTY_CURRENT_VERSION - 0.05)) {
			printer.println("");
			printer.println("#  Pretty Printer Version");
			printer.println("version=" + PRETTY_CURRENT_VERSION);
			printer.println("");
		}

		if (version < 1.0) {
			printer.println("#  Pretty.settings");
			printer.println("");
			printer.println("#  This is the number of spaces to indent for each block.");
			printer.println("#  Twice this number is the amount of space used for");
			printer.println("#  unexpected carrage returns.  Use the word \"tab\" for tabs");
			printer.println("#  and the word \"space\" for spaces.");
			printer.println("indent=4");
			printer.println("indent.char=space");
			printer.println("");
			printer.println("#  Style for { and }");
			printer.println("#  C style means that { is at the end of the line");
			printer.println("#  and } is on a line by itself.  For example,");
			printer.println("#  if (myTest) {");
			printer.println("#    //  This is c style");
			printer.println("#  }");
			printer.println("#");
			printer.println("#  PASCAL style means both { and } are on lines");
			printer.println("#  by themselves.  For example,");
			printer.println("#  if (myTest)");
			printer.println("#  {");
			printer.println("#    //  This is PASCAL style");
			printer.println("#  }");
			printer.println("#");
			printer.println("#  EMACS style means both { and } are on lines");
			printer.println("#  by themselves and indented 2 extra spaces.");
			printer.println("#  For example,");
			printer.println("#  if (myTest)");
			printer.println("#    {");
			printer.println("#      //  This is EMACS style");
			printer.println("#    }");
			printer.println("block.style=C");
			printer.println("");
			printer.println("#  The following parameter should be changed to true if you");
			printer.println("#  like your parens to have a space before and after them");
			printer.println("#  if ( x == y )    //expr.space=true");
			printer.println("#  if (x == y)      //expr.space=false");
			printer.println("expr.space=false");
			printer.println("");
			printer.println("#  The following parameter is the minimum number of blank lines");
			printer.println("#  between methods, nested classes, and nested interfaces.");
			printer.println("#  It is also the number of lines before and after");
			printer.println("#  field declarations, though field declarations will have");
			printer.println("#  what ever spacing you used.");
			printer.println("#");
			printer.println("#  Note that this is a minimum.  If your code already");
			printer.println("#  has more space between methods, then it won't shrink");
			printer.println("#  the number of blank lines.");
			printer.println("lines.between=2");
			printer.println("");
			printer.println("");
			printer.println("#");
			printer.println("#  Default Javadoc comments");
			printer.println("#");
			printer.println("# The following items are used by the mechanism that automatically");
			printer.println("# inserts javadoc comments, in case there are no existing javadoc");
			printer.println("# comments.");
			printer.println("#");
			printer.println("");
			//printer.println("#  Author - the default author");
			//printer.println("author=" + username);
			//printer.println("");
			printer.println("#  Default description of the class");
			printer.println("class.descr=Description of the Class");
			printer.println("");
			printer.println("#  Default description of the interface");
			printer.println("interface.descr=Description of the Interface");
			printer.println("");
			printer.println("#  Default description of the constructor  {0} stands for the name");
			printer.println("#  of the constructor");
			printer.println("constructor.descr=Constructor for the {0} object");
			printer.println("");
			printer.println("#  Default description of the method");
			printer.println("method.descr=Description of the Method");
			printer.println("");
			printer.println("#  Default description of the parameter");
			printer.println("param.descr=Description of Parameter");
			printer.println("");
			printer.println("#  Default description of the return value");
			printer.println("return.descr=Description of the Returned Value");
			printer.println("");
			printer.println("#  Default description of the exception");
			printer.println("exception.descr=Description of Exception");
			printer.println("");
		}
		if (version < 1.05) {
			printer.println("");
			printer.println("");
			printer.println("#  Default description of the getter.  {0} is the name of the");
			printer.println("#  attribute, {1} is the name of the class, {2} is 'class'");
			printer.println("#  or 'object' depending on whether it is static or not,");
			printer.println("#  {3} is the name of the attribute with the first letter lowercased");
			printer.println("getter.descr=Gets the {3} attribute of the {1} {2}");
			printer.println("");
			printer.println("#  Default description of the setter.  {0} is the name of the");
			printer.println("#  attribute, {1} is the name of the class, {2} is 'class'");
			printer.println("#  or 'object' depending on whether it is static or not,");
			printer.println("#  {3} is the name of the attribute with the first letter lowercased");
			printer.println("setter.descr=Sets the {3} attribute of the {1} {2}");
			printer.println("");
			printer.println("#  Parameter description for setters.  {0} is the name of the attribute,");
			printer.println("#  {3} is the name of the attribute with the first letter lowercased");
			printer.println("setter.param.descr=The new {3} value");
			printer.println("");
			printer.println("#  Return description for getters.  {0} is the name of the attribute,");
			printer.println("#  {3} is the name of the attribute with the first letter lowercased");
			printer.println("getter.return.descr=The {3} value");
			printer.println("");
		}
		if (version < 1.15) {
			printer.println("#  Default field description");
			printer.println("field.descr=Description of the Field");
			printer.println("");
			printer.println("#  Default description of the run method.  {0} is not");
			printer.println("#  applicable, {1} is the name of the class, {2} is 'class'");
			printer.println("#  or 'object' depending on whether it is static or not");
			printer.println("run.descr=Main processing method for the {1} {2}");
			printer.println("");
			printer.println("#  Default description of the run method.  {0} is not");
			printer.println("#  applicable, {1} is the name of the class, {2} is 'class'");
			printer.println("#  or 'object' depending on whether it is static or not");
			printer.println("main.descr=The main program for the {1} {2}");
			printer.println("");
			printer.println("#  Description of the main arguments");
			printer.println("main.param.descr=The command line arguments");
			printer.println("");
		}
		if (version < 1.25) {
			printer.println("");
			printer.println("");
			printer.println("#  Default description of the add method.  {0} is the name of the");
			printer.println("#  attribute, {1} is the name of the class, {2} is 'class'");
			printer.println("#  or 'object' depending on whether it is static or not,");
			printer.println("#  {3} is the name of the attribute with the first letter lowercased");
			printer.println("adder.descr=Adds a feature to the {0} attribute of the {1} {2}");
			printer.println("");
			printer.println("#  Description of the add argument");
			printer.println("adder.param.descr=The feature to be added to the {0} attribute");
			printer.println("");
		}
		if (version < 1.35) {
			printer.println("#  JUnit has a particular format for the names of methods.");
			printer.println("#  These setup for the unit tests are done in a method named");
			printer.println("#  setUp, the cleanup afterwards is done in tearDown, and");
			printer.println("#  the unit tests all start with the word test.  The following");
			printer.println("#  are the default descriptions of these methods.");
			printer.println("junit.setUp.descr=The JUnit setup method");
			printer.println("");
			printer.println("junit.test.descr=A unit test for JUnit");
			printer.println("");
			printer.println("junit.tearDown.descr=The teardown method for JUnit");
			printer.println("");
			printer.println("junit.suite.descr=A unit test suite for JUnit");
			printer.println("junit.suite.return.descr=The test suite");
		}
		if (version < 1.05) {
			printer.println("#");
			printer.println("#  Sort order");
			printer.println("#");
			printer.println("#  To change the relative priorities of the sort, adjust the number after");
			printer.println("#  the dot.  For instance, if you want all the instance parts first then");
			printer.println("#  static parts second, and within these you want the field, constructor etc");
			printer.println("#  to be sorted next, switch the number of sort.1 and sort.2.");
			printer.println("");
			printer.println("");
			printer.println("#  Check the type first");
			printer.println("#    This places the fields first, and initializers last.  Note that to keep");
			printer.println("#    things compiling initializers must be after the fields.");
			printer.println("sort.1=Type(Field,Constructor,Method,NestedClass,NestedInterface,Initializer)");
			printer.println("");
			printer.println("#  Check the class/instance next");
			printer.println("#    To place the static methods and variables first, switch the order");
			printer.println("#    of instance and static.");
			printer.println("sort.2=Class(Static,Instance)");
			printer.println("");
			printer.println("#  Check the protection next");
			printer.println("#    To sort with public methods/variables at the top of the file use Protection(public)");
			printer.println("#    To sort with private methods/variables at the top of the file use Protection(private)");
			printer.println("sort.3=Protection(public)");
			printer.println("");
			printer.println("#  Group setters and getters last");
			printer.println("#    Setters are methods that start with the word 'set'");
			printer.println("#    Getters are methods that start with the word 'get' or 'is'");
			printer.println("sort.4=Method(setter,getter,other)");
			printer.println("");
		}
		if (version < 1.15) {
			printer.println("");
			printer.println("");
			printer.println("#  Limits the level that javadoc comments are forced");
			printer.println("#  into the document.  The following are valid");
			printer.println("#  levels:");
			printer.println("#  *  all - all items must have javadoc");
			printer.println("#  *  private - same as all");
			printer.println("#  *  package - all items except private items must have javadoc");
			printer.println("#  *  default - same as package");
			printer.println("#  *  protected - protected and public items must have javadoc");
			printer.println("#  *  public - only public items must have javadoc");
			printer.println("#  *  none - nothing is required to have javadoc");
			printer.println("#");
			printer.println("#  method.minimum applies to constructors and methods");
			printer.println("method.minimum=all");
			printer.println("");
			printer.println("#  field.minimum applies to fields");
			printer.println("field.minimum=protected");
			printer.println("");
		}
		if (version < 1.35) {
			printer.println("#  class.minimum applies to classes and interfaces");
			printer.println("class.minimum=all");
		}
		if (version < 1.15) {
			printer.println("");
			printer.println("#  Is the date a required field of the class or interface");
			printer.println("date.required=true");
		}
		if (version < 1.45) {
			printer.println("");
			printer.println("#  Is there a space after the cast");
			printer.println("cast.space=true");
			printer.println("");
			printer.println("#  Star count for javadoc");
			printer.println("javadoc.star=2");
			printer.println("");
			printer.println("#  Wordwrap length for javadoc.  You must have at least");
			printer.println("#  javadoc.wordwrap.min characters in the comment and you");
			printer.println("#  must be passing javadoc.wordwrapp.max for the indenting");
			printer.println("#  plus the comment");
			printer.println("javadoc.wordwrap.max=80");
			printer.println("javadoc.wordwrap.min=40");
		}

		if (version < 1.55) {
			printer.println("");
			printer.println("#");
			printer.println("#  Header:");
			printer.println("#  Uncomment these lines if you would like");
			printer.println("#  a standard header at the beginning of each file.");
			printer.println("#  You are allowed an unlimited number of lines here,");
			printer.println("#  just number them sequentially.");
			printer.println("#");
			printer.println("#header.1=/*");
			printer.println("#header.2= *  Copyright 2000");
			printer.println("#header.3= *  ");
			printer.println("#header.4= *  <Your Organization Here>");
			printer.println("#header.5= *  All rights reserved");
			printer.println("#header.6= */");
		}

		if (version < 1.65) {
			printer.println("#  The following allow you to require and order");
			printer.println("#  tags for the classes, methods, and fields.  To");
			printer.println("#  require the tag, add the name of the tag here");
			printer.println("#  and then add a TAGNAME.descr field.  To only ");
			printer.println("#  specify the order, just include the tag here.");
			printer.println("");
			printer.println("#  Here is the order for tags for classes and interfaces");
			boolean required = true;

			try {
				if (bundle != null) {
					required = bundle.getBoolean("date.required");
				}
			}
			catch (MissingSettingsException mse) {
				//  Not really a problem
			}

			if (required) {
				printer.println("class.tags=author,created");
			}
			else {
				printer.println("class.tags=author");
			}
			printer.println("");
			printer.println("#  Here is the order for tags for methods and constructors");
			printer.println("method.tags=param,return,exception,since");
			printer.println("");
			printer.println("#  Here is the order for tags for fields");
			printer.println("field.tags=since");
			printer.println("");
			printer.println("#  In all tags that are required, there are some parameters");
			printer.println("#  that are available.  These are:");
			printer.println("#  {0} refers to the current user");
			printer.println("#  {1} refers to the current date");
			printer.println("#  {2} refers to the name of the current object");
			printer.println("");
			printer.println("#  Now we are ready to specify the author");

			try {
				printer.println("author.descr=" + bundle.getString("author"));
			}
			catch (MissingSettingsException mse) {
				printer.println("author.descr={0}");
			}
			catch (NullPointerException npe) {
				printer.println("author.descr={0}");
			}
			printer.println("");
			printer.println("#  Now we are ready to specify the created tag");
			printer.println("created.descr={1}");
			printer.println("");
		}

		if (version < 1.75) {
			printer.println("#  Whether we put a space before the @");
			printer.println("space.before.javadoc=false");
			printer.println("");
			printer.println("#  Should we sort the types and imports?");
			printer.println("sort.top=false");
			printer.println("");
		}
		if (version < 1.95) {
			printer.println("#  When the catch.start.line setting is true, catch statements look like");
			printer.println("#  try {");
			printer.println("#      //  Something here");
			printer.println("#  }");
			printer.println("#  catch (IOException ioe) {");
			printer.println("#      //  Something here");
			printer.println("#  }");
			printer.println("#  When the catch.start.line setting is falserue, catch statements look like");
			printer.println("#  try {");
			printer.println("#      //  Something here");
			printer.println("#  } catch (IOException ioe) {");
			printer.println("#      //  Something here");
			printer.println("#  }");
			printer.println("#");
			printer.println("catch.start.line=false");
			printer.println("");
		}

		if (version < 2.05) {
			printer.println("#  This determines if there should be a space after keywords");
			printer.println("#  such as if, while, or for.  When this value is true, you get:");
			printer.println("#  if (true) {");
			printer.println("#      //  Do something");
			printer.println("#  }");
			printer.println("#  When this value is false, you get:");
			printer.println("#  if(true) {");
			printer.println("#      //  Do something");
			printer.println("#  }");
			printer.println("keyword.space=true");
			printer.println("");
		}

		if (version < 2.15) {
			printer.println("");
			printer.println("#");
			printer.println("#  Do you want to lineup the names and descriptions");
			printer.println("#  in javadoc comments?");
			printer.println("#");
			printer.println("javadoc.id.lineup=true");
			printer.println("");
			printer.println("#");
			printer.println("#  How many spaces should javadoc comments be indented?");
			printer.println("#");
			printer.println("javadoc.indent=2");
			printer.println("");
			printer.println("#");
			printer.println("#  What do you do when a newline is unexpectedly encountered?");
			printer.println("#  The valid values are double and param.  Double means that");
			printer.println("#  you should indent twice.  Param means try to line up the");
			printer.println("#  the parameters.");
			printer.println("#");
			printer.println("surprise.return=double");
			printer.println("");
			printer.println("#");
			printer.println("#  To handle sun's coding standard, you want the method to begin");
			printer.println("#  with a PASCAL coding style and the {} beneath that to be C style.");
			printer.println("#  This parameter allows you to set the method style different");
			printer.println("#  from the rest.");
			printer.println("#");
			printer.println("method.block.style=C");
			printer.println("");
			printer.println("#");
			printer.println("#  Should throws part of a method/constructor declaration always be");
			printer.println("#  on its own line?");
			printer.println("#");
			printer.println("throws.newline=false");
			printer.println("");

			printer.flush();
		}

		if (version < 2.25) {
			printer.println("");
			printer.println("#");
			printer.println("#  Wordwrap the javadoc comments");
			printer.println("#");
			printer.println("reformat.comments=true");
			printer.println("");
			printer.println("#");
			printer.println("#  Single line comment type");
			printer.println("#");
			printer.println("");
			printer.println("#");
			printer.println("#  Should each single line comment be indented a certain number of spaces");
			printer.println("#  from the margin?  For this to work right be sure to indent each line with");
			printer.println("#  spaces.");
			printer.println("#");
			printer.println("singleline.comment.ownline=true");
			printer.println("");
		}

		if (version < 2.35) {
			printer.println("");
			printer.println("#");
			printer.println("#  Indent the name of the field (instance variable or class");
			printer.println("#  variable) to this column (-1 for just one space)");
			printer.println("#");
			printer.println("field.name.indent=-1");
			printer.println("");
		}

		if (version < 2.45) {
			printer.println("");
			printer.println("#");
			printer.println("#  Include javadoc comments where ever they appear.  Javadoc comments");
			printer.println("#  were originally only allowed to occur at a few places:  immediately");
			printer.println("#  before a method, immediately before a field, and immediately");
			printer.println("#  before a class or interface.  Since it is also common for people");
			printer.println("#  to include the /*** pattern at the beginning of a file, this will be");
			printer.println("#  preserved as well.");
			printer.println("#");
			printer.println("#  This was the case until JBuilder pressed the javadoc style comment into");
			printer.println("#  a new line of work - handling @todo tags.  Suddenly it was permissible");
			printer.println("#  to include javadoc comments anywhere in the file.");
			printer.println("#");
			printer.println("#  With keep.all.javadoc set to false, you get the original behavior.  All");
			printer.println("#  javadoc comments that were not in the correct place were cleaned up for");
			printer.println("#  you.  With this set to true, you can place the @todo tags wherever you please.");
			printer.println("#");
			printer.println("keep.all.javadoc=false");
			printer.println("");
		}

		if (version < 2.55) {
			printer.println("");
			printer.println("#");
			printer.println("#  End of line character(s) - either CR, CRNL, or NL");
			printer.println("#  CR means carriage return, NL means newline");
			printer.println("#");
			if (System.getProperty("os.name").toLowerCase().startsWith("win")) {
				printer.println("end.line=CRNL");
			}
			else {
				printer.println("end.line=NL");
			}
			printer.println("");

			printer.println("#");
			printer.println("#  Absolute indent before a single line comment.");
			printer.println("#");
			printer.println("singleline.comment.absoluteindent=0");
			printer.println("");
			printer.println("#");
			printer.println("#  Space used before the start of a single line ");
			printer.println("#  from the end of the code.  This value is used");
			printer.println("#  to determine the number of spaces and how these");
			printer.println("#  spaces are used based on the next few settings.");
			printer.println("#");
			printer.println("singleline.comment.incrementalindent=0");
			printer.println("");

			printer.flush();
		}

		if (version < 2.75) {
			printer.println("#");
			printer.println("#  This feature describes how the pretty printer should ");
			printer.println("#  indent single line comments (//) that share the line");
			printer.println("#  with source code.  The two choices are incremental and absolute.");
			printer.println("#    incremental  -  use an incremental indent");
			printer.println("#    absolute  -  use the absolute indent level");
			printer.println("#");
			printer.println("singleline.comment.indentstyle.shared=incremental");
			printer.println("");
			printer.println("#");
			printer.println("#  This feature describes how the pretty printer should");
			printer.println("#  indent single line comments (//) that are on their");
			printer.println("#  own line.  The two choices are code and absolute.");
			printer.println("#    code  -  use the same indent as the current code");
			printer.println("#    absolute  -  use the absolute indent level");
			printer.println("#");
			printer.println("singleline.comment.indentstyle.ownline=code");
			printer.println("");
		}

		if (version < 2.85) {
			printer.println("#");
			printer.println("#  This feature describes what type of characters are used for");
			printer.println("#  the java files.");
			printer.println("#    1 - ASCII (1 byte characters)");
			printer.println("#    2 - Unicode (2 byte characters - far east)");
			printer.println("#    3 - ASCII full (2 byte characters - far east)");
			printer.println("#");
			printer.println("char.stream.type=1");
			printer.println("");
		}

		if (version < 2.95) {
			printer.println("#");
			printer.println("#  This features sprecifies how to space out a field or a local");
			printer.println("#  variable declaration.");
			printer.println("#    single - a space between the modifiers, the type, the name ");
			printer.println("#        and the initializer");
			printer.println("#    dynamic - determine the spacing between the modifiers, type, ");
			printer.println("#        name, and initializers so everything lines up");
			printer.println("#    javadoc.dynamic - determine the spacing between the modifiers, type, ");
			printer.println("#        name, and initializers so everything lines up, except when the");
			printer.println("#        field is prefixed by a javadoc comment");
			printer.println("#    align.equals - align the equals statements of field declaration, ");
			printer.println("#        but nothing else");
			printer.println("#");
			printer.println("variable.spacing=single");
			printer.println("");
		}
		if (version < 3.05) {
			printer.println("#");
			printer.println("#  When a dynamic field spacing is used, this value specifies");
			printer.println("#  the number of additional spaces to add between the modifiers, ");
			printer.println("#  type, name, and initializer.");
			printer.println("#");
			printer.println("dynamic.variable.spacing=1");
			printer.println("");
		}

		if (version < 3.15) {
			printer.println("#");
			printer.println("#  How to format C Style comments.  Valid values are:");
			printer.println("#    leave - leave alone");
			printer.println("#    maintain.space.star - there is a row of stars to the right, but we maintain the spaces after it");
			printer.println("#    align.star - place a row of stars to the right and align on those");
			printer.println("#    align.blank - just align the comments to the right (no star)");
			printer.println("#");
			printer.println("c.style.format=align.star");
			printer.println("");
			printer.println("#");
			printer.println("#  For one of the methods above that use the align type, this is");
			printer.println("#  the number of spaces to include after the * or blank");
			printer.println("#");
			printer.println("c.style.indent=2");
			printer.println("");
			printer.println("#  Should if/then/else statements look like");
			printer.println("#  (true) is:");
			printer.println("#  if (someTest()) {");
			printer.println("#      //  Something here");
			printer.println("#  }");
			printer.println("#  else {");
			printer.println("#      //  Something here");
			printer.println("#  }");
			printer.println("#  (false) is:");
			printer.println("#  if (someTest()) {");
			printer.println("#      //  Something here");
			printer.println("#  } else {");
			printer.println("#      //  Something here");
			printer.println("#  }");
			printer.println("#  ");
			String elseLine = "false";
			try {
				if (bundle != null) {
					elseLine = bundle.getString("catch.start.line");
				}
			}
			catch (MissingSettingsException mse) {
				//  Not really a problem
			}
			printer.println("else.start.line=" + elseLine);
			printer.println("");
			printer.println("#");
			printer.println("#  Do we force if and while and for statements to have a block?  { ... }");
			printer.println("#");
			printer.println("force.block=true");
			printer.println("");
			printer.println("");
			printer.println("#");
			printer.println("#  To handle sun's coding standard, you want the class to begin");
			printer.println("#  with a PASCAL coding style and the {} beneath that to be C style.");
			printer.println("#  This parameter allows you to set the class style different");
			printer.println("#  from the rest.");
			printer.println("#");
			String blockStyle = "C";
			try {
				if (bundle != null) {
					blockStyle = bundle.getString("block.style");
				}
			}
			catch (MissingSettingsException mse) {
				//  Not really a problem
			}
			printer.println("class.block.style=" + blockStyle);
			printer.println("");
		}

		if (version < 3.25) {
			printer.println("#");
			printer.println("#  Empty methods and constructors remain on a single line");
			printer.println("#");
			printer.println("empty.block.single.line=true");
			printer.println("");
		}

		if (version < 3.35) {
			printer.println("#");
			printer.println("#  Do we force a space after a cast?");
			printer.println("#");
			printer.println("cast.force.nospace=false");
			printer.println("");
		}

		if (version < 3.45) {
			printer.println("#");
			printer.println("#  What tag name should be used for exceptions");
			printer.println("#");
			printer.println("exception.tag.name=@exception");
			printer.println("");
		}

		if (version < 3.55) {
			printer.println("#");
			printer.println("#  Should inner classes be documented");
			printer.println("#");
			printer.println("document.nested.classes=true");
			printer.println("");
			printer.println("#");
			printer.println("#  Should the document have a footer.  Include it here.");
			printer.println("#  To include more lines, just add more values");
			printer.println("#");
			printer.println("#footer.1=");
			printer.println("#footer.2=//  This is the end of the file");
			printer.println("#footer.3=");
			printer.println("");
		}
		if (version < 3.55) {
			printer.println("#");
			printer.println("#  Are javadoc comments allowed to be a single line long");
			printer.println("#");
			printer.println("allow.singleline.javadoc=false");
			printer.println("");
		}
		if (version < 3.65) {
			printer.println("#");
			printer.println("#  Should the local variables be aligned with the { and }");
			printer.println("#  or should they be indented to align with the other code?");
			printer.println("#  false means align with the code, true means align");
			printer.println("#  with the { }");
			printer.println("#");
			printer.println("variable.align.with.block=false");
			printer.println("");
		}
	}


	/**
	 *  Installs properties for visual source safe
	 *
	 *@param  printer  the printer
	 */
	private void vssSettings(PrintWriter printer)
	{
		printer.println("#  This is the full path the visual source safe's executable ");
		printer.println("vss=c:\\\\program files\\\\microsoft visual studio\\\\win32\\\\ss.exe");
		printer.println(" ");
		printer.println("#  The following are the extensions of files which are");
		printer.println("#  stored in visual source safe");
		printer.println("extension.1=.java");
		printer.println("extension.2=.properties");
		printer.println("extension.3=.xml");
		printer.println("extension.4=.html");
		printer.println("extension.5=.htm");
		printer.println(" ");
		printer.println("#  The following shows how the projects in Visual Source");
		printer.println("#  Safe map to directories on the hard disk");
		printer.println("source.1=c:\\\\java\\\\src");
		printer.println("project.1=$/Source");
		printer.println(" ");
		printer.println("source.2=c:\\\\java\\\\properties");
		printer.println("project.2=$/Properties");
		printer.println(" ");
		printer.println("source.3=c:\\\\public_html");
		printer.println("project.3=$/HTML");
		printer.println(" ");
		printer.println("source.4=c:\\\\public_html\\\\xml");
		printer.println("project.4=$/XML");
	}


	/**
	 *  Installs properties for process tracking
	 *
	 *@param  printer  the printer
	 */
	private void processSettings(PrintWriter printer)
	{
		printer.println("#  The following settings are used to set");
		printer.println("#  up the process panel.");
		printer.println("#");
		printer.println("#  The button.name is the value that appears on the button");
		printer.println("#  The button.cmd is the value that is saved to the process");
		printer.println("#      tracking file");
		printer.println("#");
		printer.println("#  The system loads all properties starting with index 0");
		printer.println("#  and continuing until one or both of the next pair of");
		printer.println("#  values is missing.");
		printer.println(" ");
		printer.println("button.name.0=Design");
		printer.println("button.cmd.0=Design");
		printer.println(" ");
		printer.println("button.name.1=Coding");
		printer.println("button.cmd.1=Coding");
		printer.println(" ");
		printer.println("button.name.2=Unit Testing");
		printer.println("button.cmd.2=Unit Testing");
		printer.println(" ");
		printer.println("button.name.3=Verification");
		printer.println("button.cmd.3=Verification");
		printer.println(" ");
		printer.println("button.name.4=Meeting");
		printer.println("button.cmd.4=Meeting");
		printer.println(" ");
		printer.println("button.name.5=Interrupt");
		printer.println("button.cmd.5=Interrupt");
		printer.println(" ");
		printer.println("#  The name of the file to store the process data in");
		printer.println("process.file=c:\\tools\\process.txt");
	}


	/**
	 *  Installs properties for process tracking
	 *
	 *@param  printer  the printer
	 *@param  version  Description of Parameter
	 *@param  dir      Description of Parameter
	 */
	private void umlSettings(PrintWriter printer, double version, String dir)
	{
		if (version < (UML_CURRENT_VERSION - 0.05)) {
			printer.println("");
			printer.println("#  UML File Version");
			printer.println("version=" + UML_CURRENT_VERSION);
			printer.println("");
		}

		if (version < 0.95) {
			printer.println("#  The following settings are used to set");
			printer.println("#  up the uml diagrams.");
			printer.println("");
			printer.println("#");
			printer.println("#  The directory containing the stub files");
			printer.println("#");
			printer.println("stub.dir=" + doubleBackslashes(dir));
			printer.println("");
			printer.println("#");
			printer.println("#  Size of the box where a segmented line changes direction");
			printer.println("#");
			printer.println("sticky.point.size=3");
			printer.println("");
			printer.println("#");
			printer.println("#  Size of the area where you must be to select the sticky point");
			printer.println("#");
			printer.println("halo.size=6");
			printer.println("");
			printer.println("#");
			printer.println("#  The type of icon for the UML diagram.  The valid types are:");
			printer.println("#    colored circle - the original for specifying scope");
			printer.println("#    letter - a letter + for public, # for protected, etc");
			printer.println("#");
			printer.println("icon.type=colored circle");
			printer.println("");
		}

		if (version < 1.05) {
			printer.println("#");
			printer.println("#  A pattern to cause the loading to skip");
			printer.println("#  a particular directory.  For instance,");
			printer.println("#  .cvs means that JRefactory will skip loading");
			printer.println("#  any directory that matches *.cvs*.  Additional");
			printer.println("#  patterns can be separated by the path separator");
			printer.println("#  character");
			printer.println("#");
			printer.println("skip.dir=");
			printer.println("");
			printer.println("#  The extension to add to the existing file when it is");
			printer.println("#  refactored.  The # represents the number of the copy");
			printer.println("#  of the file");
			printer.println("#");
			printer.println("backup.ext=.#");
			printer.println("");
		}

		if (version < 1.15) {
			printer.println("#");
			printer.println("#  This is used by the command line version");
			printer.println("#  of the program to launch a source code editor");
			printer.println("#  The command line program can get either 1 or ");
			printer.println("#  2 arguments.  These are:");
			printer.println("#     $FILE - the path to the file for the editor");
			printer.println("#     $LINE - the line number");
			printer.println("#  If your editor cannot accept the line number");
			printer.println("#  command line, leave out that variable");
			printer.println("#");
			printer.println("#source.editor=notepad $FILE");
			printer.println("#source.editor=gnuclientw -F +$LINE $FILE");
		}
	}


	/**
	 *  Description of the Method
	 */
	private void jsdkStubInstall()
	{
		FileSettings bundle = FileSettings.getSettings("Refactory", "pretty");
		bundle.setContinuallyReload(true);

		String home;
		try {
			FileSettings umlBundle = FileSettings.getSettings("Refactory", "uml");
			home = umlBundle.getString("stub.dir");
		}
		catch (MissingSettingsException mse) {
			home = System.getProperty("user.home");
		}

		File directory = new File(home + File.separator + ".Refactory");
		if (!directory.exists()) {
			directory.mkdirs();
		}

		File outFile = new File(directory, "JDK.stub");
		if (!outFile.exists()) {
			//System.out.println("Creating:  " + outFile.getPath());
			(new StubPrompter(null, outFile)).setVisible(true);
		}

		bundle.setContinuallyReload(false);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  value  Description of Parameter
	 *@return        Description of the Returned Value
	 */
	private String doubleBackslashes(String value)
	{
		StringBuffer buffer = new StringBuffer();
		int last = value.length();
		for (int ndx = 0; ndx < last; ndx++) {
			char ch = value.charAt(ndx);
			if (ch == '\\') {
				buffer.append("\\\\");
			}
			else {
				buffer.append(ch);
			}
		}
		return buffer.toString();
	}


	/**
	 *  Creates a file that marks when the refactory was installed
	 *
	 *@param  file             The file to create
	 *@exception  IOException  Description of Exception
	 */
	private void generateCreationText(File file) throws IOException
	{
		FileWriter output = new FileWriter(file);
		PrintWriter printer = new PrintWriter(output);
		printer.println("Created on " + DateFormat.getDateTimeInstance().format(new Date()));
		printer.close();
		output.close();
	}


	/**
	 *  The main program
	 *
	 *@param  args  command line arguments
	 */
	public static void main(String[] args)
	{
		(new RefactoryInstaller(false)).run();
	}
}
