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

package org.netbeans.modules.debugger.support.nodes;

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.beans.BeanInfo;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;

import org.openide.actions.DeleteAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.debugger.Debugger;
import org.openide.debugger.Watch;
import org.openide.debugger.DebuggerException;
import org.openide.TopManager;
import org.openide.util.NbBundle;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;
import org.openide.util.actions.SystemAction;

import org.netbeans.modules.debugger.support.actions.CreateVariableAction;
import org.netbeans.modules.debugger.support.actions.CreateVariableCookie;
import org.netbeans.modules.debugger.support.actions.GetVariableCookie;
import org.netbeans.modules.debugger.support.actions.CreateVariableAccessBreakpointAction;
import org.netbeans.modules.debugger.support.actions.CreateVariableModificationBreakpointAction;
import org.netbeans.modules.debugger.support.DebuggerAdapter;
import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.util.Utils;


/**
* This class represents variable as a Node.
*
* @author   Jan Jancura
*/
public class VariableNode extends AbstractNode implements GetVariableCookie,
    CreateVariableCookie {

    /** generated Serialized Version UID */
    static final long                       serialVersionUID = -4167730461290769518L;

    protected static String                 ICON_BASE =
        "/org/netbeans/core/resources/variable"; // NOI18N

    public static final String              PROP_MODIFIERS = "modifiers"; // NOI18N
    public static final String              PROP_INNER_TYPE = "innerType"; // NOI18N
    public static final String              PROP_BASE_INDEX = "baseIndex"; // NOI18N
    public static final String              PROP_DISPLAYED_LENGTH = "displayedLength"; // NOI18N
    private static ResourceBundle           bundle;


    // variables .................................................................

    /** Where to add variables. */
    private VariableHome                    variableHome;

    private boolean                         isRoot = false;

    protected AbstractVariable              variable = null;

    protected transient Debugger            debugger;

    private PropertyChangeListener          pcl;

    /** First index of an array to be displayed. */
    private int                             baseIndex = 0;

    /** Number of field of an array to be displayed. */
    private int                             displayedLength = 100;

    private boolean                         isArray;

    // init ......................................................................

    /**
    * Creates empty Variable context.
    */
    public VariableNode (
        VariableHome variableHome,
        AbstractVariable variable,
        boolean isRoot
    ) {
        super (
            variable.isLeaf () ?
            Children.LEAF :
            new VariableContextChildren (variableHome)
        );
        this.variableHome = variableHome;
        this.isRoot = isRoot;
        this.variable = variable;
        isArray = variable.isArray ();
        init ();
    }

    /**
    * Creates empty Variable context.
    */
    public VariableNode (
        VariableHome variableHome,
        AbstractVariable variable
    ) {
        this (variableHome, variable, false);
    }

    protected void init () {
        variable.addPropertyChangeListener (pcl = new PropertyChangeListener () {
                                                public void propertyChange (PropertyChangeEvent e) {
                                                    parameterChanged (e);
                                                }
                                            });
        setDisplayName (getName ());
        setName (getName ());
        setIconBase(ICON_BASE);
        // obtain debugger reference
        try {
            debugger = TopManager.getDefault ().getDebugger ();
        } catch (DebuggerException exc) {
            TopManager.getDefault ().notifyException (exc);
        }
        createProperties ();
        getCookieSet ().add (this);
        changeChildren ();
    }

    /** deserializes object */
    private void readObject (java.io.ObjectInputStream obis)
    throws java.io.IOException, ClassNotFoundException,
        java.io.NotActiveException {
        obis.defaultReadObject ();
        init ();
    }


    // implementation of Node ....................................................

    /** Creates properties for this node */
    protected void createProperties () {
        // default sheet with "properties" property set // NOI18N
        Sheet sheet = Sheet.createDefault ();
        Sheet.Set ps = sheet.get (Sheet.PROPERTIES);
        ps.put (new PropertySupport.ReadOnly (
                    Watch.PROP_VARIABLE_NAME,
                    String.class,
                    getLocalizedString ("PROP_watch_name"),
                    getLocalizedString ("HINT_watch_name")
                ) {
                    public Object getValue () {
                        return variable.getVariableName ();
                    }
                });
        createCommonProperties (bundle, ps);
        // and set new sheet
        setSheet (sheet);
    }

    /** Getter for set of actions that should be present in the
    * popup menu of this node. This set is used in construction of
    * menu returned from getContextMenu and specially when a menu for
    * more nodes is constructed.
    *
    * @return array of system actions that should be in popup menu
    */
    public SystemAction[] getActions () {
        if (isRoot)
            return new SystemAction[] {
                       //        CreateVariableModificationBreakpointAction.get (CreateVariableModificationBreakpointAction.class),
                       //        CreateVariableAccessBreakpointAction.get (CreateVariableAccessBreakpointAction.class),
                       //        null,
                       SystemAction.get (DeleteAction.class),
                       null,
                       SystemAction.get (ToolsAction.class),
                       SystemAction.get (PropertiesAction.class),
                   };
        else
            return new SystemAction[] {
                       SystemAction.get (CreateVariableAction.class),
                       null,
                       SystemAction.get (ToolsAction.class),
                       SystemAction.get (PropertiesAction.class),
                   };
    }

    /** Helper method, creates properties common to variable
    * and watch contexts. */
    void createCommonProperties (
        final ResourceBundle bundle,
        final Sheet.Set ps
    ) {
        ps.put (new PropertySupport.ReadOnly (
                    Watch.PROP_TYPE,
                    String.class,
                    getLocalizedString ("PROP_watch_type"),
                    getLocalizedString ("HINT_watch_type")
                ) {
                    public Object getValue () {
                        return variable.getType ();
                    }
                });
        ps.put (new PropertySupport.ReadOnly (
                    PROP_INNER_TYPE,
                    String.class,
                    getLocalizedString ("PROP_watch_inner_type"),
                    getLocalizedString ("HINT_watch_inner_type")
                ) {
                    public Object getValue () {
                        return variable.getInnerType ();
                    }
                });
        ps.put (new PropertySupport.ReadOnly (
                    PROP_MODIFIERS,
                    String.class,
                    getLocalizedString ("PROP_watch_modifiers"),
                    getLocalizedString ("HINT_watch_modifiers")
                ) {
                    public Object getValue () {
                        return variable.getModifiers ();
                    }
                });
        ps.put (new PropertySupport.ReadWrite (
                    Watch.PROP_AS_TEXT,
                    String.class,
                    getLocalizedString ("PROP_watch_value"),
                    getLocalizedString ("HINT_watch_value")
                ) {
                    public Object getValue () {
                        return variable.getAsText ();
                    }
                    public void setValue (Object val) throws IllegalArgumentException {
                        try {
                            variable.setAsText ((String)val);
                        } catch (ClassCastException e) {
                            throw new IllegalArgumentException ();
                        }
                        catch (DebuggerException e) {}
                    }
                });
        if (isArray) {
            ps.put (new PropertySupport.ReadWrite (
                        PROP_BASE_INDEX,
                        Integer.TYPE,
                        getLocalizedString ("PROP_base_index"),
                        getLocalizedString ("HINT_base_index")
                    ) {
                        public Object getValue () {
                            return new Integer (baseIndex);
                        }
                        public void setValue (Object val) throws IllegalArgumentException {
                            try {
                                int x = ((Integer)val).intValue ();
                                if (x < 0) throw new ClassCastException ();
                                baseIndex = x;
                                if (isArray)
                                    changeChildren ();
                                // firePropertyChange (PROP_BASE_INDEX, null, null);
                            }
                            catch (ClassCastException e) {
                                new IllegalArgumentException ();
                            }
                        }
                    });
            ps.put (new PropertySupport.ReadWrite (
                        PROP_DISPLAYED_LENGTH,
                        Integer.TYPE,
                        getLocalizedString ("PROP_displayed_length"),
                        getLocalizedString ("HINT_displayed_length")
                    ) {
                        public Object getValue () {
                            return new Integer (displayedLength);
                        }
                        public void setValue (Object val) throws IllegalArgumentException {
                            try {
                                int x = ((Integer)val).intValue ();
                                if (x < 0) throw new ClassCastException ();
                                displayedLength = x;
                                if (isArray)
                                    changeChildren ();
                                // firePropertyChange (PROP_DISPLAYED_LENGTH, null, null);
                            }
                            catch (ClassCastException e) {
                                new IllegalArgumentException ();
                            }
                        }
                    });
        }
    }

    /**
    * Variable can be removed.
    *
    * @return <CODE>true</CODE>
    */
    public boolean canDestroy () {
        return true;
    }

    /**
    * Removes the Variable from its parent and deletes it.
    */
    public void destroy () throws IOException {
        super.destroy ();
        disposeVariable ();
    }


    // GetVariableCookie implementation ..........................................

    /**
    * Returns variable instance.
    */
    public AbstractVariable getVariable () {
        return variable;
    }


    // other methods .............................................................

    /**
    * @return localized string.
    */
    static String getLocalizedString (String s) {
        if (bundle == null)
            bundle = NbBundle.getBundle (VariableNode.class);
        return bundle.getString (s);
    }

    /**
    * Disposes resources of this variable an sub-variables.
    */
    private void disposeVariable () throws IOException {
        variable.removePropertyChangeListener (pcl);
        variable = null;
        if (getChildren () == Children.LEAF) return;
        VariableContextChildren myChildren = (VariableContextChildren)
                                             getChildren ();
        if (!myChildren.initialized) return;
        Node[] n = myChildren.getNodes ();
        int i, k = n.length;
        for (i = 0; i < k; i++)
            ((VariableNode) n [i]).disposeVariable ();
    }

    /**
    * Returns human presentable name of this variable containing
    * informations about value or valide.
    *
    * @return human presentable name of this variable.
    */
    public String getName () {
        String name = variable.getVariableName ();
        if ((name == null) || (debugger == null)) return "???"; // NOI18N

        if (debugger.getState () == Debugger.DEBUGGER_NOT_RUNNING)
            return name + " " + getLocalizedString ("CTL_NoSession");
        String type = variable.getType ();
        String innerType = variable.getInnerType ();
        String value = variable.getAsText ();
        if (type == null)
            if (value == null)
                if (variable.isObject ()) return name + " = null"; // NOI18N
                else return name + " " + getLocalizedString ("CTL_NotInitialized");
            else return name + " = >" + value + "<"; // NOI18N
        if (variable.isObject ()) {
            if (type.equals (innerType))
                return name + " = (" + innerType + ") " + value; // NOI18N
            else
                return name + " = (" + type + ") (" + innerType + ") " + value; // NOI18N
        }
        else
            return name + " = (" + type + ") " + value; // NOI18N
    }

    public void createVariable () {
        variableHome.createVariable (variable);
    }

    /**
    * Sets display name and fires property change.
    */
    void parameterChanged (PropertyChangeEvent e) {
        if (variable.isArray () != isArray) {
            isArray = !isArray;
            createProperties ();
        }
        String s = getName ();
        setDisplayName (s);
        setName (s);
        changeChildren ();
        firePropertyChange (
            e.getPropertyName (),
            e.getOldValue (),
            e.getNewValue ()
        );
    }

    /**
    * Sets current variable fields.
    */
    void changeChildren () {
        // set fields of this variable as keys into our children
        if (getChildren() == Children.LEAF) {
            Node n = getParentNode ();
            if (n == null)
                return;
            AbstractVariable v = getVariable ();
            if (!v.isLeaf ()) {
                LeafRefresher parentChildren =
                    (LeafRefresher) n.getChildren ();
                parentChildren.refreshMyKey (v);
            }
            return;
        }
        VariableContextChildren myChildren =
            (VariableContextChildren) getChildren ();
        if (!myChildren.initialized) return;
        AbstractVariable [] fields = variable.getFields ();
        if (isArray) {
            int length = Math.min (displayedLength, fields.length - baseIndex);
            AbstractVariable [] v = new AbstractVariable [length];
            System.arraycopy (fields, baseIndex, v, 0, length);
            fields = v;
        }
        myChildren.setMyKeys (fields);
    }


    // innerclasses ..............................................................

    /** Special subnodes (children) for variable node */
    private static final class VariableContextChildren extends Children.Keys implements LeafRefresher {

        /** Where to add variables. */
        private VariableHome                    variableHome;
        private boolean                         initialized = false;

        VariableContextChildren (VariableHome variableHome) {
            this.variableHome = variableHome;
        }

        /** Initializes children.
        */
        protected void addNotify () {
            initialized = true;
            ((VariableNode) getNode ()).changeChildren ();
        }

        /** Creates nodes for given key.
        * @param key the key that is used
        * @return array of nodes representing the key
        */
        protected Node[] createNodes (Object key) {
            return new Node[] {
                       new VariableNode (variableHome, (AbstractVariable)key)
                   };
        }

        /** Accessor for VariableNode outer class */
        private void setMyKeys (Object[] keys) {
            setKeys(keys);
        }

        // interface LeafRefresher

        /** Accessor for VariableNode class */
        public void refreshMyKey (Object key) {
            refreshKey (key);
        }

    } // end of VariableContextChildren inner class
}

/*
 * Log
 *  14   Gandalf-post-FCS1.12.3.0    3/28/00  Daniel Prusa    
 *  13   Gandalf   1.12        1/17/00  Daniel Prusa    setAsText method throws 
 *       DebuggerException
 *  12   Gandalf   1.11        1/14/00  Daniel Prusa    NOI18N
 *  11   Gandalf   1.10        1/13/00  Daniel Prusa    NOI18N
 *  10   Gandalf   1.9         1/4/00   Jan Jancura     Use trim () on user 
 *       input.
 *  9    Gandalf   1.8         1/4/00   Daniel Prusa    enabling/disabling of 
 *       Create fixed watch action
 *  8    Gandalf   1.7         12/20/99 Daniel Prusa    Bug 4895 - Update of 
 *       watches after creating and assining a new instance    into variable 
 *       previously containing null value.  
 *  7    Gandalf   1.6         11/8/99  Jan Jancura     Somma classes renamed
 *  6    Gandalf   1.5         11/5/99  Jan Jancura     Default action updated
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         10/11/99 Jan Jancura     Bug in refreshing of 
 *       watches
 *  3    Gandalf   1.2         9/28/99  Jan Jancura     
 *  2    Gandalf   1.1         9/15/99  Jan Jancura     
 *  1    Gandalf   1.0         8/17/99  Jan Jancura     
 * $
 * Beta Change History:
 *  0    Tuborg    0.15        --/--/98 Jan Formanek    popup menu improved
 */
