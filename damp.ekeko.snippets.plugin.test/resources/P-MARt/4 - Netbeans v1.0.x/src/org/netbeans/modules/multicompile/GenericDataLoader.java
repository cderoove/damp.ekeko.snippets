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

package org.netbeans.modules.multicompile;

import java.io.*;
import java.util.*;

import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.actions.SystemAction;

/** Recognizes single files in the Repository as being of a certain type.
 *
 * @author jglick
 */
public class GenericDataLoader extends UniFileLoader {

    static final long serialVersionUID =6447258214559942476L;
    public GenericDataLoader() {
        this (GenericDataObject.class);
    }

    public GenericDataLoader(Class recognizedObject) {
        super (recognizedObject);
    }

    protected void initialize () {

        setDisplayName (GenericDataLoaderBeanInfo.getString ("LBL_loaderName"));

        ExtensionList extensions = new ExtensionList ();
        extensions.addExtension ("sample");
        setExtensions (extensions);
        extensions = new ExtensionList ();
        setSecondaryExtensions (extensions);
        setInnerClasses (false);
        setMimeType ("text/plain");

        setActions (new SystemAction[] {
                        SystemAction.get (OpenAction.class),
                        SystemAction.get (FileSystemAction.class),
                        null,
                        SystemAction.get (CompileAction.class),
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

    public ExtensionList getSecondaryExtensions () {
        return (ExtensionList) getProperty ("secondaryExtensions");
    }

    public void setSecondaryExtensions (ExtensionList nue) {
        putProperty ("secondaryExtensions", nue, true);
    }

    public boolean isInnerClasses () {
        return ((Boolean) getProperty ("innerClasses")).booleanValue ();
    }

    public void setInnerClasses (boolean nue) {
        putProperty ("innerClasses", new Boolean (nue), true);
    }

    public String getMimeType () {
        return (String) getProperty ("mimeType");
    }

    public void setMimeType (String nue) {
        putProperty ("mimeType", nue, true);
    }

    // [PENDING] more flexibility, e.g. different inner-class sep charactor, non-extension suffixes, ...

    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new GenericDataObject (primaryFile, this);
    }

    protected FileObject findPrimaryFile (FileObject fo) {
        FileObject supe = super.findPrimaryFile (fo);
        if (supe != null) return supe;
        if (! getSecondaryExtensions ().isRegistered (fo)) return null;
        FileObject dir = fo.getParent ();
        if (dir == null) return null;
        String basename = fo.getName ();
        if (isInnerClasses ()) {
            int idx = basename.indexOf ('$');
            if (idx != -1)
                basename = basename.substring (0, idx);
        }
        Enumeration exts = getExtensions ().extensions ();
        while (exts.hasMoreElements ()) {
            FileObject child = dir.getFileObject (basename, (String) exts.nextElement ());
            if (child != null) return child;
        }
        return null;
    }

    protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject file) {
        return new FileEntry.Numb (obj, file);
    }

    /** null
     *@serialData Adds Boolean innerClasses, ExtensionList secondaryExtensions, String mimeType. */
    public void writeExternal(ObjectOutput out) throws IOException {
        super.writeExternal (out);
        out.writeObject (new Boolean (isInnerClasses ()));
        out.writeObject (getSecondaryExtensions ());
        out.writeObject (getMimeType ());
    }

    /** null
     *@serialData Adds Boolean innerClasses, ExtensionList secondaryExtensions, String mimeType. */
    public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
        super.readExternal (in);
        setInnerClasses (((Boolean) in.readObject ()).booleanValue ());
        setSecondaryExtensions ((ExtensionList) in.readObject ());
        setMimeType ((String) in.readObject ());
    }

}
