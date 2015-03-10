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

package org.netbeans.modules.search.scanners;

import java.io.*;
import java.text.MessageFormat;
import java.util.*;

import org.openide.*;
import org.openide.nodes.*;
import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.util.enum.*;

import org.openidex.search.*;

import org.netbeans.modules.search.types.*;
import org.netbeans.modules.search.*;
import org.netbeans.modules.search.res.Res;

/**
 * Scanner that can scan all collection nodes in repository.
 * It tests DataObjectType by test(DataObject).
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class RepositoryScanner extends Scanner {

    /** DataObjectTypes to be applied */
    private Vector dot;
    private DataObjectType[] criteria;

    //trace the code
    private boolean TRACE = false;

    //cache of file objects to skip
    //due they were recognized
    private HashSet cache;

    /** Creates new RepositoryScanner */
    public RepositoryScanner() {
        dot = new Vector();
        cache = new HashSet();
    }

    /** Add new search type. The type will be applied on
    * scanned object if all other such added types are eveluated to true.
    */
    public void add(SearchType type) {

        t("add(" + type + ")"); // NOI18N

        if (type instanceof DataObjectType) {
            dot.add(type);
        } else {
            throw new ClassCastException("Unsupported search type"); // NOI18N
        }
    }

    /** Scan particular nodes. Test if their children
    * match added() search types.
    */
    public void scan(Node[] nodes) {

        t("scan()"); // NOI18N

        try {

            criteria = new DataObjectType[dot.size()];
            dot.copyInto(criteria);

            nodes = normalizeNodes(nodes);

            // test whether scan whole repository

            if (nodes.length == 1) {
                if ( nodes[0].getCookie(org.openide.filesystems.Repository.class) != null) {
                    scanRepository();
                    return;
                }
            }

            for (int i = 0; i<nodes.length; i++) {
                Node.Cookie cake = nodes[i].getCookie(org.openide.loaders.DataFolder.class);
                if ( cake != null) {
                    DataFolder folder = (DataFolder) cake;
                    scan(folder.getPrimaryFile());
                }
            }

        } catch ( InterruptedException ex) {
            // finnish scanning
            t("interrupted."); // NOI18N
        }
    }

    /**
    * Scan all visible filesystems.
    */
    private void scanRepository() throws InterruptedException {
        Repository rep = TopManager.getDefault().getRepository();
        Enumeration fss = rep.getFileSystems();
        while (fss.hasMoreElements()) {
            org.openide.filesystems.FileSystem fs = (org.openide.filesystems.FileSystem)fss.nextElement();
            if (fs.isValid() && !fs.isHidden())
                scan(fs.getRoot());
        }
    }

    /**
    * Scan recursively particular FileObject.
    * Fires found notifications.
    */
    private void scan(FileObject fo) throws InterruptedException {

        if (Thread.interrupted()) throw new InterruptedException();

        if (fo.isData()) { //leaf
            t("scanning " + fo); // NOI18N
            DataObject dobj = getDataObject(fo);

            if (dobj == null) return;

            SearchDetail detail = new SearchDetail();

            // test obtained object on all criteria
            for (int i = 0; i<criteria.length; i++) {
                if ( ! criteria[i].test(dobj, detail) ) return;
            }

            Node node = dobj.getNodeDelegate();

            t("Found node: "+ node); // NOI18N
            if (node != null) {
                // mark with detail cookie
                notifyFound( new Node[] {
                                 new FoundNode (node, detail.isEmpty () ? null : detail)
                             } );
            }

            return;

        } else { //data folder

            FileObject[] fos = fo.getChildren();
            if (fos.length == 0) return;
            for(int i=0; i<fos.length; i++)
                scan(fos[i]);
        }
    }

    //
    // caching model: cache holds recognized DataObjects
    //
    private boolean cacheHit(DataObject dobj) {
        return cache.contains(dobj);
    }

    private void cacheAdd(DataObject dobj) {
        cache.add(dobj);
    }


    /**
    * translates FileObject to DataObject.
    * @return DataObject or null if it was already recognized
    */
    private DataObject getDataObject(FileObject fo) {

        DataObject dobj = null;

        try {
            try {
                dobj = org.openide.loaders.DataObject.find(fo);
            }
            catch (DataObjectNotFoundException ex1) {
                t("Creating dobj from scratch"); // NOI18N
                dobj = TopManager.getDefault().getLoaderPool().findDataObject(fo);
            }

            if (cacheHit(dobj)) {
                return null;
            } else {
                cacheAdd(dobj);
            }

        } catch (DataObjectExistsException ex) {
            throw new RuntimeException("Should not occur."); // NOI18N

        } catch (IOException ex) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) // NOI18N
                ex.printStackTrace();
        }

        return dobj;

    }



    /** remove kids from set */
    private Node[] normalizeNodes(Node[] nodes) {

        Vector ret = new Vector();

        for (int i = 0; i<nodes.length; i++) {
            if (! hasParent(nodes[i],nodes)) ret.add(nodes[i]);
        }

        Node[] newNodes = new Node[ret.size()];
        ret.copyInto(newNodes);
        return newNodes;
    }

    private boolean hasParent(Node node, Node[] nodes) {
        for (Node parent = node.getParentNode(); parent != null; parent = parent.getParentNode()) {
            for (int i = 0; i<nodes.length; i++) {
                if (nodes[i].equals(parent)) return true;
            }
        }
        return false;
    }


    /** The name given to my thread.
    */
    public String toString() {
        return "RepositoryScanner"; // NOI18N
    }


    /** FOr debugging pusposes only. */
    private void t(String msg) {
        if (TRACE)
            System.err.println("RepositoryScanner: " + msg);
    }

    /** Node that may know search result details. */
    private static class FoundNode extends FilterNode {
        private DetailCookie detail;

        public FoundNode(Node original, DetailCookie detail) {
            super(original);
            this.detail = detail;
            DataObject obj = (DataObject) original.getCookie (DataObject.class);
            if (obj != null) {
                FileObject folder = obj.getPrimaryFile ().getParent ();
                if (folder != null) {
                    String pkg = folder.getPackageName ('.');
                    String hint;
                    if (pkg.equals ("")) // NOI18N
                        hint = Res.hint ("result_default_package"); // NOI18N
                    else
                        hint = MessageFormat.format (Res.hint ("result_package"), // NOI18N
                                                     new Object[] { pkg });
                    //System.err.println("will set to: " + hint);
                    disableDelegation (DELEGATE_SET_SHORT_DESCRIPTION |
                                       DELEGATE_GET_SHORT_DESCRIPTION);
                    setShortDescription (hint);
                }
            }
        }

        public Node.Cookie getCookie(Class type) {
            if (type.equals(DetailCookie.class) && detail != null) return detail;
            return super.getCookie(type);
        }
    }

}


/*
* Log
*  6    Gandalf   1.5         1/16/00  Jesse Glick     Tooltips with package on 
*       search result nodes.
*  5    Gandalf   1.4         1/13/00  Radko Najman    I18N
*  4    Gandalf   1.3         1/11/00  Petr Kuzel      Result details added.
*  3    Gandalf   1.2         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  2    Gandalf   1.1         12/15/99 Martin Balin    Fixed package name
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 

