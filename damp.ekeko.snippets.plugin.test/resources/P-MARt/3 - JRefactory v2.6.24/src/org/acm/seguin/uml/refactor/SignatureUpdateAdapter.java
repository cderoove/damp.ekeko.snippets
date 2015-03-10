/*
 *  Author:  Chris Seguin
 *
 *  This software has been developed under the copyleft
 *  rules of the GNU General Public License.  Please
 *  consult the GNU General Public License for more
 *  details about use and distribution of this software.
 */
package org.acm.seguin.uml.refactor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

/**
 *  This adapter is resposible for keeping the signature in the dialog box
 *  relatively up to date.
 *
 *@author    Chris Seguin
 */
class SignatureUpdateAdapter implements ListDataListener, ActionListener,
		FocusListener, ListSelectionListener, DocumentListener
{
	private ExtractMethodDialog emd;


	/**
	 *  Constructor for the SignatureUpdateAdapter object
	 *
	 *@param  init  the dialog box it is responsible for
	 */
	public SignatureUpdateAdapter(ExtractMethodDialog init)
	{
		emd = init;
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of Parameter
	 */
	public void intervalAdded(ListDataEvent e)
	{
		emd.update();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of Parameter
	 */
	public void intervalRemoved(ListDataEvent e)
	{
		emd.update();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of Parameter
	 */
	public void contentsChanged(ListDataEvent e)
	{
		emd.update();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of Parameter
	 */
	public void actionPerformed(ActionEvent e)
	{
		emd.update();
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of Parameter
	 */
	public void focusGained(FocusEvent e)
	{
	}


	/**
	 *  Description of the Method
	 *
	 *@param  e  Description of Parameter
	 */
	public void focusLost(FocusEvent e)
	{
		emd.update();
	}


	/**
	 *  Someone selected something in the list box
	 *
	 *@param  e  Description of Parameter
	 */
	public void valueChanged(ListSelectionEvent e)
	{
		emd.update();
	}


	/**
	 *  Document listener event
	 *
	 *@param  evt  Description of Parameter
	 */
	public void insertUpdate(DocumentEvent evt)
	{
		emd.update();
	}


	/**
	 *  Document listener event
	 *
	 *@param  e  Description of Parameter
	 */
	public void removeUpdate(DocumentEvent e)
	{
		emd.update();
	}


	/**
	 *  Document listener event
	 *
	 *@param  e  Description of Parameter
	 */
	public void changedUpdate(DocumentEvent e)
	{
		emd.update();
	}
}
