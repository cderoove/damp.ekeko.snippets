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

import java.io.IOException;
import java.util.*;

import org.openide.util.SharedClassObject;

/**
 *
 * @author  mryzl
 */

abstract public class AbstractSettingsSet extends Object implements SettingsSet {

    protected Set set = new HashSet();

    /** Test whether the set contains given shared class object.
    * @param obj object
    * @return true if the set contains the object
    */
    public boolean contains(SharedClassObject obj) {
        return set.contains(obj);
    }

    /** Add shared object to the set.
    * @param obj - object to add.
    */
    public void add(SharedClassObject obj) {
        set.add(obj);
    }

    /** Remove shared object from the set.
    * @param obj - object to remove.
    */
    public void remove(SharedClassObject obj) {
        set.remove(obj);
    }

    /** Clear SettingsSet.
    */
    public void clear() {
        set = new HashSet();
    }

    /** Get all options of the set.
     */
    public java.util.Collection getObjects() {
        return set;
    }

}

/*
* Log
*  4    Gandalf   1.3         1/11/00  Martin Ryzl     clear() added
*  3    Gandalf   1.2         1/7/00   Martin Ryzl     
*  2    Gandalf   1.1         12/22/99 Martin Ryzl     
*  1    Gandalf   1.0         12/20/99 Martin Ryzl     
* $ 
*/ 
