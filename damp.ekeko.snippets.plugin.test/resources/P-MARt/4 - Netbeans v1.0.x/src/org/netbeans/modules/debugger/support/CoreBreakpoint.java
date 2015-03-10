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

package org.netbeans.modules.debugger.support;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.ResourceBundle;
import java.util.ArrayList;
import java.util.Arrays;
import javax.swing.JComponent;

import org.openide.TopManager;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.text.Line;
import org.openide.cookies.LineCookie;
import org.openide.cookies.DebuggerCookie;
import org.openide.debugger.Breakpoint;
import org.openide.debugger.DebuggerNotFoundException;
import org.openide.debugger.Watch;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.text.Line;
import org.openide.src.ConstructorElement;

/**
* Standart implementation of breakpoint interface.
* @see org.openide.debugger.Breakpoint
*
* @author   Jan Jancura
* @version  0.23, May 26, 1998
*/
public class CoreBreakpoint extends Breakpoint implements java.io.Serializable {
    /** generated Serialized Version UID */
    static final long               serialVersionUID = 3686204002781932191L;

    /** Property name constant */
    public static final String      PROP_LINE_NUMBER = "lineNumber"; // NOI18N
    /** Property name constant */
    public static final String      PROP_CLASS_NAME = "className"; // NOI18N
    /** Property name constant */
    public static final String      PROP_METHOD_NAME = "methodName"; // NOI18N
    /** Property name constant */
    public static final String      PROP_EVENT = "event"; // NOI18N
    /** Property name constant */
    public static final String      PROP_ACTIONS = "actions"; // NOI18N
    /** Property name constant */
    public static final String      PROP_HIDDEN = "hidden"; // NOI18N
    /** Property name constant */
    public static final String      PROP_CONDITION = "condition"; // NOI18N
    /** Property name constant */
    //  public static final String      PROP_ACTION = "action"; // NOI18N

    /** bundle to obtain text information from */
    private static ResourceBundle   bundle = org.openide.util.NbBundle.getBundle
            (CoreBreakpoint.class);


    // private variables .....................................................

    protected transient AbstractDebugger  debugger;
    private boolean                   enabled = true;
    private boolean                   valid = false;
    private boolean                   hidden = false;
    private String                    condition = new String ("");
    protected transient ArrayList     lines = null;
    protected Event                   event;
    private Action[]                  actions;

    transient PropertyChangeSupport   pcs;


    // init ....................................................................

    /**
    * Non public constructor called from the AbstractDebugger only.
    * User must create breakpoint from Debugger.getNewBreakpoint () method.
    */
    protected CoreBreakpoint (AbstractDebugger debugger) {
        this.debugger = debugger;
        pcs = new PropertyChangeSupport (this);
    }

    /**
    * Non public constructor called from the AbstractDebugger only.
    * User must create breakpoint from Debugger.getNewBreakpoint () method.
    */
    protected CoreBreakpoint (AbstractDebugger debugger, boolean hidden) {
        this (debugger);
        this.hidden = hidden;
    }

    protected void init () throws java.io.IOException {
        try {
            debugger = (AbstractDebugger) TopManager.getDefault ().getDebugger ();
        } catch (DebuggerNotFoundException e) {
            throw new java.io.IOException ();
        }
        if (condition == null) condition = "";
        pcs = new PropertyChangeSupport (this);
        if (enabled) setBreakpoint ();
    }

    private void readObject (java.io.ObjectInputStream in)
    throws java.io.IOException, ClassNotFoundException {
        in.defaultReadObject ();
        init ();
    }


    // interface Breakpoint .........................................................

    /** Destroys the breakpoint. Removes it from the list of all breakpoints in the system.
    */
    public void remove () {
        //S ystem.out.println("CoreBreakpoint.remove "); // NOI18N
        clearBreakpoint ();
        debugger.removeBreakpoint (this);
    }

    /**
    * Returns the line this breakpoint is on.
    *
    * @return the line this breakpoint is on.
    */
    public Line getLine () {
        if (lines == null) return null;
        return (Line) lines.get (0);
    }

    /**
    * Returns the class name this breakpoint is in.
    *
    * @return the class name this breakpoint is in.
    */
    public String getClassName () {
        if (event instanceof ClassBreakpointEvent)
            return ((ClassBreakpointEvent) event).getClassName ();
        return null;
    }

    /**
    * Getter for the method the breakpoint is assigned to.
    *
    * @return the method element or null
    */
    public ConstructorElement getMethod () {
        return null; //[PENDING]
    }

    /**
    * Getter method for condition property.
    *
    * @return state of condition property.
    */
    public String getCondition () {
        return condition;
    }

    /**
    * Setter method for condition property.
    *
    * @param state of condition property.
    */
    public void setCondition (String expr) {
        if (condition == expr) return;
        String old = condition;
        condition = expr;
        firePropertyChange (PROP_CONDITION, old, expr);
    }

    /**
    * Getter method for enabled property.
    *
    * @return state of enabled property.
    */
    public boolean isEnabled () {
        return enabled;
    }

    /**
    * Setter method for enabled property.
    *
    * @param state of enabled property.
    */
    public void setEnabled (boolean enabled) {
        if (enabled == this.enabled) return;
        boolean old = this.enabled;
        this.enabled = enabled;
        if (enabled) setBreakpoint ();
        else clearBreakpoint ();
        firePropertyChange (PROP_ENABLED, new Boolean (old), new Boolean (enabled));
    }

    /**
    * Getter method for valid property.
    *
    * @return state of valid property.
    */
    public boolean isValid () {
        return valid;
    }

    /**
    * Adds listener on the property changing.
    */
    public synchronized void addPropertyChangeListener (PropertyChangeListener listener) {
        pcs.addPropertyChangeListener (listener);
    }

    /**
    * Removes listener on the property changing.
    */
    public synchronized void removePropertyChangeListener (PropertyChangeListener listener){
        pcs.removePropertyChangeListener (listener);
    }


    // other public methods .........................................................

    /**
    * Sets line on this breakpoint.
    */
    public void setLine (Line line) {
        clearBreakpoint ();
        if (event instanceof LineBreakpointEvent) {
            ((LineBreakpointEvent) event).setLine (line);
        } else {
            initEvent (LineBreakpointEvent.class);
            ((LineBreakpointEvent) event).setLine (line);
        }
    }

    /**
    * Sets line number of this breakpoint.
    *
    * @param lineNumber line number of this breakpoint.
    */
    public void setLineNumber (int lineNumber) {
        clearBreakpoint ();
        if (event instanceof LineBreakpointEvent) {
            ((LineBreakpointEvent) event).setLineNumber (lineNumber);
        } else {
            initEvent (LineBreakpointEvent.class);
            ((LineBreakpointEvent) event).setLineNumber (lineNumber);
        }
    }

    /**
    * Sets class name of this breakpoint.
    *
    * @param name name of class of this breakpoint.
    */
    public void setClassName (String name) {
        name = name.trim ();
        clearBreakpoint ();
        if (event instanceof ClassBreakpointEvent) {
            ((ClassBreakpointEvent) event).setClassName (name);
        } else {
            initEvent (ClassBreakpointEvent.class);
            ((ClassBreakpointEvent) event).setClassName (name);
        }
    }

    /**
    * Returns the method name this breakpoint is in.
    *
    * @return the class name this breakpoint is in.
    */
    public String getMethodName () {
        if (event instanceof MethodBreakpointEvent)
            return ((MethodBreakpointEvent) event).getMethodName ();
        return null;
    }

    /**
    * Sets breakpoint on the method with name specified.
    *
    * @param name name of method to set breakpoint on.
    */
    public void setMethodName (String name) {
        name = name.trim ();
        clearBreakpoint ();
        if (event instanceof MethodBreakpointEvent) {
            ((MethodBreakpointEvent) event).setMethodName (name);
        } else {
            initEvent (MethodBreakpointEvent.class);
            ((MethodBreakpointEvent) event).setMethodName (name);
        }
    }

    /**
    * Returns the line number this breakpoint is on.
    *
    * @return the line number this breakpoint is on.
    */
    public int getLineNumber () {
        if (event instanceof LineBreakpointEvent)
            return ((LineBreakpointEvent) event).getLineNumber ();
        return -1;
    }

    /**
    * Returns current breakpoint event for given instance of debugger (used
    * in multisession debugger implementation).
    */
    public Event getEvent (AbstractDebugger debugger) {
        return event;
    }

    /**
    * Sets current breakpoint event.
    */
    public void setEvent (Event i) {
        clearBreakpoint ();
        Event old = event;
        event = i.get (this);
        initActions ();
        setBreakpoint ();
        firePropertyChange (PROP_EVENT, old, event);
    }

    /**  Returns events available for this breakpoint.
    */
    public CoreBreakpoint.Event[] getBreakpointEvents () {
        return debugger.getBreakpointEvents ();
    }

    /**
    * Returns actions availabled for this breakpoint.
    */
    public CoreBreakpoint.Action[] getActions () {
        return actions;
    }

    /**
    * Returns action of given type.
    */
    public CoreBreakpoint.Action getAction (Class actionType) {
        int i, k = actions.length;
        for (i = 0; i < k; i++)
            if (actions [i].getClass ().isAssignableFrom (actionType))
                return actions [i];
        return null;
    }

    /**
    * Returns actions availabled for this breakpoint.
    */
    public void setActions (CoreBreakpoint.Action[] act) {
        Action[] old = actions;
        actions = act;
        int i, k = actions.length;
        for (i = 0; i < k; i ++)
            actions [i] = actions [i].get (this);
        firePropertyChange (PROP_ACTIONS, old, actions);
    }

    /**
    * Returns debugger instance.
    */
    /*  public AbstractDebugger getDebugger () {
        return debugger;
      }*/

    /**
    * Performs given event for all actions.
    */

    private void perform(Event e) {
        // [PENDING] implementation of filtering here..
        boolean performActions = true;
        AbstractDebugger debugger = (AbstractDebugger) e.getDebugger ();
        if (debugger.supportsExpressions ()) {
            AbstractWatch watch = (AbstractWatch) debugger.createWatch (condition, true);
            watch.refresh (e.getThread ());
            String type = watch.getType();
            String value = watch.getAsText();
            watch.remove ();
            if ((type != null) && type.equals ("boolean") && (value != null) && value.equals ("false")) // NOI18N
                performActions = false;
            if ((!condition.trim ().equals ("")) && ((type == null) || (!type.equals ("boolean")))) { // NOI18N
                // ivalid condition, report message ...
                final ResourceBundle bundle = org.openide.util.NbBundle.getBundle (CoreBreakpoint.class);
                debugger.println (bundle.getString ("CTL_Incorrect_condition") + ": " + // NOI18N
                                  bundle.getString ("CTL_breakpoint_at") + " " + e.getDisplayName () + ".", debugger.ERR_OUT); // NOI18N
            }
        }
        if (! performActions)
            ((StopEvent)e).stop (false);
        else { // perform actions ...
            int i, k = actions.length;
            for (i = 0; i < k; i ++) // stop actions will be performed in the end
                if (! ((actions [i]) instanceof StopAction))
                    actions [i].perform (e);
            for (i = 0; i < k; i ++)
                if ((actions [i]) instanceof StopAction)
                    actions [i].perform (e);
        }
    }


    /**
    * Sets or updates breakpoint. Calls set method of currently selected event, sets
    * value of valid property and marks lines in the editor.
    */
    protected void setBreakpoint () {
        if (!enabled) return;
        boolean valid = false;
        if (event != null) {
            if (debugger.getState () == AbstractDebugger.DEBUGGER_NOT_RUNNING)
                event.remove ();
            else
                valid = event.set ();
            Line[] l = event.getLines ();
            if (l != null)
                remark (new ArrayList (Arrays.asList (l)));
            else
                remark (null);
        }
        setValid (valid);
    }

    /**
    * Removes breakpoint from this position.
    * Clears Line and breakpoint from RemoteDebugger.
    */
    protected void clearBreakpoint () {
        mark (false);
        lines = null;
        if (event != null)
            event.remove ();
        setValid (false);
    }

    public boolean isHidden () {
        return hidden;
    }

    public void setHidden (boolean hidden) {
        if (this.hidden == hidden) return;
        this.hidden = hidden;
        Line[] l;
        if (enabled && ((l = event.getLines ()) != null))
            remark (new ArrayList (Arrays.asList (l)));
        else
            remark (null);
        firePropertyChange (PROP_HIDDEN, new Boolean (!hidden), new Boolean (hidden));
        debugger.changeHidden (this);
    }

    // private helper methods .................................................................

    /**
    * Sets property valide and fires it.
    *
    * @param valid value of this property to be set.
    */
    protected void setValid (boolean valid) {
        if (valid == this.valid) return;
        boolean old = this.valid;
        this.valid = valid;
        firePropertyChange (PROP_VALID, new Boolean (old), new Boolean (valid));
    }

    /**
    * Marks or unmarks all important lines.
    */
    protected void mark (boolean b) {
        if (lines == null) return;
        if (isHidden () && b) return;
        int i, k = lines.size ();
        for (i = 0; i < k; i++)
            ((Line) lines.get (i)).setBreakpoint (b);
    }

    /**
    * Remarks lines to new positions.
    */
    protected void remark (ArrayList n) {
        int i, k = (n == null) ? 0 : n.size ();
        if (!isHidden ())
            for (i = 0; i < k; i++)
                if ((lines == null) || !lines.remove (n.get (i)))
                    ((Line) n.get (i)).setBreakpoint (true);
        k = (lines == null) ? 0 : lines.size ();
        for (i = 0; i < k; i++)
            ((Line) lines.get (i)).setBreakpoint (false);
        if (isHidden ())
            lines = null;
        else
            lines = n;
    }

    /**
    * Creates breakpoint actions from debugger actions and event actions.
    */
    protected void initActions () {
        int l = (event == null) ? 0 : event.getBreakpointActions ().length;
        actions = new CoreBreakpoint.Action [debugger.getBreakpointActions ().length + l];
        System.arraycopy (debugger.getBreakpointActions (), 0, actions, 0, debugger.getBreakpointActions ().length);
        if (event != null)
            System.arraycopy (event.getBreakpointActions (), 0, actions, debugger.getBreakpointActions ().length, l);
        int i, k = actions.length;
        for (i = 0; i < k; i ++)
            actions [i] = actions [i].get (this);
    }

    /**
    * Initializes and sets event of given type.
    */
    protected Event initEvent (Class cl) {
        Event old = event;
        event = getEvent (cl);
        event = event.get (this);
        firePropertyChange (PROP_EVENT, old, event);
        initActions ();
        if ((old instanceof ClassBreakpointEvent) &&
                (event instanceof ClassBreakpointEvent)
           ) ((ClassBreakpointEvent) event).setClassName (((ClassBreakpointEvent) old).getClassName ());
        return event;
    }

    /**
    * Returns event of given type.
    */
    protected Event getEvent (Class cl) {
        Event[] impls = debugger.getBreakpointEvents ();
        int i, k = impls.length;
        for (i = 0; i < k; i++)
            if (cl.isAssignableFrom (impls [i].getClass ()))
                return impls [i];
        return null;
    }

    /**
    * Fires property change.
    */
    protected void firePropertyChange (String s, Object o, Object n) {
        pcs.firePropertyChange (s, o, n);
    }

    /**
    * Fires property change of some event property (Needs re-setBreakpoint).
    */
    protected void fireEventPropertyChange (Event e, String s, Object o, Object n) {
        firePropertyChange (null, o, n);
        if (enabled) setBreakpoint ();
    }

    /**
    * Returns string representation of this class.
    */
    public String toString() {
        return "The Breakpoint: " + getClassName () + ((getLineNumber () < 0) ? // NOI18N
                ("." + getMethodName ()) : (":" + getLineNumber ())); // NOI18N
    }


    // innerclasses .................................................................................

    /**
    * Breakpoint event implementation represents type of breakpoint (like breakpoint on class, 
    * method, exception). 
    */
    public abstract static class Event implements java.io.Serializable {
        static final long serialVersionUID =-4232563710736961248L;

        /** Proper instance of breakpoint. */
        private CoreBreakpoint         breakpoint;
        /** Proper instance of debugger. */
        private AbstractDebugger          debugger;

        private void readObject (java.io.ObjectInputStream in)
        throws java.io.IOException, ClassNotFoundException {
            in.defaultReadObject ();
            try {
                debugger = (AbstractDebugger) TopManager.getDefault ().getDebugger ();
            } catch (DebuggerNotFoundException e) {
                throw new java.io.IOException ();
            }
        }


        /**
        * Returns a new instance of event for given breakpoint.
        */
        public Event get (CoreBreakpoint breakpoint) {
            Event i = getNewInstance ();
            i.breakpoint = breakpoint;
            i.debugger = breakpoint.debugger;
            return i;
        }

        /**
        * Returns a new instance of event for given breakpoint and debugger.
        */
        public Event get (CoreBreakpoint breakpoint, AbstractDebugger debugger) {
            Event i = getNewInstance ();
            i.breakpoint = breakpoint;
            i.debugger = debugger;
            return i;
        }

        /**
        * Returns breakpoint for this event.
        */
        public CoreBreakpoint getBreakpoint () {
            return breakpoint;
        }

        /**
        * Returns debugger instance for this event.
        */
        public AbstractDebugger getDebugger () {
            return debugger;
        }

        /**
        * Fires change of property.
        */
        protected void firePropertyChange (String s, Object o, Object n) {
            breakpoint.fireEventPropertyChange (this, s, o, n);
        }

        /**
        * Must be called when breakpoint is reached.
        */
        protected void perform () {
            breakpoint.perform (this);
        }

        /**
        * Returns actions available specially for this version of event.
        */
        public CoreBreakpoint.Action[] getBreakpointActions () {
            return new CoreBreakpoint.Action [0];
        }

        /**
        * Returns new initialized instance of event.
        */
        protected abstract Event getNewInstance ();

        /**
        * Returns specific properties of this event.
        */
        public abstract Node.Property[] getProperties();

        /**
        * Returns name of type of this event.
        */
        public abstract String getTypeName ();

        /**
        * Returns display name of this event.
        */
        public abstract String getTypeDisplayName ();

        /**
        * Returns display name of this instance of event. It will be used
        * as the name of the breakpoint.
        */
        public abstract String getDisplayName ();

        /**
        * Returns name of icon.
        */
        public abstract String getIconBase ();

        /**
        * Returns lines to highlite in the editor.
        */
        public abstract Line[] getLines ();

        /**
        * Returns customizer visual component.
        */
        public abstract JComponent getCustomizer ();

        /**
        * Sets breakpoint.
        */
        public abstract boolean set ();

        /**
        * Clears breakpoint.
        */
        public abstract void remove ();

        /**
        * Aditional ifno about debugger state when this event occures.
        * If event do not produce this type of info, null is returned.
        */
        public AbstractThread getThread () {
            return null;
        }

        /**
        * Aditional ifno about debugger state when this event occures.
        * If event do not produce this type of info, null is returned.
        */
        public CallStackFrame[] getCallStack () {
            return null;
        }

        /**
        * Aditional info about debugger state when this event occures.
        * If event do not produce this type of info, null is returned.
        */
        public AbstractVariable getVariable () {
            return null;
        }

        /**
        * Set valid property of this breakpoint.
        */
        public void setValid (boolean valid) {
            breakpoint.setValid (valid);
        }
    }

    /**
    * Breakpoint action implementation represents actions which will be performed on some 
    * breakpoint event (like stop debugger, show some message, show stack trace). 
    */
    public abstract static class Action implements java.io.Serializable {
        static final long serialVersionUID = 5993327662505228239L;

        /** Proper instance of debugger. */
        private CoreBreakpoint breakpoint;

        /**
        * Returns new initialized instance of action.
        */
        protected abstract Action getNewInstance ();

        /**
        * Returns breakpoint for this event.
        */
        public CoreBreakpoint getBreakpoint () {
            return breakpoint;
        }

        /**
        * Returns debugger instance.
        */
        public AbstractDebugger getDebugger () {
            return breakpoint.debugger;
        }

        /**
        * Fires change of property.
        */
        protected void firePropertyChange (String s, Object o, Object n) {
            breakpoint.firePropertyChange (s, o, n);
        }

        /**
        * Returns specific properties of this event.
        */
        public Node.Property[] getProperties () {
            return new Node.Property [0];
        }

        /**
        * Returns customizer visuall component or null, if Action is not 
        * customizable.
        */
        public JComponent getCustomizer () {
            return null;
        }

        /**
        * This method is called for each action when some breakpoint event is riched.
        */
        protected abstract void perform (Event e);


        // helper methods .............................................................................

        /**
        * Returns a new instance of action for given breakpoint.
        */
        Action get (CoreBreakpoint breakpoint) {
            Action i = getNewInstance ();
            i.breakpoint = breakpoint;
            return i;
        }
    }
}

/*
 * Log
 *  3    Tuborg    1.2         
 */
