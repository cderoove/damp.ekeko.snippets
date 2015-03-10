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

import java.util.Comparator;
import java.util.ResourceBundle;

import org.openide.util.NbBundle;
import org.openide.nodes.Node;
import org.openide.nodes.Children;
import org.openide.nodes.AbstractNode;

import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.CallStackFrame;

/**
* This class represents callstack as a Node.
* Class is final only for performance reasons.
* Can be happily unfinaled if desired.
*
* @author   Jan Jancura
*/
public final class CallStackNode extends AbstractNode {
    /** generated Serialized Version UID */
    static final long                 serialVersionUID = -8259352660663524178L;


    // static variables ..........................................................

    private static String             ICON_BASE =
        "/org/netbeans/core/resources/callstack"; // NOI18N
    private static ResourceBundle     bundle;


    // variables .................................................................

    private AbstractThread            thread;


    // init ......................................................................

    /**
    * Creates empty CallStackNode node.
    */
    CallStackNode (VariableHome variableHome, AbstractThread thread) {
        super (new CallStackChildren (variableHome));
        this.thread = thread;
        String s = getLocalizedString ("CTL_CallStack");
        setDisplayName (s);
        setName (s);
        setIconBase (ICON_BASE);
        changeChildren ();
    }


    // helper methods ............................................................

    /**
    * @return localized string.
    */
    static String getLocalizedString (String s) {
        if (bundle == null)
            bundle = NbBundle.getBundle (CallStackNode.class);
        return bundle.getString (s);
    }

    void changeChildren () {
        CallStackChildren myChildren = (CallStackChildren) getChildren();
        CallStackFrame [] callStack = thread.getCallStack ();
        Node [] node = myChildren.getNodes ();
        int stackLength = callStack.length;
        int nodesLength = node.length;

        int i, k = Math.min (stackLength, nodesLength);
        for (i = 0; i < k; i++)
            ((CallStackLineNode) node [nodesLength - i - 1]).
            updateCallStackFrame (callStack [stackLength - i - 1]);
        for (i = k; i < stackLength; i++) {
            myChildren.add (callStack [stackLength - i - 1], i);
        }
        for (i = k; i < node.length; i++) {
            myChildren.remove (new Node [] {node [nodesLength - i - 1]});
        }
    }


    // innerclasses ..............................................................

    /** Special locales subnodes (children) */
    private static final class CallStackChildren extends Children.SortedArray
        implements Comparator {

        /** Where to add variables. */
        private VariableHome              variableHome;

        CallStackChildren (VariableHome variableHome) {
            this.variableHome = variableHome;
            setComparator (this);
        }

        public int compare (Object o1, Object o2) throws ClassCastException {
            int index1 = ((CallStackLineNode) o1).getIndex ();
            int index2 = ((CallStackLineNode) o2).getIndex ();
            if (index1 > index2) return -1;
            if (index1 == index2) return 0;
            return 1;
        }

        private void add (CallStackFrame stackFrame, int index) {
            add (new Node [] {
                     new CallStackLineNode (variableHome, stackFrame, index)
                 });
        }

    } // end of CallStackChildren inner class

}

/*
 * Log
 *  6    Gandalf-post-FCS1.4.3.0     3/28/00  Daniel Prusa    
 *  5    Gandalf   1.4         1/13/00  Daniel Prusa    NOI18N
 *  4    Gandalf   1.3         1/12/00  Daniel Prusa    setName (String) added -
 *       bad serialization fixed
 *  3    Gandalf   1.2         11/8/99  Jan Jancura     Somma classes renamed
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         8/17/99  Jan Jancura     
 * $
 */
