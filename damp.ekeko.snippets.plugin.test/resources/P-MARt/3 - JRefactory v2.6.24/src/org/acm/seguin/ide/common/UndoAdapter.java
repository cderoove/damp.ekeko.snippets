package org.acm.seguin.ide.common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.acm.seguin.refactor.undo.UndoAction;
import org.acm.seguin.refactor.undo.UndoStack;

/**
 *  General software component that can perform an undo operation
 *
 *@author    Chris Seguin
 */
public class UndoAdapter implements ActionListener {
	/**
	 *  When the menu item is selected, do this
	 *
	 *@param  evt  The event
	 */
	public void actionPerformed(ActionEvent evt) {
		if (UndoStack.get().isStackEmpty()) {
			JOptionPane.showMessageDialog(null,
					"No more refactorings to undo.",
					"Undo Refactoring",
					JOptionPane.ERROR_MESSAGE);
		}
		else {
			UndoAction action = UndoStack.get().peek();
			int result = JOptionPane.showConfirmDialog(null,
					"Would you like to undo the following refactoring?\n" +
					action.getDescription(),
					"Undo Refactoring",
					JOptionPane.YES_NO_OPTION);

			if (result == JOptionPane.YES_OPTION) {
				UndoStack.get().undo();
			}
		}
	}
}
