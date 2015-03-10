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

package org.openidex.projects;

import org.openide.util.SharedClassObject;

/**
 *
 * @author  mryzl
 */

public interface OptionProcessor {

    /** Say false to all options.
    */
    public static final OptionProcessor EMPTY = new Empty();

    /** Say false to all options.
    */
    public static class Empty implements OptionProcessor {
        public boolean canProcess(SharedClassObject sco) {
            return false;
        }
    }

    /** Say true to all options.
    */
    public static final OptionProcessor ALL = new All();

    /** Say true to all options.
    */
    public static class All implements OptionProcessor {
        public boolean canProcess(SharedClassObject sco) {
            return true;
        }
    }

    /** Set that implements OptionProcessor. Say false to all options in the set.
    */
    public static class Set extends java.util.HashSet implements OptionProcessor {
        public boolean canProcess(SharedClassObject sco) {
            return !contains(sco);
        }
    }

    /**
    */
    public boolean canProcess(SharedClassObject sco);

}

/*
* Log
*  4    Gandalf   1.3         1/13/00  Martin Ryzl     
*  3    Gandalf   1.2         1/3/00   Martin Ryzl     
*  2    Gandalf   1.1         1/3/00   Martin Ryzl     problems with compilation
*       under jdk version < 1.3 removed
*  1    Gandalf   1.0         12/20/99 Martin Ryzl     
* $ 
*/ 
