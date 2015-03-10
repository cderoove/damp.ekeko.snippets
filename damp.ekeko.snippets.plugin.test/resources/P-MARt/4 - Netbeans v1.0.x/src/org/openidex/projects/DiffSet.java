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

/**
 *
 * @author  mryzl
 */

public interface DiffSet {

    /** Add an item.
    * @param name - name of the item
    * @param value - value of the item
    */
    public void add(String name, Object value);

    /** Remove the item.
    * @param name name of the item to remove
    */
    public void remove(String name);

    /** List of added items.
    * @return list of added items.
    */
    public List addedItems();

    /** Added item by name.
    * @param name - name of the item
    * @return value of the item.
    */
    public Object addedItem(String name);


    /** Collection of removed items.
    * @return collection of removed items.
    */
    public Set removedItems();

    /** Test whether the diff set should be applied on fresh settings or not.
    * @return true if it should be applied on fresh settings
    */
    public boolean isClear();

    /** Set clear flag.
    * @param clear 
    */
    public void setClear(boolean clear);

    /** Get order of items.
     * @return Value of property order.
     */
    public List getOrder();

    /** Set an order for items.
     * @param order New value of property order.
     */
    public void setOrder(List order);

    /** Store DiffSet.
    */
    public void store() throws IOException;

    /** Clear DiffSet.
    */
    public void clear();
}

/*
* Log
*  3    Gandalf   1.2         1/11/00  Martin Ryzl     clear() added
*  2    Gandalf   1.1         12/22/99 Martin Ryzl     
*  1    Gandalf   1.0         12/20/99 Martin Ryzl     
* $ 
*/ 
