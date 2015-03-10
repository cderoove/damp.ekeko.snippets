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

package org.netbeans.modules.search.res;

import java.util.*;
import java.awt.*;

import javax.swing.*;

import org.openide.util.*;



/**
 * Utility class for loading resources: texts and icons.
 *
 * Due to behaviour of NbBundle can not be named Bundle.class!!
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class Res extends Object {

    final static Class clzz = Res.class;

    final static ResourceBundle bundle = NbBundle.getBundle(clzz);

    private static String res(String id) {
        try {
            return bundle.getString(id);
        } catch (MissingResourceException ex) {
            return "?_" + id; // NOI18N
        }
    }

    public static String text(String id) {
        return res("TEXT_" + id); // NOI18N
    }

    public static String hint(String id) {
        return res("HINT_" + id); // NOI18N
    }

    public static ImageIcon icon(String id) {
        return new ImageIcon (clzz.getResource(res("$ICON_BASE") + res("ICON_" + id))); // NOI18N
    }

    public static Image image(String id) {
        return new ImageIcon (clzz.getResource(res("$ICON_BASE") + res("ICON_" + id))).getImage(); // NOI18N
    }

    public static void main(String args[]) {
        System.err.println("Returned: " + new Res().text("STOP"));
    }

}


/*
* Log
*  9    Gandalf   1.8         1/13/00  Radko Najman    I18N
*  8    Gandalf   1.7         1/11/00  Petr Kuzel      Result details added.
*  7    Gandalf   1.6         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  6    Gandalf   1.5         12/23/99 Petr Kuzel      Architecture improved.
*  5    Gandalf   1.4         12/17/99 Petr Kuzel      Bundling.
*  4    Gandalf   1.3         12/16/99 Petr Kuzel      
*  3    Gandalf   1.2         12/15/99 Petr Kuzel      
*  2    Gandalf   1.1         12/15/99 Martin Balin    Fixed package name
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 

