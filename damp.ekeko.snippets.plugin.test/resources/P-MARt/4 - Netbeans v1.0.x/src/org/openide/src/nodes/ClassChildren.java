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

package org.openide.src.nodes;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;
import java.util.Comparator;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.HashMap;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.cookies.FilterCookie;
import org.openide.util.WeakListener;
import org.openide.src.*;

/** Normal implementation of children list for a class element node.
* Semantics are similar to those of {@link SourceChildren}.
* @author Dafe Simonek, Jan Jancura
*/
public class ClassChildren extends Children.Keys implements FilterCookie {

    /** Support for PACKAGE modifier */
    private static int                  PPP_MASK = SourceElementFilter.PUBLIC +
            SourceElementFilter.PRIVATE +
            SourceElementFilter.PROTECTED;
    /** Converts property names to filter. 
     * @associates Integer*/
    protected static HashMap              propToFilter;

    /** For sorting groups of elements. */
    private static Comparator           comparator = new Comparator () {
                public int compare (Object o1, Object o2) {
                    if (o1 instanceof MemberElement)
                        if (o2 instanceof MemberElement)
                            return ((MemberElement) o1).getName ().getName ().compareToIgnoreCase (
                                       ((MemberElement) o2).getName ().getName ()
                                   );
                        else
                            return -1;
                    else
                        if (o2 instanceof MemberElement)
                            return 1;
                        else
                            return 0;
                }
            };

    static {
        propToFilter = new HashMap ();
        propToFilter.put (ElementProperties.PROP_CLASSES, new Integer (ClassElementFilter.CLASS | ClassElementFilter.INTERFACE));
        propToFilter.put (ElementProperties.PROP_METHODS, new Integer (ClassElementFilter.METHOD));
        propToFilter.put (ElementProperties.PROP_FIELDS, new Integer (ClassElementFilter.FIELD));
        propToFilter.put (ElementProperties.PROP_CONSTRUCTORS, new Integer (ClassElementFilter.CONSTRUCTOR));
        propToFilter.put (ElementProperties.PROP_INITIALIZERS, new Integer (ClassElementFilter.CONSTRUCTOR));
    }

    /** The class element whose subelements are represented. */
    protected ClassElement              element;
    /** Filter for elements, or <code>null</code> to disable. */
    protected ClassElementFilter        filter;
    /** Factory for creating new child nodes. */
    protected ElementNodeFactory        factory;
    /** Weak listener to the element and filter changes */
    private PropertyChangeListener      wPropL;
    /** Listener to the element and filter changes. This reference must
    * be kept to prevent the listener from finalizing when we are alive */
    private ElementListener             propL;
    /** Central memory of mankind is used when some elements are changed */
    protected Collection[]              cpl;
    /** Flag saying whether we have our nodes initialized */
    private boolean                     nodesInited = false;


    // init ................................................................................

    /** Create class children with the default factory.
    * The children are initially unfiltered.
    * @param element attached class element (non-<code>null</code>)
    */
    public ClassChildren (final ClassElement element) {
        this(DefaultFactory.READ_WRITE, element);
    }

    /** Create class children.
    * The children are initially unfiltered.
    * @param factory the factory to use to create new children
    * @param element attached class element (non-<code>null</code>)
    */
    public ClassChildren (final ElementNodeFactory factory,
                          final ClassElement element) {
        super();
        this.element = element;
        this.factory = factory;
        this.filter = null;
    }


    /********** Implementation of filter cookie **********/

    /* @return The class of currently asociated filter or null
    * if no filter is asociated with these children.
    */
    public Class getFilterClass () {
        return ClassElementFilter.class;
    }

    /* @return The filter currently asociated with these children
    */
    public Object getFilter () {
        return filter;
    }

    /* Sets new filter for these children.
    * @param filter New filter. Null == disable filtering.
    */
    public void setFilter (final Object filter) {
        if (!(filter instanceof ClassElementFilter))
            throw new IllegalArgumentException();

        this.filter = (ClassElementFilter)filter;
        // change element nodes according to the new filter
        if (nodesInited)
            refreshAllKeys ();
    }


    // Children implementation ..............................................................

    /* Overrides initNodes to run the preparation task of the
    * source element, call refreshKeys and start to
    * listen to the changes in the element too. */
    protected void addNotify () {
        refreshAllKeys ();
        // listen to the changes in the class element
        if (wPropL == null) {
            propL = new ElementListener();
            wPropL = WeakListener.propertyChange (propL, element);
        }
        element.addPropertyChangeListener (wPropL);
        nodesInited = true;
    }

    protected void removeNotify () {
        setKeys (java.util.Collections.EMPTY_SET);
        nodesInited = false;
    }

    /* Creates node for given key.
    * The node is created using node factory.
    */
    protected Node[] createNodes (final Object key) {
        if (key instanceof MethodElement) {
            return new Node[] { factory.createMethodNode((MethodElement)key) };
        }
        if (key instanceof FieldElement) {
            return new Node[] { factory.createFieldNode((FieldElement)key) };
        }
        if (key instanceof ConstructorElement) {
            return new Node[] { factory.createConstructorNode((ConstructorElement)key) };
        }
        if (key instanceof ClassElement) {
            return new Node[] { factory.createClassNode((ClassElement)key) };
        }
        if (key instanceof InitializerElement) {
            return new Node[] { factory.createInitializerNode((InitializerElement)key) };
        }
        // ?? unknown type
        return new Node[0];
    }


    /************** utility methods ************/

    /** Updates all the keys (elements) according to the current filter &
    * ordering.
    */
    protected void refreshAllKeys () {
        cpl = new Collection [getOrder ().length];
        refreshKeys (ClassElementFilter.ALL);
    }

    /** Updates all the keys with given filter.
    */
    protected void refreshKeys (int filter) {
        int[] order = getOrder ();
        LinkedList keys = new LinkedList();
        // build ordered and filtered keys for the subelements
        for (int i = 0; i < order.length; i++) {
            if (((order[i] & filter) != 0) || (cpl [i] == null))
                keys.addAll (cpl [i] = getKeysOfType (order[i]));
            else
                keys.addAll (cpl [i]);
        }
        // set new keys
        setKeys(keys);
    }

    /** Filters and returns the keys of specified type.
    */
    protected Collection getKeysOfType (final int elementType) {
        LinkedList keys = new LinkedList();
        if ((elementType & ClassElementFilter.EXTENDS) != 0) {
            keys.add (element.getSuperclass ());
        }
        if ((elementType & ClassElementFilter.IMPLEMENTS) != 0) {
            keys.addAll (Arrays.asList (element.getInterfaces ()));
        }
        if ((elementType & ClassElementFilter.FIELD) != 0) {
            filterModifiers (element.getFields (), keys);
        }
        if ((elementType & ClassElementFilter.CONSTRUCTOR) != 0) {
            filterModifiers (element.getConstructors (), keys);
            keys.addAll (Arrays.asList (element.getInitializers ()));
        }
        if ((elementType & ClassElementFilter.METHOD) != 0) {
            filterModifiers (element.getMethods (), keys);
        }
        if ((elementType & (ClassElementFilter.CLASS + ClassElementFilter.INTERFACE)) != 0) {
            filterClassModifiers (element.getClasses (), keys, elementType);
        }
        if ((filter == null) || filter.isSorted ())
            Collections.sort (keys, comparator);
        return keys;
    }

    /** Returns order form filter.
    */
    protected int[] getOrder () {
        return (filter == null || (filter.getOrder() == null))
               ? ClassElementFilter.DEFAULT_ORDER : filter.getOrder();
    }

    /** Returns modifier filter form filter.
    */
    private int getModifierFilter () {
        if (filter == null) return ClassElementFilter.ALL_MODIFIERS;
        return filter.getModifiers ();
    }

    /** Filters MemberElements for modifiers, and adds them to the given collection.
    */
    private void filterModifiers (MemberElement[] elements, Collection keys) {
        int ff = getModifierFilter ();
        int i, k = elements.length;
        for (i = 0; i < k; i ++) {
            int f = elements [i].getModifiers ();
            if ((f & PPP_MASK) == 0) f += ClassElementFilter.PACKAGE;
            if ((f & ff) != 0) keys.add (elements [i]);
        }
    }

    /** Filters ClassElements for their type, and adds them to the given collection.
    */
    private void filterClassModifiers (ClassElement[] elements, Collection keys, int filter) {
        int ff = getModifierFilter ();
        int i, k = elements.length;
        for (i = 0; i < k; i ++) {
            int f = elements [i].getModifiers ();
            if ((f & PPP_MASK) == 0) f += ClassElementFilter.PACKAGE;
            if ((f & ff) == 0) continue;
            if (elements [i].isClass ()) {
                if ((filter & ClassElementFilter.CLASS) != 0) keys.add (elements [i]);
            } else
                if ((filter & ClassElementFilter.INTERFACE) != 0) keys.add (elements [i]);
        }
    }


    // innerclasses ...........................................................................

    /** The listener for listening to the property changes in the filter.
    */
    private final class ElementListener implements PropertyChangeListener {
        /** This method is called when the change of properties occurs in the element.
        * PENDING - (for Hanz - should be implemented better, change only the
        * keys which belong to the changed property).
        * -> YES MY LORD! ANOTHER WISH?
        */
        public void propertyChange (PropertyChangeEvent evt) {
            Integer i = (Integer) propToFilter.get (evt.getPropertyName ());
            if (i != null) refreshKeys (i.intValue ());
        }
    } // end of ElementListener inner class
}

/*
* Log
*  24   src-jtulach1.23        11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  23   src-jtulach1.22        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  22   src-jtulach1.21        9/13/99  Petr Hamernik   fixed bug in changes of 
*       interfaces
*  21   src-jtulach1.20        7/19/99  Jan Jancura     
*  20   src-jtulach1.19        7/1/99   Jan Jancura     Support for sorting
*  19   src-jtulach1.18        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  18   src-jtulach1.17        5/16/99  Jaroslav Tulach System.out commented
*  17   src-jtulach1.16        5/15/99  Jaroslav Tulach Changes in hierarchy to 
*       work better in DataObjectFilter
*  16   src-jtulach1.15        4/22/99  Jaroslav Tulach The previous version was 
*       good.
*  15   src-jtulach1.14        4/21/99  Jan Jancura     Rolled back - bug in 
*       parsing
*  14   src-jtulach1.13        4/21/99  Jan Jancura     Optimalization in 
*       Children.Keys applied
*  13   src-jtulach1.12        4/16/99  Jaroslav Tulach Changes in children.
*  12   src-jtulach1.11        4/16/99  Jan Jancura     
*  11   src-jtulach1.10        4/6/99   Petr Hamernik   order of children changed
*       (innerclasses are last)
*  10   src-jtulach1.9         4/2/99   Jesse Glick     [JavaDoc]
*  9    src-jtulach1.8         4/2/99   Jan Jancura     ObjectBrowser Support
*  8    src-jtulach1.7         3/16/99  Petr Hamernik   renaming static fields
*  7    src-jtulach1.6         3/15/99  Petr Hamernik   
*  6    src-jtulach1.5         3/10/99  Petr Hamernik   small bug-fix
*  5    src-jtulach1.4         2/9/99   David Simonek   
*  4    src-jtulach1.3         2/9/99   David Simonek   little fixes - init in 
*       separate thread
*  3    src-jtulach1.2         2/3/99   David Simonek   
*  2    src-jtulach1.1         2/3/99   David Simonek   getting it to work 
*       properly
*  1    src-jtulach1.0         1/29/99  David Simonek   
* $
*/
