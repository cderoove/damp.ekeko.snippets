/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;

import javax.swing.Action;

import org.acm.seguin.ide.common.UndoAdapter;
import org.acm.seguin.refactor.undo.UndoStack;

/**
 *  Performs the undo operation
 *
 *@author    Chris Seguin
 */
public class UndoAction extends UndoAdapter implements Action {
	private PropertyChangeSupport support;
	private HashMap values;
	private boolean enabled;


	/**
	 *  Constructor for the UndoAction object
	 */
	public UndoAction() {
		support = new PropertyChangeSupport(this);
		values = new HashMap();
		enabled = true;

		putValue(NAME, "Undo");
		putValue(SHORT_DESCRIPTION, "Undo Refactoring");
		putValue(LONG_DESCRIPTION, "Undoes the last refactoring");
	}


	/**
	 *  Sets the Enabled attribute of the PrettyPrinterAction object
	 *
	 *@param  value  The new Enabled value
	 */
	public void setEnabled(boolean value) {
		enabled = value;
	}


	/**
	 *  Gets the Value attribute of the PrettyPrinterAction object
	 *
	 *@param  key  Description of Parameter
	 *@return      The Value value
	 */
	public Object getValue(String key) {
		return values.get(key);
	}


	/**
	 *  Gets the Enabled attribute of the PrettyPrinterAction object
	 *
	 *@return    The Enabled value
	 */
	public boolean isEnabled() {
		if (!enabled) {
			return false;
		}

		return !UndoStack.get().isStackEmpty();
	}


	/**
	 *  Sets the Value attribute of the PrettyPrinterAction object
	 *
	 *@param  key    The new key value
	 *@param  value  The new value value
	 */
	public void putValue(String key, Object value) {
		Object oldValue = getValue(key);
		Object newValue = value;
		support.firePropertyChange(key, oldValue, newValue);
		values.put(key, value);
	}


	/**
	 *  Adds a feature to the PropertyChangeListener attribute of the
	 *  PrettyPrinterAction object
	 *
	 *@param  listener  The feature to be added to the PropertyChangeListener
	 *      attribute
	 */
	public void addPropertyChangeListener(PropertyChangeListener listener) {
		support.addPropertyChangeListener(listener);
	}


	/**
	 *  Removes a listener
	 *
	 *@param  listener  the listener to be removed
	 */
	public void removePropertyChangeListener(PropertyChangeListener listener) {
		support.removePropertyChangeListener(listener);
	}
}
