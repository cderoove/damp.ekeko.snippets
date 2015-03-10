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

package org.netbeans.modules.antlr.editor;

import org.netbeans.modules.editor.options.*;

/**
* Options for the java editor kit
*
* @author Miloslav Metelka
* @version 1.00
*/
public class GOptions extends BaseOptions {

    public static final String GRAMMAR = "grammar";

    static final long serialVersionUID =3409313048987440397L;
    public GOptions() {
        super(GKit.class, GRAMMAR);
    }

}

/*
 * Log
 *  1    Gandalf-post-FCS1.0         4/7/00   Jesse Glick     
 * $
 */
