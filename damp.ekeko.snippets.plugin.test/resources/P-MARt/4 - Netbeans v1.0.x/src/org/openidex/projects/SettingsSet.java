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
import org.openide.util.SharedClassObject;

/**
 *
 * @author  mryzl
 */

public interface SettingsSet {

    /** Test whether the set contains given shared class object.
    * @param obj object
    * @return true if the set contains the object
    */
    public boolean contains(SharedClassObject obj);

    /** Get all options of the set.
    * @return Collection of SharedClassObjects
    */
    public java.util.Collection getObjects();

    /** Add shared object to the set.
    * @param obj - object to add.
    */
    public void add(SharedClassObject obj) ;

    /** Remove shared object from the set.
    * @param obj - object to remove.
    */
    public void remove(SharedClassObject obj);

    /** Clear SettingsSet.
    */
    public void clear();

    /** Write options to storage device. Writing of some options
    * can be prohibited by option processor.
    *
    * @param processor - processor that controls writing of an option
    * @return an array of written objects
    */
    public void write(OptionProcessor processor) throws IOException;

    /** Read options from storage device. Reading of some options
    * can be prohibited by option processor.
    *
    * @param processor - processor that controls writing of an option
    * @return an array of read objects
    */
    public void read(OptionProcessor processor) throws IOException;
}

/*
* Log
*  3    Gandalf   1.2         1/11/00  Martin Ryzl     clear() added
*  2    Gandalf   1.1         12/22/99 Martin Ryzl     
*  1    Gandalf   1.0         12/20/99 Martin Ryzl     
* $ 
*/ 
