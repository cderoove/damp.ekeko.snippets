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
import java.beans.*;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import java.lang.ref.WeakReference;

import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.actions.PropertiesAction;
import org.openide.actions.ToolsAction;
import org.openide.debugger.DebuggerException;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.debugger.Debugger;
import org.openide.nodes.Node;
import org.openide.nodes.Sheet;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.PropertySupport;
import org.openide.text.Line;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
import org.openide.util.actions.CallableSystemAction;

import org.netbeans.modules.debugger.support.DebuggerAdapter;
import org.netbeans.modules.debugger.support.AbstractDebugger;
import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.actions.SuspendResumeAction;
import org.netbeans.modules.debugger.support.actions.SwitchOnAction;
import org.netbeans.modules.debugger.support.actions.SuspendCookie;
import org.netbeans.modules.debugger.support.actions.SwitchOnCookie;
import org.netbeans.modules.debugger.support.actions.GoToSourceCookie;
import org.netbeans.modules.debugger.support.actions.GoToSourceAction;


/**
* This class representates threads and threadGroups as a Node.
* This class is final only for performance reasons, can be happily
* unfinaled if desired.
*
* @author   Jan Jancura
*/
public final class ThreadNode extends AbstractNode implements SuspendCookie,
    SwitchOnCookie, GoToSourceCookie {

    /** generated Serialized Version UID */
    static final long serialVersionUID = 2202990736781011814L;


    // static ....................................................................

    /** Property names */
    public static final String        PROP_THREAD_NAME         = "threadName"; // NOI18N
    public static final String        PROP_THREAD_STATE        = "threadState"; // NOI18N
    public static final String        PROP_THREAD_CLASS        = "threadClass"; // NOI18N
    public static final String        PROP_THREAD_METHOD       = "threadMethod"; // NOI18N
    public static final String        PROP_THREAD_LINE         = "threadLine"; // NOI18N
    public static final String        PROP_THREAD_STACK_DEPTH  = "threadStackDepth"; // NOI18N
    public static final String        PROP_THREAD_FRAME_INDEX  = "threadFrameIndex"; // NOI18N
    public static final String        PROP_THREAD_SUSPENDED    = "threadSuspended"; // NOI18N

    public static final int           STATE_MAIN               = 1;
    public static final int           STATE_STOPPED            = 2;

    protected static String           ICON_CURRENT             =
        "/org/netbeans/modules/debugger/resources/threadCurrent"; // NOI18N
    protected static String           ICON_SUSPENDED           =
        "/org/netbeans/modules/debugger/resources/threadSuspended"; // NOI18N
    protected static String           ICON_RUNNING             =
        "/org/netbeans/modules/debugger/resources/threadRunning"; // NOI18N

    /** Icons for threads. */
    protected static Image            threadFolderIcon;
    protected static Image            threadFolderIcon32;
    protected static Image            threadCurrentIcon;
    protected static Image            threadCurrentIcon32;
    protected static Image            threadRunningIcon;
    protected static Image            threadRunningIcon32;

    /** System actions of this node */
    private static SystemAction[]     staticActions;
    private static ResourceBundle     bundle;


    // variables .................................................................

    private AbstractDebugger          debugger;
    private AbstractThread            thread;
    private ThreadListener            threadListener;
    private Node[]                    children = null;
    private CallStackNode             callStack;
    private LocalesRootNode           locales;
    private int                       state = 4;
    /** Where to add variables. */
    private VariableHome              variableHome;
    private boolean                   oldSuspended = false;


    // init ......................................................................

    /**
    * Creates ThreadNode.
    */
    public ThreadNode (VariableHome variableHome, AbstractThread thread) {
        super (new Children.Array());
        this.variableHome = variableHome;
        this.thread = thread;
        initialize();
        //S ystem.out.println ("Thread created " + num++); // NOI18N
    }
    //static int num = 0;

    /**
    * Remove listeners.
    */
    protected void finalize () {
        //S ystem.out.println ("Thread destroyed " + --num); // NOI18N
        debugger.removePropertyChangeListener (threadListener);
        thread.removePropertyChangeListener (threadListener);
    }

    /** Performs basic initialization of children, listeners, etc. */
    private void initialize () {
        initializeChildren ();
        setName ();
        createProperties ();
        getCookieSet ().add (this);
        try {
            debugger = (AbstractDebugger) TopManager.getDefault ().getDebugger ();
        } catch (DebuggerNotFoundException ex) {
            TopManager.getDefault ().notifyException (ex);
            return;
        }
        threadListener = new ThreadListener (
                             this
                         );
        debugger.addPropertyChangeListener (threadListener);
        thread.addPropertyChangeListener (threadListener);
    }

    /** Initializes children (subnodes) of this node */
    private void initializeChildren () {
        Children myChildren = getChildren ();
        myChildren.add (
            new Node[] {
                callStack = new CallStackNode (variableHome, thread),
                locales = new LocalesRootNode (variableHome, thread)
            }
        );
    }


    // Node implementation .......................................................

    /** Method that prepares properties. Called from initialize.
    */
    private void createProperties () {
        // default sheet with "properties" property set // NOI18N
        Sheet sheet = Sheet.createDefault();
        Sheet.Set props = sheet.get(Sheet.PROPERTIES);
        props.put (new PropertySupport.ReadOnly (
                       PROP_THREAD_NAME,
                       String.class,
                       getLocalizedString ("PROP_thread_name"),
                       getLocalizedString ("HINT_thread_name")
                   ) {
                       public Object getValue () throws InvocationTargetException {
                           try {
                               return thread.getName ();
                           } catch (java.lang.Exception e) {
                               throw new InvocationTargetException (e);
                           }
                       }
                   });
        props.put (new PropertySupport.ReadOnly (
                       PROP_THREAD_STATE,
                       String.class,
                       getLocalizedString ("PROP_thread_state"),
                       getLocalizedString ("HINT_thread_state")
                   ) {
                       public Object getValue () throws InvocationTargetException {
                           try {
                               return thread.getState ();
                           } catch (java.lang.Exception e) {
                               throw new InvocationTargetException (e);
                           }
                       }
                   });
        props.put (new PropertySupport.ReadOnly (
                       PROP_THREAD_CLASS,
                       String.class,
                       getLocalizedString ("PROP_thread_class"),
                       getLocalizedString ("HINT_thread_class")
                   ) {
                       public Object getValue () throws InvocationTargetException {
                           try {
                               return thread.getClassName ();
                           } catch (java.lang.Exception e) {
                               throw new InvocationTargetException (e);
                           }
                       }
                   });
        props.put (new PropertySupport.ReadOnly (
                       PROP_THREAD_METHOD,
                       String.class,
                       getLocalizedString ("PROP_thread_method"),
                       getLocalizedString ("HINT_thread_method")
                   ) {
                       public Object getValue () throws InvocationTargetException {
                           try {
                               return thread.getMethod ();
                           } catch (java.lang.Exception e) {
                               throw new InvocationTargetException (e);
                           }
                       }
                   });
        props.put (new PropertySupport.ReadOnly (
                       PROP_THREAD_LINE,
                       Integer.TYPE,
                       getLocalizedString ("PROP_thread_line"),
                       getLocalizedString ("HINT_thread_line")
                   ) {
                       public Object getValue () throws InvocationTargetException {
                           try {
                               return new Integer (thread.getLineNumber ());
                           } catch (java.lang.Exception e) {
                               throw new InvocationTargetException (e);
                           }
                       }
                   });
        props.put (new PropertySupport.ReadOnly (
                       PROP_THREAD_STACK_DEPTH,
                       Integer.TYPE,
                       getLocalizedString ("PROP_thread_stack_depth"),
                       getLocalizedString ("HINT_thread_stack_depth")
                   ) {
                       public Object getValue () throws InvocationTargetException {
                           try {
                               return new Integer (thread.getStackDepth ());
                           } catch (java.lang.Exception e) {
                               throw new InvocationTargetException (e);
                           }
                       }
                   });
        props.put (new PropertySupport.ReadWrite (
                       PROP_THREAD_SUSPENDED,
                       Boolean.TYPE,
                       getLocalizedString ("PROP_thread_suspended"),
                       getLocalizedString ("HINT_thread_suspended")
                   ) {
                       public Object getValue () throws InvocationTargetException {
                           try {
                               return new Boolean (thread.isSuspended ());
                           } catch (java.lang.Exception e) {
                               throw new InvocationTargetException (e);
                           }
                       }
                       public void setValue (Object val) throws
                           InvocationTargetException {
                           if (!(val instanceof Boolean))
                               throw new IllegalArgumentException ();
                           setSuspended (((Boolean)val).booleanValue ());
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
                                SystemAction.get (SwitchOnAction.class),
                                SystemAction.get (GoToSourceAction.class),
                                SystemAction.get (SuspendResumeAction.class),
                                null,
                                SystemAction.get (ToolsAction.class),
                                SystemAction.get (PropertiesAction.class)
                            };
        return staticActions;
    }

    /**
    * Returns default action.
    */
    public SystemAction getDefaultAction () {
        return SystemAction.get (SwitchOnAction.class);
    }

    public void destroy () throws java.io.IOException {
        super.destroy ();
        debugger.removePropertyChangeListener (threadListener);
        thread.removePropertyChangeListener (threadListener);
    }


    // SwitchOnCookie ........................................................................................

    /**
    * Sets this thread to be suspended.
    */
    public boolean canSetCurrent () {
        return (!thread.isCurrent ()) && isSuspended ();
    }

    /**
    * Sets this thread to be current.
    */
    public void setCurrent () {
        thread.setCurrent (true);
    }


    // SuspendCookie .............................................................

    /**
    * Sets this thread to be suspended.
    */
    public void setSuspended (boolean b) {
        try {
            thread.setSuspended (b);
            changeChildren ();
        } catch (DebuggerException e) {
            TopManager.getDefault ().notify (
                new NotifyDescriptor.Exception (
                    e.getTargetException (),
                    getLocalizedString ("EXC_Debugger") + ": " + e.getMessage ()
                )
            );
        }
    }

    /**
    * Sets this thread to be suspended.
    */
    public boolean isSuspended () {
        try {
            return thread.isSuspended ();
        } catch (DebuggerException e) {
            return false;
        }
    }


    // GoToSourceCookie implementation ...........................................

    /**
    * Goes to source.
    */
    public void goToSource () {
        try {
            Line l = thread.getLine ();
            if (l != null) l.show (Line.SHOW_GOTO);
        } catch (DebuggerException e) {
        }
    }

    /**
    * Returns true if source is availabled.
    */
    public boolean canGoToSource () {
        try {
            Line l = thread.getLine ();
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
            bundle = NbBundle.getBundle (ThreadNode.class);
        return bundle.getString (s);
    }

    /**
    * Sets curent icon (running thread, current thread, suspended thread).
    */
    void refreshIcon () {
        if ((state & STATE_MAIN) != 0) {
            setIconBase (ICON_CURRENT); // NOI18N
        } else
            if ((state & STATE_STOPPED) != 0) {
                setIconBase (ICON_SUSPENDED); // NOI18N
            } else {
                setIconBase (ICON_RUNNING); // NOI18N
            }
    }

    /** equals if thread equals o.thread
    * @param o
    */
    public final boolean equals (Object o) {
        return o instanceof ThreadNode && ((ThreadNode) o).thread.equals (thread);
    }

    /**
    * @return hashCode of thread
    */
    public final int hashCode () {
        return thread.hashCode ();
    }

    /**
    * Updates name & icon, and fires change.
    */
    void changeProperties () {
        setName ();
        firePropertyChange (null, null, null);
    }

    /**
    * Updates name & icon.
    */
    void setName () {
        // read thread state
        int newS = 0;
        if (thread.isCurrent ())
            newS = STATE_MAIN;
        try {
            if (thread.isSuspended ()) newS |= STATE_STOPPED;
        } catch (java.lang.Exception e) {
        }

        // change icon
        if (state != newS) {
            state = newS;
            refreshIcon ();
        }

        // update name
        try {
            String s = thread.getName () + " (" + thread.getState () + ")"; // NOI18N
            setDisplayName (s);
            setName (s);
        } catch (org.openide.debugger.DebuggerException e) {
            String s = getLocalizedString ("CTL_Thread");
            setDisplayName (s);
            setName (s);
        }
    }

    /**
    * Refresh locales & callstack.
    */
    void changeChildren () {
        boolean s = isSuspended ();

        if (oldSuspended || s) {
            if (s || (debugger.getLastAction () == AbstractDebugger.ACTION_GO)) {
                locales.changeChildren ();
                callStack.changeChildren ();
            }
        }
        oldSuspended = s;
    }

    AbstractThread getDebuggerThread () {
        return thread;
    }


    // innerclasses ..............................................................

    /**
    * Listens on thread and debugger.
    */
    private static class ThreadListener implements PropertyChangeListener {

        private WeakReference             node;
        private AbstractDebugger          debugger;
        private AbstractThread            thread;


        ThreadListener (ThreadNode n) {
            node = new WeakReference (n);
            debugger = n.debugger;
            thread = n.thread;
        }

        public void propertyChange (PropertyChangeEvent e) {
            ThreadNode tn = (ThreadNode) node.get ();
            if (tn == null) {
                debugger.removePropertyChangeListener (this);
                thread.removePropertyChangeListener (this);
                return;
            }
            if (e.getSource () instanceof AbstractDebugger) {
                if (!e.getPropertyName ().equals (debugger.PROP_STATE)) return;
                int debuggerState = debugger.getState ();
                if (debuggerState == Debugger.DEBUGGER_STARTING) return;
                tn.changeChildren ();
            } else {
                tn.changeProperties ();
            }
        }
    }
}

/*
 * Log
 *  13   Gandalf-post-FCS1.11.3.0    3/28/00  Daniel Prusa    
 *  12   Gandalf   1.11        1/14/00  Daniel Prusa    NOI18N
 *  11   Gandalf   1.10        1/13/00  Daniel Prusa    NOI18N
 *  10   Gandalf   1.9         1/6/00   Jan Jancura     Refresh of Threads & 
 *       Watches, Weakization of Nodes
 *  9    Gandalf   1.8         11/8/99  Jan Jancura     Somma classes renamed
 *  8    Gandalf   1.7         11/5/99  Jan Jancura     Default action updated
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         10/11/99 Jan Jancura     Bug in refreshing 
 *       callstacks
 *  5    Gandalf   1.4         10/1/99  Jan Jancura     Current thread & bug 
 *       4108
 *  4    Gandalf   1.3         9/28/99  Jan Jancura     
 *  3    Gandalf   1.2         9/15/99  Jan Jancura     
 *  2    Gandalf   1.1         8/18/99  Jan Jancura     Localization & Current 
 *       thread & Current session
 *  1    Gandalf   1.0         8/17/99  Jan Jancura     
 * $
 */
