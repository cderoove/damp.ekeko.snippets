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

package org.netbeans.modules.apisupport.beanbrowser;

import java.beans.*;
import java.util.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.util.*;

/** A list of all properties in a property set.
* The keys are of type Node.Property.
*/
public class PropSetKids extends Children.Keys {

    private Collection keys;

    private Node original;
    private Node.PropertySet ps;
    private PropertyChangeListener pcListener = null;

    public PropSetKids (Node original, Node.PropertySet ps) {
        this.ps = ps;
        this.original = original;
    }

    private void setKeys0 (Collection c) {
        setKeys (c);
        keys = c;
    }

    /** Update the key list.
    * Looks for all properties which are readable, and not primitive or String or Class.
    */
    private void updateKeys () {
        Collection newKeys = new ArrayList ();
        Node.Property[] props = ps.getProperties ();
        for (int j = 0; j < props.length; j++) {
            Node.Property prop = props[j];
            if (prop.canRead ()) {
                Class type = prop.getValueType ();
                if (! (type.isPrimitive () || type == String.class || type == Class.class)) {
                    newKeys.add (prop);
                }
            }
        }
        setKeys0 (newKeys);
    }

    /** Set the keys.
    * Also attach a listener to the original node so that if one of its
    * properties (node properties, not meta-properties of the node itself)
    * changes, the children can be recalculated.
    */
    protected void addNotify () {
        updateKeys ();
        if (pcListener == null) {
            pcListener = new PropertyChangeListener () {
                             public void propertyChange (PropertyChangeEvent ev) {
                                 String prop = ev.getPropertyName ();
                                 Iterator it = getKeysIterator ();
                                 Object found = null;
                                 while (it.hasNext ()) {
                                     Object key = it.next ();
                                     if (key instanceof Node.Property && ((Node.Property) key).getName ().equals (prop)) {
                                         found = key;
                                     }
                                 }
                                 if (found == null) {
                                     // Should not happen.
                                     updateKeys ();
                                 } else {
                                     refreshKey0 (found);
                                 }
                             }
                         };
            original.addPropertyChangeListener (pcListener);
        }
    }
    // Inner class access methods:
    private void refreshKey0 (Object key) {
        refreshKey (key);
    }
    private Iterator getKeysIterator () {
        return keys.iterator ();
    }

    /** Remove the listener. */
    private void doUnListen () {
        if (pcListener != null) {
            original.removePropertyChangeListener (pcListener);
            pcListener = null;
        }
    }
    protected void finalize () throws Exception {
        doUnListen ();
    }
    protected void removeNotify () {
        doUnListen ();
        setKeys0 (Collections.EMPTY_SET);
    }

    /** Create the node for this property.
    * @param key the property
    * @return the (one) node to represent it
    */
    protected Node[] createNodes (Object key) {
        return new Node[] { makePropertyNode ((Node.Property) key) };
    }

    /** Make a node for a property and its value.
    * @param prop the property to represent
    * @return a node to represent it
    */
    private static Node makePropertyNode (Node.Property prop) {
        Class type = prop.getValueType ();
        Node node;
        try {
            node = makeObjectNode (prop.getValue ());
        } catch (Exception e) {
            node = makeErrorNode (e);
        }
        node.setDisplayName (Utilities.getClassName (type) + " " + prop.getDisplayName () + " = " + node.getDisplayName ());
        return node;
    }

    /** Make a node to meaningfully represent some object.
    * Special treatment for null; arrays or generalized collections; String and Class objects.
    * All else gets a RefinedBeanNode.
    * The name and tooltip are set to something helpful.
    * @param val the object to represent
    * @return a node displaying it
    */
    public static Node makeObjectNode (Object val) {
        if (val == null) {
            return makePlainNode ("null");
        } else if (val instanceof Object[]) {
            return makeCollectionNode (Collections.enumeration (Arrays.asList ((Object[]) val)));
        } else if (val.getClass ().isArray ()) {
            return makeCollectionNode (Collections.enumeration (Arrays.asList (Utilities.toObjectArray (val))));
        } else if (val instanceof Enumeration) {
            return makeCollectionNode ((Enumeration) val);
        } else if (val instanceof Collection) {
            return makeCollectionNode (Collections.enumeration ((Collection) val));
        } else if (val instanceof String) {
            return makePlainNode ("\"" + (String) val + "\"");
        } else if (val instanceof Class) {
            return makePlainNode ("class " + ((Class) val).getName ());
        } else {
            Node objnode;
            try {
                objnode = new RefinedBeanNode (val);
            } catch (IntrospectionException e) {
                objnode = makeErrorNode (e);
            }
            objnode.setShortDescription ("String value: `" + val + "'; short description: " + objnode.getShortDescription ());
            objnode.setDisplayName (objnode.getDisplayName () + " (class " + val.getClass ().getName () + ")");
            return Wrapper.make (objnode);
        }
    }

    /** Make a leaf node just displaying some text.
    * @param name the text
    * @return the node
    */
    static Node makePlainNode (String name) {
        AbstractNode toret = new AbstractNode (Children.LEAF) {
                                 public HelpCtx getHelpCtx () {
                                     return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser");
                                 }
                             };
        toret.setName (name);
        toret.setIconBase ("/org/netbeans/modules/apisupport/resources/BeanBrowserIcon");
        return toret;
    }

    /** Make a node representing an error condition and describing the error.
    * @param t the error
    * @return a node displaying it (as a Bean)
    */
    static Node makeErrorNode (Throwable t) {
        Node node = makeObjectNode (t);
        node.setDisplayName ("[thrown] " + node.getDisplayName ());
        return node;
    }

    /** Make a node representing an array or list or somesuch.
    * Safety valve warns the user before creating a huge array.
    * @param val an Enuemration of Object's
    * @return a node displaying the objects as children
    */
    private static Node makeCollectionNode (final Enumeration val) {
        final Node[] _base = new Node[] { null };
        final String defaultName = "<list of objects>";
        Children kids = new Children.Array () {
                            protected void addNotify () {
                                new Thread (new Runnable () {
                                                public void run () {
                                                    int count = 0;
                                                    while (val.hasMoreElements ()) {
                                                        Node n = makeObjectNode (val.nextElement ());
                                                        n.setDisplayName ("[" + count + "] " + n.getDisplayName ());
                                                        add (new Node[] { n });
                                                        if (count++ == 50) {
                                                            if (! NotifyDescriptor.OK_OPTION.equals (
                                                                        TopManager.getDefault ().notify (new NotifyDescriptor.Confirmation (new String[] {
                                                                                                             "There were over 50 elements in this array.",
                                                                                                             "Actually show all of them?"
                                                                                                         })))) {
                                                                break;
                                                            }
                                                        }
                                                    }
                                                    if (defaultName.equals (_base[0].getDisplayName ())) {
                                                        _base[0].setDisplayName ("A list of " + count + " children...");
                                                        _base[0].setShortDescription (_base[0].getDisplayName ());
                                                    } else {
                                                        _base[0].setShortDescription ("[" + count + " children] " + _base[0].getShortDescription ());
                                                    }
                                                }
                                            }, "making collection node").start ();
                            }
                        };
        AbstractNode base = new AbstractNode (kids) {
                                public HelpCtx getHelpCtx () {
                                    return new HelpCtx ("org.netbeans.modules.apisupport.beanbrowser");
                                }
                            };
        _base[0] = base;
        base.setName ("collection");
        base.setDisplayName (defaultName);
        base.setIconBase ("/org/netbeans/modules/apisupport/resources/BeanBrowserIcon");
        return base;
    }
}

/*
 * Log
 *  20   Gandalf-post-FCS1.17.1.1    3/30/00  Jesse Glick     Collection nodes now 
 *       made only on demand.
 *  19   Gandalf-post-FCS1.17.1.0    3/9/00   Jesse Glick     Minor collection node 
 *       bugfix.
 *  18   Gandalf   1.17        2/4/00   Jesse Glick     Clipboard support.
 *  17   Gandalf   1.16        1/19/00  Jesse Glick     Collection node 
 *       improvements.
 *  16   Gandalf   1.15        1/13/00  Jesse Glick     FeatureDescriptor bug.
 *  15   Gandalf   1.14        1/12/00  Jesse Glick     Hopefully fixing a 
 *       deadlock.
 *  14   Gandalf   1.13        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  13   Gandalf   1.12        10/13/99 Jesse Glick     Various fixes and 
 *       enhancements:    - added a Changes.txt    - fixed handling of 
 *       OpenAPIs.zip on install/uninstall (previously did not correctly unmount
 *       on uninstall, nor check for already-mounted on install)    - added a 
 *       CompilerTypeTester    - display name & icon updates from Tim    - 
 *       removed link to ToDo.txt from docs page    - various BeanInfo's, both 
 *       in templates and in the support itself, did not display superclass 
 *       BeanInfo correctly    - ExecutorTester now permits user to customize 
 *       new executor instance before running it
 *  12   Gandalf   1.11        10/7/99  Jesse Glick     Made a method public; 
 *       context help.
 *  11   Gandalf   1.10        10/7/99  Jesse Glick     Package change. Also 
 *       cloning in Wrapper.make, which may be necessary.
 *  10   Gandalf   1.9         9/10/99  Jesse Glick     Children.Keys.keys 
 *       removed.
 *  9    Gandalf   1.8         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    Gandalf   1.7         5/27/99  Jesse Glick     Clean-up: comments, 
 *       licenses, removed debugging code, a few minor code changes.
 *  7    Gandalf   1.6         5/26/99  Jesse Glick     Display child counts of 
 *       arrays.
 *  6    Gandalf   1.5         5/25/99  Jesse Glick     Fully cleaned up name 
 *       handling, looks much nicer now. Much safer too.
 *  5    Gandalf   1.4         5/24/99  Jesse Glick     Using RefinedBeanNode 
 *       for CustomizeBeanAction.
 *  4    Gandalf   1.3         5/21/99  Jesse Glick     Confirmation before 
 *       showing huge array.
 *  3    Gandalf   1.2         5/19/99  Jesse Glick     Arrays of primitives 
 *       handled, sort of.
 *  2    Gandalf   1.1         5/19/99  Jesse Glick     More useful error node.
 *  1    Gandalf   1.0         5/18/99  Jesse Glick     
 * $
 */
