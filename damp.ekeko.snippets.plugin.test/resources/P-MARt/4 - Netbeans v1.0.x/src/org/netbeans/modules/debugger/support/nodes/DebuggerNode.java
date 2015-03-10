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
import java.util.Vector;
import java.util.Collection;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.loaders.DataNode;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectExistsException;
import org.openide.debugger.Debugger;
import org.openide.debugger.DebuggerException;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.nodes.*;

import org.netbeans.modules.debugger.support.DebuggerAdapter;
import org.netbeans.modules.debugger.support.AbstractDebugger;
import org.netbeans.modules.debugger.support.DebuggerModule;

/** The node representing debugger.
*
* @author Jan Jancura, Petr Hamernik, Jaroslav Tulach
*/
public class DebuggerNode extends AbstractNode {


    // static ....................................................................

    /** generated Serialized Version UID */
    static final long serialVersionUID = 6394601904375687521L;

    public static final String    PROP_SHOW_MESSAGES      = "showMessages"; // NOI18N
    public static final String    PROP_SHOW_IN_EDITOR     = "showInEditor"; // NOI18N
    public static final String    PROP_REMOTE_DEBUGGING   = "remoteDebugging"; // NOI18N
    public static final String    PROP_TOTAL_MEMORY       = "totalMemory"; // NOI18N
    public static final String    PROP_FREE_MEMORY        = "freeMemory"; // NOI18N
    public static final String    PROP_SOURCE_PATH        = "sourcePath"; // NOI18N
    public static final String    PROP_EXCEPTION_CATCH_LIST = "exceptionCatchList"; // NOI18N
    public static final String    PROP_DEBUGGER_STATE     = "debuggerState"; // NOI18N

    private static final String   ICON_BASE =
        "/org/netbeans/core/resources/debugger"; // NOI18N

    private static ResourceBundle bundle;

    /** Debugger state property value */
    public static final String    DEBUGGER_NOT_RUNNING =
        getLocalizedString ("VAL_NotRrunning");
    /** Debugger state property value */
    public static final String    DEBUGGER_STARTING =
        getLocalizedString ("VAL_Starting");
    /** Debugger state property value */
    public static final String    DEBUGGER_RUNNING =
        getLocalizedString ("VAL_Running");
    /** Debugger state property value */
    public static final String    DEBUGGER_STOPPED =
        getLocalizedString ("VAL_Stopped");


    // variables .................................................................

    /** Reference to the debugger */
    private transient AbstractDebugger debugger;


    // init ......................................................................

    /** constructor.
    */
    public DebuggerNode () {
        super (new Children.Array ());
        String name = getLocalizedString ("CTL_Debugger");
        setDisplayName (name);
        setName (name);
        setShortDescription (getLocalizedString ("HINT_Debugger"));
        setIconBase (ICON_BASE);
        initialize ();
    }

    /** Performs initialization - subnodes, properties, listeners */
    private void initialize () {
        // initialize chidren
        Collection c = DebuggerModule.getNodes ();

        getChildren ().add (
            (Node []) c.toArray (new Node [c.size ()])
        );
        // properties...
        createProperties();
    }


    // node implementation .......................................................

    /** Creates property sets */
    protected void createProperties () {
        // default sheet with "properties" property set // NOI18N
        Sheet sheet = Sheet.createDefault();
        Sheet.Set ps = sheet.get (Sheet.PROPERTIES);
        ps.put(new PropertySupport.ReadOnly (
                   PROP_DEBUGGER_STATE,
                   String.class,
                   getLocalizedString ("PROP_debugger_state"),
                   getLocalizedString ("HINT_debugger_state")
               ) {
                   public Object getValue () throws IllegalAccessException,
                       IllegalArgumentException, InvocationTargetException {
                       return getDebuggerState ();
                   }
               });
        // and set new sheet
        setSheet (sheet);
    }


    // other methods .............................................................

    /**
    * @return localized string.
    */
    static String getLocalizedString (String s) {
        if (bundle == null)
            bundle = NbBundle.getBundle (DebuggerNode.class);
        return bundle.getString (s);
    }

    private AbstractDebugger getDebugger () {
        if (debugger != null) return debugger;

        try {
            debugger = (AbstractDebugger) TopManager.getDefault ().getDebugger ();
        } catch (DebuggerNotFoundException e) {
            return null;
        }
        debugger.addDebuggerListener (
            new DebuggerAdapter () {
                public void debuggerStateChanged (int debuggerState) {
                    DebuggerNode.this.debuggerStateChanged ();
                }
            }
        );
        return debugger;
    }

    /**
    * Display debugger messages in the debugger output window property.
    *
    * @return true if messages are displayed in the debugger output window.
    */
    public boolean isShowMessages () {
        return getDebugger ().isShowMessages ();
    }

    /**
    * Display debugger messages in the debugger output window property.
    *
    * @param b true if messages are displayed in the debugger output window.
    */
    public void setShowMessages (boolean showMessages) {
        getDebugger ().setShowMessages (showMessages);
    }

    /**
    * Returns state of debugger.
    *
    * &return state of debugger
    */
    public String getDebuggerState () {
        if (getDebugger () == null) return ""; // NOI18N
        switch (debugger.getState ()) {
        case Debugger.DEBUGGER_NOT_RUNNING: return DEBUGGER_NOT_RUNNING;
        case Debugger.DEBUGGER_STARTING:    return DEBUGGER_STARTING;
        case Debugger.DEBUGGER_RUNNING:     return DEBUGGER_RUNNING;
        case Debugger.DEBUGGER_STOPPED:     return DEBUGGER_STOPPED;
        }
        return ""; // NOI18N
    }

    /** Fires change of state
    */
    void debuggerStateChanged () {
        firePropertyChange (null, null, null);
    }
}

/*
 * Log
 *  12   Gandalf-post-FCS1.10.3.0    3/28/00  Daniel Prusa    
 *  11   Gandalf   1.10        1/14/00  Daniel Prusa    NOI18N
 *  10   Gandalf   1.9         1/13/00  Daniel Prusa    NOI18N
 *  9    Gandalf   1.8         11/11/99 Jesse Glick     Display miscellany.
 *  8    Gandalf   1.7         11/8/99  Jan Jancura     Somma classes renamed
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         8/10/99  Jan Jancura     Deserialization of Views
 *  5    Gandalf   1.4         8/9/99   Jan Jancura     Functionality of modes 
 *       moved to Module
 *  4    Gandalf   1.3         7/30/99  David Simonek   names of the nodes 
 *       updated, needed for nodes serialization 
 *  3    Gandalf   1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         6/4/99   Jan Jancura     
 *  1    Gandalf   1.0         6/1/99   Jan Jancura     
 * $
 * Beta Change History:
 *  0    Tuborg    0.15        --/--/98 Petr Hamernik   initializing of subnodes changed
 *  0    Tuborg    0.17        --/--/98 Jaroslav Tulach new node model
 *  0    Tuborg    0.18        --/--/98 Petr Hamernik   data object deleted...
 */
