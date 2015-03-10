package org.acm.seguin.uml.jpg;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;

import org.acm.seguin.io.ExtensionFileFilter;
import org.acm.seguin.uml.UMLPackage;

/**
 *  Object that handles a mouse event on the save operation.
 *
 *@author    Chris Seguin
 */
public class SaveAdapter implements ActionListener {
	private UMLPackage diagram;
	private static File directory = null;


	/**
	 *  Constructor for the SaveAdapter object
	 *
	 *@param  packageDiagram  Description of Parameter
	 */
	public SaveAdapter(UMLPackage packageDiagram) {
		diagram = packageDiagram;
	}


	/**
	 *  Performs the action
	 *
	 *@param  evt  Description of Parameter
	 */
	public void actionPerformed(ActionEvent evt) {
		String filename = getFilename();
		if (filename == null) {
			return;
		}
		(new Save(filename, diagram)).run();
	}


	/**
	 *  Gets the Filename to save the file as
	 *
	 *@return    The Filename value
	 */
	private String getFilename() {
		JFileChooser chooser = new JFileChooser();

		//  Create the java file filter
		ExtensionFileFilter filter = new ExtensionFileFilter();
		filter.addExtension(".jpg");
		filter.setDescription("JPG Image Files (.jpg)");
		chooser.setFileFilter(filter);

		//  Set it so that files only can be selected and it is a save box
		chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		chooser.setDialogType(JFileChooser.SAVE_DIALOG);

		//  Set the directory to the current directory
		if (SaveAdapter.directory == null) {
			SaveAdapter.directory = new File(System.getProperty("user.dir"));
		}
		chooser.setCurrentDirectory(SaveAdapter.directory);

		//  Get the user's selection
		int returnVal = chooser.showSaveDialog(null);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			File selectedFile = chooser.getSelectedFile();
			SaveAdapter.directory = selectedFile.getParentFile();
			return selectedFile.getAbsolutePath();
		}

		return null;
	}
}
