/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is Forte for Java, Community Edition. The Initial
 * Developer of the Original Code is Sun Microsystems, Inc. Portions
 * Copyright 1997-2000 Sun Microsystems, Inc. All Rights Reserved.
 */

package org.openide.awt;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.*;

import javax.swing.*;

import org.openide.cookies.ExecCookie;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.WeakListener;

/** Menu item associated with data object. When
* pressed it executes the data object.
*
* @author Jaroslav Tulach
*/
class ExecBridge extends Object implements ActionListener, PropertyChangeListener {
    /** object to execute */
    private DataObject obj;
    /** associated button */
    private AbstractButton button;

    /** Creates new ExecMenuItem */
    private ExecBridge(DataObject obj, AbstractButton button) {
        this.obj = obj;
        this.button = button;

        obj.getNodeDelegate ().addPropertyChangeListener (WeakListener.propertyChange (this, obj.getNodeDelegate ()));

        updateState ();
    }

    /** Executes the object.
    */
    public void actionPerformed (ActionEvent ev) {
        ExecCookie ec = (ExecCookie)obj.getCookie (ExecCookie.class);
        if (ec != null) {
            ec.start ();
        }
    }

    /** Listen to changes of exec cookie on the data object.
    */
    public void propertyChange (PropertyChangeEvent ev) {
        if (Node.PROP_COOKIE.equals (ev.getPropertyName ())) {
            updateState ();
        }
        if (Node.PROP_DISPLAY_NAME.equals (ev.getPropertyName ())) {
            updateState ();
        }
        if (Node.PROP_ICON.equals (ev.getPropertyName ())) {
            updateState ();
        }
    }

    /** Updates state
    */
    private void updateState () {
        org.openide.nodes.Node node = obj.getNodeDelegate ();
        button.setText (node.getDisplayName ());
        Icon icon = new ImageIcon (node.getIcon (BeanInfo.ICON_COLOR_16x16));
        button.setIcon (icon);

        button.setEnabled (node.getCookie (ExecCookie.class) != null);
    }

    /** Creates menu item associated with the data object.
    */
    public static JMenuItem createMenuItem (DataObject obj) {
        JMenuItem item = new JMenuItem ();
        ExecBridge eb = new ExecBridge (obj, item);
        item.addActionListener (eb);
        return item;
    }

    /** Creates toolbar component associated with the object.
    */
    public static JButton createButton (DataObject obj) {
        JButton item = new JButton ();
        ExecBridge eb = new ExecBridge (obj, item);
        item.addActionListener (eb);
        return item;
    }

}

/*
* Log
*  3    Gandalf   1.2         11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         6/9/99   Jaroslav Tulach 
* $
*/