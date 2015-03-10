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

package org.netbeans.modules.javadoc.search;

import java.util.ArrayList;
import java.util.Enumeration;

import org.openide.TopManager;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileSystemCapability;
import org.openide.filesystems.FileObject;

/** This class represents one file system in repository which was found
 * to be a directory with documentation in formated by standard 1.2 doclet.
 * The static method {@link #getDocFileSystems} returns all such systems in
 * the repository.
 *
 * @author Petr Hrebejk
 */
public class DocFileSystem extends Object {

    FileObject indexFileObject;

    public DocFileSystem( FileObject indexFileObject ) {
        this.indexFileObject = indexFileObject;
    }

    FileObject getIndexFile( ) {
        return indexFileObject;
    }



    static boolean isDocFolder( DataFolder df ) {
        return false;

    }

    public static FileObject getDocFileObject( FileSystem fs ) {

        FileObject fo = fs.find( "index-files", null, null ); // NOI18N
        if ( fo != null ) {
            return fo;
        }

        fo = fs.find( "", "index-all", "html" ); // NOI18N
        if ( fo != null ) {
            return fo;
        }

        fo = fs.find( "api.index-files", null, null ); // NOI18N
        if ( fo != null ) {
            return fo;
        }

        fo = fs.find( "api", "index-all", "html" ); // NOI18N
        if ( fo != null ) {
            return fo;
        }

        return null;
    }

    static DocFileSystem[] getFolders() {
        ArrayList result = new ArrayList();
        //Enumeration fileSystems = TopManager.getDefault().getRepository().getFileSystems();
        Enumeration fileSystems = FileSystemCapability.DOC.fileSystems();

        while ( fileSystems.hasMoreElements() ) {
            FileSystem fs = (FileSystem)fileSystems.nextElement();
            FileObject fo = getDocFileObject( fs );

            if ( fo != null ) {
                result.add( new DocFileSystem( fo ) );
            }

        }

        DocFileSystem[] dsa = new DocFileSystem[result.size()];
        result.toArray( dsa );
        return dsa;
    }
}

/*
 * Log
 *  8    Gandalf   1.7         1/12/00  Petr Hrebejk    i18n
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         8/13/99  Petr Hrebejk    Exception icopn added & 
 *       Jdoc repository moved to this package
 *  5    Gandalf   1.4         7/30/99  Petr Hrebejk    Search uses 
 *       FileSystemCapabilities
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         5/27/99  Petr Hrebejk    Crtl+F1 documentation 
 *       search form editor added
 *  2    Gandalf   1.1         5/14/99  Petr Hrebejk    
 *  1    Gandalf   1.0         5/13/99  Petr Hrebejk    
 * $ 
 */ 