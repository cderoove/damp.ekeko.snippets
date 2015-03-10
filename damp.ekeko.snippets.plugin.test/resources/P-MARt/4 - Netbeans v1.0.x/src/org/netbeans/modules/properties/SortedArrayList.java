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


/** An ArrayList whose elements are sorted by a comparator. The elements are unique (this is a set).
*  Update operations form the underlying arraylist should not be used !
*  Uses binary search algorithms.
*
* @author Petr Jiricka
*/
public class SortedArrayList extends ArrayList {

    /** Comparator we are using */
    Comparator cmp;

    static final long serialVersionUID =3124433776536521278L;
    /** Creates a new sorted arraylist with the given comparator. */
    SortedArrayList(Comparator cmp) {
        super();
        this.cmp = cmp;
        if (cmp == null)
            throw new IllegalArgumentException();
    }

    /** Adds the element
    * @return true if the structure did not contain the element 
    */                                        
    public boolean setAdd(Object element) {
        int pos = findPosition(element);
        if (pos == size()) {
            add(element);
            return true;
        }
        else
            if (cmp.compare(element, get(pos)) == 0)
                return false;
            else {
                add(pos, element);
                return true;
            }
    }

    /** Removes the element.
    * @return true if the structure contained the element
    */
    public boolean setRemove(Object element) {
        int pos = findPosition(element);
        if (pos == size())
            return false;
        if (cmp.compare(element, get(pos)) == 0) {
            remove(pos);
            return true;
        }
        else
            return false;
    }

    /** Return the position on which object element is stored or -1 if the structure
    * does not contain it.
    */
    public int setContains(Object element) {
        int pos = findPosition(element);
        if (pos == size())
            return -1;
        if (cmp.compare(element, get(pos)) == 0)
            return pos;
        else
            return -1;
    }

    /** Finds a position where the object is or where it should be inserted */
    private int findPosition(Object element) {
        int lower = 0;
        int upper = size();
        if (upper == 0)
            return 0;

        int shoot;
        int comp;
        while (upper - lower > 1) {
            shoot = lower + ((upper - lower) / 2); // shoot is always between lower and upper
            comp = cmp.compare(element, get(shoot));
            if (comp == 0)
                return shoot;
            else
                if (comp < 0)
                    upper = shoot;
                else
                    lower = shoot;
        }
        // now upper == lower + 1,
        comp = cmp.compare(element, get(lower));
        if (comp <= 0)
            return lower;
        else
            return upper;
    }

    /** Returns true if all items of both lists are the same.
    */
    public boolean equals(Object another) {
        if (another == null)
            return false;
        if (!(another instanceof SortedArrayList))
            return false;

        SortedArrayList sal = (SortedArrayList)another;

        if (size() != sal.size())
            return false;

        for (int i = 0; i<size(); i++) {
            if ((get(i) == null) && (sal.get(i) == null))
                ;
            else
                if (!get(i).equals(sal.get(i)))
                    return false;
        }

        return true;
    }
}

/*
 * <<Log>>
 */
