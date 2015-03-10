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

package org.netbeans.modules.antlr;

import java.util.*;

import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.filesystems.*;

import org.openide.text.*;
import org.openide.cookies.*;

import org.netbeans.modules.antlr.nodes.*;

/**
 *
 * @author  jleppanen
 * @version 
 */
public class GDataObject extends MultiDataObject {
    List javaFiles = new Vector();

    /** Creates new GDataObject */
    GDataObject(FileObject fo, MultiFileLoader loader) throws DataObjectExistsException {
        super(fo,loader);
        CookieSet cookies = getCookieSet();
        // use editor support
        EditorSupport es = new EditorSupport(getPrimaryEntry());
        es.setMIMEType (AntlrModuleInstall.ANTLR_GRAMMAR_FILE_MIMETYPE);
        cookies.add(es);
        // support compilation
        cookies.add(new CompilerSupport.Compile(getPrimaryEntry()));
        cookies.add(new CompilerSupport.Build(getPrimaryEntry()));
        cookies.add(new ViewSupport(getPrimaryEntry()));
        setCookieSet(cookies);
    }

    /*
    void createSubDOs() throws org.openide.loaders.DataObjectExistsException {
      java.util.Set secondaryEntries = secondaryEntries();
      java.util.Iterator i = secondaryEntries.iterator();
      while (i.hasNext()) {
        MultiDataObject.Entry entry = (MultiDataObject.Entry)i.next();
        FileObject fo = entry.getFile();
        Node n = null;
        if (fo.hasExt("html")) {
          //n = new GHtmlNode(entry);
        } else if (fo.hasExt("txt")) {
          //n = new GTxtNode(entry);
        } else if (fo.hasExt("java")) {
          JavaDataObject obj = new JavaDataObject(entry.getFile(),getMultiFileLoader());
          new JavaNode(obj);
        } else {
          //n = new GHtmlNode(entry);
        }
      }      
}*/

    protected Node createNodeDelegate() {
        return new GNode(this);
    }
    //FileObject handleMove(DataFolder df) throws IOException { }

    public java.util.List findAssociatedFiles(FileObject fo) {
        final FileObject dir = fo.getParent();
        String s = fo.getName();

        final Vector v = new Vector();
        class X {
            X addFO(String name,String ext) {
                FileObject file = dir.getFileObject(name,ext);
                if (file!=null) { v.add(file); }
                return this;
            }
            X addJavaFO(String name) {
                FileObject file = dir.getFileObject(name,"java");
                javaFiles.add(file);
                return addFO(name, "java");
            }
        }
        new X()
        .addFO(s,"java")
        .addFO(s+"TokenTypes","java")
        .addFO(s+"TokenTypes","txt")
        .addFO(s+"_Errors","txt")
        .addFO(s+"Lexer","java")
        .addFO(s+"Parser","java");

        return v;
    }
}