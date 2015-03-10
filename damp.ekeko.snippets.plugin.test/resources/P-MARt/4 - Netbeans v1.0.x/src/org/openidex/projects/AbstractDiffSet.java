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

public class AbstractDiffSet extends Object implements DiffSet {
    /**
     * @associates String 
     */
    protected List items;

    /**
     * @associates Object 
     */
    protected Map addMap;

    /**
     * @associates String 
     */
    protected Set removeSet;
    protected List order;
    protected boolean clear = true;

    {
        addMap = new HashMap();
        removeSet = new HashSet();
        items = new LinkedList();
        order = new LinkedList();
    }

    /** Creates new DiffSetImpl.
    */
    public AbstractDiffSet() {
    }

    /** Add the item. If it was previously removed, cancel its record in "removed" set.
    * @param name
    * @param value
    */
    public void add(String name, Object value) {
        removeSet.remove(name);
        if (!items.contains(name)) {
            items.add(name);
        }
        addMap.put(name, value);
    }

    /** Remove the item. If it was previously added, cancel its record in "added" set.
    * @param name
    */
    public void remove(String name) {
        items.remove(name);
        addMap.remove(name);
        removeSet.add(name);
    }

    /**
    * @return Map of added items
    */
    public List addedItems() {
        return new ArrayList(items);
    }

    /** Added item by name.
    * @param name - name of the item
    * @return value of the item.
    */
    public Object addedItem(String name) {
        return addMap.get(name);
    }

    /**
    * @return set of removed items
    */
    public Set removedItems() {
        return new HashSet(removeSet);
    }

    /** Test whether the diff set should be applied on fresh settings or not.
     * @return true if it should be applied on fresh settings
     */
    public boolean isClear() {
        return clear;
    }

    /** Set clear flag.
    * @param clear 
    */
    public void setClear(boolean clear) {
        this.clear = clear;
    }

    /** Get order of items.
     * @return Value of property order.
     */
    public List getOrder() {
        return order;
    }

    /** Set an order for items.
     * @param order New value of property order.
     */
    public void setOrder(List order) {
        this.order = order;
    }

    /** Store DiffSet.
     */
    public void store() throws IOException {
    }

    /** Clear DiffSet.
    */
    public void clear() {
        addMap = new HashMap();
        removeSet = new HashSet();
        items = new LinkedList();
        order = new LinkedList();
    }
}

/*
* Log
*  3    Gandalf   1.2         1/11/00  Martin Ryzl     clear() added
*  2    Gandalf   1.1         12/22/99 Martin Ryzl     
*  1    Gandalf   1.0         12/20/99 Martin Ryzl     
* $ 
*/ 
