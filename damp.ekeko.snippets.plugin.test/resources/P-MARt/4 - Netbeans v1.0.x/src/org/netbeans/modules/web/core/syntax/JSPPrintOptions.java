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
public class JSPPrintOptions extends org.netbeans.modules.editor.options.BasePrintOptions {

    public static final String JSP = "jsp"; // NOI18N

    static final long serialVersionUID =6426608187973252147L;
    public JSPPrintOptions() {
        super (JSPKit.class, JSP);
    }

    public JSPPrintOptions (Class kitClass, String typeName) {
        super (kitClass, typeName);
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
 *  5    Gandalf-post-FCS1.3.1.0     4/5/00   Petr Jiricka    Token names and examples
 *       from bundles.
 *  4    Gandalf   1.3         1/12/00  Petr Jiricka    Options name in Editor 
 *       Options
 *  3    Gandalf   1.2         11/27/99 Patrik Knakal   
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/13/99  Petr Jiricka    
 * $
 */
