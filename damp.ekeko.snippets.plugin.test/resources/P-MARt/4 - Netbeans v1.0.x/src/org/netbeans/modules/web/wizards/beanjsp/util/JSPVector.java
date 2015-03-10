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

package  org.netbeans.modules.web.wizards.beanjsp.util;

import org.netbeans.modules.web.util.*;

import javax.swing.event.*;
import javax.swing.*;
import java.util.*;


public class JSPVector extends Vector {

    public JSPVector() { super();}
    public JSPVector (Collection collection) { super(collection);}

    public int moveUp(int idx) {
        if(idx == 0 )
            return idx;
        if(this.size() > 1  && idx < this.size())
            swap(idx,--idx);
        return idx;
    }

    public int moveDown(int idx) {
        if(idx == this.size()-1)
            return idx;
        if(this.size() > 1  && idx < this.size())
            swap(idx,++idx);
        return idx;
    }

    private void swap(int i, int j) {
        Object tmp = this.get(i);
        this.set(i, this.get(j));
        this.set(j, tmp);
    }


    public boolean removeAllByKey(Object key) {

        // Debug.println("Size of Vector :"+this.size());

        Vector removeVec = new Vector();

        Iterator itemIterator = this.iterator();
        boolean itemsRemoved = false;
        for(;itemIterator.hasNext();){
            Object item = itemIterator.next();
            if(item instanceof JSPItem) {
                if(((JSPItem)item).hasKey(key)) {
                    // Debug.println("Removing the item : "+ item );
                    // itemIterator.remove();
                    removeVec.add(item);
                    itemsRemoved = true;
                }
            }else {
                // Debug.println("Not a JSPItem");
            }
        }

        // Debug.println("Remove Vec size "+removeVec.size());
        super.removeAll((Collection)removeVec);

        // Debug.println("After removeall by key, size of vec "+this.size());
        return itemsRemoved;
    }


    public static int moveUpListItem(DefaultListModel listModel, int idx) {
        if(idx == 0 )
            return idx;
        if(listModel.size() > 1  && idx < listModel.size())
            swapListItem(listModel,idx,--idx);
        return idx;
    }

    public static int moveDownListItem(DefaultListModel listModel, int idx) {
        if(idx == listModel.size()-1)
            return idx;
        if(listModel.size() > 1  && idx < listModel.size())
            swapListItem(listModel,idx,++idx);
        return idx;
    }

    private static void swapListItem(DefaultListModel listModel,int i, int j) {
        Object tmp = listModel.get(i);
        listModel.set(i, listModel.get(j));
        listModel.set(j, tmp);
    }


}


