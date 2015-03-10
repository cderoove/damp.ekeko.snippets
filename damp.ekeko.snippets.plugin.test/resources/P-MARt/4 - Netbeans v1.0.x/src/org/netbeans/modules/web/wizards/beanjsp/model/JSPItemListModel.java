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

package  org.netbeans.modules.web.wizards.beanjsp.model;

import java.util.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.table.*;
import javax.swing.event.*;

import org.netbeans.modules.web.wizards.beanjsp.util.*;
import org.netbeans.modules.web.util.*;


public class JSPItemListModel extends javax.swing.AbstractListModel {

    JSPVector items;
    Map keys;

    public JSPItemListModel() {
        this(new JSPVector());
    }

    public JSPItemListModel(JSPVector items) {
        this.items = items;
    }

    //// abstarct list model

    public int getSize() { return items.size(); }
    public Object getElementAt(int index) { return items.get(index); }

    //// BEAN METHOD MODEL methos

    public Object get(int idx) {
        return items.get(idx);
    }

    public void add(Object item) {
        items.add(item);
        int idx = items.size()-1;
        fireIntervalAdded(this, idx, idx);
    }

    public Object remove(int idx) {
        Object item = (Object) items.remove(idx);
        fireIntervalRemoved(this, idx, idx);
        return item;
    }

    public int moveUp(int idx) {
        int newIdx = items.moveUp(idx);
        this.fireContentsChanged(this, idx, idx);
        return newIdx;
    }

    public int moveDown(int idx) {
        int newIdx = items.moveDown(idx);
        this.fireContentsChanged(this, idx, idx);
        return newIdx;
    }

    public void removeAll() {
        int idx = items.size()-1;
        items.removeAllElements();
        if(idx >= 0)
            fireIntervalRemoved(this, 0, idx);
    }

    public void addItems(JSPVector newItems) {
        if(newItems.size() <= 0)
            return;
        int idx = items.size()-1;
        items.addAll(newItems);
        if(idx < 0 )
            idx = 0;
        fireIntervalAdded(this, idx, idx+newItems.size());
    }

public JSPVector getItems() { return items; }

    //// helper functions to search and remove items

    public boolean removeAll(Object key) {
        // Debug.println("Removing items from Item List");
        boolean itemsRemoved = items.removeAllByKey(key);

        if(itemsRemoved) {
            int idx = items.size()-1;
            this.fireContentsChanged(this, 0,idx);
        }

        return itemsRemoved;
    }

}



