/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.ide.jbuilder;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;
import javax.swing.KeyStroke;
import javax.swing.text.Keymap;

import org.acm.seguin.ide.common.action.GenericAction;

/**
 *  Modifies the key bindings whenever the keymap changes
 *
 *@author    Chris Seguin
 */
public class ModifyKeyBinding implements PropertyChangeListener {
	private Action prettyPrint;
	private Action extractMethod;


	/**
	 *  Constructor for the ModifyKeyBinding object
	 *
	 *@param  one  Description of Parameter
	 *@param  two  Description of Parameter
	 */
	public ModifyKeyBinding(Action one, Action two)
	{
		prettyPrint = one;
		extractMethod = two;

		setHotKeys();
	}



	/**
	 *  The EditorManager will call this function anytime it fires a property
	 *  change
	 *
	 *@param  e  the event
	 */
	public void propertyChange(PropertyChangeEvent e)
	{
		String propertyName = e.getPropertyName();

		// We are only interested in keymap changes
		if (propertyName.equals(EditorManager.keymapAttribute)) {
			setHotKeys();
		}
	}


	/**
	 *  Sets the HotKeys attribute of the ModifyKeyBinding object
	 */
	private void setHotKeys()
	{
		Keymap keymap = EditorManager.getKeymap();
		if (keymap == null) {
			System.out.println("No keymap");
			return;
		}

		KeyStroke stroke = (KeyStroke) prettyPrint.getValue(GenericAction.ACCELERATOR);
		if (stroke != null) {
			keymap.addActionForKeyStroke(stroke, prettyPrint);
		}

		stroke = (KeyStroke) extractMethod.getValue(GenericAction.ACCELERATOR);
		if (stroke != null) {
			keymap.addActionForKeyStroke(stroke, extractMethod);
		}
	}
}
