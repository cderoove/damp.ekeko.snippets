/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.summary.load;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.GridLayout;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JProgressBar;

import org.acm.seguin.tools.install.RefactoryStorage;

/**
 *  Reports to the user the status of the loading using stdout
 *
 *@author    Chris Seguin
 */
public class SwingLoadStatus extends JDialog implements LoadStatus {
	private JLabel label;
	private JProgressBar progress;
	private int count;
	private int max;
	private int fivePercent;
	private String oldName;
	private RefactoryStorage lengths;


	/**
	 *  Constructor for the SwingLoadStatus object
	 */
	public SwingLoadStatus() {
		super(new JFrame(), "Loading source files", false);

		getContentPane().setLayout(new GridLayout(2,1));

		label = new JLabel("Loading:  XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		Dimension size = label.getPreferredSize();
		label.setSize(size);
		getContentPane().add(label);

		progress = new JProgressBar();
		progress.setSize(size);
		getContentPane().add(progress);

		//setSize(230, 70);
		pack();
		setVisible(true);

		oldName = null;
		lengths = new RefactoryStorage();
	}


	/**
	 *  Sets the Root attribute of the LoadStatus object
	 *
	 *@param  name  The new Root value
	 */
	public void setRoot(String name) {
		if (oldName != null) {
			lengths.addKey(oldName + ".count", count);
		}

		if (name.endsWith(".stub")) {
			name = name.substring(0, name.length() - 5);
			progress.setForeground(Color.red);
		}
		else {
			progress.setForeground(Color.blue);
		}
		label.setText("Loading:  " + name);
		label.setSize(label.getPreferredSize());
		count = 0;
		progress.setValue(count);
		max = lengths.getValue(name + ".count");
		progress.setMaximum(max);
		fivePercent = max / 20;

		oldName = name;
	}


	/**
	 *  Sets the CurrentFile attribute of the LoadStatus object
	 *
	 *@param  name  The new CurrentFile value
	 */
	public void setCurrentFile(String name) {
		count++;
		if (fivePercent < 1) {
			progress.setValue(count);
		}
		else if (count % fivePercent == 0) {
			progress.setValue(count);
		}
	}


	/**
	 *  Completed the loading
	 */
	public void done() {
		dispose();
		if (oldName != null) {
			lengths.addKey(oldName + ".count", count);
		}
		lengths.store();
	}
}
