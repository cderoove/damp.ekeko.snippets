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

package org.netbeans.modules.properties;

import java.util.*;


/** Data structure which is a cross between Map and ArrayList.
*  Update operations form the underlying map should not be used !
*
* @author Petr Jiricka
*/
public class ArrayMapList extends HashMap {

    /** list holding keys in the correct order, the underlying HashMap holds records of type (key, value) 
     * @associates Object*/
    private ArrayList list;

    static final long serialVersionUID =9098307358746080344L;
    public ArrayMapList() {
        super();
        list = new ArrayList();
    }

    /** Add an object with the given index and key */
    public void add(int index, Object key, Object element) {
        super.put(key, element);
        list.add(index, key);
    }

    /** Add an object with the given key to the end of the list*/
    public boolean add(Object key, Object element) {
        super.put(key, element);
        list.add(key);
        return true;
    }

    /** Clears the list */
    public void clear() {
        super.clear();
        list.clear();
    }

    /** Removes an item from the list */
    public Object remove(int index) {
        list.remove(index);
        return super.remove(list.get(index));
    }

    /** Removes an item from the list (sequential op.) */
    public Object remove(Object key) {
        list.remove(list.indexOf(key));
        return super.remove(key);
    }

    /** Changes the key for the given element.
    * @return the element represented by the key or null if not found.
    */
    public Object changeKey(Object oldKey, Object newKey) {
        int index = list.indexOf(oldKey);
        if (index == -1)
            return null;
        Object elem = super.remove(oldKey);
        if (elem == null)
            return null;
        else {
            super.put(newKey, elem);
            list.set(index, newKey);
            return elem;
        }
    }

    /** For calls from the iterator. */
    private Object superRemove(Object key) {
        return super.remove(key);
    }

    /** Sets the object on the given position to key and element */
    public Object set(int index, Object key, Object element) {
        Object obj = super.remove(list.get(index));
        super.put(key, element);
        list.set(index, key);
        return obj;
    }

    /** Retrieves an object with the given index */
    public Object get(int index) {
        return super.get(list.get(index));
    }

    /** Retrieves the index of the given key
    * @return index on which key is located, or -1 if not contained.
    */
    public int indexOf(Object key) {
        return list.indexOf(key);
    }

    /** Returns an iterator. Items are sorted by their order in the list. */
    public Iterator iterator() {
        return new Iterator() {
                   // iterator which relies on the list's iterator
                   private Iterator listIt = list.iterator();
                   private Object lastKey;

                   public boolean hasNext() {
                       return listIt.hasNext();
                   }

                   public Object next() {
                       lastKey = listIt.next();
                       return get(lastKey);
                   }

                   public void remove() {
                       if (lastKey == null)
                           throw new IllegalStateException();
                       listIt.remove();
                       ArrayMapList.this.superRemove(lastKey);
                   }

               };
    }

    // can also use get(Object key)
    // PENDING many other methods should throw NotImplementedException
}

/*
 * <<Log>>
 */
