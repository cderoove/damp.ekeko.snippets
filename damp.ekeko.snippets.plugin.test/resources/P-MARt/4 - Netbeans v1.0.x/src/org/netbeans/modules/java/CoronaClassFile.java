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

package org.netbeans.modules.java;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import sun.tools.java.ClassFile;

import org.openide.cookies.SaveCookie;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;

/**
* This class is used to represent a file loaded from a FileSystem.
*
* @author Ales Novak
*/
public class CoronaClassFile extends ClassFile {

    /** Object to represent */
    private FileObject file;
    /** DataObject for the file. */
    private DataObject dataObject;
    /** parserinput stream for this CCF. */
    Util.ParserInputStream pis;


    /**
    * @param file is a FileObject we want to work with.
    */
    public CoronaClassFile (FileObject file) {
        super (null);
        this.file = file;
    }

    /** always returns false - for backward compatibility */
    public boolean isZipped() {
        return false;
    }

    /**
    * @return input stream for the FileObject
    */
    public InputStream getInputStream()
    throws IOException    {
        checkSave();
        return file.getInputStream();
    }

    private void checkSave() {
        if (dataObject == null) {
            try {
                dataObject = DataObject.find(file);
            } catch (DataObjectNotFoundException e) {
            } // ignore
        }
        if (dataObject != null) {
            try {
                if (dataObject instanceof JavaDataObject) {
                    ((JavaDataObject) dataObject).getJavaEditor().saveDocumentIfNecessary(false);
                } else {
                    SaveCookie cookie = (SaveCookie) dataObject.getCookie(SaveCookie.class);
                    if (cookie != null) {
                        cookie.save();
                    }
                }
            } catch (IOException e) {
            } // ignore
        }
    }

    /** decides when file exists
    * @return true iff it is a valid file
    */
    public boolean exists() {
        return (file != null) && (file.isValid());
    }

    /**
    * @param return true iff the file is directory
    */
    public boolean isDirectory() {
        return file.isFolder();
    }

    /** for backward compatibility */
    public long lastModified() {
        checkSave();
        return file.lastModified().getTime ();
    }

    /**
    * @return path to the file
    */
    public String getPath() {
        return file.getPackageName(File.separatorChar);
    }

    /**
    * @return name.ext of the file
    */
    public String getName() {
        return file.getName() + (file.getExt().compareTo("") == 0 ? "": "." + file.getExt()); // NOI18N
    }

    /** always throws exception */
    public String getAbsoluteName() {
        //return (file.getPackageName('.') + file.getName());
        throw new org.openide.util.NotImplementedException();
    }

    /**
    * @return legth of the file
    */
    public long length() {
        return file.getSize();
    }

    /**
    * @return name of the file
    */
    public String toString() {
        return getName();
        //file.toString();
    }

    //our method
    /**
    * @return underlying file
    */
    public FileObject getFile() {
        return file;
    }
}

/*
 * Log
 *  8    Gandalf-post-FCS1.6.2.0     2/24/00  Ian Formanek    Post FCS changes
 *  7    src-jtulach1.6         1/12/00  Petr Hamernik   i18n: perl script used (
 *       //NOI18N comments added )
 *  6    src-jtulach1.5         11/5/99  Ales Novak      #2206
 *  5    src-jtulach1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    src-jtulach1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    src-jtulach1.2         5/15/99  Ales Novak      saving used sources 
 *       added
 *  2    src-jtulach1.1         4/23/99  Petr Hrebejk    Classes temporay made 
 *       public
 *  1    src-jtulach1.0         3/28/99  Ales Novak      
 * $
 */
