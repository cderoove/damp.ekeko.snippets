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

package org.openide.util.actions;

import java.beans.PropertyChangeEvent;

import org.openide.nodes.Node;
import org.openide.nodes.NodeAdapter;

/** An action
* dependent on the cookies of the selected nodes.
*
* @author   Petr Hamernik, Jaroslav Tulach, Dafe Simonek
*/
public abstract class CookieAction extends NodeAction {
    /** name of property with cookies for this action */
    private static final String PROP_COOKIES = "cookies"; // NOI18N

    /** name of property holding cookies changes listener for this action */
    private static final String PROP_COOKIE_CHANGE = "cookieChange"; // NOI18N

    /** Action will be enabled if there are one or more selected nodes
    * and there is exactly one node which supports the given cookies. */
    public static final int MODE_ONE  = 0x01;

    /** Action will be enabled if there are several selected nodes
    * and some of them (at least one, but not all)
    * support the given cookies. */
    public static final int MODE_SOME = 0x02;

    /** Action will be enabled if there are one or more selected nodes
    * and all of them support the given cookies. */
    public static final int MODE_ALL  = 0x04;

    /** Action will be enabled if there is exactly one selected node
    * and it supports the given cookies. */
    public static final int MODE_EXACTLY_ONE  = 0x08;

    /** Action will be enabled if there are one or more selected nodes
    * and any of them (one, all, or some) support the given cookies. */
    public static final int MODE_ANY  = 0x07;
    // [PENDING] 0x06 should suffice, yes? --jglick

    static final long serialVersionUID =6031319415908298424L;
    /** Get the mode of the action, i.e.<!-- --> how strict it should be about
    * cookie support.
    * @return the mode of the action. Possible values are disjunctions of the <code>MODE_XXX</code>
    * constants. */
    protected abstract int mode();

    /** Get the cookies that this action requires. The cookies are disjunctive, i.e. a node
    * must support AT LEAST ONE of the cookies specified by this method.
    *
    * @return a list of cookies
    */
    protected abstract Class[] cookieClasses ();

    /* Initialize the action (and cookies change listener).
    */
    protected void initialize () {
        super.initialize ();
        CookiesChangeListener listener = new CookiesChangeListener(getClass());
        putProperty(PROP_COOKIE_CHANGE, listener);
    }

    /* Activates cookie change listener. */
    protected void addNotify () {
        super.addNotify();
        CookiesChangeListener listener =
            (CookiesChangeListener)getProperty (PROP_COOKIE_CHANGE);
        listener.setActive(true);
    }

    /* Deactivates cookie changes listener */
    protected void removeNotify () {
        super.removeNotify();
        CookiesChangeListener listener =
            (CookiesChangeListener)getProperty (PROP_COOKIE_CHANGE);
        listener.setActive(false);
    }

    /** Getter for cookies.
    * @return the set of cookies for this
    */
    private Class[] getCookies () {
        Class[] ret = (Class[])getProperty (PROP_COOKIES);
        if (ret != null) return ret;
        ret = cookieClasses ();
        putProperty (PROP_COOKIES, ret);
        return ret;
    }

    /** Test for enablement based on the cookies of selected nodes.
    * Generally subclasses should not override this except for strange
    * purposes, and then only calling the super method and adding a check.
    * Just use {@link #cookieClasses} and {@link #mode} to specify
    * the enablement logic.
    * @param activatedNodes the set of activated nodes
    * @return <code>true</code> to enable
    */
    protected boolean enable (Node[] activatedNodes) {
        if (activatedNodes.length == 0)
            return false;
        // sets new nodes to cookie change listener
        ((CookiesChangeListener)getProperty(PROP_COOKIE_CHANGE)).
        setNodes(activatedNodes);
        // perform enable / disable logic
        return doEnable(activatedNodes);
    }

    /** Helper, actually performs enable / disable logic */
    boolean doEnable (Node[] activatedNodes) {
        int supported = resolveSupported(activatedNodes);
        if (supported == 0)
            return false;
        int mode = mode ();
        return
            // [PENDING] shouldn't MODE_ONE also say: && supported == 1? --jglick
            ((mode & MODE_ONE) != 0) ||
            (((mode & MODE_ALL) != 0) && (supported == activatedNodes.length)) ||
            (((mode & MODE_EXACTLY_ONE) != 0) && (activatedNodes.length == 1)) ||
            (((mode & MODE_SOME) != 0) && (supported < activatedNodes.length));
    }

    /**
    * Implementation of the above method.
    *
    * @param activatedNodes gives array of actually activated nodes.
    * @return number of supported classes
    */
    private int resolveSupported (Node[] activatedNodes) {
        int total = activatedNodes.length;
        int ret = 0;

        Class[] cookies = getCookies();
        for (int i = 0; i < total; i++) {
            for (int j = 0; j < cookies.length; j++) {
                // test for supported cookies
                if (activatedNodes[i].getCookie(cookies[j]) != null) {
                    ret++;
                    break;
                }
            }
        }

        return ret;
    }

    /** Tracks changes of cookie classes in currently selected nodes
    */
    static final class CookiesChangeListener extends NodeAdapter {

        /** The nodes we are currently listening */
        Node[] nodes;
        /** True if we are active */
        boolean active;
        /** the class of the action we work with */
        private Class clazz;

        /** Constructor - asociates with given cookie action class
        */
        public CookiesChangeListener (Class clazz) {
            this.clazz = clazz;
        }

        /** Activates/deactivates the listener.
        * Listener tracks cookie classes changes only if it is active.
        * @param active active flag
        */
        void setActive (boolean active) {
            if (this.active == active)
                return;
            this.active = active;
            if (nodes != null) {
                if (active)
                    attachListeners(nodes);
                else {
                    detachListeners(nodes);
                    nodes = null;
                }
            }
        }

        /** Is the listener active?
        */
        boolean isActive () {
            return active;
        }

        /** Sets the nodes to work on */
        void setNodes (Node[] newNodes) {
            // detach old nodes
            if (nodes != null)
                detachListeners(nodes);
            // attach to new nodes if we are active
            if ((newNodes != null) && active) {
                attachListeners(newNodes);
            }
            nodes = newNodes;
        }

        /** Removes itself as a listener from given nodes */
        void detachListeners (Node[] nodes) {
            for (int i = 0; i < nodes.length; i++) {
                nodes[i].removeNodeListener(this);
            }
        }

        /** Attach itself as a listener to the given nodes */
        void attachListeners (Node[] nodes) {
            for (int i = 0; i < nodes.length; i++) {
                nodes[i].addNodeListener(this);
            }
        }

        /** Reacts to the cookie classes change -
        * calls enable on asociated action */
        public void propertyChange (PropertyChangeEvent ev) {
            // filter only cookie classes changes
            if (!Node.PROP_COOKIE.equals(ev.getPropertyName()))
                return;
            // find asociated action
            CookieAction a = (CookieAction)findObject(clazz);
            // let the action to enable / disable itself
            if ((nodes != null) && (a != null)) {
                a.setEnabled (a.enable (nodes));
            }
        }

    } // end of CookiesChangeListener


}

/*
 * Log
 *  11   Gandalf   1.10        1/12/00  Pavel Buzek     I18N
 *  10   Gandalf   1.9         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  9    Gandalf   1.8         10/6/99  Petr Jiricka    Clarified Javadoc for 
 *       cookieClasses
 *  8    Gandalf   1.7         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  7    Gandalf   1.6         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  6    Gandalf   1.5         6/1/99   Jesse Glick     [JavaDoc]
 *  5    Gandalf   1.4         4/8/99   David Simonek   debigging comments 
 *       removed...
 *  4    Gandalf   1.3         3/29/99  David Simonek   cookie action now 
 *       listens on cookie changes
 *  3    Gandalf   1.2         3/26/99  Jesse Glick     [JavaDoc]
 *  2    Gandalf   1.1         3/26/99  Jesse Glick     [JavaDoc]
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.12        --/--/98 Jan Formanek    reflecting changes in cookies, bugfix
 */
