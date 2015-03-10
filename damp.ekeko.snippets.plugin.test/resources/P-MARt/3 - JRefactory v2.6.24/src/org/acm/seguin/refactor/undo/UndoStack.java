/*
 * Author:  Chris Seguin
 *
 * This software has been developed under the copyleft
 * rules of the GNU General Public License.  Please
 * consult the GNU General Public License for more
 * details about use and distribution of this software.
 */
package org.acm.seguin.refactor.undo;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.Stack;

import org.acm.seguin.refactor.Refactoring;
import org.acm.seguin.uml.loader.ReloaderSingleton;
import org.acm.seguin.util.FileSettings;

/**
 *  The stack of refactorings that we can undo. This stack holds all the
 *  refactorings that have occurred in the system. <P>
 *
 *  This object is a singleton object because we only want one object
 *  responsible for storing the refactorings that can be undone.
 *
 *@author    Chris Seguin
 */
public class UndoStack {
	/**
	 *  The stack that contains the actual elements
	 */
	private Stack stack;

	private static UndoStack singleton;


	/**
	 *  Constructor for the UndoStack object
	 */
	public UndoStack() {
		if (!load()) {
			stack = new Stack();
		}
	}


	/**
	 *  Gets the StackEmpty attribute of the UndoStack object
	 *
	 *@return    The StackEmpty value
	 */
	public boolean isStackEmpty() {
		return stack.isEmpty();
	}


	/**
	 *  Adds a refactoring to the undo stack. You provide the refactoring, this
	 *  method provides the undo action.
	 *
	 *@param  ref  the refactoring about to be performed
	 *@return      an undo action
	 */
	public UndoAction add(Refactoring ref) {
		UndoAction action = new UndoAction(ref.getDescription());
		stack.push(action);
		return action;
	}


	/**
	 *  Return the top option without removing it from the stack
	 *
	 *@return    the top object
	 */
	public UndoAction peek() {
		return (UndoAction) stack.peek();
	}


	/**
	 *  Lists the undo actions in the stack
	 *
	 *@return    an iterator of undo actions
	 */
	public Iterator list() {
		return stack.iterator();
	}


	/**
	 *  Performs an undo of the top action
	 */
	public void undo() {
		UndoAction action = (UndoAction) stack.pop();
		action.undo();
		ReloaderSingleton.reload();
	}


	/**
	 *  Description of the Method
	 */
	public void done() {
		save();
	}


	/**
	 *  Deletes the undo stack
	 */
	public void delete() {
		File file = getFile();
		file.delete();
		stack = new Stack();
	}


	/**
	 *  Gets the stack file
	 *
	 *@return    The File value
	 */
	private File getFile() {
		File dir = new File(FileSettings.getSettingsRoot());
		return new File(dir, "undo.stk");
	}


	/**
	 *  Saves the undo stack to the disk
	 */
	private void save() {
		try {
			File file = getFile();
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream(file));
			output.writeObject(stack);
			output.flush();
			output.close();
		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.out);
		}
	}


	/**
	 *  Loads the undo stack from the disk
	 *
	 *@return    Description of the Returned Value
	 */
	private boolean load() {
		try {
			File file = getFile();
			ObjectInputStream input = new ObjectInputStream(new FileInputStream(file));
			stack = (Stack) input.readObject();
			input.close();

			return true;
		}
		catch (FileNotFoundException fnfe) {
			//  Expected - this is normal the first time
		}
		catch (IOException ioe) {
			ioe.printStackTrace(System.out);
		}
		catch (ClassNotFoundException cnfe) {
			cnfe.printStackTrace(System.out);
		}

		return false;
	}


	/**
	 *  Gets the singleton undo operation
	 *
	 *@return    the undo stack for the system
	 */
	public static UndoStack get() {
		if (singleton == null) {
			singleton = new UndoStack();
		}

		return singleton;
	}
}
