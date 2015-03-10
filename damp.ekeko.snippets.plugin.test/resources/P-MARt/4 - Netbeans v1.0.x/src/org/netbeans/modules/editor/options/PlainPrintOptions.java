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

package org.netbeans.modules.editor.options;

import org.netbeans.editor.ext.PlainKit;
import org.openide.util.NbBundle;

/**
* Options for the plain editor kit
*
* @author Miloslav Metelka
* @version 1.00
*/
public class PlainPrintOptions extends BasePrintOptions {

    public static final String PLAIN = "plain"; // NOI18N

    static final long serialVersionUID =-2630025235038437833L;

    public PlainPrintOptions() {
        super(PlainKit.class, PLAIN);
    }

    public PlainPrintOptions(Class kitClass, String typeName) {
        super(kitClass, typeName);
    }

}

/*
 * Log
 *  5    Gandalf   1.4         1/13/00  Miloslav Metelka Localization
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         7/29/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/20/99  Miloslav Metelka 
 * $
 */
