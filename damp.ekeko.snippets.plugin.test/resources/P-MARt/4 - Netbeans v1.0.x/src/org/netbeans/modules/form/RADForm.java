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

import org.netbeans.modules.form.forminfo.*;

import java.awt.Container;

/**
*
* @author Ian Formanek
*/
public class RADForm {

    private FormInfo formInfo;
    private RADComponent topRADComponent;

    public RADForm (FormInfo formInfo) {
        this.formInfo = formInfo;
    }

    /** Called by form manager to attach this RADForm to the form it manages */
    public void initialize (FormManager2 formManager) {
        if (formInfo.getTopContainer () != null) {
            topRADComponent = new RADVisualFormContainer (formInfo);
        } else {
            topRADComponent = new RADFormContainer (formInfo);
        }
        topRADComponent.initialize (formManager);
        topRADComponent.setComponent (formInfo.getFormInstance ().getClass ());
        topRADComponent.setName (formManager.getFormObject ().getName ()); // [PENDING - message format]
    }

    public FormInfo getFormInfo () {
        return formInfo;
    }

    public RADComponent getTopLevelComponent () {
        return topRADComponent;
    }

    public FormContainer getFormContainer () {
        return (FormContainer)topRADComponent;
    }

    public boolean allowsVisualComponents () {
        return (formInfo.getTopContainer () != null);
    }


    // Ideas:
    // 1. Dialog, Frame, ... - will be synthesized, not real instance will be created until TestMode
    // 2. Panel, JInternalFrame
    //    - real top-level component instance will be created
    //    - it is necessary to combine synthesized properties (size, ...) with real (live) properties
}

/*
 * Log
 *  10   Gandalf   1.9         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         6/29/99  Ian Formanek    added method 
 *       getFormContainer
 *  8    Gandalf   1.7         6/6/99   Ian Formanek    New FormInfo design 
 *       employed to provide correct top-level bean properties
 *  7    Gandalf   1.6         5/15/99  Ian Formanek    
 *  6    Gandalf   1.5         5/11/99  Ian Formanek    Build 318 version
 *  5    Gandalf   1.4         5/10/99  Ian Formanek    
 *  4    Gandalf   1.3         5/5/99   Ian Formanek    
 *  3    Gandalf   1.2         5/4/99   Ian Formanek    Package change
 *  2    Gandalf   1.1         4/29/99  Ian Formanek    
 *  1    Gandalf   1.0         4/29/99  Ian Formanek    
 * $
 */
