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

package org.netbeans.modules.apisupport;

import java.io.*;

import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.*;
import org.openide.modules.ModuleDescription;

import org.netbeans.modules.jarpackager.*;
import org.netbeans.modules.jarpackager.actions.*;

public class ModuleDataLoader extends JarDataLoader {

    private static final long serialVersionUID =-5017581069154105157L;
    public ModuleDataLoader () {
        this (ModuleDataObject.class);
    }

    public ModuleDataLoader (Class recognizedObject) {
        super (recognizedObject);
    }

    protected void initialize () {

        super.initialize ();

        setDisplayName ("OpenIDE Modules");

        // Use same actions as JAR packager.

    }

    protected FileObject findPrimaryFile (FileObject fo) {
        if (fo.hasExt (getArchiveExt ())) {
            FileObject content = FileUtil.findBrother (fo, getExtension ());
            if (content == null || ! isModuleContent (content))
                return null;
            else
                return content;
        } else if (fo.hasExt (getExtension ())) {
            if (! isModuleContent (fo))
                return null;
            else
                return fo;
        } else {
            return null;
        }
    }

    private boolean isModuleContent (FileObject fo) {
        if (fo.getSize () == 0) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                System.err.println ("Warning: ignoring empty or invalid JAR content object " + fo.getPackageNameExt ('/', '.'));
            return false;
        }
        try {
            InputStream is = fo.getInputStream ();
            try {
                JarContent content = new JarContent ();
                ObjectInputStream ois = new ObjectInputStream (is);
                try {
                    content.readContent (ois);
                    return content.getManifest ().getMainAttributes ().
                           getValue (ModuleDescription.TAG_MAGIC) != null;
                } finally {
                    ois.close ();
                }
            } finally {
                is.close ();
            }
        } catch (Exception e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions")) {
                e.printStackTrace ();
                System.err.println ("While checking: " + fo.getPackageNameExt ('/', '.'));
            }
            return false;
        }
    }

    protected MultiDataObject createMultiObject (FileObject primaryFile) throws IOException {
        return new ModuleDataObject (primaryFile, this);
    }

}

/*
 * Log
 *  10   Gandalf-post-FCS1.8.1.0     3/28/00  Jesse Glick     SVUIDs.
 *  9    Gandalf   1.8         2/4/00   Jesse Glick     Context actions bugfix.
 *  8    Gandalf   1.7         1/26/00  Jesse Glick     Live manifest parsing.
 *  7    Gandalf   1.6         1/22/00  Jesse Glick     
 *  6    Gandalf   1.5         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Gandalf   1.4         10/14/99 Jesse Glick     Works with new 
 *       JarPackager changes.
 *  4    Gandalf   1.3         10/5/99  Jesse Glick     Sundry API changes 
 *       affecting me.
 *  3    Gandalf   1.2         9/30/99  Jesse Glick     Package rename and misc.
 *  2    Gandalf   1.1         9/22/99  Jesse Glick     Using regular 
 *       .jarContent extension, and recognizing modules by magic tag.
 *  1    Gandalf   1.0         9/17/99  Jesse Glick     
 * $
 */
