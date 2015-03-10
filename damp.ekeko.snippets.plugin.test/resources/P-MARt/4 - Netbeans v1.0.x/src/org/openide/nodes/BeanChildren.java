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

package org.openide.nodes;

import java.lang.ref.WeakReference;
import java.beans.beancontext.*;
import java.beans.IntrospectionException;
import java.util.Map;
import java.util.HashMap;
import java.util.Arrays;

/** Class that represents bean children of a JavaBeans context.
* It listens on the bean context changes and creates nodes for
* child beans. By default {@link BeanNode}s are created for all
* child beans, but this behaviour can be changed by
* providing a different factory to the constructor.
*
* @author Jaroslav Tulach
*/
public class BeanChildren extends Children.Map {
    /** default factory for creation of children */
    private static final Factory DEFAULT_FACTORY = new BeanFactory ();

    /** bean context to work on */
    private BeanContext bean;

    /** factory for creation of subnodes */
    private Factory factory;

    /** context listener */
    private ContextL contextL;

    /** Create {@link BeanNode} children based on a Bean context.
    * @param bean the context
    */
    public BeanChildren(BeanContext bean) {
        this (bean, DEFAULT_FACTORY);
    }

    /** Create children based on a Bean context.
    * @param bean the context
    * @param factory a factory to use for creation of child nodes
    */
    public BeanChildren (BeanContext bean, Factory factory) {
        this.bean = bean;
        this.factory = factory;
    }

    /** Helper method. Converts array of beans to map from
    * the (beans, Nodes)
    * @param array array of beans
    * @return map (Object, Node)
    */
    private java.util.Map createMap (Object[] array) {
        HashMap map = new HashMap ();
        for (int i = 0; i < array.length; i++) {
            try {
                map.put (array[i], factory.createNode (array[i]));
            } catch (IntrospectionException ex) {
                // ignore the exception
            }
        }
        return map;
    }

    /* Initializes children.
    *
    * @return map (Object, Node)
    */
    protected java.util.Map initMap () {
        // attaches a listener to the bean
        contextL = new ContextL (this);
        bean.addBeanContextMembershipListener (contextL);

        // test if there is a child
        //if (bean.size () == 0) return null;

        return createMap (bean.toArray ());
    }

    /** Cease listening to changes in the bean context membership.
    */
    protected void finalize () {
        if (contextL != null)
            bean.removeBeanContextMembershipListener (contextL);
    }

    /** Controls which nodes
    * are created for a child bean.
    * @see BeanChildren#BeanChildren(BeanContext, BeanChildren.Factory)
    */
    public static interface Factory {
        /** Create a node for a child bean.
        * @param bean the bean
        * @return the node for the bean
        * @exception IntrospectionException if the node cannot be created
        */
        public Node createNode (Object bean) throws IntrospectionException;
    }

    /** Default factory. Creates BeanNode for each bean
    */
    private static class BeanFactory extends Object implements Factory {
        /** @return bean node */
        public Node createNode (Object bean) throws IntrospectionException {
            return new BeanNode (bean);
        }
    }

    /** Context listener.
    */
    private static final class ContextL implements BeanContextMembershipListener {
        /** weak reference to the BeanChildren object */
        private WeakReference ref;

        /** Constructor */
        ContextL (BeanChildren bc) {
            ref = new WeakReference (bc);
        }

        /** Listener method that is called when a bean is added to
        * the bean context.
        * @param bcme event describing the action
        */
        public void childrenAdded (BeanContextMembershipEvent bcme) {
            BeanChildren bc = (BeanChildren)ref.get ();
            if (bc != null) {
                bc.putAll (bc.createMap (bcme.toArray ()));
            }
        }

        /** Listener method that is called when a bean is removed to
        * the bean context.
        * @param bcme event describing the action
        */
        public void childrenRemoved (BeanContextMembershipEvent bcme) {
            BeanChildren bc = (BeanChildren)ref.get ();
            if (bc != null) {
                bc.removeAll (Arrays.asList (bcme.toArray ()));
            }
        }
    }

}

/*
* Log
*  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  6    Gandalf   1.5         8/30/99  Jesse Glick     Fixed 
*       NullPointerException from finalizing uninitialized BeanChildren; see EAP
*       27.8.99 Bryan Vold.
*  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    Gandalf   1.3         4/30/99  Ales Novak      initMap does not return 
*       null
*  3    Gandalf   1.2         3/18/99  Jesse Glick     [JavaDoc]
*  2    Gandalf   1.1         3/16/99  Jesse Glick     [JavaDoc]
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
