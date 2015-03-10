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

/**
* Options for the plain editor kit
*
* @author Miloslav Metelka
* @version 1.00
*/
public class PlainOptions extends BaseOptions {

    public static final String PLAIN = "plain"; // NOI18N

    static final long serialVersionUID =-7082075147378689853L;

    public PlainOptions() {
        this(PlainKit.class, PLAIN);
    }

    public PlainOptions(Class kitClass, String typeName) {
        super(kitClass, typeName);
    }

}

/*
 * Log
 *  8    Gandalf   1.7         1/13/00  Miloslav Metelka Localization
 *  7    Gandalf   1.6         11/27/99 Patrik Knakal   
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         7/26/99  Miloslav Metelka 
 *  4    Gandalf   1.3         7/20/99  Miloslav Metelka 
 *  3    Gandalf   1.2         7/9/99   Ales Novak      print options change
 *  2    Gandalf   1.1         7/3/99   Ian Formanek    Changed package 
 *       statement to make it compilable
 *  1    Gandalf   1.0         6/30/99  Ales Novak      
 * $
 */
