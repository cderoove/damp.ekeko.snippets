package org.acm.seguin.ide.elixir;

import java.io.File;
import java.io.IOException;

import org.acm.seguin.ide.common.SourceBrowser;

/**
 *  This source browser allows Elixir to load files.
 *
 *@author    Chris Seguin
 */
public class ElixirSourceBrowser extends SourceBrowser {
	/**
	 *  Determines if the system is in a state where
	 *  it can browse the source code
	 *
	 *@return    true if the source code browsing is enabled
	 */
	public boolean canBrowseSource() {
		return true;
	}


	/**
	 *  Actually browses to the file
	 *
	 *@param  filename  the file
	 *@param  line      the line in the file
	 */
	public void gotoSource(File file, int line) {
		if ((file == null) || !file.exists()) {
			return;
		}
		try {
			String name = file.getCanonicalPath();
			ViewManager vm = FrameManager.current().open(name);
			//System.out.println("View type:  " + vm.getView().getClass().getName());
			if (vm instanceof BasicViewManager) {
				((BasicViewManager) vm).setLineNo(line);
			}
		}
		catch (IOException ioe) {
		}
	}
}
