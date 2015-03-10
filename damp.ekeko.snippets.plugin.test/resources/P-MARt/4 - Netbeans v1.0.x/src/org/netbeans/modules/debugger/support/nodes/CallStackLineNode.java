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
import java.beans.BeanInfo;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import java.util.ArrayList;

import org.openide.TopManager;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.cookies.LineCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.debugger.DebuggerException;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.PropertySupport;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.CallableSystemAction;

import org.netbeans.modules.debugger.support.AbstractVariable;
import org.netbeans.modules.debugger.support.CallStackFrame;
import org.netbeans.modules.debugger.support.actions.GoToSourceAction;
import org.netbeans.modules.debugger.support.actions.GoToSourceCookie;


/**
* This class callstack frame as a Node.
* This class is final only for performance reasons.
* Can be happily unfinaled if desired.
*
* @author   Jan Jancura
*/
public final class CallStackLineNode extends AbstractNode implements
    GoToSourceCookie {
    /** generated Serialized Version UID */
    static final long               serialVersionUID = -4901112834613957792L;


    // static ....................................................................

    /** Property name constant */
    public static final String      PROPERTY_CLASS_NAME = "className"; // NOI18N
    /** Property name constant */
    public static final String      PROPERTY_METHOD_NAME = "methodName"; // NOI18N
    /** Property name constant */
    public static final String      PROPERTY_LINE_NUMBER = "lineNumber"; // NOI18N

    /** Icon base for this node */
    private static final String     ICON_BASE =
        "/org/netbeans/core/resources/callstack"; // NOI18N
    private static ResourceBundle   bundle;


    // variables .................................................................

    /** Popup menu actions. */
    private SystemAction[]          staticActions;

    /** CallStack frame of this CallStackLineNode. */
    private CallStackFrame          stackFrame;

    /** index of line in call stack */
    private int                     index;

    // init ......................................................................

    /** Creates call stack line with asociated stack frame
    */
    CallStackLineNode (
        VariableHome variableHome,
        CallStackFrame stackFrame,
        int index
    ) {
        super (new CallStackLineChildren (variableHome, stackFrame));
        this.stackFrame = stackFrame;
        this.index = index;
        initialize ();
    }

    /** Performes initialization of this nodeS */
    private void initialize () {
        try {
            String s = stackFrame.getClassName () + '.' +
                       stackFrame.getMethodName () + " : " + // NOI18N
                       stackFrame.getLineNumber ();
            setDisplayName (s);
            setName (s);
        } catch (DebuggerException e) {
            String s = e.getMessage ();
            setDisplayName (s);
            setName (s);
        }
        setIconBase (ICON_BASE);
        createProperties ();
        getCookieSet ().add (this);
        changeChildren ();
    }


    // Node implementation .......................................................

    /** Creates properties of this node */
    private void createProperties () {
        // default sheet with "properties" property set // NOI18N
        Sheet sheet = Sheet.createDefault ();
        Sheet.Set props = sheet.get (Sheet.PROPERTIES);
        props.put (new PropertySupport.ReadOnly (
                       PROPERTY_CLASS_NAME,
                       String.class,
                       getLocalizedString ("PROP_call_stack_class_name"),
                       getLocalizedString ("HINT_call_stack_class_name")
                   ) {
                       public Object getValue () throws InvocationTargetException {
                           try {
                               return stackFrame.getClassName ();
                           } catch (Exception e) {
                               throw new InvocationTargetException (e);
                           }
                       }
                   });
        props.put (new PropertySupport.ReadOnly (
                       PROPERTY_METHOD_NAME,
                       String.class,
                       getLocalizedString ("PROP_call_stack_method_name"),
                       getLocalizedString ("HINT_call_stack_method_name")
                   ) {
                       public Object getValue () throws InvocationTargetException {
                           try {
                               return stackFrame.getMethodName ();
                           } catch (Exception e) {
                               throw new InvocationTargetException (e);
                           }
                       }
                   });
        props.put (new PropertySupport.ReadOnly (
                       PROPERTY_LINE_NUMBER,
                       Integer.TYPE,
                       getLocalizedString ("PROP_call_stack_line_number"),
                       getLocalizedString ("HINT_call_stack_line_number")
                   ) {
                       public Object getValue () throws InvocationTargetException {
                           try {
                               return new Integer (stackFrame.getLineNumber ());
                           } catch (Exception e) {
                               throw new InvocationTargetException (e);
                           }
                       }
                   });
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
        if (staticActions == null)
            staticActions = new SystemAction[] {
                                SystemAction.get (GoToSourceAction.class),
                                null,
                                SystemAction.get (ToolsAction.class),
                                SystemAction.get (PropertiesAction.class),
                            };
        return staticActions;
    }

    /**
    * Returns default action.
    */
    public SystemAction getDefaultAction () {
        return SystemAction.get (GoToSourceAction.class);
    }


    // GoToSourceCookie implementation ...........................................

    /**
    * Goes to source.
    */
    public void goToSource () {
        try {
            Line l = stackFrame.getLine ();
            if (l != null) l.show (Line.SHOW_GOTO);
        } catch (DebuggerException e) {
        }
    }

    /**
    * Returns true if source is availabled.
    */
    public boolean canGoToSource () {
        try {
            Line l = stackFrame.getLine ();
            if (l != null) return true;
        } catch (DebuggerException e) {
        }
        return false;
    }


    // helper methods ............................................................

    /**
    * @return localized string.
    */
    static String getLocalizedString (String s) {
        if (bundle == null)
            bundle = NbBundle.getBundle (CallStackLineNode.class);
        return bundle.getString (s);
    }

    /** Sets a new CallStackFrame and updates node. */
    void updateCallStackFrame (CallStackFrame stackFrame) {
        this.stackFrame = stackFrame;
        try {
            String s = stackFrame.getClassName () + '.' +
                       stackFrame.getMethodName () + " : " + // NOI18N
                       stackFrame.getLineNumber ();
            setDisplayName (s);
            setName (s);
        } catch (DebuggerException e) {
            String s = e.getMessage ();
            setDisplayName (s);
            setName (s);
        }
        changeChildren ();
    }

    /** Returns index in callstack */
    int getIndex () {
        return index;
    }

    void changeChildren () {
        // set locales as keys into our children
        CallStackLineChildren myChildren = (CallStackLineChildren) getChildren ();
        myChildren.setMyKeys (stackFrame.getLocales ());
    }


    // innerclass ................................................................

    /** Empty list of children. Does not allow anybody to insert a node.
    * Treated especially in the attachTo method.
    */
    private static final class CallStackLineChildren extends Children.Keys implements LeafRefresher {
        /** CallStack frame of this CallStackLineNode. */
        private CallStackFrame                 stackFrame;

        /** Where to add variables. */
        private VariableHome                   variableHome;

        /** Constructs children asociated with given stack frame */
        CallStackLineChildren (
            VariableHome variableHome,
            final CallStackFrame stackFrame
        ) {
            super ();
            this.variableHome = variableHome;
            this.stackFrame = stackFrame;
        }

        /** Creates nodes for given key.
        * @param key the key that is used
        * @return array of nodes representing the key
        */
        protected Node[] createNodes (final Object key) {
            return new Node[] {
                       new VariableNode (variableHome, (AbstractVariable)key)
                   };
        }

        /** Accessor for LocalesRootNode outer class */
        private void setMyKeys (final Object[] keys) {
            setKeys (keys);
        }

        // interface LeafRefresher

        /** Accessor for VariableNode class */
        public void refreshMyKey (Object key) {
            refreshKey (key);
        }

    }
}

/*
 * Log
 *  11   Gandalf-post-FCS1.9.3.0     3/28/00  Daniel Prusa    
 *  10   Gandalf   1.9         1/14/00  Daniel Prusa    NOI18N
 *  9    Gandalf   1.8         1/13/00  Daniel Prusa    NOI18N
 *  8    Gandalf   1.7         1/12/00  Daniel Prusa    setName (String) added -
 *       bad serialization fixed
 *  7    Gandalf   1.6         12/7/99  Daniel Prusa    Bug 4482 fixed
 *  6    Gandalf   1.5         11/8/99  Jan Jancura     Somma classes renamed
 *  5    Gandalf   1.4         11/5/99  Jan Jancura     Default action updated
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/28/99  Jan Jancura     
 *  2    Gandalf   1.1         9/15/99  Jan Jancura     
 *  1    Gandalf   1.0         8/17/99  Jan Jancura     
 * $
 */
