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

package org.netbeans.modules.form;

import org.openide.explorer.ExplorerPanel;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.windows.*;

import java.awt.*;
import java.io.*;
import javax.swing.JPopupMenu;

/** The top component for the form window (design time representation of the form)
 *
 * @author  Ian Formanek
 */
public class FormTopComponent extends TopComponent {

    private FormManager2 formManager;
    private FormDataObject formObject;

    transient JPopupMenu displayedPopup;

    // FINALIZE DEBUG METHOD
    public void finalize () throws Throwable {
        super.finalize ();
        if (System.getProperty ("netbeans.debug.form.finalize") != null) {
            System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this); // NOI18N
        }
    } // FINALIZE DEBUG METHOD

    static final long serialVersionUID =5367782511020527952L;
    /** For externalization only */
    public FormTopComponent () {
    }

    /** Creates a new FormTopComponent for form represented by specified FormManager2
    * @param formManager The FormManager2 that represents the form to be displayed by this FormTopComponent
    */
    public FormTopComponent (FormDataObject formObject, FormManager2 formManager) {
        super ();
        this.formObject = formObject;
        this.formManager = formManager;
        setLayout (new BorderLayout ());
        init ();
    }

    private void init () {
        setName (java.text.MessageFormat.format (
                     FormEditor.getFormBundle ().getString ("FMT_FormWindowTitle"),
                     new Object[] { formManager.getFormObject ().getName () }
                 )
                );
    }

    public HelpCtx getHelpCtx () {
        return ExplorerPanel.getHelpCtx (getActivatedNodes (),
                                         new HelpCtx (FormTopComponent.class));
    }

    /** Provides a top-level container which is supposed to contain the top-level components
    * of the form - i.e. if the form type is JDialog and it contains 2 buttons in a FlowLayout, then
    * the FlowLayout will be set on the container provided by this method and the buttons will be
    * its subcomponents.
    * @return the top-level Container component
    */
    java.awt.Container getFormContainer () {
        return this;
    }

    public Dimension getPreferredSize () {
        return new Dimension (FormEditor.DEFAULT_FORM_WIDTH, FormEditor.DEFAULT_FORM_HEIGHT);
    }

    /** Called when this component is activated.
    * This happens when the parent window of this component gets focus
    * (and this component is the preferred one in it), <em>or</em> when
    * this component is selected in its window (and its window was already focussed).
    * Override this method to perform some special action on component activation:
    * typically, set performers for relevant actions.
    * Remember to call the super method.
    * The default implementation does nothing.
    */
    protected void componentActivated () {
        super.componentActivated ();
        FormEditor.getComponentInspector ().focusForm (formManager);
        updateActivatedNodes ();
        FormEditor.actions.attach (FormEditor.getComponentInspector ().getExplorerManager ());
    }

    /** Called when this component is deactivated.
    * This happens when the parent window of this component loses focus
    * (and this component is the preferred one in the parent),
    * <em>or</em> when this component loses preference in the parent window
    * (and the parent window is focussed).
    * Override this method to perform some special action on component deactivation:
    * typically, unset performers for relevant actions.
    * Remember to call the super method.
    * The default implementation does nothing.
    */
    protected void componentDeactivated () {
        if ((displayedPopup != null) && (displayedPopup.isVisible ())) {
            displayedPopup.setVisible (false);
            displayedPopup = null;
        }
        FormEditor.actions.detach ();
        super.componentDeactivated ();
    }

    void updateActivatedNodes () {
        if (FormEditor.getComponentInspector ().getFocusedForm () == formManager) {
            setActivatedNodes (FormEditor.getComponentInspector ().getSelectedNodes());
        }
    }

    /** Serialize this top component.
    * Subclasses wishing to store state must call the super method, then write to the stream.
    * @param out the stream to serialize to
    */
    public void writeExternal (ObjectOutput out)
    throws IOException {
        super.writeExternal (out);
        out.writeObject(formObject);
    }

    /** Deserialize this top component.
    * Subclasses wishing to store state must call the super method, then read from the stream.
    * @param in the stream to deserialize from
    */
    public void readExternal (ObjectInput in)
    throws IOException, ClassNotFoundException {
        super.readExternal (in);
        Object o = in.readObject();
        if (o instanceof FormDataObject) {
            formObject = (FormDataObject)o;
            setLayout (new BorderLayout ());
            if (formObject.getFormEditor ().loadFormInternal (this)) {
                formManager = formObject.getFormEditor ().getFormManager ();
                init ();
            } else {
                // failed to load the form => [PENDING]
            }
        }
    }
}

/*
 * Log
 *  21   Gandalf   1.20        1/5/00   Ian Formanek    NOI18N
 *  20   Gandalf   1.19        11/27/99 Patrik Knakal   
 *  19   Gandalf   1.18        11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  18   Gandalf   1.17        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  17   Gandalf   1.16        8/6/99   Ian Formanek    Fixed bug 3140 - exit 
 *       and restart Developer with opened Frame (and others Form Editor 
 *       components) not work correctly
 *  16   Gandalf   1.15        8/1/99   Ian Formanek    Improved deserialization
 *       of opened forms
 *  15   Gandalf   1.14        7/27/99  Jesse Glick     Context help (rolled 
 *       back to 1.11).
 *  14   Gandalf   1.13        7/19/99  Jesse Glick     Context help.
 *  13   Gandalf   1.12        7/8/99   Jesse Glick     Context help.
 *  12   Gandalf   1.11        7/4/99   Ian Formanek    Popup menu is hidden 
 *       when form window loses focus
 *  11   Gandalf   1.10        6/25/99  Ian Formanek    uses constants for size
 *  10   Gandalf   1.9         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  9    Gandalf   1.8         6/7/99   Ian Formanek    Externalization 
 *       constructor
 *  8    Gandalf   1.7         6/2/99   Ian Formanek    ToolsAction, Reorder
 *  7    Gandalf   1.6         5/30/99  Ian Formanek    Name formatting
 *  6    Gandalf   1.5         5/16/99  Ian Formanek    
 *  5    Gandalf   1.4         5/15/99  Ian Formanek    
 *  4    Gandalf   1.3         5/12/99  Ian Formanek    
 *  3    Gandalf   1.2         5/11/99  Ian Formanek    Build 318 version
 *  2    Gandalf   1.1         5/4/99   Ian Formanek    Package change
 *  1    Gandalf   1.0         4/29/99  Ian Formanek    
 * $
 */
