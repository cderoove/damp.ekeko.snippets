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
import org.openide.filesystems.*;
import org.openide.modules.*;
import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.util.NbBundle;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author  jleppanen
 * @version 
 */
public class GLoader
            extends MultiFileLoader
{
    static final String ANTLR_GENERATED_TAG="Generated from ANTLR grammar file";

    /** Creates new GLoader */
    public GLoader() {
        super(GDataObject.class);
        // initialize
        setDisplayName(NbBundle.getBundle(GLoader.class).getString("PROP_GLoader_Name"));
        //ExtensionList exts = new ExtensionList();
        //exts.addExtension("g");
        //setExtensions(exts);
        setActions(new SystemAction[] {
                       SystemAction.get(OpenAction.class),
                       SystemAction.get(ViewAction.class),
                       SystemAction.get (FileSystemAction.class),
                       null,
                       SystemAction.get (CompileAction.class),
                       SystemAction.get (BuildAction.class),
                       null,
                       SystemAction.get(CutAction.class),
                       SystemAction.get(CopyAction.class),
                       SystemAction.get(PasteAction.class),
                       null,
                       SystemAction.get(DeleteAction.class),
                       SystemAction.get(RenameAction.class),
                       null,
                       SystemAction.get(SaveAsTemplateAction.class),
                       null,
                       SystemAction.get(ToolsAction.class),
                       SystemAction.get(PropertiesAction.class),
                   });
    }

    protected MultiDataObject createMultiObject(FileObject primaryFile) throws DataObjectExistsException {
        GDataObject mdo = new GDataObject(primaryFile, this);
        GCompiler.Manager.register(GDataObject.class, new GCompiler.Manager());
        //CompilerSupport cs = new CompilerSupport.Compile(mdo.getPrimaryEntry());
        //CompilerSupport cs = new CompilerSupport.Compile(mdo.getPrimaryEntry());
        //cs.setCompilerManager(mdo.getPrimaryEntry(), new GCompiler.Manager());

        findAndTagFiles(primaryFile, mdo);
        /*FileObject parent = primaryFile.getParent();
        for (Enumeration e = parent.getData() ; e.hasMoreElements() ;) {
          e.nextElement();
    }*/

        return mdo;
    }

    protected FileObject findPrimaryFile(FileObject fo) {
        //System.out.println("Finding file: "+fo);
        if (fo.hasExt("g")) {
            return fo;
        }

        String gFile = (String)fo.getAttribute(ANTLR_GENERATED_TAG);
        if (gFile != null) {
            FileObject gFO = fo.getParent().getFileObject(gFile,"g");
            System.out.println("Recognized tagged: "+fo+" to "+gFO);

            return gFO;
        }

        return null;
    }
    protected MultiDataObject.Entry createPrimaryEntry(MultiDataObject mdo, FileObject fo) {
        fo.setImportant(true);
        return new FileEntry(mdo,fo);
    }
    protected MultiDataObject.Entry createSecondaryEntry(MultiDataObject mdo, FileObject fo) {
        //System.out.println("SE: "+ fo);
        return new FileEntry(mdo,fo);
    }

    // ------------------------
    void findAndTagFiles(FileObject fo,GDataObject obj) {
        List files = obj.findAssociatedFiles(fo);
        Iterator i = files.iterator();
        for (;i.hasNext();) {
            //System.out.println("FILE: "+files[i]);
            tagFile((FileObject)i.next(), fo);
        }
    }
    void tagFile(FileObject secondary,FileObject primary) {
        if (secondary==null) {
            return;
        }
        try {
            System.out.println("TAGGING File: "+secondary.getName());
            secondary.setAttribute(ANTLR_GENERATED_TAG, primary.getName());
            markFile(secondary);
        } catch (java.io.IOException ex) {
            System.out.println("ANTLR MODULE: tagging failed:"+ex);
        }
    }
}
