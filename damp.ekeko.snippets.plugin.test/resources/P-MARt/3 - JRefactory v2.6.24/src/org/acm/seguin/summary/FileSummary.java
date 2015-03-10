/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.summary;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;

import org.acm.seguin.parser.ast.ASTTypeDeclaration;
import org.acm.seguin.parser.ast.SimpleNode;
import org.acm.seguin.parser.factory.BufferParserFactory;
import org.acm.seguin.parser.factory.FileParserFactory;
import org.acm.seguin.parser.factory.InputStreamParserFactory;
import org.acm.seguin.parser.factory.ParserFactory;

/**
 *  Stores a summary of a java file
 *
 *@author     Chris Seguin
 *@created    June 6, 1999
 */
public class FileSummary extends Summary {
	//  Instance Variables
	private File theFile;
	private ArrayList importList;
	private LinkedList typeList;
	private boolean isMoving;
	private boolean delete;
	private Date lastModified;

	//  Class Variables
	private static HashMap fileMap;


	/**
	 *  Creates a file map
	 *
	 *@param  parentSummary  the parent summary
	 *@param  initFile       the file
	 */
	protected FileSummary(Summary parentSummary, File initFile)
	{
		//  Initialize parent class
		super(parentSummary);

		//  Set instance Variables
		theFile = initFile;
		importList = null;
		typeList = null;
		isMoving = false;
		delete = false;
		lastModified = new Date();
	}


	/**
	 *  Change whether this file is moving or not
	 *
	 *@param  way  the way that this parameter is changing
	 */
	public void setMoving(boolean way)
	{
		isMoving = way;
	}


	/**
	 *  Mark whether this file should be deleted
	 *
	 *@param  way  the way that this parameter is changing
	 */
	public void setDeleted(boolean way)
	{
		delete = way;
	}


	/**
	 *  Return the name of the file
	 *
	 *@return    a string containing the name
	 */
	public String getName()
	{
		if (theFile == null) {
			return "";
		}

		return theFile.getName();
	}


	/**
	 *  Return the file
	 *
	 *@return    the file object
	 */
	public File getFile()
	{
		return theFile;
	}


	/**
	 *  Return the list of imports
	 *
	 *@return    an iterator containing the imports
	 */
	public Iterator getImports()
	{
		if (importList == null) {
			return null;
		}

		return importList.iterator();
	}


	/**
	 *  Counts the types stored in the file
	 *
	 *@return    the number of types in this file
	 */
	public int getTypeCount()
	{
		if (typeList == null) {
			return 0;
		}

		return typeList.size();
	}


	/**
	 *  Get the list of types stored in this file
	 *
	 *@return    an iterator over the types
	 */
	public Iterator getTypes()
	{
		if (typeList == null) {
			return null;
		}

		return typeList.iterator();
	}


	/**
	 *  Is this file moving to a new package
	 *
	 *@return    true if the file is moving
	 */
	public boolean isMoving()
	{
		return isMoving;
	}


	/**
	 *  Has this file been deleted
	 *
	 *@return    true if the file is deleted
	 */
	public boolean isDeleted()
	{
		return delete;
	}


	/**
	 *  Provide method to visit a node
	 *
	 *@param  visitor  the visitor
	 *@param  data     the data for the visit
	 *@return          some new data
	 */
	public Object accept(SummaryVisitor visitor, Object data)
	{
		return visitor.visit(this, data);
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	public String toString()
	{
		if (theFile == null) {
			return "FileSummary<Framework File>";
		}
		return "FileSummary<" + theFile.getName() + ">";
	}


	/**
	 *  Add an import summary
	 *
	 *@param  importSummary  the summary of what was imported
	 */
	protected void add(ImportSummary importSummary)
	{
		if (importSummary != null) {
			//  Initialize the import list
			if (importList == null) {
				initImportList();
			}

			//  Add it in
			importList.add(importSummary);
		}
	}


	/**
	 *  Add an type summary
	 *
	 *@param  typeSummary  the summary of the type
	 */
	protected void add(TypeSummary typeSummary)
	{
		if (typeSummary != null) {
			//  Initialize the type list
			if (typeList == null) {
				initTypeList();
			}

			//  Add it to the list
			typeList.add(typeSummary);
		}
	}


	/**
	 *  Initialize the type list
	 */
	private void initTypeList()
	{
		typeList = new LinkedList();
	}


	/**
	 *  Initialize the import list
	 */
	private void initImportList()
	{
		importList = new ArrayList();
	}


	/**
	 *  Get the file summary for a particular file
	 *
	 *@param  file  the file we are looking up
	 *@return       the file summary
	 */
	public static FileSummary getFileSummary(File file)
	{
		if (fileMap == null) {
			init();
		}

		FileSummary result = (FileSummary) fileMap.get(getKey(file));
		if (result == null) {
			SummaryLoaderState state = loadNewFileSummary(file);
			result = linkFileSummary(state, file);
		}
		else {
			Date currentModificationTime = new Date(file.lastModified());
			if (currentModificationTime.after(result.lastModified)) {
				resetFileSummary(file, result);
				result.lastModified = new Date(file.lastModified());

				//  Create a new file summary object
				ParserFactory factory = new FileParserFactory(file);
				SimpleNode root = factory.getAbstractSyntaxTree(false);
				if (root == null) {
					return null;
				}
				reloadFileSummary(file, result, root);
			}
		}

		return result;
	}


	/**
	 *  Get the file summary for a particular file
	 *
	 *@param  buffer  the buffer that is used to load the summary
	 *@return         the file summary
	 */
	public static FileSummary getFileSummary(String buffer)
	{
		//  Create a new file summary object
		ParserFactory factory = new BufferParserFactory(buffer);
		SimpleNode root = factory.getAbstractSyntaxTree(false);
		if ((root == null) || (!hasType(root))) {
			return null;
		}

		//  Start the summary
		SummaryLoaderState state = new SummaryLoaderState();
		state.setFile(null);
		root.jjtAccept(new SummaryLoadVisitor(), state);

		//  Associate them together
		FileSummary result = (FileSummary) state.getCurrentSummary();
		((PackageSummary) result.getParent()).addFileSummary(result);

		return result;
	}


	/**
	 *  Remove any file summaries that have been deleted
	 */
	public static void removeDeletedSummaries()
	{
		if (fileMap == null) {
			init();
			return;
		}

		LinkedList temp = new LinkedList();
		Iterator keys = fileMap.values().iterator();
		while (keys.hasNext()) {
			FileSummary next = (FileSummary) keys.next();
			File file = next.getFile();
			if ((file != null) && !file.exists()) {
				temp.add(file);
			}
		}

		Iterator iter = temp.iterator();
		while (iter.hasNext()) {
			File next = (File) iter.next();
			removeFileSummary(next);
		}
	}


	/**
	 *  Remove the file summary for a particular file
	 *
	 *@param  file  the file we are looking up
	 */
	public static void removeFileSummary(File file)
	{
		if (fileMap == null) {
			init();
		}

		String key = getKey(file);
		FileSummary fileSummary = (FileSummary) fileMap.get(key);
		if (fileSummary != null) {
			//  There is something to remove
			fileMap.remove(key);

			//  Get the parent
			PackageSummary parent = (PackageSummary) fileSummary.getParent();
			parent.deleteFileSummary(fileSummary);
		}
	}


	/**
	 *  Get the key that is used to index the files
	 *
	 *@param  file  the file we are using to find the key
	 *@return       the key
	 */
	protected static String getKey(File file)
	{
		try {
			return file.getCanonicalPath();
		}
		catch (IOException ioe) {
			return "Unknown";
		}
	}


	/**
	 *  Registers a single new file. This method is used by the rapid metadata
	 *  reloader
	 *
	 *@param  summary  Description of Parameter
	 */
	static void register(FileSummary summary)
	{
		if (fileMap == null) {
			init();
		}

		File file = summary.getFile();
		if (file == null) {
			return;
		}

		fileMap.put(getKey(file), summary);
	}


	/**
	 *  Scans through the tree looking for a type declaration
	 *
	 *@param  root  the root of the abstract syntax tree
	 *@return       true if there is a type node present
	 */
	private static boolean hasType(SimpleNode root)
	{
		int last = root.jjtGetNumChildren();
		for (int ndx = 0; ndx < last; ndx++) {
			if (root.jjtGetChild(ndx) instanceof ASTTypeDeclaration) {
				return true;
			}
		}
		return false;
	}


	/**
	 *  Initialization method
	 */
	private static void init()
	{
		if (fileMap == null) {
			fileMap = new HashMap();
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@param  file  Description of Parameter
	 *@return       Description of the Returned Value
	 */
	private static SummaryLoaderState loadNewFileSummary(File file)
	{
		//  Create a new file summary object
		ParserFactory factory = new FileParserFactory(file);
		SimpleNode root = factory.getAbstractSyntaxTree(false);
		if (root == null) {
			return null;
		}

		//  Start the summary
		SummaryLoaderState state = new SummaryLoaderState();
		state.setFile(file);
		root.jjtAccept(new SummaryLoadVisitor(), state);
		return state;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  state  Description of Parameter
	 *@param  file   Description of Parameter
	 *@return        Description of the Returned Value
	 */
	private static FileSummary linkFileSummary(SummaryLoaderState state, File file)
	{
		FileSummary result;

		//  Associate them together
		result = (FileSummary) state.getCurrentSummary();
		((PackageSummary) result.getParent()).addFileSummary(result);

		//  Store it away
		fileMap.put(getKey(file), result);

		return result;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  file    Description of Parameter
	 *@param  result  Description of Parameter
	 *@param  root    Description of Parameter
	 */
	private static void reloadFileSummary(File file, FileSummary result, SimpleNode root)
	{
		SummaryLoaderState state = new SummaryLoaderState();
		state.setFile(file);
		state.startSummary(result);
		state.setCode(SummaryLoaderState.LOAD_FILE);
		root.jjtAccept(new SummaryLoadVisitor(), state);
	}


	/**
	 *  Description of the Method
	 *
	 *@param  file  Description of Parameter
	 */
	private static void resetFileSummary(File file, FileSummary result)
	{
		if (result == null) {
			return;
		}

		result.theFile = file;
		result.importList = null;
		result.typeList = null;
		result.isMoving = false;
		result.delete = false;
	}

	/**
	 *  This method allows JBuilder to load a file summary from
	 *  the buffer
	 *
	 *@param file the file
	 *@param input the input stream
	 *@return the file summary loaded
	 */
	public static FileSummary reloadFromBuffer(File file, InputStream input) {
		if (fileMap == null) {
			init();
		}

		if (file == null) {
			System.out.println("No file!");
			return null;
		}

		String key = getKey(file);
		if (key == null) {
			System.out.println("No key:  " + file.toString());
			return null;
		}

		FileSummary result = (FileSummary) fileMap.get(key);

		if (result == null) {
			System.out.println("No initial file summary");
			SummaryLoaderState state = loadNewFileSummary(file);
			result = linkFileSummary(state, file);

			//  If you still can't get something that makes sense abort
			if (result == null) {
				System.out.println("Unable to load the file summary from the file");
				return null;
			}
		}

		resetFileSummary(file, result);
		result.lastModified = new Date(file.lastModified());

		//  Create a new file summary object
		ParserFactory factory;
		if (input == null) {
			factory = new FileParserFactory(file);
		}
		else {
			factory = new InputStreamParserFactory(input, key);
		}
		SimpleNode root = factory.getAbstractSyntaxTree(false);
		if (root == null) {
			System.out.println("Unable to get a parse tree for this file:  Using existing file summary");
			return result;
		}
		reloadFileSummary(file, result, root);

		return result;
	}
}
