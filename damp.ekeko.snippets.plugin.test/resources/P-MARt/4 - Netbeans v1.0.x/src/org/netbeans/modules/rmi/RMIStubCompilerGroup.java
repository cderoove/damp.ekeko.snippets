/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.rmi;

import java.io.File;
import java.io.IOException;

import org.openide.compiler.Compiler;
import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.*;

/** RMI stub compiler group.
 * 
 *
 * @author Martin Ryzl
 */
public class RMIStubCompilerGroup extends ExternalCompilerGroup {

    private static boolean DEBUG = false;

    /** TAG for format. */
    public static final String TAG_PACKAGEROOT = "packageroot"; // NOI18N

    /** File object for the file system. */
    FileObject fs;

    public void add(Compiler c) throws IllegalArgumentException {
        // fs must be file object, folder
        try {
            fs = (FileObject)c.compilerGroupKey();
        } catch (Exception ex) {
            throw new IllegalArgumentException();
        }
        if (!fs.isFolder()) throw new IllegalArgumentException();
        super.add(c);
    }

    /** Allows subclasses to provide their own format for parsing
    * the arguments of NbProcessDescriptor contained in the
    * ExternalCompiler.
    * <P>
    * This implementation creates new format Format with settings
    * from NbClassPath.createXXXX and executes them in the provided
    * process descriptor.
    *
    * @param desc description of program to start
    * @param files the argument to compiler list of files to compile (or reference
    *   to the file with @files)
    * @return format to use for changing the command line of the compiler
    * @exception IOException if exec fails
    */
    protected Process createProcess (NbProcessDescriptor desc, String[] files) throws IOException {

        ExternalCompilerGroup.Format format = new ExternalCompilerGroup.Format(files);
        java.util.Map map = format.getMap();
        map.put(TAG_PACKAGEROOT, getPackageRoot(fs));

        return desc.exec (format);
    }

    /** Get classpath of the root of the filesystem of the given file object.
    * @param fo - file object of the filesystem
    * @return classpath
    */
    public static String getPackageRoot(FileObject fo) throws IllegalArgumentException {
        final StringBuffer pr = new StringBuffer(64);

        try {
            fo.getFileSystem().prepareEnvironment(new FileSystem.Environment() {
                                                      public void addClassPath(String element) {
                                                          pr.append(element);
                                                      }
                                                  });
        } catch (FileStateInvalidException ex) {
            throw new IllegalArgumentException();
        } catch (EnvironmentNotSupportedException ex) {
            // use current directory
            return "."; // NOI18N
        }
        // root must be a directory!
        String root = pr.toString();
        File fr = new File(root);
        try {
            if (fr.isDirectory()) return root;
        } catch (Exception ex) {
            // use default value
        }
        return "."; // NOI18N
    }
}



/*
 * <<Log>>
 *  6    Gandalf-post-FCS1.4.1.0     3/20/00  Martin Ryzl     localization
 *  5    Gandalf   1.4         1/24/00  Martin Ryzl     compilation of inner 
 *       classes added
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         10/8/99  Martin Ryzl     debug info commented out
 *  2    Gandalf   1.1         10/7/99  Martin Ryzl     
 *  1    Gandalf   1.0         10/6/99  Martin Ryzl     
 * $
 */
