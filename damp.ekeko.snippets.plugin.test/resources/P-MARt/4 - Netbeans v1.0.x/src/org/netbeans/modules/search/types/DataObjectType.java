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

package org.netbeans.modules.search.types;

import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;

import org.openidex.search.*;

import org.netbeans.modules.search.*;

/**
 * Type used by RepositoryScanner.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public abstract class DataObjectType extends SearchType implements TypeConstants {

    public static final long serialVersionUID = 1L; //forever 1

    //detail is produced during search
    private transient SearchDetail detail;

    /** Creates new RepositoryType */
    public DataObjectType() {
        //a new criteron can be valid or invalid depending on type
        //be pesimistic
        setValid(false);
    }

    public boolean test(DataObject dobj, SearchDetail detail) {
        this.detail = detail;
        return test(dobj);
    }

    /** Add new detail to detail set. */
    protected void addDetail(Object detail) {
        this.detail.add(detail);
    }

    protected abstract boolean test(DataObject dobj);

    /** @return org.netbeans.modules.search.scanners.RepositoryScanner.class
    */  
    public Class getScannerClass() {
        return org.netbeans.modules.search.scanners.RepositoryScanner.class;
    }

    /** Overload it if your criterion supports StructuredDetail or so. */
    public Class[] getDetailClasses() {
        return new Class[] {String.class};
    }

    /** @return true if any node represent repository node
    */
    public boolean enabled(Node[] nodes) {

        if (nodes == null) return false;
        if (nodes.length == 0) return false;

        // NodeCookie test

        for (int i =0; i<nodes.length; i++ ) {

            Node obj = nodes[i];

            //     if (obj.isLeaf()) continue; // optimalization that can cause probs in object browser

            if ( obj.getCookie(org.openide.loaders.DataFolder.class) != null) {
                return true;
            }

            if ( obj.getCookie(org.openide.filesystems.Repository.class) != null) {
                return true;
            }
        }

        return false;
    }

    public HelpCtx getHelpCtx() {
        return null;
    }

}


/*
* Log
*  12   Gandalf-post-FCS1.7.2.3     4/7/00   Petr Kuzel      serialVersionUID must 
*       stay intact due to back compatability.
*  11   Gandalf-post-FCS1.7.2.2     4/4/00   Petr Kuzel      Comments + output window 
*       fix
*  10   Gandalf-post-FCS1.7.2.1     3/9/00   Petr Kuzel      I18N
*  9    Gandalf-post-FCS1.7.2.0     2/24/00  Ian Formanek    Post FCS changes
*  8    Gandalf   1.7         1/18/00  Jesse Glick     Context help.
*  7    Gandalf   1.6         1/13/00  Radko Najman    I18N
*  6    Gandalf   1.5         1/11/00  Petr Kuzel      Result details added.
*  5    Gandalf   1.4         1/10/00  Petr Kuzel      "valid" fired.
*  4    Gandalf   1.3         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  3    Gandalf   1.2         12/23/99 Petr Kuzel      Architecture improved.
*  2    Gandalf   1.1         12/15/99 Martin Balin    Fixed package name
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 

