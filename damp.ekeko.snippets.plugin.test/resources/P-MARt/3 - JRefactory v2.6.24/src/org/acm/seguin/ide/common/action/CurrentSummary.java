/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.common.action;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.acm.seguin.ide.common.EditorOperations;
import org.acm.seguin.ide.common.MultipleDirClassDiagramReloader;
import org.acm.seguin.summary.FieldSummary;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.MethodSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.summary.TypeSummary;

/**
 *  Determines what the current summary is based on the information from the
 *  IDE.
 *
 *@author    Chris Seguin
 */
public abstract class CurrentSummary extends Object implements DocumentListener {
	/**
	 *  Has this file changed since the last time this was invoked
	 */
	protected boolean upToDate;
	private Summary summary;
	private FileSummary fileSummary;

	private static CurrentSummary singleton = null;


	/**
	 *  Constructor for the CurrentSummary object
	 */
	protected CurrentSummary()
	{
		summary = null;
		fileSummary = null;
		upToDate = false;
	}


	/**
	 *  Gets the CurrentSummary attribute of the CurrentSummary object
	 *
	 *@return    The CurrentSummary value
	 */
	public Summary getCurrentSummary()
	{
		if ((summary == null) || upToDate || !isSameFile() || !isInSameSummary()) {
			lockAccess();
		}

		System.out.println("Summary is:  " + summary + " from " + getLineNumber());
		return summary;
	}


	/**
	 *  Method that receives notification when the editor changes
	 *
	 *@param  evt  Description of Parameter
	 */
	public void insertUpdate(DocumentEvent evt)
	{
		upToDate = false;
	}


	/**
	 *  Method that receives notification when the editor changes
	 *
	 *@param  evt  Description of Parameter
	 */
	public void removeUpdate(DocumentEvent evt)
	{
		upToDate = false;
	}


	/**
	 *  Method that receives notification when the editor changes
	 *
	 *@param  evt  Description of Parameter
	 */
	public void changedUpdate(DocumentEvent evt)
	{
		upToDate = false;
	}


	/**
	 *  Description of the Method
	 */
	public void reset()
	{
		upToDate = false;
	}


	/**
	 *  Reloads all the metadata before attempting to perform a refactoring.
	 */
	public void updateMetaData()
	{
		MultipleDirClassDiagramReloader reloader = getMetadataReloader();

		reloader.setNecessary(true);
		reloader.reload();
	}


	/**
	 *  Returns the initial line number
	 *
	 *@return    The LineNumber value
	 */
	protected int getLineNumber()
	{
		return EditorOperations.get().getLineNumber();
	}


	/**
	 *  Gets the ActiveFile attribute of the CurrentSummary object
	 *
	 *@return    The ActiveFile value
	 */
	protected File getActiveFile()
	{
		return EditorOperations.get().getFile();
	}


	/**
	 *  Gets the reloader
	 *
	 *@return    The MetadataReloader value
	 */
	protected abstract MultipleDirClassDiagramReloader getMetadataReloader();


	/**
	 *  Register with the current document
	 */
	protected abstract void registerWithCurrentDocument();


	/**
	 *  Gets the SameNode attribute of the CurrentSummary object
	 *
	 *@return    The SameNode value
	 */
	private boolean isSameFile()
	{
		if (fileSummary == null) {
			return false;
		}

		boolean result = (fileSummary.getFile() == getActiveFile());
		//System.out.println("Node is the same:  " + result);
		return result;
	}


	/**
	 *  Gets the InType attribute of the CurrentSummary object
	 *
	 *@param  fileSummary  Description of Parameter
	 *@param  lineNumber   Description of Parameter
	 *@return              The InType value
	 */
	private Summary getInType(FileSummary fileSummary, int lineNumber)
	{
		Iterator iter = fileSummary.getTypes();
		if (iter == null) {
			return null;
		}

		//System.out.println("Searching for:  " + lineNumber);
		while (iter.hasNext()) {
			TypeSummary next = (TypeSummary) iter.next();
			//System.out.println("Type:  " + next.toString() + " " + next.getStartLine() + ", " + next.getEndLine());
			if ((next.getStartLine() <= lineNumber) && (next.getEndLine() >= lineNumber)) {
				return findSummaryInType(next, lineNumber);
			}
		}

		return null;
	}


	/**
	 *  Gets the InMethod attribute of the CurrentSummary object
	 *
	 *@param  typeSummary  Description of Parameter
	 *@param  lineNumber   Description of Parameter
	 *@return              The InMethod value
	 */
	private Summary isInMethod(TypeSummary typeSummary, int lineNumber)
	{
		Iterator iter = typeSummary.getMethods();
		if (iter == null) {
			return null;
		}

		while (iter.hasNext()) {
			MethodSummary next = (MethodSummary) iter.next();

			//System.out.println("Method:  " + next.toString() + " " + next.getStartLine() + ", " + next.getEndLine());
			if ((next.getStartLine() <= lineNumber) && (next.getEndLine() >= lineNumber)) {
				return next;
			}
		}

		return null;
	}


	/**
	 *  Gets the InField attribute of the CurrentSummary object
	 *
	 *@param  typeSummary  Description of Parameter
	 *@param  lineNumber   Description of Parameter
	 *@return              The InField value
	 */
	private Summary isInField(TypeSummary typeSummary, int lineNumber)
	{
		Iterator iter = typeSummary.getFields();
		if (iter == null) {
			return null;
		}

		while (iter.hasNext()) {
			FieldSummary next = (FieldSummary) iter.next();
			//System.out.println("Field:  " + next.toString() + " " + next.getStartLine() + ", " + next.getEndLine());
			if ((next.getStartLine() <= lineNumber) && (next.getEndLine() >= lineNumber)) {
				return next;
			}
		}

		return null;
	}


	/**
	 *  Gets the InNestedClass attribute of the CurrentSummary object
	 *
	 *@param  typeSummary  Description of Parameter
	 *@param  lineNumber   Description of Parameter
	 *@return              The InNestedClass value
	 */
	private Summary isInNestedClass(TypeSummary typeSummary, int lineNumber)
	{
		Iterator iter = typeSummary.getTypes();
		if (iter == null) {
			return null;
		}

		while (iter.hasNext()) {
			TypeSummary next = (TypeSummary) iter.next();
			//System.out.println("Type:  " + next.toString() + " " + next.getStartLine() + ", " + next.getEndLine());
			if ((next.getStartLine() <= lineNumber) && (next.getEndLine() >= lineNumber)) {
				return findSummaryInType(next, lineNumber);
			}
		}

		return null;
	}


	/**
	 *  Gets the InSameSummary attribute of the CurrentSummary object
	 *
	 *@return    The InSameSummary value
	 */
	private boolean isInSameSummary()
	{
		int lineNumber = getLineNumber();
		return ((summary.getStartLine() <= lineNumber) && (summary.getEndLine() >= lineNumber));
	}


	/**
	 *  Description of the Method
	 *
	 *@return    Description of the Returned Value
	 */
	private Summary find()
	{
		try {
			registerWithCurrentDocument();

			int lineNumber = getLineNumber();
			if (lineNumber == -1) {
				//System.out.println("Unable to get the line number:  " + lastActive + "  " + lineNumber);
				return null;
			}

			if (!upToDate || (fileSummary == null)) {
				fileSummary = reloadNode();
			}

			if (fileSummary == null) {
				//System.out.println("Unable to load a file summary");
				return null;
			}

			Summary summary = getInType(fileSummary, lineNumber);
			if (summary != null) {
				//System.out.println("Found a summary:  " + summary);
				return summary;
			}

			//System.out.println("Just able to return the file summary");
			return fileSummary;
		}
		catch (IOException ioe) {
			return null;
		}
	}


	/**
	 *  Description of the Method
	 *
	 *@return                  Description of the Returned Value
	 *@exception  IOException  Description of Exception
	 */
	private FileSummary reloadNode() throws IOException
	{
		if (EditorOperations.get().isJavaFile()) {
			String contents = EditorOperations.get().getStringFromIDE();
			ByteArrayInputStream bais = new ByteArrayInputStream(contents.getBytes());

			return FileSummary.reloadFromBuffer(EditorOperations.get().getFile(), bais);
		}

		return null;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  next        Description of Parameter
	 *@param  lineNumber  Description of Parameter
	 *@return             Description of the Returned Value
	 */
	private Summary findSummaryInType(TypeSummary next, int lineNumber)
	{
		Summary result = isInMethod(next, lineNumber);
		if (result != null) {
			return result;
		}

		result = isInField(next, lineNumber);
		if (result != null) {
			return result;
		}

		result = isInNestedClass(next, lineNumber);
		if (result != null) {
			return result;
		}

		return next;
	}


	/**
	 *  Only does one find at a time
	 */
	private synchronized void lockAccess()
	{
		if ((summary == null) || !upToDate || !isSameFile() || !isInSameSummary()) {
			//System.out.println("About to find the summary");
			summary = find();
			upToDate = true;
			//System.out.println("Done");
		}

		//System.out.println("Finished lock access");
	}


	/**
	 *  Method to get the singleton object
	 *
	 *@return    the current summary
	 */
	public static CurrentSummary get()
	{
		return singleton;
	}


	/**
	 *  Register the current summary
	 *
	 *@param  value  Description of Parameter
	 */
	public static void register(CurrentSummary value)
	{
		singleton = value;
	}
}
