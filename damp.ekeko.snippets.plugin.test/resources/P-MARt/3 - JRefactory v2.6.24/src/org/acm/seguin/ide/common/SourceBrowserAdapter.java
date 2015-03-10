package org.acm.seguin.ide.common;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import org.acm.seguin.summary.FileSummary;
import org.acm.seguin.summary.Summary;
import org.acm.seguin.uml.ISourceful;

/**
 *  Generic adapter for browsing source code
 *
 *@author    Chris Seguin
 */
public class SourceBrowserAdapter implements ActionListener {
	private ISourceful activeComponent;


	/**
	 *  Constructor for the SourceBrowserAdapter object
	 *
	 *@param  component  Description of Parameter
	 */
	public SourceBrowserAdapter(ISourceful component) {
		activeComponent = component;
	}


	/**
	 *  Responds to this item being selected
	 *
	 *@param  evt  Description of Parameter
	 */
	public void actionPerformed(ActionEvent evt) {
		File file = findFile();
		int line = getLine();

		SourceBrowser.get().gotoSource(file, line);
	}


	/**
	 *  Get the line number of the start of the current activeComponent.
	 *
	 *@return    The line number.
	 */
	protected int getLine() {
		return getSummary().getDeclarationLine();
	}


	/**
	 *  Get the Summary of the activeComponent.
	 *
	 *@return    The Summary of the activeComponent.
	 */
	protected Summary getSummary() {
		return activeComponent.getSourceSummary();
	}


	/**
	 *  Look up the chain of Summary parents to find the File the activeComponent
	 *  is sourced in.
	 *
	 *@return    The File.
	 */
	protected File findFile() {
		Summary summary = getSummary();
		while (!(summary instanceof FileSummary)) {
			summary = summary.getParent();
		}
		FileSummary fileSummary = (FileSummary) summary;
		return fileSummary.getFile();
	}
}
