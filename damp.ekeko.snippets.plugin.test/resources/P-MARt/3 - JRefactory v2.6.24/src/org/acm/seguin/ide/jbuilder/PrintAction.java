/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.HashMap;
import javax.swing.Action;
import com.borland.primetime.ide.Browser;
import com.borland.primetime.node.Node;
import com.borland.primetime.node.TextFileNode;
import com.borland.primetime.vfs.Buffer;
import org.acm.seguin.ide.common.TextPrinter;
import org.acm.seguin.uml.print.PrintingThread;

/**
 *  Pretty printer action button
 *
 *@author    Chris Seguin
 */
public class PrintAction extends TextPrinter implements Action {
	private PropertyChangeSupport support;
	private HashMap values;
	private boolean enabled;


	/**
	 *  Constructor for the PrintAction object
	 */
	public PrintAction() {
		support = new PropertyChangeSupport(this);
		values = new HashMap();
		enabled = true;

		putValue(NAME, "Print");
		putValue(SHORT_DESCRIPTION, "Print");
		putValue(LONG_DESCRIPTION, "Prints the current file");
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

		Node active = getActiveNode();
		return (active instanceof TextFileNode) ||
				(active instanceof UMLNode);
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


	/**
	 *  The pretty printer action
	 *
	 *@param  evt  the action that occurred
	 */
	public void actionPerformed(ActionEvent evt) {
		Node active = getActiveNode();
		if (active instanceof TextFileNode) {
			//  Get the data from the window
			String windowText = getStringFromIDE();
			String fullFilename = getFilenameFromIDE();

			print(fullFilename, windowText);
		}
		else if (active instanceof UMLNode) {
			UMLNode node = (UMLNode) active;
			(new PrintingThread(node.getDiagram())).start();
		}
	}


	/**
	 *  Gets the initial string from the IDE
	 *
	 *@return    The file in string format
	 */
	protected String getFilenameFromIDE() {
		Node active = getActiveNode();
		if (active instanceof TextFileNode) {
			TextFileNode jtn = (TextFileNode) active;
			return jtn.getDisplayName();
		}

		return "Unknown filename";
	}


	/**
	 *  Gets the initial string from the IDE
	 *
	 *@return    The file in string format
	 */
	protected String getStringFromIDE() {
		Node active = getActiveNode();
		if (active instanceof TextFileNode) {
			TextFileNode jtn = (TextFileNode) active;
            try {
    			Buffer buffer = jtn.getBuffer();
    			byte[] contents = buffer.getContent();
    			return new String(contents);
            }
            catch (java.io.IOException ioex) {
              ioex.printStackTrace();
            }
		}

		return null;
	}


	/**
	 *  Gets the ActiveNode attribute of the PrintAction object
	 *
	 *@return    The ActiveNode value
	 */
	private Node getActiveNode() {
		Browser browser = Browser.getActiveBrowser();
		return browser.getActiveNode();
	}
}
