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

package org.netbeans.modules.projects.content;

import java.io.*;

import org.openide.filesystems.*;
import org.openidex.projects.*;

/**
 *
 * @author  mryzl
 */

public class XMLDiffSet extends AbstractDiffSet {

    FileObject fo;

    /** Creates new XMLDiffSet.
    */
    public XMLDiffSet(FileObject folder, String name, String ext, boolean create) throws IOException {
        fo = folder.getFileObject(name, ext);
        if (fo == null) {
            if (create) {
                fo = folder.createData(name, ext);
                store();
            } else throw new IOException("null file object"); // NOI18N
        }
    }

    /** Creates new XMLDiffSet.
    */
    public XMLDiffSet(FileObject fo) throws IOException {
        this.fo = fo;
    }

    /** Loads Diff Set. */
    public void load() throws IOException {
        loadDiffSet(fo, this);
    }

    /** Store diff set.
    */
    public void store() throws IOException {
        saveDiffSet(fo, this);
    }

    /** Static methods. */

    /** Save diff set to the given file object.
    * @param fo file object.
    * @param diffset diffset to be saved.
    */
    protected void loadDiffSet(FileObject fo, DiffSet diffset) throws IOException {
        if (fo == null) return;

        Reader reader = null;
        try {
            reader = new InputStreamReader(fo.getInputStream());
            XMLSupport.loadDiffSet(reader, diffset);
        } finally {
            if (reader != null) reader.close();
        }
    }

    /** Load diff set from the given file object.
    * @param fo file object.
    * @param diffset diffset that will be filled up.
    */
    protected void saveDiffSet(FileObject fo, DiffSet diffset) throws IOException {
        if (fo == null) return;

        FileLock lock = null;
        Writer writer = null;
        try {
            lock = fo.lock();
            writer = new OutputStreamWriter(fo.getOutputStream(lock));
            XMLSupport.saveDiffSet(writer, diffset);
        } finally {
            if (lock != null) lock.releaseLock();
            if (writer != null) writer.close();
        }
    }
}

/*
* Log
*  2    Gandalf   1.1         2/4/00   Martin Ryzl     correct handling of wrong
*       XML files
*  1    Gandalf   1.0         12/22/99 Martin Ryzl     
* $ 
*/ 
