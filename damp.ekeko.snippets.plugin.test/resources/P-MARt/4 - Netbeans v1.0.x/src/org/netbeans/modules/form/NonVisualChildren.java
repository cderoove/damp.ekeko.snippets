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

import org.openide.nodes.*;

/**
*
* @author Ian Formanek
*/
public class NonVisualChildren extends Children.Keys {

    private FormManager2 manager;

    // FINALIZE DEBUG METHOD
    public void finalize () throws Throwable {
        super.finalize ();
        if (System.getProperty ("netbeans.debug.form.finalize") != null) {
            System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this); // NOI18N
        }
    } // FINALIZE DEBUG METHOD

    /** Creates new NonVisualChildren for specified ComponentContainer
    * @param container The component container for which this children should be created
    */
    public NonVisualChildren (FormManager2 manager) {
        super ();
        this.manager = manager;
        updateKeys ();
    }

    /** Create nodes for a given key.
    * @param key the key
    * @return child nodes for this key
    */
    protected Node[] createNodes (Object key) {
        return new Node[] { new RADComponentNode ((RADComponent)key) };
    }

    void updateKeys () {
        RADComponent[] subComps = manager.getNonVisualComponents ();
        setKeys (subComps);
    }
}

/*
 * Log
 *  5    Gandalf   1.4         1/5/00   Ian Formanek    NOI18N
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         6/2/99   Ian Formanek    ToolsAction, Reorder
 *  1    Gandalf   1.0         5/24/99  Ian Formanek    
 * $
 */
