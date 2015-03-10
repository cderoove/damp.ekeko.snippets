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

package org.netbeans.modules.emacs;

import java.util.*;

public class EmacsEvent extends EventObject {

    private final String type;
    private final Object[] args;
    private boolean outOfSequence;

    public EmacsEvent (Object source, String type, Object[] args) {
        this (source, type, args, false);
    }

    public EmacsEvent (Object source, String type, Object[] args, boolean oos) {
        super (source);
        this.type = type;
        this.args = args;
        this.outOfSequence = oos;
    }

    public String getType () {
        return type;
    }

    public Object[] getArgs () {
        return args;
    }

    public boolean isOutOfSequence () {
        return outOfSequence;
    }

    public String toString () {
        StringBuffer buf = new StringBuffer ("EmacsEvent[");
        if (outOfSequence) buf.append ("OutOfSequence:");
        buf.append (source);
        buf.append (',');
        buf.append (type);
        for (int i = 0; i < args.length; i++) {
            buf.append (',');
            buf.append (args[i]);
        }
        buf.append (']');
        return buf.toString ();
    }

}
