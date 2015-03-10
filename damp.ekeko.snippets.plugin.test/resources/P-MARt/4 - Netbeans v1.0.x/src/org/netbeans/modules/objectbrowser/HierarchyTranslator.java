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

package org.netbeans.modules.objectbrowser;

import java.beans.*;

import org.openide.nodes.Node;

/**
*
* @author   Jan Jancura
*/
public interface HierarchyTranslator {

    public static final String     PROPERTY_DEFAULT_INFO_BUS_NAME = "explorerViews"; // NOI18N
    public static final String     PROPERTY_DEFAULT_TRANSLATOR_ITEM_NAME = "translator"; // NOI18N

    public String[] getFilterNames ();

    public String[] getFilterComments ();


    public Node translate (Node[] nodes, boolean[] filter, Filter f);

    public Filter getFilter (Node[] nodes, boolean[] filter, ExplorerBean explorerBean);

    public interface Filter {

        public java.awt.Component getComponent ();

        public void addPropertyChangeListener (PropertyChangeListener e);
        public void removePropertyChangeListener (PropertyChangeListener e);
    }
}

/*
 * Log
 *  5    src-jtulach1.4         1/13/00  Radko Najman    I18N
 *  4    src-jtulach1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    src-jtulach1.2         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    src-jtulach1.1         4/2/99   Jan Jancura     
 *  1    src-jtulach1.0         3/23/99  Jan Jancura     
 * $
 */
