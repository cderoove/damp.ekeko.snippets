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

package  org.netbeans.modules.web.util;

import java.lang.*;

public class Debug  {

    public final static boolean DEBUG = false;
    public final static boolean TEST = false;
    public final static boolean TRACE = false;

    public static void println(String out) {
        if(DEBUG)
            System.out.println(out);
    }

    public static void print(Exception ex) {
        if(DEBUG)
            System.out.println("Exception: "+ex.getMessage());			// NOI18N
        if(TRACE)
            ex.printStackTrace();
    }

}


