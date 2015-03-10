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

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.beans.*;
import java.beans.beancontext.BeanContext;
import java.beans.beancontext.BeanContextChild;
import java.beans.beancontext.BeanContextProxy;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.BufferedOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ResourceBundle;
import java.util.ArrayList;

import org.openide.util.datatransfer.NewType;
import org.openide.util.datatransfer.PasteType;
import org.openide.util.datatransfer.ExTransferable;
import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.loaders.DataFolder;
import org.openide.actions.CopyAction;
import org.openide.actions.CustomizeBeanAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.loaders.InstanceSupport;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.RequestProcessor;
import org.openide.util.WeakListener;
import org.openide.util.actions.SystemAction;
import org.openide.explorer.propertysheet.editors.NodeCustomizer;


/** Represents one JavaBean in the nodes hierarchy.
* It provides all methods that are needed for communication between
* the IDE and the bean.
* <p>You may use this node type for an already-existing JavaBean (possibly
* using BeanContext) in order for its JavaBean properties to be reflected
* as corresponding node properties. Thus, it serves as a compatibility wrapper.
*
* @author Jan Jancura, Ian Formanek, Jaroslav Tulach
*/
public class BeanNode extends AbstractNode {
    // static ..................................................................................................................

    // [PENDING] use same consts in Sheet instead  --jglick
    /** Names of propertySets. */
    private static final String PROPERTIES = "properties"; // NOI18N
    private static final String EXPERT = "expert"; // NOI18N

    /** Icon base for bean nodes */
    private static final String ICON_BASE = "/org/netbeans/core/resources/beans"; // NOI18N

    private static Children getChildren (Object bean) {
        if (bean instanceof BeanContext)
            return new BeanChildren ((BeanContext)bean);
        if (bean instanceof BeanContextProxy) {
            BeanContextChild bch = ((BeanContextProxy)bean).getBeanContextProxy();
            if (bch instanceof BeanContext)
                return new BeanChildren ((BeanContext)bch);
        }
        return Children.LEAF;
    }


    // variables .............................................................................................................

    /** bean */
    private Object bean;


    /** bean info for the bean */
    private BeanInfo beanInfo;
    /** functions to operate on beans */
    private Method nameGetter   = null;
    private Method nameSetter   = null;
    /** remove PropertyChangeListener method */
    private Method removePCLMethod = null;

    /** listener for properties */
    private PropL propertyChangeListener = null;

    /** is synchronization of name in progress */
    private boolean synchronizeName;

    // init ..................................................................................................................

    /**
    * Constructs a node for a JavaBean. If the bean is a {@link BeanContext},
    * creates a child list as well.
    *
    * @param bean the bean this node will be based on
    * @throws IntrospectionException if the bean cannot be analyzed
    */
    public BeanNode (Object bean) throws IntrospectionException {
        this (
            bean,
            getChildren (bean)
        );
    }

    /** Constructs a node for a JavaBean with a defined child list.
    * Intended for use by subclasses with different strategies for computing the children.
    * @param bean the bean this node will be based on
    * @param children children for the node
    * @throws IntrospectionException if the bean cannot be analyzed
    */
    protected BeanNode (Object bean, Children children) throws IntrospectionException {
        super (children);
        this.bean = bean;
        initialization ();
    }

    /** Set whether or not to keep the node name and Bean name synchronized automatically.
    * If enabled, the node will listen to changes in the name of the bean
    * and update the (system) name of the node appropriately. The name of the bean can
    * be obtained by calling <code>getName ()</code>, <code>getDisplayName ()</code> or from {@link BeanDescriptor#getDisplayName}.
    * <p>Also when the (system) name of the node is changing, the change propagates if possible to
    * methods <code>setName (String)</code> or <code>setDisplayName (String)</code>. (This
    * does not apply to setting the display name of the node, however.)
    * <P>
    * By default this feature is turned on.
    *  
    * @param watch <code>true</code> if the name of the node should be synchronized with
    *   the name of the bean, <code>false</code> if the name of the node should be independent
    *   or manually updated
    *
    */
    protected void setSynchronizeName (boolean watch) {
        synchronizeName = watch;
    }

    /** Provides access to the bean represented by this BeanNode.
    * @return instance of the bean represented by this BeanNode
    */
    protected Object getBean () {
        return bean;
    }

    /** Detaches all listeners from the bean and destroys it.
    * @throws IOException if there was a problem
    */
    public void destroy () throws IOException {
        if (removePCLMethod != null) {
            try {
                Object o = Beans.getInstanceOf (bean, removePCLMethod.getDeclaringClass ());
                removePCLMethod.invoke (o, new Object[] {propertyChangeListener});
            } catch (IllegalAccessException ee) {
            } catch (IllegalArgumentException ee) {
            } catch (InvocationTargetException ee) {
            }
        }
        super.destroy ();
    }


    /** Can this node be removed?
    * @return <CODE>true</CODE> in this implementation
    */
    public boolean canDestroy () {
        return true;
    }

    /** Set the node name.
    * Also may attempt to change the name of the bean,
    * according to {@link #setSynchronizeName}.
    * @param s the new name
    */
    public void setName (String s) {
        if (synchronizeName) {
            Method m = nameSetter;
            if (m != null) {
                try {
                    m.invoke (bean, new Object[] {s});
                } catch (Exception ex) {}
            }
        }
        super.setName (s);
    }

    /** Can this node be renamed?
    * @return <code>true</code> in this implementation
    */
    public boolean canRename () {
        return true;
    }

    /** Get an icon for this node in the closed state.
    * Uses the Bean's icon if possible.
    *
    * @param type constant from {@link java.beans.BeanInfo}
    * @return icon to use
    */
    public Image getIcon (int type) {
        Image image = beanInfo.getIcon (type);
        if (image != null) return image;
        return super.getIcon(type);
    }

    /** Get an icon for this node in the open state.
    *
    * @param type type constants
    * @return icon to use. The default implementation just uses {@link #getIcon}.
    */
    public Image getOpenedIcon (int type) {
        return getIcon(type);
    }

    public HelpCtx getHelpCtx () {
        HelpCtx test = InstanceSupport.findHelp ((InstanceCookie) getCookie (InstanceCookie.class));
        if (test != null)
            return test;
        else
            return new HelpCtx (BeanNode.class);
    }

    /** Prepare node properties based on the bean, storing them into the current property sheet.
    * Called when the bean info is ready.
    * This implementation always creates a set for standard properties
    * and may create a set for expert ones if there are any.
    * @see #computeProperties
    * @param bean bean to compute properties for
    * @param info information about the bean
    */
    protected void createProperties (Object bean, BeanInfo info) {
        Descriptor d = computeProperties (bean, beanInfo);

        Sheet sets = getSheet ();
        Sheet.Set pset = Sheet.createPropertiesSet ();
        pset.put (d.property);

        sets.put (pset);

        if (d.expert.length != 0) {
            Sheet.Set eset = Sheet.createExpertSet ();
            eset.put (d.expert);

            sets.put (eset);
        }
    }

    /** Can this node be copied?
    * @return <code>true</code> in the default implementation
    */
    public boolean canCopy () {
        return true;
    }

    /** Can this node be cut?
    * @return <code>false</code> in the default implementation
    */
    public boolean canCut () {
        return false;
    }

    /* Getter for set of actions that should be present in the
    * popup menu of this node. This set is used in construction of
    * menu returned from getContextMenu and specially when a menu for
    * more nodes is constructed.
    *
    * @return array of system actions that should be in popup menu
    */
    protected SystemAction[] createActions () {
        return new SystemAction[] {
                   SystemAction.get (CustomizeBeanAction.class),
                   null,
                   SystemAction.get (CopyAction.class),
                   null,
                   SystemAction.get (ToolsAction.class),
                   SystemAction.get (PropertiesAction.class)
               };
    }

    /* Test if there is a customizer for this node. If <CODE>true</CODE>
    * the customizer can be obtained via <CODE>getCustomizer</CODE> method.
    *
    * @return <CODE>true</CODE> if there is a customizer.
    */
    public boolean hasCustomizer () {
        // true if we have already computed beanInfo and it has customizer class
        return beanInfo.getBeanDescriptor ().getCustomizerClass () != null;
    }

    /* Returns the customizer component.
    * @return the component or <CODE>null</CODE> if there is no customizer
    */
    public java.awt.Component getCustomizer () {
        Class clazz = beanInfo.getBeanDescriptor ().getCustomizerClass ();
        if (clazz == null) return null;

        Object o;
        try {
            o = clazz.newInstance ();
        } catch (InstantiationException e) {
            return null;
        } catch (IllegalAccessException e) {
            return null;
        }

        if (!(o instanceof java.awt.Component) ||
                !(o instanceof java.beans.Customizer)) return null;

        Customizer cust = ((java.beans.Customizer)o);

        // if the customizer worries about the node
        // inform it
        if (cust instanceof NodeCustomizer) {
            ((NodeCustomizer)cust).attach (this);
        }

        cust.setObject (bean);

        if (removePCLMethod == null) {
            cust.addPropertyChangeListener (
                new PropertyChangeListener () {
                    public void propertyChange(PropertyChangeEvent e) {
                        firePropertyChange (
                            e.getPropertyName (), e.getOldValue (), e.getNewValue ()
                        );
                    }
                });
        }

        return (java.awt.Component)o;
    }


    /** Computes a descriptor for properties from a bean info.
    *
    * @param bean bean to create properties for
    * @param info about the bean
    * @return three property lists
    */
    public static Descriptor computeProperties (Object bean, BeanInfo info) {
        ArrayList property = new ArrayList ();
        ArrayList expert = new ArrayList ();
        ArrayList hidden = new ArrayList ();

        PropertyDescriptor[] propertyDescriptor = info.getPropertyDescriptors ();

        int k = propertyDescriptor.length;
        for (int i = 0; i < k; i ++) {
            Node.Property prop;

            if (propertyDescriptor[i] instanceof IndexedPropertyDescriptor) {
                IndexedPropertyDescriptor p = (IndexedPropertyDescriptor) propertyDescriptor [i];
                IndexedPropertySupport support =  new IndexedPropertySupport (
                                                      bean, p.getPropertyType (),
                                                      p.getIndexedPropertyType(), p.getReadMethod (), p.getWriteMethod (),
                                                      p.getIndexedReadMethod (), p.getIndexedWriteMethod ()
                                                  );
                support.setName (p.getName ());
                support.setDisplayName (p.getDisplayName ());
                support.setShortDescription (p.getShortDescription ());

                prop = support;
            } else {
                PropertyDescriptor p = propertyDescriptor [i];
                PropertySupport.Reflection support = new PropertySupport.Reflection (
                                                         bean, p.getPropertyType (),
                                                         p.getReadMethod (), p.getWriteMethod ()
                                                     );
                support.setName (p.getName ());
                support.setDisplayName (p.getDisplayName ());
                support.setShortDescription (p.getShortDescription ());
                support.setPropertyEditorClass (p.getPropertyEditorClass ());

                prop = support;
            }
            if (propertyDescriptor[i].isHidden ()) {
                // hidden property
                hidden.add (prop);
            } else {
                if (propertyDescriptor[i].isExpert ()) {
                    expert.add (prop);
                } else {
                    property.add (prop);
                }
            }
        }// for

        return new Descriptor (property, expert, hidden);
    }



    //
    //
    // Initialization methods
    //
    //


    /** Performs initalization of the node
    */
    private void initialization () throws IntrospectionException {
        setIconBase (ICON_BASE);
        setDefaultAction (SystemAction.get (PropertiesAction.class));

        setSynchronizeName (true);

        // Find the first public superclass of the actual class.
        // Should not introspect on a private class, because then the method objects
        // used for the property descriptors will not be callable without an
        // IllegalAccessException, even if overriding a public method from a public superclass.
        Class clazz = bean.getClass ();
        while (! Modifier.isPublic (clazz.getModifiers ())) {
            clazz = clazz.getSuperclass ();
            if (clazz == null) clazz = Object.class; // in case it was an interface
        }
        beanInfo = Utilities.getBeanInfo (clazz);

        // resolving the name of this bean
        BeanDescriptor descriptor = beanInfo.getBeanDescriptor ();
        setShortDescription (descriptor.getShortDescription ());
        registerName ();
        setNameSilently (getNameForBean ());

        // add propertyChangeListener
        EventSetDescriptor[] eventSetDescriptors = beanInfo.getEventSetDescriptors();
        int i, k = eventSetDescriptors.length;
        Method method = null;
        for (i = 0; i < k; i++) {
            method = eventSetDescriptors [i].getAddListenerMethod ();
            if ((method != null) && (method.getName ().equals ("addPropertyChangeListener"))) // NOI18N
                break;
        }
        if (i != k) {
            try {
                Object o = Beans.getInstanceOf (bean, method.getDeclaringClass ());
                propertyChangeListener = new PropL ();
                method.invoke (o, new Object[] { WeakListener.propertyChange (propertyChangeListener, o) });
                removePCLMethod = eventSetDescriptors [i].getRemoveListenerMethod ();
            } catch (IllegalAccessException ee) {
            } catch (IllegalArgumentException ee) {
            } catch (InvocationTargetException ee) {}
        }

        createProperties (bean, beanInfo);

        getCookieSet ().add (new InstanceSupport.Instance (bean));
    }

    // name resolving methods

    /**
    * Finds setter and getter methods for the name of the bean. Resisters listener
    * for changing of name.
    */
    private void registerName () {
        // [PENDING] ought to use introspection, rather than look up the methods by name  --jglick
        Class clazz = bean.getClass ();
        Class[] param = new Class [0];

        // find getter for the name
        try {
            nameGetter = clazz.getMethod ("getName", param); // NOI18N
        } catch (Exception e) {
            try {
                nameGetter = clazz.getMethod ("getDisplayName", param); // NOI18N
            } catch (Exception ee) {
                nameGetter = null;
                return;
            }
        }

        // this code tests wheter everything is fine and the getter is
        // invokable
        try {
            nameGetter.invoke (bean, null);
        } catch (Exception e) {
            nameGetter = null;
            return;
        }

        // find the setter for the name
        param = new Class[] {String.class};
        try {
            // tries to find method setName (String)
            nameSetter = clazz.getMethod ("setName", param); // NOI18N
        } catch (Exception e) {
            try {
                nameSetter = clazz.getMethod ("setDisplayName", param); // NOI18N
            } catch (Exception ee) {
                nameSetter = null;
            }
        }

    }

    /**
    * Returns name of the bean.
    */
    private String getNameForBean () {
        try {
            if (nameGetter != null) {
                return (String)nameGetter.invoke (bean, null);
            }
        } catch (Exception ex) {
        }
        // it should not fail but if it fails then return toString value
        BeanDescriptor descriptor = beanInfo.getBeanDescriptor ();
        return descriptor.getDisplayName ();
    }

    /** To allow innerclasses to access the super.setName method.
    */
    void setNameSilently (String name) {
        super.setName (name);
    }


    /** Descriptor of three types of properties. Regular,
    * expert and hidden.
    */
    public static final class Descriptor extends Object {
        /** Regular properties. */
        public final Node.Property[] property;
        /** Expert properties. */
        public final Node.Property[] expert;
        /** Hidden properties. */
        public final Node.Property[] hidden;

        /** private constructor */
        Descriptor (ArrayList p, ArrayList e, ArrayList h) {
            property = new Node.Property[p.size ()];
            p.toArray (property);

            expert = new Node.Property[e.size ()];
            e.toArray (expert);

            hidden = new Node.Property[h.size ()];
            h.toArray (hidden);
        }
    }

    /** Property change listener to update the properties of the node and
    * also the name of the node (sometimes)
    */
    private final class PropL extends Object implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent e) {
            firePropertyChange (e.getPropertyName (), e.getOldValue (), e.getNewValue ());

            if (synchronizeName) {
                String name = e.getPropertyName ();
                if (name == null || name.equals ("name") || name.equals ("displayName")) { // NOI18N
                    String newName = getNameForBean ();
                    if (!newName.equals (getName ())) {
                        setNameSilently (newName);
                    }
                }
            }
        }
    }
}

/*
 * Log
 *  30   Gandalf   1.29        1/12/00  Jesse Glick     NOI18N
 *  29   Gandalf   1.28        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  28   Gandalf   1.27        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  27   Gandalf   1.26        7/21/99  Ian Formanek    Added protected method 
 *       getBean to provide access to the bean represented by the BeanNode
 *  26   Gandalf   1.25        6/30/99  Ian Formanek    Reflecting package 
 *       change of NodeCustomizer
 *  25   Gandalf   1.24        6/25/99  Jesse Glick     Instances can have 
 *       sensible help contexts.
 *  24   Gandalf   1.23        6/9/99   Ian Formanek    ToolsAction
 *  23   Gandalf   1.22        6/8/99   Ian Formanek    Minor changes
 *  22   Gandalf   1.21        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  21   Gandalf   1.20        6/3/99   Jaroslav Tulach NodePropertyEditor & 
 *       NodeCustomizer
 *  20   Gandalf   1.19        5/27/99  Jesse Glick     [JavaDoc]
 *  19   Gandalf   1.18        5/27/99  Jaroslav Tulach Executors rearanged.
 *  18   Gandalf   1.17        5/26/99  Jesse Glick     ST#1937 -- only 
 *       introspecting on public superclasses.
 *  17   Gandalf   1.16        5/9/99   Ian Formanek    Fixed last change
 *  16   Gandalf   1.15        5/8/99   Ian Formanek    Uses createPropertiesSet
 *       and cretaeExpertSet from Sheet
 *  15   Gandalf   1.14        4/9/99   Ian Formanek    Removed debug printlns
 *  14   Gandalf   1.13        3/29/99  Jesse Glick     [JavaDoc]
 *  13   Gandalf   1.12        3/27/99  Jaroslav Tulach Support for serializing 
 *       beans into folder + implemented for control panel and repository
 *  12   Gandalf   1.11        3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  11   Gandalf   1.10        3/18/99  Jesse Glick     [JavaDoc]
 *  10   Gandalf   1.9         3/17/99  Jesse Glick     [JavaDoc]
 *  9    Gandalf   1.8         3/17/99  Jesse Glick     [JavaDoc]
 *  8    Gandalf   1.7         3/16/99  Jesse Glick     [JavaDoc] and very minor
 *       code changes.
 *  7    Gandalf   1.6         3/6/99   David Simonek   
 *  6    Gandalf   1.5         2/25/99  Jaroslav Tulach Change of clipboard 
 *       management  
 *  5    Gandalf   1.4         2/4/99   Petr Hamernik   changes to be compiled 
 *       by jikes
 *  4    Gandalf   1.3         1/7/99   Ian Formanek    fixed resource names
 *  3    Gandalf   1.2         1/6/99   Jan Jancura     
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach Change of package of 
 *       DataObject
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.17        --/--/98 Jan Jancura     PanContextSupportAdded
 *  0    Tuborg    0.24        --/--/98 Jan Formanek    bugfix (icon name)
 *  0    Tuborg    0.26        --/--/98 Jan Formanek    ClipboardOperation.NONE employed
 *  0    Tuborg    0.27        --/--/98 Jan Formanek    reflecting changes in PropertySupport
 *  0    Tuborg    0.28        --/--/98 Ales Novak      Clipboardoperation, Menu
 *  0    Tuborg    0.29        --/--/98 Jaroslav Tulach Clipboard operations moved to the node
 *  0    Tuborg    0.30        --/--/98 Jan Jancura     Get customizer repaired
 *  0    Tuborg    0.31        --/--/98 Jan Formanek    Now also sets the expert and hidden flags for the
 *  0    Tuborg    0.31        --/--/98 Jan Formanek    properties from reflection
 *  0    Tuborg    0.32        --/--/98 Jan Jancura     bugfix
 *  0    Tuborg    0.34        --/--/98 Jan Jancura     setParent, setBean, ()constructor for serialization
 *  0    Tuborg    0.35        --/--/98 Jaroslav Tulach added getNewTypes method
 *  0    Tuborg    0.37        --/--/98 Jan Jancura     PropertySet
 *  0    Tuborg    0.40        --/--/98 Jan Formanek    changed the stupid context menu
 *  0    Tuborg    0.41        --/--/98 Jan Formanek    does not show the expert tab if there are no expert properties
 */
