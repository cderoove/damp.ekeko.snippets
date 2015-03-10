/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.awt;

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.StringTokenizer;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**
 *  The exception dialog box. Displays the message in one scroll pane and the
 *  stack trace in another.
 *
 *@author    Chris Seguin
 */
public class ExceptionDialog extends JDialog implements ActionListener {
	/**
	 *  Constructor for the ExceptionDialog object
	 *
	 *@param  thrown  Description of Parameter
	 */
	public ExceptionDialog(Throwable thrown)
	{
		this(thrown, null, true);
	}


	/**
	 *  Constructor for the ExceptionDialog object
	 *
	 *@param  thrown  Description of Parameter
	 *@param  parent  Description of Parameter
	 */
	public ExceptionDialog(Throwable thrown, Frame parent)
	{
		this(thrown, parent, true);
	}


	/**
	 *  Constructor for the ExceptionDialog object
	 *
	 *@param  thrown  Description of Parameter
	 *@param  parent  Description of Parameter
	 *@param  modal   Description of Parameter
	 */
	public ExceptionDialog(Throwable thrown, Frame parent, boolean modal)
	{
		super(parent, modal);
		setTitle("Exception thrown");
		setResizable(false);

		getContentPane().setLayout(null);

		JLabel sizer = new JLabel("XXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXXX");
		Dimension size = sizer.getPreferredSize();
		int scrollHeight = 6 * size.height;
		Dimension scrollSize = new Dimension(size.width, scrollHeight);

		JScrollPane messagePane = new JScrollPane(createPanel(untab(thrown.getMessage())));
		messagePane.setPreferredSize(scrollSize);
		messagePane.setSize(scrollSize);
		messagePane.setLocation(10, 10);
		getContentPane().add(messagePane);

		JScrollPane stackPane = new JScrollPane(createStackTrace(thrown));
		stackPane.setPreferredSize(scrollSize);
		stackPane.setSize(scrollSize);
		stackPane.setLocation(10, 20 + scrollHeight);
		getContentPane().add(stackPane);

		JButton ok = new JButton("OK");
		Dimension okSize = ok.getPreferredSize();
		ok.setSize(okSize);
		ok.setLocation((20 + size.width - okSize.width) / 2,
				30 + scrollHeight * 2);
		getContentPane().add(ok);
		ok.addActionListener(this);

		int wide = 20 + size.width;
		int high = 90 + scrollHeight * 2;
		setSize(wide, high);

		CenterDialog.center(this);
	}


	/**
	 *  The user has pressed the OK button
	 *
	 *@param  evt  the OK button event
	 */
	public void actionPerformed(ActionEvent evt)
	{
		dispose();
	}


	/**
	 *  Creates a panel based on a string
	 *
	 *@param  value  the string
	 *@return        the panel
	 */
	private JPanel createPanel(String value)
	{
		LinkedList list = new LinkedList();
		StringTokenizer tok = new StringTokenizer(value, "\n\r");
		while (tok.hasMoreTokens()) {
			list.add(tok.nextToken());
		}

		JPanel result = new JPanel();
		result.setLayout(new GridLayout(list.size(), 1));
		Iterator iter = list.iterator();
		while (iter.hasNext()) {
			JLabel label = new JLabel((String) iter.next());
			result.add(label);
		}

		return result;
	}


	/**
	 *  Creates a panel containing the stack trace of the exception
	 *
	 *@param  thrown  the exception
	 *@return         the panel
	 */
	private JPanel createStackTrace(Throwable thrown)
	{
		StringWriter stringWriter = new StringWriter();
		PrintWriter output = new PrintWriter(stringWriter);
		thrown.printStackTrace(output);
		output.close();

		return createPanel(untab(stringWriter.toString()));
	}


	/**
	 *  Description of the Method
	 *
	 *@param  value  Description of Parameter
	 *@return        Description of the Returned Value
	 */
	private String untab(String value)
	{
		StringBuffer buffer = new StringBuffer();
		int last = value.length();

		for (int ndx = 0; ndx < last; ndx++) {
			if (value.charAt(ndx) == '\t') {
				buffer.append("        ");
			}
			else {
				buffer.append(value.charAt(ndx));
			}
		}

		return buffer.toString();
	}


	/**
	 *  The main program for the ExceptionDialog class. This program is used to
	 *  test this dialog box.
	 *
	 *@param  args  The command line arguments
	 */
	public static void main(String[] args)
	{
		(new ExceptionDialog(new Exception("Here is\na six line\nerror\nmesage\nfor you to\nread.\n \nDo you enjoy?"))).setVisible(true);
	}
}
