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

package org.netbeans.modules.web.core.syntax;

import org.openide.util.NbBundle;

/**
 * Options for the java editor kit
 *                  
 * @author Petr Jiricka, Libor Kramolis
 */
public class JSPOptions extends org.netbeans.modules.editor.options.BaseOptions {
    static final long serialVersionUID = 2347735706857337892L;

    public static final String JSP = "jsp"; // NOI18N

    public JSPOptions() {
        super (JSPKit.class, JSP);
    }

    /** @return localized string */
    protected String getString(String s) {
        try {
            String res = NbBundle.getBundle(JSPOptions.class).getString(s);
            return (res == null) ? super.getString(s) : res;
        }
        catch (Exception e) {
            return super.getString(s);
        }
    }


}

/*
 * Log
 *  4    Gandalf-post-FCS1.2.1.0     4/5/00   Petr Jiricka    Token names and examples
 *       from bundles.
 *  3    Gandalf   1.2         1/12/00  Petr Jiricka    Options name in Editor 
 *       Options
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/13/99  Petr Jiricka    
 * $
 */
