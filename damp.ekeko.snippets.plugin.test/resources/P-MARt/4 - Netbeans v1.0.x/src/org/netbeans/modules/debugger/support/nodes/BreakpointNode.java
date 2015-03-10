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

import org.openide.TopManager;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.debugger.DebuggerException;
import org.openide.cookies.LineCookie;
import org.openide.debugger.Breakpoint;
import org.openide.debugger.Debugger;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.actions.DeleteAction;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.CallableSystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.BeanNode;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.PropertySupport;
import org.openide.nodes.Sheet;

import org.netbeans.modules.debugger.support.actions.GoToSourceAction;
import org.netbeans.modules.debugger.support.actions.GoToSourceCookie;
import org.netbeans.modules.debugger.support.AbstractDebugger;
import org.netbeans.modules.debugger.support.CoreBreakpoint;
import org.netbeans.modules.debugger.support.DebuggerAdapter;


/**
* This class represents breakpoint as a Node.
* This class is final only for performance reasons,
* can be happily unfinbaled if desired.
*
* @author   Jan Jancura
*/
public final class BreakpointNode extends AbstractNode implements
    GoToSourceCookie {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -8680621542479107034L;


    // static variables ..........................................................

    private static final String       ICON_BASE =
        "/org/netbeans/modules/debugger/resources/breakpoint"; // NOI18N
    /** Property name constant */
    public static final String        PROP_LINE_NUMBER = "lineNumber"; // NOI18N
    /** Property name constant */
    public static final String        PROP_CLASS_NAME = "className"; // NOI18N
    /** Property name constant */
    public static final String        PROP_METHOD_NAME = "methodName"; // NOI18N
    private static SystemAction []    staticActions;
    private static ResourceBundle     bundle;


    // variables .................................................................

    /* Breakpoint asociated to this node. */
    private CoreBreakpoint            breakpoint = null;
    /* Current instance of debugger. */
    private AbstractDebugger          debugger = null;


    // init ......................................................................

    /**
    * Creates empty BreakpointNode.
    */
    public BreakpointNode (final CoreBreakpoint breakpoint) {
        super (Children.LEAF);
        this.breakpoint = breakpoint;
        init ();
    }

    private void init () {
        try {
            debugger = (AbstractDebugger) TopManager.getDefault ().getDebugger ();
        } catch (DebuggerNotFoundException e) {
        }
        createProperties ();
        getCookieSet ().add (this);
        parameterChanged ();
        breakpoint.addPropertyChangeListener (new PropertyChangeListener () {
                                                  public void propertyChange (PropertyChangeEvent e) {
                                                      if ( (e.getPropertyName () != null) &&
                                                              ( e.getPropertyName ().equals (CoreBreakpoint.PROP_EVENT) ||
                                                                e.getPropertyName ().equals (CoreBreakpoint.PROP_ACTIONS)
                                                              )
                                                         ) createProperties ();
                                                      else
                                                          propertyChanged (e.getPropertyName ());
                                                  }
                                              });
    }


    // implementation of Node ....................................................

    /** Creates properties for this node */
    private void createProperties () {
        // default sheet with "properties" property set // NOI18N
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = Sheet.createPropertiesSet ();

        // Properties of Event
        CoreBreakpoint.Event be = breakpoint.getEvent (debugger);
        if (be != null) {
            ps.put (be.getProperties ());
        }

        // Enabled, Valid and Condition properties
        ps.put (new PropertySupport.ReadWrite (
                    breakpoint.PROP_ENABLED,
                    Boolean.TYPE,
                    getLocalizedString ("PROP_breakpoint_enabled"),
                    getLocalizedString ("HINT_breakpoint_enabled")
                ) {
                    public Object getValue () {
                        return new Boolean (breakpoint.isEnabled ());
                    }
                    public void setValue (Object val) throws IllegalArgumentException {
                        try {
                            breakpoint.setEnabled (((Boolean)val).booleanValue ());
                        } catch (ClassCastException e) {
                            throw new IllegalArgumentException ();
                        }
                    }
                });
        ps.put (new PropertySupport.ReadOnly (
                    Breakpoint.PROP_VALID,
                    Boolean.TYPE,
                    getLocalizedString ("PROP_breakpoint_valid"),
                    getLocalizedString ("HINT_breakpoint_valid")
                ) {
                    public Object getValue () {
                        return new Boolean (breakpoint.isValid ());
                    }
                });

        try {
            AbstractDebugger debugger = (AbstractDebugger) TopManager.getDefault ().
                                        getDebugger();
            if (debugger.supportsExpressions ()) {
                ps.put (new PropertySupport.ReadWrite (
                            breakpoint.PROP_CONDITION,
                            java.lang.String.class,
                            getLocalizedString ("PROP_condition"),
                            getLocalizedString ("HINT_condition")
                        ) {
                            public Object getValue () {
                                return breakpoint.getCondition ();
                            }
                            public void setValue (Object val) throws IllegalArgumentException {
                                try {
                                    breakpoint.setCondition ((String)val);
                                } catch (ClassCastException e) {
                                    throw new IllegalArgumentException ();
                                }
                            }
                        });
            }
        }
        catch (DebuggerNotFoundException e) {
        }

        // Properties of Actions
        CoreBreakpoint.Action[] ba = breakpoint.getActions ();
        if (ba != null) {
            int i, k = ba.length;
            for (i = 0; i < k; i++)
                ps.put (ba [i].getProperties ());
        }

        // and set new sheet
        sheet.put (ps);
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
        if (staticActions == null) {
            staticActions = new SystemAction[] {
                                SystemAction.get (GoToSourceAction.class),
                                null,
                                SystemAction.get (DeleteAction.class),
                                null,
                                SystemAction.get (ToolsAction.class),
                                SystemAction.get (PropertiesAction.class),
                            };
        }
        return staticActions;
    }

    /**
    * breakpoint can be removed.
    *
    * @return <CODE>true</CODE>
    */
    public boolean canDestroy () {
        return true;
    }

    /**
    * Deletes breakpoint and removes the node too.
    * Ovverrides destroy() from abstract node.
    */
    public void destroy () throws IOException {
        // remove breakpoint
        breakpoint.remove ();
        // remove node
        super.destroy ();
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
        Line l = breakpoint.getLine ();
        if (l == null) return;
        l.show (Line.SHOW_GOTO);
    }

    /**
    * Returns true if source is availabled.
    */
    public boolean canGoToSource () {
        return breakpoint.getLine () != null;
    }


    // other methods .............................................................

    /**
    * @return localized string.
    */
    static String getLocalizedString (String s) {
        if (bundle == null)
            bundle = NbBundle.getBundle (BreakpointNode.class);
        return bundle.getString (s);
    }

    /**
    * Returns breakpoint.
    */
    Breakpoint getBreakpoint () {
        return breakpoint;
    }

    /**
    * Sets display name and fires property change.
    */
    void propertyChanged (String propertyName) {
        if ( (propertyName != null) && propertyName.
                equals (CoreBreakpoint.PROP_EVENT)
           ) createProperties ();
        parameterChanged ();
        firePropertyChange (null, null, null);
    }

    /**
    * Sets display name and fires property change.
    */
    void parameterChanged () {
        CoreBreakpoint.Event event = breakpoint.getEvent (debugger);
        if (event == null) return;
        String s = event.getDisplayName ();
        setName (s);
        setDisplayName (s);
        s = event.getIconBase ();
        if (s == null) s = ICON_BASE;
        setIconBase (s);
    }

    /**
    * Returns human presentable name of this breakpoint containing
    * informations about lineNumber, className e.t.c.
    *
    * @return human presentable name of this breakpoint.
    */
    /*  public String getName () {
        int l = breakpoint.getLineNumber ();
        String c = breakpoint.getClassName ();
        String m = breakpoint.getMethodName ();
        if ((c == null) || (c.length () < 1)) return "???";
        if (l >= 0) return c + ": " + l;
        if ((m != null) && (m.length () > 0)) return c + "." + m;
        return c;
      }*/
}

/*
 * Log
 *  10   Gandalf-post-FCS1.8.4.0     3/28/00  Daniel Prusa    
 *  9    Gandalf   1.8         1/14/00  Daniel Prusa    NOI18N
 *  8    Gandalf   1.7         1/13/00  Daniel Prusa    NOI18N
 *  7    Gandalf   1.6         12/10/99 Jan Jancura     Bugs in support of 
 *       refreshing of properties
 *  6    Gandalf   1.5         11/29/99 Jan Jancura     Support for 
 *       modifications of property set.
 *  5    Gandalf   1.4         11/8/99  Jan Jancura     Somma classes renamed
 *  4    Gandalf   1.3         11/5/99  Jan Jancura     Default action updated
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         9/28/99  Jan Jancura     
 *  1    Gandalf   1.0         8/17/99  Jan Jancura     
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    popup menu improved
 */
