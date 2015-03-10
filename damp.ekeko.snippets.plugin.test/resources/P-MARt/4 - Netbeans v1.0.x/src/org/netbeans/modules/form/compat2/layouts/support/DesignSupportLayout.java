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

import org.netbeans.modules.form.compat2.layouts.DesignLayout;

/** An abstract superclass of design-time supports for LayoutManagers.
* This class is intended as a superclass of design layout supports for
* containers with defined behaviour - i.e. where it is not possible to modify the layout,
* but it is still necessary to be able to add to the container.
*
* @author   Ian Formanek
*/
public abstract class DesignSupportLayout extends DesignLayout {
    /** A JDK 1.1 serial version UID */
    static final long serialVersionUID = -3288653854902945606L;

    /** An icon of the design-layout. This icon will be used on the ComponentPalette
    * for this layout's item.
    * @param  type the desired type of the icon (BeanInfo.ICON_???)
    * @return layout's icon.
    */
    public java.awt.Image getIcon(int type) {
        return null;
    }

    /** A display name of the layout will be used for displaying the layout in
    * the components hierarchy during design-time.
    * @return layout's display name.
    */
    final public String getDisplayName() {
        return "SupportLayout"; // NOI18N
    }
}

/*
 * Log
 *  5    Gandalf   1.4         1/12/00  Ian Formanek    NOI18N
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         5/11/99  Ian Formanek    Build 318 version
 *  2    Gandalf   1.1         5/10/99  Ian Formanek    
 *  1    Gandalf   1.0         3/29/99  Ian Formanek    
 * $
 */
