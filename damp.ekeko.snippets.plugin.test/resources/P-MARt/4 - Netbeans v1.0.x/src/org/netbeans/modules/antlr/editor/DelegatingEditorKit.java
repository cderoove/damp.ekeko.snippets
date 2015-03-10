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

import java.lang.reflect.*;
import java.io.*;
import javax.swing.*;
import javax.swing.text.*;

import org.openide.TopManager;

import org.netbeans.editor.Settings;

// XXX FOR TESTING

public class DelegatingEditorKit
            extends EditorKit
            implements org.netbeans.modules.antlr.AntlrTypes
{
    public static final String contentType =
        "text/plain";
    //ANTLR_GRAMMAR_FILE_MIMETYPE;

    public static void main (String[] ign) {
        JEditorPane.registerEditorKitForContentType (
            contentType,
            "org.netbeans.modules.antlr.editor.DelegatingEditorKit",
            TopManager.getDefault ().currentClassLoader ());
        System.err.println("Delegation of contentType: "+contentType+" to ANTLR GKit installed");
    }

    public Object clone () {
        Object o = null;
        try {
            System.err.println("cloning to ANTLR GKit...");
            Class clazz = TopManager.getDefault ().currentClassLoader ().
                          loadClass ("org.netbeans.modules.antlr.editor.NbEditorGKit");
            o = clazz.newInstance ();
            System.err.println(".. new instance "+o);
        } catch (Exception e) {
            System.err.println("ANTLR GKit delegation failed!!!!");
            e.printStackTrace ();
            return null;
        }
        // - Set coloring manager -
        System.out.println("Setting ANTLR GKit Coloring Manager");
        Settings.setValue(GKit.class, Settings.COLORING_MANAGER, new GColoringManager());
        return o;
    }

    public String getContentType () {
        return contentType;
    }

    public ViewFactory getViewFactory () { throw new RuntimeException (); }
    public Action[] getActions () { throw new RuntimeException (); }
    public Caret createCaret () { throw new RuntimeException (); }
    public Document createDefaultDocument () { throw new RuntimeException (); }
    public void read (InputStream in, Document doc, int pos) throws IOException, BadLocationException { throw new RuntimeException (); }
    public void write (OutputStream out, Document doc, int pos, int len) throws IOException, BadLocationException { throw new RuntimeException (); }
    public void read (Reader in, Document doc, int pos) throws IOException, BadLocationException { throw new RuntimeException (); }
    public void write (Writer out, Document doc, int pos, int len) throws IOException, BadLocationException { throw new RuntimeException (); }
}
