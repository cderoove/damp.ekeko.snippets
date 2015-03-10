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

import org.netbeans.modules.form.compat2.layouts.*;
import org.netbeans.modules.form.*;

/** A design-time support layout.
*
* @author   Ian Formanek
*/
public class SingleDesignSupportLayout extends DesignSupportLayout {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -4345262182700502754L;

    public SingleDesignSupportLayout () {
        this (true);
    }

    public SingleDesignSupportLayout (boolean generateComponentAddCode) {
        this.generateComponentAddCode = generateComponentAddCode;
    }

    /** Assigns this DesignLayout to the specified RADVisualContainer.
    * @param cont The RADVisualContainer that represents a container that will be
    *             managed by this layout or null as a notification that this layout
    *             is not a designLayout for its current container anymore
    */
    public void setRADContainer (RADVisualContainer cont) {
        super.setRADContainer(cont);
        singleCD = new SingleSupportConstraintsDescription();
    }

    /** Returns a constraint to be used for adding a component to the
    * specified position (e.g. in BorderLayout, the "North", "South", ...
    * will be determined by the position in the container).
    * The default implementation just returns the default constraints.
    * A special constraints object will be returned for layouts that
    * wish to set the position&size of the components rather than add
    * with constraints.
    * @param position The position within the container for which the
    *                 constraints should be returned.
    */
    public DesignLayout.ConstraintsDescription getConstraintsDescription(java.awt.Point position) {
        return singleCD;
    }

    protected boolean canAdd () {
        return true;
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
        if (canAdd ())
            getContainer().add(getFormManager ().getVisualRepresentation (node));
    }

    /** Returns a class of the layout that this DesignLayout represents (e.g.
    * returns FlowLayout.class from DesignFlowLayout).
    * @return a class of the layout represented by this DesignLayout or null if the 
    *         design layout does not represent a "real" layout (e.g. support layouts for JTabbedPane, ...)
    */
    public Class getLayoutClass() {
        return null;
    }

    // -----------------------------------------------------------------------------
    // Code generation

    /** Generates the code for initialization of this layout, e.g. panel1.setLayout (new BorderLayout ());.
    * @param cont   The container that is managed by this layout
    * @return the init code for the layout or null if it should not be generated
    */
    public String generateInitCode(RADVisualContainer cont) {
        return null;
    }

    /** Generates the code for adding specified component to this layout.
    * @param comp   The component to be added to this layout
    * @param cont   The container that is managed by this layout
    */
    public String generateComponentCode(RADVisualContainer cont, RADVisualComponent comp) {
        if (generateComponentAddCode) {
            StringBuffer buf = new StringBuffer();
            buf.append(createContainerGenName(cont));
            buf.append("add ("); // NOI18N
            buf.append(comp.getName());
            buf.append(");\n"); // NOI18N

            return buf.toString();
        } else
            return ""; // no code generation here // NOI18N
    }

    // -----------------------------------------------------------------------------
    // constraints innerclass

    /** The ConstraintsDescription class encapsulates constraints data and
    * operations on a constraints that will be used for adding components
    * to the layout.
    */
    public static class SingleSupportConstraintsDescription extends DesignLayout.ConstraintsDescription {
        /** A JDK 1.1 serial version UID */
        static final long serialVersionUID = -3865967802602855972L;

        public String getConstraintsString() {
            return ""; // NOI18N
        }

        public Object getConstraintsObject() {
            return null;
        }
    }

    // -----------------------------------------------------------------------------
    // private area

    transient private SingleSupportConstraintsDescription singleCD;

    private boolean generateComponentAddCode;

}

/*
 * Log
 *  9    Gandalf   1.8         1/13/00  Ian Formanek    NOI18N #2
 *  8    Gandalf   1.7         1/12/00  Ian Formanek    NOI18N
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         9/24/99  Ian Formanek    generateInitCode method 
 *       clarified
 *  5    Gandalf   1.4         7/31/99  Ian Formanek    Cleaned up comments
 *  4    Gandalf   1.3         6/27/99  Ian Formanek    Removed indent parameter
 *       from code generation methods
 *  3    Gandalf   1.2         5/11/99  Ian Formanek    Build 318 version
 *  2    Gandalf   1.1         5/10/99  Ian Formanek    
 *  1    Gandalf   1.0         3/29/99  Ian Formanek    
 * $
 */
