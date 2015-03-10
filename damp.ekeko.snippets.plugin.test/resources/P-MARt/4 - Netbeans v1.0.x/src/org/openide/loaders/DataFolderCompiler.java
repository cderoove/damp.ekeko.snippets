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

import java.util.Enumeration;
import java.util.Vector;
import java.util.WeakHashMap;
import java.util.HashSet;

import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.cookies.CompilerCookie;

/** This class is implementation of compiler for folders. It means that
* folders implements CompilerCookie and returns this compiler. This compiler
* scans the folder and compile files (and folders) which implements
* CompilationCookie too.
*
* @author Jaroslav Tulach
*/
class DataFolderCompiler extends Object
    implements CompilerCookie.Compile, CompilerCookie.Build, CompilerCookie.Clean {
    /** Folder which is compiled by this compiler */
    private DataFolder folder;
    /** which cookie the compiler needs */
    private Class cookieClass;
    /** Map of CompilerJob:set of processed DataObjects fixes #1694 */
    private static WeakHashMap job2Data = new WeakHashMap(8);

    /* Creates new compiler for the given folder. */
    public DataFolderCompiler (DataFolder folder, Class cookieClass) {
        this.folder = folder;
        this.cookieClass = cookieClass;
    }

    /** Supports all depths.
    * @param depth the depth to test
    * @return true
    */
    public boolean isDepthSupported (Compiler.Depth depth) {
        return true;
    }

    /** A method that allows the cookie to add its compiler(s)
    * into a compiler job. The depth parameter specifies whether
    * the cookie should continue with its children or not.
    * If the depth.isLastDepth () is true then no children
    * should be processed. Otherwise process the children with
    * new cookie obtained by a call to depth.nextDepth ().
    *
    * @param job the compiler job to add the compiler for this cookie to
    * @param depth the depth to use for compilation
    *
    * @see org.openide.compiler.CompilerJob
    * @see org.openide.compiler.Compiler.Depth
    */
    public void addToJob (CompilerJob job, Compiler.Depth depth) {
        // do nothing if this is the last depth
        if (depth.isLastDepth ()) return;

        // add to the job children
        depth = depth.nextDepth ();

        HashSet set = (HashSet) job2Data.get(job);
        boolean first;

        if (set == null) {
            set = new HashSet(4);
            job2Data.put(job, set);
            first = true;
        } else {
            first = false;
        }

        Enumeration en = folder.children ();
        while (en.hasMoreElements ()) {
            DataObject obj = (DataObject)en.nextElement ();
            CompilerCookie c = (CompilerCookie)obj.getCookie (cookieClass);
            if (! set.contains(obj)) {
                set.add(obj);
                if (c != null) {
                    c.addToJob (job, depth);
                }
            } // else it was processed before
        }

        if (first) {
            job2Data.remove(job);
        }
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  2    Gandalf   1.1         5/20/99  Ales Novak      bugfix #1694
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.14        --/--/98 Jan Formanek    reflecting changes in cookies
 */
