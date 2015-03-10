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
import java.awt.Panel;

/** FormInfo for java.awt.Panel.
*
* @author Ian Formanek
*/
public class PanelFormInfo extends FormInfo {

    // --------------------------------------------------------------------------------------
    // Private variables

    /** The form top-level container */
    private Container topContainer;

    // --------------------------------------------------------------------------------------
    // FormInfo methods

    /** Used to create the design-time instance of the form object, which is used only for
    * displaing properties and events of the form.  I.e. it is not displayed visually, instead
    * the FormTopComponent is used with the container provided from <code>getTopContainer ()</code> method.
    * @return the instance of the form
    * @see #getTopContainer
    */
    public Object getFormInstance () {
        return getTopContainer ();
    }

    /** Used to provide the container which is used during design-time as the top-level container.
    * The container provided by this class should not be a Window, as it is added as a component to the
    * FormTopComponent, rather a JPanel, Panel or JDesktopPane should be used according to the form type. 
    * By returning a <code>null</code> value, the form info declares that it does not represent a "visual" form and the visual
    * ediuting should not be used with it.
    * @return the top level container which will be used during design-time or null if the form is not visual
    */
    public Container getTopContainer () {
        if (topContainer == null) {
            topContainer = new Panel ();
        }
        return topContainer;
    }

}

/*
 * Log
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/6/99   Ian Formanek    FormInfo design 
 *       finalized
 *  2    Gandalf   1.1         5/12/99  Ian Formanek    
 *  1    Gandalf   1.0         5/3/99   Ian Formanek    
 * $
 */
