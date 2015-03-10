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

package org.openide.loaders;

import java.io.*;

import org.openide.filesystems.*;
import org.openide.util.NbBundle;

/** Entry that works with plain files. Copies, moves,
* renames and deletes them without any modification.
*
* @author Jaroslav Tulach
*/
public class FileEntry extends MultiDataObject.Entry {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 5972727204237511983L;

    /** Creates new file entry initially attached to a given file object.
    * @param obj the data object this entry belongs to
    * @param fo the file object for the entry
    */
    public FileEntry(MultiDataObject obj, FileObject fo) {
        obj.super (fo);
    }

    /* Makes a copy to given folder.
    * @param f the folder to copy to
    * @param suffix the suffix to add to the name of original file
    */
    public FileObject copy (FileObject f, String suffix) throws IOException {
        FileObject fo = getFile();
        String newName = fo.getName() + suffix;
        return fo.copy (f, newName, fo.getExt ());
    }

    /* Renames underlying fileobject. This implementation return the
    * same file.
    *
    * @param name new name
    * @return file object with renamed file
    */
    public FileObject rename (String name) throws IOException {
        FileLock lock = takeLock();
        try {
            getFile().rename(lock, name, getFile().getExt());
        } finally {
            lock.releaseLock();
        }
        return getFile ();
    }

    /* Moves file to another folder
    * @param f the folder
    * @param suffix the suffix to append to original name of the file
    * @return new file object for the file
    */
    public FileObject move (FileObject f, String suffix) throws IOException {
        FileObject fo = getFile();
        FileLock lock = takeLock ();
        try {
            String newName = fo.getName() + suffix;
            FileObject dest = fo.move (lock, f, newName, fo.getExt ());
            return dest;
        } finally {
            lock.releaseLock ();
        }
    }

    /* Deletes file object
    */
    public void delete () throws IOException {
        /* JST: This fixes bug 4660. But I am not sure whether this will not
        * create another or open some old bug.
            
            if (isLocked())
              throw new IOException(NbBundle.getBundle (FileEntry.class).getString ("EXC_SharedAccess"));
        */

        FileLock lock = takeLock();
        try {
            getFile().delete(lock);
        }
        finally {
            lock.releaseLock();
        }
    }

    /* Creates dataobject from template. Copies the file
    * @param f the folder to create instance in
    * @param name name of the file or null if it should be choosen automaticly
    */
    public FileObject createFromTemplate (FileObject f, String name) throws IOException {
        if (name == null) {
            name = FileUtil.findFreeFileName(
                       f,
                       getFile ().getName (), getFile ().getExt ()
                   );
        }
        FileObject fo = getFile().copy (f, name, getFile().getExt ());
        // unmark template state
        DataObject.setTemplate (fo, false);

        return fo;
    }

    /** Specialized entry that simplifies substitution when a file entry
    * is created from template.
    * Subclasses must implement
    * {@link #createFormat} and return a valid text format that
    * will be used for converting the lines of the original file
    * to lines in the newly created one.
    */
    public abstract static class Format extends FileEntry {
        static final long serialVersionUID =8896750589709521197L;
        /** Create a new entry initially attached to a given file object.
        * @param obj the data object this entry belongs to
        * @param fo the file object for the entry
        */
        public Format (MultiDataObject obj, FileObject fo) {
            super (obj, fo);
        }

        /* Creates dataobject from template. Copies the file and applyes substitutions
        * provided by the createFormat method.
        *
        * @param f the folder to create instance in
        * @param name name of the file or null if it should be choosen automaticly
        */
        public FileObject createFromTemplate (FileObject f, String name) throws IOException {
            String ext = getFile ().getExt ();

            if (name == null) {
                name = FileUtil.findFreeFileName(
                           f,
                           getFile ().getName (), ext
                       );
            }
            FileObject fo = f.createData (name, ext);

            java.text.Format frm = createFormat (f, name, ext);

            BufferedReader r = new BufferedReader (new InputStreamReader (getFile ().getInputStream ()));
            try {
                FileLock lock = fo.lock ();
                try {
                    BufferedWriter w = new BufferedWriter (new OutputStreamWriter (fo.getOutputStream (lock)));

                    try {
                        String line = null;
                        String current;
                        while ((current = r.readLine ()) != null) {
                            if (line != null) {
                                // newline between lines
                                w.newLine ();
                            }
                            line = frm.format (current);
                            w.write (line);
                        }
                    } finally {
                        w.close ();
                    }
                } finally {
                    lock.releaseLock ();
                }
            } finally {
                r.close ();
            }

            // copy attributes
            FileUtil.copyAttributes (getFile (), fo);

            // unmark template state
            DataObject.setTemplate (fo, false);

            return fo;
        }

        /** Provide a suitable format for
        * substitution of lines.
        *
        * @param target the target folder of the installation
        * @param n the name the file will have
        * @param e the extension the file will have
        * @return a format to use for formatting lines
        */
        protected abstract java.text.Format createFormat (FileObject target, String n, String e);

    }


    /** Simple file entry variant. It does nearly nothing.
    * When a file is copied, it does nothing. If it is moved
    * or renamed it deletes the file.
    * <P>
    * Useful for representing useless files.
    */
    public final static class Numb extends MultiDataObject.Entry {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -6572157492885890612L;

        /**
         * Create a dummy entry.
         * @param obj the data object this entry belongs to
         * @param fo the file object to create an entry for
         */
        public Numb (MultiDataObject obj, FileObject fo) {
            obj.super (fo);
        }

        /** Does nothing.
        * @param f ignored
        * @param suffix ignored
        * @return <code>null</code>
        */
        public FileObject copy (FileObject f, String suffix) {
            return null;
        }

        /** Removes file.
         * @param name ignored
        * @return <code>null</code>
         * @throws IOException in case of problem
        */
        public FileObject rename (String name) throws IOException {
            stdBehaving();
            return null;
        }

        /** Removes file.
         * @param f ignored
         * @param suffix ignored
        * @return <code>null</code>
         * @throws IOException in case of problem
        */
        public FileObject move (FileObject f, String suffix) throws IOException {
            stdBehaving();
            return null;
        }

        /** Removes file.
         * @throws IOException in case of problem
         */
        public void delete () throws IOException {
            stdBehaving();
        }

        /** Removes file.
         * @throws IOException in case of problem
         */
        private void stdBehaving () throws IOException {
            if (getFile() == null)
                return;

            if (isLocked())
                throw new IOException (NbBundle.getBundle (FileEntry.class).getString ("EXC_SharedAccess"));

            FileLock lock = takeLock();
            try {
                getFile().delete(lock);
            } finally {
                if (lock != null)
                    lock.releaseLock();
            }
        }

        /** Does nothing.
         * @param f ignored
         * @param name ignored
         * @return <code>null</code>
         */
        public FileObject createFromTemplate (FileObject f, String name) {
            return null;
        }
    }
}

/*
* Log
*  15   Gandalf   1.14        12/6/99  Jaroslav Tulach #4660  
*  14   Gandalf   1.13        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  13   Gandalf   1.12        10/1/99  Jaroslav Tulach Uses FileObject.copy & 
*       move
*  12   Gandalf   1.11        8/18/99  Ian Formanek    Generated serial version 
*       UID
*  11   Gandalf   1.10        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  10   Gandalf   1.9         3/31/99  Jesse Glick     [JavaDoc]
*  9    Gandalf   1.8         3/31/99  Jaroslav Tulach FileEntry.Format
*  8    Gandalf   1.7         3/26/99  Ian Formanek    Fixed use of obsoleted 
*       NbBundle.getBundle (this)
*  7    Gandalf   1.6         3/22/99  Jaroslav Tulach Fixed creation from 
*       template
*  6    Gandalf   1.5         3/15/99  Jesse Glick     [JavaDoc]
*  5    Gandalf   1.4         3/14/99  Jaroslav Tulach Change of 
*       MultiDataObject.Entry.
*  4    Gandalf   1.3         3/9/99   Jesse Glick     [JavaDoc]
*  3    Gandalf   1.2         3/9/99   Jaroslav Tulach Works even there is no 
*       secondary entry.
*  2    Gandalf   1.1         1/6/99   Ian Formanek    
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
