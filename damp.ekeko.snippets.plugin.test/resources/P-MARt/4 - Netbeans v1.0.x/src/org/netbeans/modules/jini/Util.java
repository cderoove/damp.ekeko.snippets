/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jini;

import java.util.*;

import org.openide.util.NbBundle;

/** Jini utils.
*
* @author Martin Ryzl, Petr Kuzel
*/
public class Util {

    /** Getter for string.
     */
    public static String getString(String name) {
        return NbBundle.getBundle(JiniNode.class).getString(name);
    }

    public static Object[] removeNull(Object[] array) {
        int items = 0;
        Object[] na;

        for(int i = 0; i < array.length; i++) {
            if (array[i] != null) items++;
        }

        na = (Object[])java.lang.reflect.Array.newInstance(array.getClass().getComponentType(), items);

        int j = 0;
        for(int i = 0; i < array.length; i++) {
            if (array[i] != null) na[j++] = array[i];
        }
        return na;
    }

    /** Collates object by toString() collating. */
    static class ClassCollator implements Comparator {

        java.text.Collator collator = java.text.Collator.getInstance();

        public boolean equals(final java.lang.Object p1) {
            if (p1 instanceof ClassCollator)
                return true;
            return false;
        }

        public int compare(final java.lang.Object a, final java.lang.Object b) {
            return collator.compare(a.toString(), b.toString());
        }
    }

}


/*
* <<Log>>
*  6    Gandalf   1.5         2/7/00   Petr Kuzel      More service details
*  5    Gandalf   1.4         2/2/00   Petr Kuzel      Jini module upon 1.1alpha
*  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  2    Gandalf   1.1         6/5/99   Martin Ryzl     better jini
*  1    Gandalf   1.0         6/4/99   Martin Ryzl     
* $ 
*/ 

