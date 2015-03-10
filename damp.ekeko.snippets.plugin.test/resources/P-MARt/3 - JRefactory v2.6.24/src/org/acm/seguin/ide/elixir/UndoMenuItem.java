package org.acm.seguin.ide.elixir;

import org.acm.seguin.ide.common.UndoAdapter;

/**
 *  Essentially an adapter that performs the Undo
 *  Refactoring operation.
 *
 *@author    Chris Seguin
 */
public class UndoMenuItem {
	/**
	 *  The static method that Elixir invokes to
	 *  Undo the last refactoring.
	 */
	public static void undo() {
		(new UndoAdapter()).actionPerformed(null);
	}
}
