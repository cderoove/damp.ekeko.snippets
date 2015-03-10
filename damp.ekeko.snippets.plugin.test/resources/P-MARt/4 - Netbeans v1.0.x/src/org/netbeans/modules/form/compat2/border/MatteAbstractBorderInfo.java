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

package org.netbeans.modules.form.compat2.border;

import java.awt.*;
import java.beans.*;

import javax.swing.border.*;

import org.openide.nodes.*;

/**
*
* @author   Petr Hamernik
* @version  1.02, Aug 07, 1998
*/
public abstract class MatteAbstractBorderInfo extends BorderInfoSupport {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 654063516803487565L;

    private static final String PROP_TOP = "top"; // NOI18N
    private static final String PROP_LEFT = "left"; // NOI18N
    private static final String PROP_BOTTOM = "bottom"; // NOI18N
    private static final String PROP_RIGHT = "right"; // NOI18N

    protected int top;
    protected int left;
    protected int bottom;
    protected int right;

    public MatteAbstractBorderInfo() {
        top = 1;
        left = 1;
        bottom = 1;
        right = 1;
    }

    protected MatteBorder getMatteBorder() {
        return (MatteBorder) getBorder();
    }

    protected abstract void updateBorder();

    /** Returns the border's properties for specified component.
    * The default implementation returns empty array.
    * @param node the RADVisualNode of the component which properties we request
    * @return the layout-specific properties for specified component
    */
    public Node.Property[] getInsetsProperties() {
        return new Node.Property[] {
                   new BorderProp(PROP_TOP, Integer.TYPE,
                                  BorderInfo.bundle.getString("PROP_Top"),
                                  BorderInfo.bundle.getString("HINT_Top")) {
                       public Object getValue () {
                           return new Integer(top);
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Integer) {
                               top = ((Integer)val).intValue();
                               updateBorder();
                               firePropChange();
                           }
                           else throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return (top == 1);
                       }
                   },
                   new BorderProp(PROP_LEFT, Integer.TYPE,
                                  BorderInfo.bundle.getString("PROP_Left"),
                                  BorderInfo.bundle.getString("HINT_Left")) {
                       public Object getValue () {
                           return new Integer(left);
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Integer) {
                               left = ((Integer)val).intValue();
                               updateBorder();
                               firePropChange();
                           }
                           else throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return (left == 1);
                       }
                   },
                   new BorderProp(PROP_BOTTOM, Integer.TYPE,
                                  BorderInfo.bundle.getString("PROP_Bottom"),
                                  BorderInfo.bundle.getString("HINT_Bottom")) {
                       public Object getValue () {
                           return new Integer(bottom);
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Integer) {
                               bottom = ((Integer)val).intValue();
                               updateBorder();
                               firePropChange();
                           }
                           else throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return (bottom == 1);
                       }
                   },
                   new BorderProp(PROP_RIGHT, Integer.TYPE,
                                  BorderInfo.bundle.getString("PROP_Right"),
                                  BorderInfo.bundle.getString("HINT_Right")) {
                       public Object getValue () {
                           return new Integer(right);
                       }
                       public void setValue (Object val) throws IllegalArgumentException {
                           if (val instanceof Integer) {
                               right = ((Integer)val).intValue();
                               updateBorder();
                               firePropChange();
                           }
                           else throw new IllegalArgumentException();
                       }
                       public boolean isDefault() {
                           return (right == 1);
                       }
                   }
               };
    }
}

/*
 * Log
 *  4    Gandalf   1.3         1/12/00  Ian Formanek    NOI18N
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         4/2/99   Ian Formanek    
 * $
 */
