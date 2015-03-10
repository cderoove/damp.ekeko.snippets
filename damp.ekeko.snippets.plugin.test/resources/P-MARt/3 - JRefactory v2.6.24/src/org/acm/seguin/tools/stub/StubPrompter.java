/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.tools.stub;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;

import org.acm.seguin.awt.CenterDialog;
import org.acm.seguin.io.ExtensionFileFilter;

/**
 *  Asks the user where to start loading the JDK source code files
 *
 *@author    Chris Seguin
 */
public class StubPrompter extends JDialog implements ActionListener {
	private JTextArea filename;
	private File result;


	/**
	 *  Constructor for the StubPrompter object
	 *
	 *@param  frame   Description of Parameter
	 *@param  output  Description of Parameter
	 */
	public StubPrompter(JFrame frame, File output)
	{
		super(frame, "JDK Summary Generator", true);

		result = output;

		setSize(300, 205);
		getContentPane().setLayout(null);

		JLabel instructions1 = new JLabel("To effectively use this tool it is necessary to have");
		instructions1.setLocation(5, 5);
		instructions1.setSize(instructions1.getPreferredSize());
		getContentPane().add(instructions1);

		int height = instructions1.getPreferredSize().height;

		JLabel instructions2 = new JLabel("some overview of the Java Development Kit's");
		instructions2.setLocation(5, 5 + height);
		instructions2.setSize(instructions2.getPreferredSize());
		getContentPane().add(instructions2);

		JLabel instructions3 = new JLabel("source code.");
		instructions3.setLocation(5, 5 + 2 * height);
		instructions3.setSize(instructions3.getPreferredSize());
		getContentPane().add(instructions3);

		JLabel instructions4 = new JLabel("Please enter the jar or zip file that contains the");
		instructions4.setLocation(5, 15 + 3 * height);
		instructions4.setSize(instructions4.getPreferredSize());
		getContentPane().add(instructions4);

		JLabel instructions5 = new JLabel("source.  It is usually called src.jar.");
		instructions5.setLocation(5, 15 + 4 * height);
		instructions5.setSize(instructions5.getPreferredSize());
		getContentPane().add(instructions5);

		filename = new JTextArea();
		filename.setLocation(5, 15 + 6 * height);
		filename.setSize(190, 25);
		getContentPane().add(filename);

		JButton browse = new JButton("Browse");
		browse.setLocation(200, 15 + 6 * height);
		browse.setSize(85, 25);
		getContentPane().add(browse);
		browse.addActionListener(this);

		JButton okButton = new JButton("OK");
		okButton.setLocation(5, 45 + 6 * height);
		okButton.setSize(85, 25);
		getContentPane().add(okButton);
		okButton.addActionListener(this);

		CenterDialog.center(this, frame);
	}


	/**
	 *  The user has pressed a button. Handle the action appropriately.
	 *
	 *@param  evt  A description of the action
	 */
	public void actionPerformed(ActionEvent evt)
	{
		if (evt.getActionCommand().equals("OK")) {
			//System.out.println("OK button:  " + filename.getText());
			String name = filename.getText();
			File file = new File(name);
			if (file.exists()) {
				dispose();
				(new StubGenerator(name, result)).run();
			}
			else {
				JOptionPane.showMessageDialog(this,
						"The file you entered does not exist.\nPlease select the source code for the JDK.",
						"File does not exist",
						JOptionPane.ERROR_MESSAGE);
			}
		}
		else if (evt.getActionCommand().equals("Browse")) {
			//System.out.println("Browse button");
			JFileChooser chooser = new JFileChooser();

			ExtensionFileFilter zipFilter = new ExtensionFileFilter();
			zipFilter.addExtension(".zip");
			zipFilter.setDescription("Zip files");
			chooser.addChoosableFileFilter(zipFilter);

			ExtensionFileFilter jarFilter = new ExtensionFileFilter();
			jarFilter.addExtension(".jar");
			jarFilter.setDescription("Jar files");
			chooser.setFileFilter(jarFilter);

			chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);

			int result = chooser.showOpenDialog(this);
			if (result == JFileChooser.APPROVE_OPTION) {
				File selected = chooser.getSelectedFile();
				String path = null;
				try {
					path = selected.getCanonicalPath();
				}
				catch (IOException ioe) {
					path = selected.getPath();
				}
				filename.setText(path);
			}
		}
	}


	/**
	 *  The main program for the StubPrompter class
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args)
	{
		(new StubPrompter(null, new File("c:\\temp\\test.stub"))).setVisible(true);
	}
}
