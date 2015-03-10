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

import org.netbeans.editor.ext.HTMLKit;

/**
* Options for the java editor kit
*
* @author Miloslav Metelka
* @version 1.00
*/
public class HTMLOptions extends BaseOptions {

    public static final String HTML = "html"; // NOI18N

    static final long serialVersionUID =3409313048987440397L;
    public HTMLOptions() {
        super(HTMLKit.class, HTML);
    }

}

/*
 * Log
 *  7    Gandalf   1.6         1/13/00  Miloslav Metelka Localization
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  4    Gandalf   1.3         7/26/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/21/99  Miloslav Metelka 
 *  2    Gandalf   1.1         7/20/99  Miloslav Metelka 
 *  1    Gandalf   1.0         7/9/99   Ales Novak      
 * $
 */
