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
public class FormEvent extends java.util.EventObject {

    static final long serialVersionUID =4894539842200832521L;
    public FormEvent (RADComponent source) {
        super (source);
    }

    public RADComponent getRADComponent () {
        return (RADComponent)getSource ();
    }
}

/*
 * Log
 *  7    Gandalf   1.6         11/27/99 Patrik Knakal   
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  4    Gandalf   1.3         5/12/99  Ian Formanek    
 *  3    Gandalf   1.2         5/5/99   Ian Formanek    
 *  2    Gandalf   1.1         5/4/99   Ian Formanek    Package change
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */
