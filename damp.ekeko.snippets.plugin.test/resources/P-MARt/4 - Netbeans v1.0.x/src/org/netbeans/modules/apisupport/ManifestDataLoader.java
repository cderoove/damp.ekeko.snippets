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
import java.util.Enumeration;
import java.util.jar.*;

import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.modules.ModuleDescription;
import org.openide.util.actions.SystemAction;

public class ManifestDataLoader extends UniFileLoader {

    private static final long serialVersionUID =5851848653958772627L;
    public ManifestDataLoader() {
        this (ManifestDataObject.class);
    }

    public ManifestDataLoader(Class recognizedObject) {
        super (recognizedObject);
    }

    protected void initialize () {

        super.initialize ();

        setDisplayName ("OpenIDE Module Manifests");

        ExtensionList extensions = new ExtensionList ();
        extensions.addExtension ("mf");
        extensions.addExtension ("MF");
        setExtensions (extensions);

        extensions = new ExtensionList ();
        extensions.addExtension ("mf-");
        setPrefixes (extensions);

        setActions (new SystemAction[] {
                        SystemAction.get (OpenAction.class),
                        SystemAction.get (FileSystemAction.class),
                        null,
                        SystemAction.get (ExecuteAction.class),
                        null,
                        SystemAction.get (CutAction.class),
                        SystemAction.get (CopyAction.class),
                        SystemAction.get (PasteAction.class),
                        null,
                        SystemAction.get (DeleteAction.class),
                        SystemAction.get (RenameAction.class),
                        null,
                        SystemAction.get (SaveAsTemplateAction.class),
                        null,
                        SystemAction.get (ToolsAction.class),
                        SystemAction.get (PropertiesAction.class),
                    });

    }

    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new ManifestDataObject (primaryFile, this);
    }

    public ExtensionList getPrefixes () {
        return (ExtensionList) getProperty ("prefixes");
    }

    public void setPrefixes (ExtensionList nue) {
        putProperty ("prefixes", nue, true);
    }

    protected FileObject findPrimaryFile (FileObject fo) {
        FileObject test = super.findPrimaryFile (fo);
        if (test == null) {
            // Too slow to recognize any *.txt. Just look for some of them.
            Enumeration exts = getPrefixes ().extensions ();
            while (exts.hasMoreElements ()) {
                String prefix = (String) exts.nextElement ();
                if (fo.getName ().startsWith (prefix)) {
                    test = fo;
                    break;
                }
            }
            if (test == null) return null;
        }
        try {
            InputStream is = test.getInputStream ();
            try {
                Manifest m = new Manifest (is);
                Attributes attr = m.getMainAttributes ();
                String module = attr.getValue (ModuleDescription.TAG_MAGIC);
                if (module != null)
                    return test;
                else
                    return null;
            } catch (IOException ioe) {
                // Do not even print it--maybe just a random non-mf text file.
                return null;
            } finally {
                is.close ();
            }
        } catch (IOException ioe) {
            return null;
        }
    }

    public void readExternal (ObjectInput oi) throws IOException, ClassNotFoundException {
        super.readExternal (oi);
        setPrefixes ((ExtensionList) oi.readObject ());
    }

    public void writeExternal (ObjectOutput oo) throws IOException {
        super.writeExternal (oo);
        oo.writeObject (getPrefixes ());
    }

}

/*
 * Log
 *  5    Gandalf-post-FCS1.2.2.1     3/28/00  Jesse Glick     SVUIDs.
 *  4    Gandalf-post-FCS1.2.2.0     3/9/00   Jesse Glick     Backport of 1.2.1.0 from
 *       Jaga.
 *  3    Gandalf   1.2         2/4/00   Jesse Glick     
 *  2    Gandalf   1.1         1/26/00  Jesse Glick     Configurable prefixes.
 *  1    Gandalf   1.0         1/22/00  Jesse Glick     
 * $
 */
