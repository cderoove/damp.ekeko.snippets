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

import org.openide.loaders.*;
import org.openide.filesystems.*;
import org.openide.modules.*;
import org.openide.compiler.*;
import org.openide.compiler.Compiler;
// For editor
import org.netbeans.editor.Settings;
import org.netbeans.modules.antlr.editor.*;

/**
 *
 * @author  jleppanen
 * @version 
 */

public class AntlrModuleInstall
            extends Object
            implements ModuleInstall, AntlrTypes
{
    private static final boolean isDebug = true;
    private static final void debugPrint(String s) {
        if (isDebug) {
            System.err.println(s);
        }
    }

    // *** ModuleInstall ***
    public void installed() {
        debugPrint("ANTLR Module installed");
        restored();
    }
    public boolean closing() {
        debugPrint("ANTLR Module de-activated");
        return true;
    }
    public void restored() {
        debugPrint("ANTLR Module activated");
        Compiler.Manager.register(GDataObject.class, new GCompiler.Manager());
        registerEditor();
        copyTemplates ();
    }
    public void uninstalled() {
        debugPrint("ANTLR Module uninstalled");
        removeTemplates();
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
        Settings.setValue(NbEditorGKit.class, Settings.COLORING_MANAGER, new GColoringManager());
    }

    // -----------------------------------------------------------------------------
    // Private methods
    private void copyTemplates () {
        debugPrint("ANTLR MODULE Copying templates..");
        try {
            org.openide.filesystems.FileUtil.extractJar (
                org.openide.TopManager.getDefault ().getPlaces ().folders().templates ().getPrimaryFile (),
                getClass ().getClassLoader ().getResourceAsStream ("org/netbeans/modules/antlr/templates.jar")
            );
        } catch (java.io.IOException e) {
            org.openide.TopManager.getDefault ().notifyException (e);
        }
    }
    private void removeTemplates () {
        debugPrint("ANTLR MODULE removeTemplates are UNIMPLEMENTED");
    }



}