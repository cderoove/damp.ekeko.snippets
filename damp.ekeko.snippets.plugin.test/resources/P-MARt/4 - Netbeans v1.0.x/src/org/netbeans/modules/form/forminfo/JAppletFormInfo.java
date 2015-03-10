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

package org.netbeans.modules.form.forminfo;

import java.awt.Container;
import javax.swing.JApplet;
import javax.swing.JPanel;
import org.netbeans.modules.form.JMenuBarContainer;

/** FormInfo for javax.swing.JApplet.
*
* @author Ian Formanek
*/
public class JAppletFormInfo extends FormInfo implements JMenuBarContainer {

    // --------------------------------------------------------------------------------------
    // Private variables

    /** The form instance */
    private Object formInstance;
    /** The form top-level container */
    private JPanel topPanel;
    /** The form top-level container for adding */
    private JPanel innerPanel;

    private javax.swing.JMenuBar currentMenu = null;
    private boolean initialized = false;

    // --------------------------------------------------------------------------------------
    // FormInfo methods

    /** Used to create the design-time instance of the form object, which is used only for
    * displaing properties and events of the form.  I.e. it is not displayed visually, instead
    * the FormTopComponent is used with the container provided from <code>getTopContainer ()</code> method.
    * @return the instance of the form
    * @see #getTopContainer
    */
    public Object getFormInstance () {
        if (formInstance == null) {
            formInstance = new JApplet ();
        }
        return formInstance;
    }

    /** Used to provide the container which is used during design-time as the top-level container.
    * The container provided by this class should not be a Window, as it is added as a component to the
    * FormTopComponent, rather a JPanel, Panel or JDesktopPane should be used according to the form type. 
    * By returning a <code>null</code> value, the form info declares that it does not represent a "visual" form and the visual
    * ediuting should not be used with it.
    * @return the top level container which will be used during design-time or null if the form is not visual
    */
    public Container getTopContainer () {
        if (!initialized) {
            initialize ();
        }
        return topPanel;
    }


    /** Used to provide the container which is used during design-time as the top-level container for adding components.
    * The container provided by this class should not be a Window, as it is added as a component to the
    * FormTopComponent, rather a JPanel, Panel or JDesktopPane should be used according to the form type. 
    * By returning a <code>null</code> value, the form info declares that it does not represent a "visual" form and the visual
    * ediuting should not be used with it.
    * @return the top level container which will be used during design-time or null if the form is not visual
    */
    public Container getTopAddContainer () {
        if (!initialized) {
            initialize ();
        }
        return innerPanel;
    }

    /** By overriding this method, the form info can specify a string which is used to add top-level
    * components - i.e. for java.awt.Frame, the default (empty string) implementation is used, while
    * for javax.swing.JFrame a <code>"getContentPane ()."</code> will be returned.
    * @return the String to be used for adding to the top-level container
    * @see #getTopContainer
    */
    public String getContainerGenName () {
        return "getContentPane ()."; // NOI18N
    }

    private void initialize () {
        topPanel = new JPanel ();
        topPanel.setLayout (new java.awt.BorderLayout ());
        topPanel.add (innerPanel = new JPanel ());
        innerPanel.setLayout (new java.awt.BorderLayout ());
        initialized = true;
    }

    // --------------------------------------------------------------------------------------
    // JMenuBarContainer implementation

    /** Can be used to set the JMenuBar of this form.
    * @param menuBar The JMenuBar component or null to set no menu.
    */
    public void setJMenuBar (javax.swing.JMenuBar menuBar) {
        if (currentMenu != null) {
            topPanel.remove (currentMenu);
        }
        currentMenu = menuBar;
        if (currentMenu != null) {
            topPanel.add (currentMenu, java.awt.BorderLayout.NORTH);
        }
        topPanel.validate ();
        topPanel.repaint ();
    }

    /** Can be used to obtain the JMenuBar of this form.
    * @return The JMenuBar component or null if no menu is set.
    */
    public javax.swing.JMenuBar getJMenuBar () {
        return currentMenu;
    }

}

/*
 * Log
 *  5    Gandalf   1.4         1/5/00   Ian Formanek    NOI18N
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         7/16/99  Ian Formanek    implements 
 *       JMenuBarContainer
 *  2    Gandalf   1.1         6/6/99   Ian Formanek    FormInfo design 
 *       finalized
 *  1    Gandalf   1.0         5/12/99  Ian Formanek    
 * $
 */
