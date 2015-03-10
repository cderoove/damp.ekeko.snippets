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
public class FormEventEvent extends FormEvent {

    private EventsManager.EventHandler event;

    static final long serialVersionUID =-5922874388933485317L;
    public FormEventEvent (RADComponent source, EventsManager.EventHandler event) {
        super (source);
        this.event = event;
    }

    public EventsManager.EventHandler getEventHandler () {
        return event;
    }
}

/*
 * Log
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  1    Gandalf   1.0         5/10/99  Ian Formanek    
 * $
 */
