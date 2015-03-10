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

package org.netbeans.modules.antlr.editor;

import javax.swing.text.*;

import org.netbeans.modules.antlr.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.text.*;
import org.openide.cookies.*;
import org.openide.TopManager;

import org.netbeans.editor.Settings;

public class Main
            extends Object
            implements AntlrTypes
{
    public static void main (String args[]) throws Exception {
        System.out.println("Testing EditorKit");
        registerEditor();

        EditorKit kit = javax.swing.JEditorPane.createEditorKitForContentType(ANTLR_GRAMMAR_FILE_MIMETYPE);
        if (kit==null) { return; }
        System.out.println("EditorKit: "+kit);

        FileObject fo=TopManager.getDefault().getRepository().find("tst", "Tst", "g");
        if (fo==null) { System.out.println("File not found:"+fo); return; }
        System.out.println("FileObject: "+fo+" mime:"+fo.getMIMEType ());
        DataObject obj=null;
        try {
            //obj=TopManager.getDefault().getLoaderPool().findDataObject(fo);
            obj=DataObject.find(fo);
        } catch (Exception e) {
            System.out.println("Caught "+e);
            obj=null;
        }
        if (obj==null) { System.out.println("No DataObject found"); return; }
        System.out.println("DataObject: "+obj);
        // Get the DataObject opened in editor with created editor
        EditorCookie ec=(EditorCookie)(obj.getCookie(EditorCookie.class));
        if (ec==null) { System.out.println("DataObject don't have EC"); return; }
        System.out.println("EditorKit: "+ec);
        //StyledDocument doc=ec.openDocument();
        ec.open();
    }
    public static void registerEditor() {
        javax.swing.JEditorPane.registerEditorKitForContentType(
            ANTLR_GRAMMAR_FILE_MIMETYPE,
            "org.netbeans.modules.antlr.editor.NbEditorGKit",
            Main.class.getClassLoader()
        );
        org.openide.filesystems.FileUtil.setMIMEType(
            ANTLR_G_EXTENSION,
            ANTLR_GRAMMAR_FILE_MIMETYPE
        );
        Settings.setValue(GKit.class, Settings.COLORING_MANAGER, new GColoringManager());
    }
}