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

import java.awt.*;
import javax.swing.JPanel;

import org.netbeans.modules.form.MenuBarContainer;

/** FormInfo for java.awt.Frame.
*
* @author Ian Formanek
*/
public class FrameFormInfo extends FormInfo implements MenuBarContainer {

    // --------------------------------------------------------------------------------------
    // Private variables

    /** The form instance */
    private Frame formInstance;
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
            formInstance = new Frame ();

            // we must set these explicitly, as the window is never displayed and the fornt and colors would stay null
            formInstance.setBackground (SystemColor.window);
            formInstance.setForeground (SystemColor.windowText);
            formInstance.setFont (new Font ("Dialog", Font.PLAIN, 12)); // NOI18N
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

    private void initialize () {
        topPanel = new JPanel ();
        topPanel.setLayout (new java.awt.BorderLayout ());
        topPanel.add (innerPanel = new JPanel ());
        innerPanel.setLayout (new java.awt.BorderLayout ());
        initialized = true;
    }

    // --------------------------------------------------------------------------------------
    // JMenuBarContainer implementation

    /** Can be used to set the MenuBar of this form.
    * @param menuBar The MenuBar component or null to set no menu.
    */
    public void setMenuBar (MenuBar menuBar) {
        getFormInstance (); // enforce creation if not initialized yet
        formInstance.setMenuBar (menuBar);
        if (currentMenu != null) {
            topPanel.remove (currentMenu);
        }
        if (menuBar!=null) {
            currentMenu = (javax.swing.JMenuBar) org.netbeans.modules.form.RADMenuItemComponent.findDesignTimeMenu (menuBar);
            topPanel.add (currentMenu, java.awt.BorderLayout.NORTH);
        }
        topPanel.validate ();
        topPanel.repaint ();
    }

    /** Can be used to obtain the MenuBar of this form.
    * @return The MenuBar component or null if no menu is set.
    */
    public MenuBar getMenuBar () {
        getFormInstance (); // enforce creation if not initialized yet
        return formInstance.getMenuBar ();
    }

}

/*
 * Log
 *  8    Gandalf   1.7         1/17/00  Pavel Buzek     setting menubar to null 
 *       did not work (fixed)
 *  7    Gandalf   1.6         1/5/00   Ian Formanek    NOI18N
 *  6    Gandalf   1.5         12/2/99  Pavel Buzek     AWT menu is displayed in
 *       form at design time (a swing equivalent is created for each awt menu 
 *       and displyed instead)
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/6/99   Ian Formanek    Fixed bug 3448 - Menu 
 *       added to AWT form is not set to frame in code.
 *  3    Gandalf   1.2         9/6/99   Ian Formanek    Fixed bug 3230 - When I 
 *       instantiate new AWTform, background is set to null which causes 
 *       Exception when trying to use property editor.
 *  2    Gandalf   1.1         6/6/99   Ian Formanek    FormInfo design 
 *       finalized
 *  1    Gandalf   1.0         5/12/99  Ian Formanek    
 * $
 */
