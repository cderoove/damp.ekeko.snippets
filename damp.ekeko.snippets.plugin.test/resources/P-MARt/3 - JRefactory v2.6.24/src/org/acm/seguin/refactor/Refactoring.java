/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.refactor;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.util.Date;

import org.acm.seguin.refactor.undo.UndoAction;
import org.acm.seguin.refactor.undo.UndoStack;
import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.util.FileSettings;

/**
 *  Adds a class that is either a parent or a child of an existing class.
 *
 *@author    Chris Seguin
 */
public abstract class Refactoring {
	private ComplexTransform complex = null;

	/**
	 *  The repackage refactoring
	 */
	public final static int REPACKAGE = 1;
	/**
	 *  The rename class refactoring
	 */
	public final static int RENAME_CLASS = 2;
	/**
	 *  The add child refactoring
	 */
	public final static int ADD_CHILD = 4;
	/**
	 *  The add parent refactoring
	 */
	public final static int ADD_PARENT = 3;
	/**
	 *  The remove class refactoring
	 */
	public final static int REMOVE_CLASS = 5;
	/**
	 *  Extracts the interface
	 */
	public final static int EXTRACT_INTERFACE = 6;

	/**
	 *  Pushes the field into the parent class
	 */
	public final static int PUSH_DOWN_FIELD = 101;
	/**
	 *  Pushes the field into the child classes
	 */
	public final static int PUSH_UP_FIELD = 102;
	/**
	 *  Renames the field
	 */
	public final static int RENAME_FIELD = 103;

	/**
	 *  Pushes the method into the parent class
	 */
	public final static int PUSH_UP_METHOD = 201;
	/**
	 *  Pushes the method signature into the parent class
	 */
	public final static int PUSH_UP_ABSTRACT_METHOD = 202;
	/**
	 *  Pushes the method into the child classes
	 */
	public final static int PUSH_DOWN_METHOD = 203;
	/**
	 *  Moves the method into another class
	 */
	public final static int MOVE_METHOD = 204;
	/**
	 *  Extracts code from one method to create a new method
	 */
	public final static int EXTRACT_METHOD = 205;
	/**
	 *  Extracts code from one method to create a new method
	 */
	public final static int RENAME_METHOD = 206;

	/**
	 *  Renames a parameter
	 */
	public final static int RENAME_PARAMETER = 251;


	/**
	 *  Constructor for the Refactoring object
	 */
	public Refactoring() { }


	/**
	 *  Gets the description of the refactoring
	 *
	 *@return    the description
	 */
	public abstract String getDescription();


	/**
	 *  Gets the id for this refactoring to track which refactorings are used.
	 *
	 *@return    the id
	 */
	public abstract int getID();


	/**
	 *  Main program that performst the transformation
	 *
	 *@exception  RefactoringException  Description of Exception
	 */
	public void run() throws RefactoringException
	{
		try {
			preconditions();
			transform();
			UndoStack.get().done();

			recordUsage();
		}
		catch (RefactoringException re) {
			throw re;
		}
		catch (Throwable thrown) {
			thrown.printStackTrace(System.out);
		}
	}


	/**
	 *  Gets a complex transform object for this refactoring
	 *
	 *@return    The ComplexTransform value
	 */
	protected ComplexTransform getComplexTransform()
	{
		if (complex == null) {
			UndoAction undo = UndoStack.get().add(this);
			complex = new ComplexTransform(undo);
		}

		return complex;
	}


	/**
	 *  Describes the preconditions that must be true for this refactoring to be
	 *  applied
	 *
	 *@exception  RefactoringException  thrown if one or more of the
	 *      preconditions is not satisfied. The text of the exception provides a
	 *      hint of what went wrong.
	 */
	protected abstract void preconditions() throws RefactoringException;


	/**
	 *  Performs the transform on the rest of the classes
	 */
	protected abstract void transform();


	/**
	 *  Check that we are allowed to adjust the destination
	 *
	 *@param  loop                      the summary
	 *@param  message                   the message
	 *@exception  RefactoringException  problem report
	 */
	protected void checkDestinationFile(Summary loop, String message) throws RefactoringException
	{
		while (loop != null) {
			if (loop instanceof FileSummary) {
				FileSummary temp = (FileSummary) loop;
				if (temp.getFile() == null) {
					throw new RefactoringException(message);
				}

				loop = null;
			}
			else {
				loop = loop.getParent();
			}
		}
	}


	/**
	 *  Record the refactoring usage
	 */
	private void recordUsage()
	{
		try {
			String dir = FileSettings.getSettingsRoot();
			FileWriter fileWriter = new FileWriter(dir + File.separator + "log.txt", true);
			PrintWriter output = new PrintWriter(fileWriter);
			DateFormat df = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
			output.println(getID() + ", " + df.format(new Date()));
			output.close();
		}
		catch (IOException ioe) {
		}
	}
}
