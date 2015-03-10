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

import java.util.ResourceBundle;

import org.openide.nodes.Node;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.NbBundle;

import org.netbeans.modules.debugger.support.AbstractThread;
import org.netbeans.modules.debugger.support.AbstractVariable;


/**
* This class represents locales as a Node.
* This class is final only for performance reasons.
* Can be unfinaled if desired.
*
* @author   Jan Jancura
*/
public final class LocalesRootNode extends AbstractNode {


    // static variables ..........................................................

    /** generated Serialized Version UID */
    static final long                  serialVersionUID = -6346315017458451778L;

    private static String              ICON_BASE =
        "/org/netbeans/core/resources/watches"; // NOI18N

    private static ResourceBundle      bundle;


    // variables .................................................................

    private AbstractThread             thread;


    // init ......................................................................

    /**
    * Creates empty BreakpointsContext.
    */
    LocalesRootNode (VariableHome variableHome, AbstractThread thread) {
        super (new LocalesRootChildren (variableHome));
        this.thread = thread;
        String s = getLocalizedString ("CTL_Locales");
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
            bundle = NbBundle.getBundle (LocalesRootNode.class);
        return bundle.getString (s);
    }

    void changeChildren () {
        // set locales as keys into our children
        LocalesRootChildren myChildren = (LocalesRootChildren) getChildren ();
        myChildren.setMyKeys (thread.getLocales ());
    }


    // innerclasses ..............................................................

    /** Special locales subnodes (children) */
    private static final class LocalesRootChildren extends Children.Keys implements LeafRefresher {

        /** Where to add variables. */
        private VariableHome                   variableHome;

        LocalesRootChildren (VariableHome variableHome) {
            this.variableHome = variableHome;
        }

        /** Creates nodes for given key.
        * @param key the key that is used
        * @return array of nodes representing the key
        */
        protected Node[] createNodes (final Object key) {
            return new Node[] {
                       new VariableNode (variableHome, (AbstractVariable) key)
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

    } // end of LocalesChildren inner class

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
