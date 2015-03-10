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
import java.util.Collection;
import java.util.LinkedList;
import java.util.Arrays;
import java.util.List;

import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.cookies.FilterCookie;
import org.openide.src.ElementProperties;
import org.openide.src.SourceElement;
import org.openide.src.ClassElement;
import org.openide.src.Identifier;
import org.openide.util.WeakListener;

/** Normal implementation of children for source element nodes.
* <P>
* Ordering and filtering of the children can be customized
* using {@link SourceElementFilter}.
* {@link FilterCookie} is implemented to provide a means
* for user customization of the filter.
* <p>The child list listens to changes in the source element, as well as the filter, and
* automatically updates itself as appropriate.
* <p>A child factory can be used to cause the children list to create
* non-{@link DefaultFactory default} child nodes, if desired, both at the time of the creation
* of the children list, and when new children are added.
* <p>The children list may be unattached to any source element temporarily,
* in which case it will have no children (except possibly an error indicator).
*
* @author Dafe Simonek, Jan Jancura
*/
public class SourceChildren extends Children.Keys implements FilterCookie {

    /** The key describing state of source element */
    static final Object                   NOT_KEY = new Object();
    /** The key describing state of source element */
    static final Object                   ERROR_KEY = new Object();
    /** PACKAGE modifier support */
    private static int                    PPP_MASK = SourceElementFilter.PUBLIC +
            SourceElementFilter.PRIVATE +
            SourceElementFilter.PROTECTED;

    /** The element whose subelements are represented. */
    protected SourceElement               element;
    /** Filter for elements. Can be <code>null</code>, in which case
    * modifier filtering is disabled, and ordering may be reset to the default order. */
    protected SourceElementFilter         filter;
    /** Factory for obtaining class nodes. */
    protected ElementNodeFactory          factory;
    /** Weak listener to the element and filter changes */
    private PropertyChangeListener        wPropL;
    /** Listener to the element and filter changes. This reference must
    * be kept to prevent the listener from finalizing when we are alive */
    private ElementListener               propL;
    /** Flag saying whether we have our nodes initialized */
    private boolean                       nodesInited = false;


    // init ................................................................................

    /** Create a children list with the default factory and no attached source element.
    */
    public SourceChildren () {
        this (DefaultFactory.READ_WRITE, null);
    }

    /** Create a children list with the default factory.
    * @param element source element to attach to, or <code>null</code>
    */
    public SourceChildren (final SourceElement element) {
        this(DefaultFactory.READ_WRITE, element);
    }

    /** Create a children list with no attached source element.
    * @param factory a factory for creating children
    */
    public SourceChildren (final ElementNodeFactory factory) {
        this(factory, null);
    }

    /** Create a children list.
    * @param factory a factory for creating children
    * @param element source element to attach to, or <code>null</code>
    */
    public SourceChildren (final ElementNodeFactory factory,
                           final SourceElement element) {
        super();
        this.element = element;
        this.factory = factory;
        this.filter = new SourceElementFilter ();
    }


    // FilterCookie implementation .............................................................

    /* @return The class of currently asociated filter or null
    * if no filter is asociated with these children.
    */
    public Class getFilterClass () {
        return SourceElementFilter.class;
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
        if (!(filter instanceof SourceElementFilter))
            throw new IllegalArgumentException();

        this.filter = (SourceElementFilter)filter;
        // change element nodes according to the new filter
        if (nodesInited)
            refreshKeys ();
    }


    // Children implementation ..............................................................

    /* Overrides initNodes to run the preparation task of the
    * source element, call refreshKeys and start to
    * listen to the changes in the element too. */
    protected void addNotify () {
        if (element != null) {
            // listen to the source element property changes
            if (wPropL == null) {
                propL = new ElementListener();
                wPropL = WeakListener.propertyChange(propL, element);
            }
            element.addPropertyChangeListener (wPropL);
            element.prepare();
        }
        refreshKeys ();
        nodesInited = true;
    }

    protected void removeNotify () {
        setKeys (java.util.Collections.EMPTY_SET);
        nodesInited = false;
    }

    /* Create nodes for given key.
    * The node is created using node factory.
    */
    protected Node[] createNodes (final Object key) {
        // find out the type of the key and create appropriate node
        if (key instanceof ClassElement)
            return new Node[] { factory.createClassNode((ClassElement)key) };
        if (NOT_KEY.equals(key))
            return new Node[] { factory.createWaitNode() };
        // never should get here
        return new Node[] { factory.createErrorNode() };
    }


    // main public methods ..................................................................

    /** Get the currently attached source element.
    * @return the element, or <code>null</code> if unattached
    */
    public SourceElement getElement () {
        return element;
    }

    /** Set a new source element to get information about children from.
    * @param element the new element, or <code>null</code> to detach
    */
    public void setElement (final SourceElement element) {
        if (this.element != null) {
            this.element.removePropertyChangeListener(wPropL);
        }
        this.element = element;
        if (this.element != null) {
            if (wPropL == null) {
                propL = new ElementListener();
                wPropL = WeakListener.propertyChange(propL, this.element);
            }
            this.element.addPropertyChangeListener(wPropL);
        }
        // change element nodes according to the new element
        if (nodesInited) {
            if (this.element != null) this.element.prepare();
            refreshKeys ();
        }
    }

    // other methods ..........................................................................

    /** Refreshes the keys according to the current state of the element and
    * filter etc.
    * (This method is also called when the change of properties occurs either
    * in the filter or in the element)
    * PENDING - (for Hanz - should be implemented better, change only the
    * keys which belong to the changed property).
    * @param evt the event describing changed property (or null to signalize
    * that all keys should be refreshed)
    */
    private void refreshKeys () {
        int status = (element == null) ? SourceElement.STATUS_ERROR
                     : element.getStatus();
        switch (status) {
        case SourceElement.STATUS_NOT:
            setKeys(new Object[] { NOT_KEY });

            // start parsing
            element.prepare ();

            break;
        case SourceElement.STATUS_ERROR:
            setKeys(new Object[] { ERROR_KEY });
            break;
        case SourceElement.STATUS_PARTIAL:
        case SourceElement.STATUS_OK:
            refreshAllKeys();
            break;
        }
    }

    /** Updates all the keys (elements) according to the current
    * filter and ordering */
    private void refreshAllKeys () {
        int[] order = (filter == null || (filter.getOrder() == null))
                      ? SourceElementFilter.DEFAULT_ORDER : filter.getOrder();

        LinkedList keys = new LinkedList();
        // build ordered and filtered keys for the subelements
        for (int i = 0; i < order.length; i++)
            addKeysOfType(keys, order[i]);

        // set new keys
        setKeys(keys);
    }

    /** Filters and adds the keys of specified type to the given
    * key collection.
    */
    private void addKeysOfType (Collection keys, final int elementType) {
        if (elementType == SourceElementFilter.IMPORT) {
            // PENDING imports are not solved yet...maybe ImportsChildren???
            //keys.addAll(Arrays.asList(element.getImports()));
            return;
        } else {
            List cls;
            if ((filter != null) && filter.isAllClasses()) {
                cls = Arrays.asList (element.getAllClasses ());
            } else {
                cls = Arrays.asList (element.getClasses ());
            }
            int i = cls.size () - 1;
            for (; i >= 0 ; i--) {
                ClassElement classElement = (ClassElement)cls.get (i);
                int modifiers = classElement.getModifiers ();
                if ((modifiers & PPP_MASK) == 0) modifiers += SourceElementFilter.PACKAGE;
                if ((filter.getModifiers () & modifiers) == 0) continue;
                if (classElement.isClass ()) {
                    if ((elementType & SourceElementFilter.CLASS) != 0) keys.add (classElement);
                } else
                    if ((elementType & SourceElementFilter.INTERFACE) != 0) keys.add (classElement);
            }
        }
    }


    // innerclasses ...........................................................................

    /** The listener for listening to the property changes in the filter.
    */
    private final class ElementListener implements PropertyChangeListener {
        public void propertyChange (PropertyChangeEvent evt) {
            boolean refresh = ElementProperties.PROP_CLASSES.equals(evt.getPropertyName());
            if (!refresh && ElementProperties.PROP_STATUS.equals(evt.getPropertyName())) {
                Integer val = (Integer) evt.getNewValue();
                refresh = ((val == null) || (val.intValue() != SourceElement.STATUS_NOT));
            }
            if (refresh)
                refreshKeys();
        }

    } // end of ElementListener inner class
}

/*
* Log
*  24   src-jtulach1.23        11/5/99  Jaroslav Tulach WeakListener has now 
*       registration methods.
*  23   src-jtulach1.22        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  22   src-jtulach1.21        8/27/99  Petr Hamernik   optimization of 
*       ElementListener events handling
*  21   src-jtulach1.20        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  20   src-jtulach1.19        5/15/99  Jaroslav Tulach 
*  19   src-jtulach1.18        5/15/99  Jaroslav Tulach Changes in hierarchy to 
*       work better in DataObjectFilter
*  18   src-jtulach1.17        5/13/99  Jan Jancura     System.out.println 
*       cleared
*  17   src-jtulach1.16        5/13/99  Jan Jancura     Bug in lazy 
*       initialization
*  16   src-jtulach1.15        4/22/99  Jaroslav Tulach When status NOT_PARSED 
*       starts parsing.
*  15   src-jtulach1.14        4/21/99  Jan Jancura     Rolled back - bug in 
*       parsing
*  14   src-jtulach1.13        4/21/99  Jan Jancura     Optimalization in 
*       Children.Keys applied
*  13   src-jtulach1.12        4/16/99  Jaroslav Tulach Changes in children.
*  12   src-jtulach1.11        4/13/99  Petr Hamernik   bugfix - children stays 
*       in "Please wait..."
*  11   src-jtulach1.10        4/2/99   Jesse Glick     [JavaDoc]
*  10   src-jtulach1.9         4/2/99   Jan Jancura     ObjectBrowser Support
*  9    src-jtulach1.8         3/26/99  Petr Hamernik   small improvements
*  8    src-jtulach1.7         3/16/99  Petr Hamernik   renaming static fields
*  7    src-jtulach1.6         3/15/99  Petr Hamernik   
*  6    src-jtulach1.5         2/11/99  David Simonek   
*  5    src-jtulach1.4         2/10/99  David Simonek   
*  4    src-jtulach1.3         2/9/99   David Simonek   little fixes - init in 
*       separate thread
*  3    src-jtulach1.2         2/3/99   David Simonek   getting it to work 
*       properly
*  2    src-jtulach1.1         2/1/99   David Simonek   
*  1    src-jtulach1.0         1/29/99  David Simonek   
* $
*/
