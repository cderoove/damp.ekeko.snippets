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

package org.netbeans.modules.form.compat2.layouts.support;


import javax.swing.JScrollPane;

import org.openide.nodes.*;
import org.openide.util.NbBundle;
import org.netbeans.modules.form.*;

/** A design-time support layout.
*
* @author   Ian Formanek
*/
final public class JScrollPaneSupportLayout extends SingleDesignSupportLayout {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -8895849636081002563L;

    private static JScrollPaneConstraintsDescription scrollCD = new JScrollPaneConstraintsDescription ();

    /** bundle to obtain text information from */
    private static final java.util.ResourceBundle bundle = NbBundle.getBundle (JScrollPaneSupportLayout.class);

    public JScrollPaneSupportLayout () {
        super (false);
    }

    /** Returns a description of the constraints for the
    * specified position. The constraints description object will be used
    * for adding, getting textual description of the constraints and
    * the popup menu.
    * The default implementation just returns the default constraints
    * description.
    * @param position The position within the container for which the
    *                 constraints description should be returned.
    */
    public ConstraintsDescription getConstraintsDescription(java.awt.Point position) {
        return scrollCD;
    }

    /** Adds specified component to this layout. The constraints for the component
    * are acquired from the component by method getConstraints().
    * If the getConstraints() returns null, a new constraints should be
    * synthesized, and set in the component via setConstraints().
    * @param node The component to add
    * @see RADVisualComponent#getConstraints
    * @see RADVisualComponent#setConstraints
    * @see #removeComponent
    */
    public void addComponent (RADVisualComponent node) {
        setScrolledComponent (node);
    }

    /** Removes specified component from this layout.
    * The default implementation removes the visual representation of the specified node
    * from the container.
    * @param node The component to remove
    * @see #addComponent
    */
    public void removeComponent (RADVisualComponent node) {
        if ((scrolledComponent != null) && (scrolledComponent.equals (node)))
            setScrolledComponent (null);
    }

    public void updateLayout() {
        setScrolledComponent(null);

        RADVisualComponent[] children = getRADContainer ().getSubComponents ();
        if (children.length > 1)
            System.err.println("JScrollPaneSupportLayout.updateLayout: children.lenth = "
                               +children.length);
        if (children.length > 0)
            setScrolledComponent(children[0]);

        getContainer ().validate ();
        getContainer ().repaint ();
    }

    /** The JScrollPane container can actually have multiple children.
    * The value of this property determines which of them is the right one.
    * @return the component that is actually inserted in the JScrollPane 
    */
    public void setScrolledComponent (RADVisualComponent node) {
        scrolledComponent = node;
        if (node != null)
            ((JScrollPane)getContainer()).setViewportView (getFormManager ().getVisualRepresentation (node));
        else
            ((JScrollPane)getContainer()).setViewportView (null);
    }

    /** Generates the code for adding specified component to this layout.
    * @param comp   The component to be added to this layout
    * @param cont   The container that is managed by this layout
    */
    public String generateComponentCode(RADVisualContainer cont, RADVisualComponent comp) {
        // special support for some containers
        StringBuffer buf = new StringBuffer ();
        RADVisualComponent[] children = cont.getSubComponents ();
        if (children.length > 0) {
            buf.append (cont.getName ());
            buf.append (".setViewportView ("); // NOI18N
            buf.append (children[0].getName ());
            buf.append (");\n"); // NOI18N
        }
        return buf.toString ();
    }

    // -----------------------------------------------------------------------------
    // constraints innerclass

    /** The ConstraintsDescription class encapsulates constraints data and
    * operations on a constraints that will be used for adding components
    * to the layout.
    */
    final public static class JScrollPaneConstraintsDescription extends SingleDesignSupportLayout.SingleSupportConstraintsDescription {
        /** A JDK 1.1 serial version UID */
        static final long serialVersionUID = 3149310821719911592L;

        /** Returns a textual descriptions of constraints represented by this
        * class. E.g. for BorderLayout, it is a text "Center" or "North".
        * @return textual descriptions of the constraints
        */
        public String getConstraintsString() {
            return bundle.getString ("MSG_ADD_jscroll");
        }

    }

    transient private RADVisualComponent scrolledComponent;
}

/*
 * Log
 *  10   Gandalf   1.9         2/16/00  Tran Duc Trung  FIX: a child component 
 *       of JScrollPane is not displayed after form is reopened
 *  9    Gandalf   1.8         2/15/00  Tran Duc Trung  FIX: child in viewport 
 *       is not displayed
 *  8    Gandalf   1.7         1/12/00  Ian Formanek    NOI18N
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         6/27/99  Ian Formanek    Removed indent parameter
 *       from code generation methods
 *  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/15/99  Ian Formanek    
 *  3    Gandalf   1.2         5/11/99  Ian Formanek    Build 318 version
 *  2    Gandalf   1.1         5/10/99  Ian Formanek    
 *  1    Gandalf   1.0         3/29/99  Ian Formanek    
 * $
 */
