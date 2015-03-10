/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.jarpackager;

import java.util.*;
import java.io.IOException;
import java.text.MessageFormat;

import org.openide.filesystems.FileObject;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.CompilerGroup;
import org.openide.compiler.ErrorEvent;
import org.openide.compiler.ProgressEvent;
import org.openide.util.NbBundle;

import org.netbeans.modules.jarpackager.util.ProgressListener;

/** Compiler for the jar archives.
* Compiling of jar archive is actually a repackaging 
* (recreating) the archive to keep the information up to date.
*
* @author Dafe Simonek
*/
final class JarCompiler extends Compiler {

    /** The instance desribing content of the jar */
    JarContent jc;
    /** The archive to compile */
    FileObject fo;

    /** Creates new JarCompiler */
    public JarCompiler (FileObject fo, JarContent jc) {
        super();
        this.jc = jc;
        this.fo = fo;
    }

    /** Check whether the compiler is up to date.
    * PENDING - always return false for now...
    *
    * @return false if jar content is null, true otherwise
    */
    protected boolean isUpToDate () {
        return false;
    }

    /** Get the associated <code>CompilerGroup</code> container class.
    * The compiler and compiler group should typically be implemented in parallel.
    * @return a class assignable to {@link CompilerGroup}
    */
    public Class compilerGroupClass () {
        return Group.class;
    }

    /** @return true if two jar compilers have the same file object
    * and jar content attached, false otherwise */
    public boolean equals (Object obj) {
        if (obj instanceof JarCompiler) {
            JarCompiler other = (JarCompiler)obj;
            return super.equals(obj) && (jc == other.jc) && (fo == other.fo);
        }
        return false;
    }

    /** @return Hash code for this jar compiler */
    public int hashCode() {
        return ((fo == null) ? 0 : fo.hashCode()) ^ ((jc == null) ? 0 : jc.hashCode());
    }


    public static final class Group extends CompilerGroup
        implements ProgressListener {

        /** All jar contents whcih we should compile 
         * @associates Compiler*/
        List contents = new LinkedList();
        /** Worker instance we delegate the work to */
        JarCreater curCreater;

        public void add (Compiler c) throws IllegalArgumentException {
            // check
            if (!(c instanceof JarCompiler))
                throw new IllegalArgumentException();
            contents.add(c);
        }

        /** Finally compile the jar (update its content) */
        public boolean start () {
            JarCompiler curJc = null;
            curCreater = null;
            try {
                for (Iterator iter = contents.iterator(); iter.hasNext(); ) {
                    curJc = (JarCompiler)iter.next();
                    // now create jar
                    curCreater = new JarCreater(curJc.jc);
                    curCreater.addProgressListener(this);
                    curCreater.createJar(curJc.fo);
                    curCreater.removeProgressListener(this);
                }
            } catch (IOException exc) {
                if (System.getProperty("netbeans.debug.exceptions") != null) {
                    exc.printStackTrace();
                }
                if (curCreater != null) {
                    curCreater.removeProgressListener(this);
                }
                // some error, fire error event...
                fireErrorEvent(new ErrorEvent(this, curJc.fo, 0, 0,
                                              exc.getMessage(), "")); // NOI18N
                return false;
            } finally {
                curCreater = null;
            }
            return true;
        }

        /** Notification about packaging (compiling) progress. */
        public void progress (int percent, String description) {
            fireProgressEvent(
                new ProgressEvent(this, curCreater.getProcessedFileObject())
            );
        }

    } // end of Group inner class

}

/*
* <<Log>>
*  13   Gandalf   1.12        1/25/00  David Simonek   Various bugfixes and i18n
*  12   Gandalf   1.11        1/16/00  David Simonek   i18n
*  11   Gandalf   1.10        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems copyright in file comment
*  10   Gandalf   1.9         10/14/99 David Simonek   manifest updating 
*       bugfixes
*  9    Gandalf   1.8         10/13/99 David Simonek   jar content now primary 
*       file, other small changes
*  8    Gandalf   1.7         10/4/99  David Simonek   
*  7    Gandalf   1.6         7/31/99  David Simonek   lot of bugfixes
*  6    Gandalf   1.5         6/9/99   David Simonek   bugfixes, progress 
*       dialog, compiling progress..
*  5    Gandalf   1.4         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    Gandalf   1.3         6/8/99   David Simonek   
*  3    Gandalf   1.2         6/8/99   David Simonek   bugfixes....
*  2    Gandalf   1.1         6/4/99   Petr Hamernik   temporary version
*  1    Gandalf   1.0         6/4/99   David Simonek   
* $
*/