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

package org.netbeans.modules.makefile;

import java.io.File;
import java.io.IOException;
import java.text.*;
import java.util.*;

import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.execution.NbProcessDescriptor;
import org.openide.filesystems.*;
import org.openide.util.*;

/** Compiler group.
* Runs one makefile at a time.
*
* @author Jaroslav Tulach, Jesse Glick
*/
public class MakefileCompilerGroup extends ExternalCompilerGroup {

    /** The target to use.
     */
    private String target;
    /** The path to the makefile we are running.
     */
    private String makefileName;

    /** Add the (one) compiler to the group.
     * @param c must be a make compiler
     * @throws IllegalArgumentException if not
     */
    public void add (Compiler c) throws IllegalArgumentException {
        if (! (c instanceof MakefileCompiler)) throw new IllegalArgumentException ();
        super.add (c);
        MakefileCompiler mc = (MakefileCompiler) c;
        target = mc.getTarget ();
    }

    /** Create the process to run make.
     * @param desc the command template
     * @param files must be one filename
     * @throws IOException if not, or any other problem
     * @return the process
     */
    protected Process createProcess (NbProcessDescriptor desc, String[] files)
    throws IOException {
        if (files.length != 1) throw new IOException ("Cannot be applied to >1 files");
        makefileName = files[0];
        return desc.exec (new Format (makefileName, target));
    }

    /** All real listeners to changes.
     */
    private Set listeners = new HashSet ();
    /** A dummy listener which will delegate.
     */
    private CompilerListener myselfListener = null;
    /** Add a compiler listener but trap and delegate error events.
     * This permits us to fix up otherwise missing file pointers
     * where needed.
     * @param l the listener to add
     */
    public synchronized void addCompilerListener (CompilerListener l) {
        listeners.add (l);
        if (myselfListener == null) {
            myselfListener = new CompilerListener () {
                                 public void compilerError (ErrorEvent ev) {
                                     //System.err.println("ErrorEvent: file=" + ev.getFile () + " message=" + ev.getMessage () + " referenceText=" + ev.getReferenceText ());
                                     String message = ev.getMessage ();
                                     if (ev.getFile () == null && message != null) {
                                         //System.err.println("will try to correct null file");
                                         // This is a real hack. Basically we do not wish to fiddle with the
                                         // regular expressions all over again. However, the external compiler
                                         // implementation currently includes the partial file match information
                                         // in a specially formatted message string constructed from the file
                                         // name that it knows of, the line and column numbers, and the original
                                         // message. So we are just reversing this message and trying to extract
                                         // the original file name, then trying to find it ourselves relative to
                                         // the directory the makefile is in (which is often how the file name will
                                         // be reported, rather than an absolute name as is customary for regular
                                         // compilers since this is what is passed to them by the IDE).
                                         String dummyMessage;
                                         try {
                                             dummyMessage = NbBundle.getBundle (ExternalCompiler.class).getString ("MSG_Unknown_file");
                                         } catch (MissingResourceException mre) {
                                             System.err.println("WARNING: could not find original MSG_Unknown_file key;");
                                             System.err.println("compilation errors from Makefiles may not be hyperlinked");
                                             dummyMessage = NbBundle.getBundle (MakefileCompilerGroup.class).getString ("MSG_Unknown_file");
                                         }
                                         //System.err.println("dummyMessage=" + dummyMessage);
                                         MessageFormat format = new MessageFormat (dummyMessage);
                                         try {
                                             Object[] parse = format.parse (message);
                                             if (parse.length >= 4 && (parse[0] instanceof String) && (parse[3] instanceof String)) {
                                                 String file = (String) parse[0];
                                                 //System.err.println("file=" + file);
                                                 final String origMessage = (String) parse[3];
                                                 //System.err.println("origMessage=" + origMessage);
                                                 // Calculate probable file name based on makefile name and stated name;
                                                 // should handle e.g. ..\..\foo\Bar.java and such things
                                                 File real = new File (new File (makefileName).getParentFile (), file.replace ('/', File.separatorChar));
                                                 try {
                                                     real = real.getCanonicalFile ();
                                                 } catch (IOException ioe) {
                                                     ioe.printStackTrace ();
                                                 }
                                                 final String realS = real.toString ();
                                                 //System.err.println("realS=" + realS);
                                                 // Now look for it in filesystems which add one directory to the classpath:
                                                 Enumeration fss = FileSystemCapability.COMPILE.fileSystems ();
                 FOUNDYA:
                                                 while (fss.hasMoreElements ()) {
                                                     final FileSystem fs = (FileSystem) fss.nextElement ();
                                                     //System.err.println("fs=" + fs.getDisplayName ());
                                                     try {
                                                         final ErrorEvent[] fixed = new ErrorEvent[] { null };
                                                         final ErrorEvent origEvent = ev;
                                                         fs.prepareEnvironment (new FileSystem.Environment () {
                                                                                    public void addClassPath (String classPathElement) {
                                                                                        File rootPath = new File (classPathElement);
                                                                                        if (rootPath.isDirectory ()) {
                                                                                            try {
                                                                                                rootPath = rootPath.getCanonicalFile ();
                                                                                            } catch (IOException ioe) {
                                                                                                ioe.printStackTrace ();
                                                                                            }
                                                                                            String rootPathS = rootPath.toString ();
                                                                                            //System.err.println("rootPathS=" + rootPathS);
                                                                                            if (realS.startsWith (rootPathS)) {
                                                                                                String relative = realS.substring (rootPathS.length ());
                                                                                                if (relative.startsWith (File.separator)) relative = relative.substring (1);
                                                                                                //System.err.println("relative=" + relative);
                                                                                                FileObject fo = fs.findResource (relative.replace (File.separatorChar, '/'));
                                                                                                if (fo != null) {
                                                                                                    // About time! :-)
                                                                                                    //System.err.println("fixing! fo=" + fo);
                                                                                                    fixed[0] = new ErrorEvent (origEvent.getCompilerGroup (),
                                                                                                                               fo,
                                                                                                                               origEvent.getLine (),
                                                                                                                               origEvent.getColumn (),
                                                                                                                               origMessage,
                                                                                                                               origEvent.getReferenceText ());
                                                                                                }
                                                                                            }
                                                                                        }
                                                                                    }
                                                                                });
                                                         if (fixed[0] != null) {
                                                             ev = fixed[0];
                                                             break FOUNDYA;
                                                         }
                                                     } catch (EnvironmentNotSupportedException ense) {
                                                         //System.err.println("got: " + ense);
                                                     }
                                                 }
                                             } else {
                                                 //System.err.println("Parse result did not start/end with a String or was not four elements");
                                                 //System.err.println("Parse result:");
                                                 //for (int i = 0; i < parse.length; i++)
                                                 //  System.err.println("\t" + parse[i]);
                                             }
                                         } catch (ParseException pe) {
                                             //System.err.println("Could not parse the message");
                                         }
                                     }
                                     synchronized (MakefileCompilerGroup.this) {
                                         Iterator it = listeners.iterator ();
                                         while (it.hasNext ())
                                             ((CompilerListener) it.next ()).compilerError (ev);
                                     }
                                 }
                                 public void compilerProgress (ProgressEvent ev) {
                                     //System.err.println("ProgressEvent: file=" + ev.getFile () + " task=" + ev.getTask ());
                                     synchronized (MakefileCompilerGroup.this) {
                                         Iterator it = listeners.iterator ();
                                         while (it.hasNext ())
                                             ((CompilerListener) it.next ()).compilerProgress (ev);
                                     }
                                 }
                             };
            super.addCompilerListener (myselfListener);
        }
    }
    /** Remove a listener.
     * @param l the listener to remove
     */
    public synchronized void removeCompilerListener (CompilerListener l) {
        listeners.remove (l);
    }

    /** Formats files and directory tags.
    */
    public static class Format extends MapFormat {
        /** Tag for the makefile basename.
         */
        public static final String TAG_MAKEFILE = "makefile";
        /** Tag for the makefile's containing directory.
         */
        public static final String TAG_DIRECTORY = "directory";
        /** Tag for the makefile's full path name.
         */
        public static final String TAG_DIRECTORY_AND_MAKEFILE = "directory_and_makefile";
        /** Tag for the makefile target.
         */
        public static final String TAG_TARGET = "target";

        /** generated
         */
        private static final long serialVersionUID = 449845585043666525L;

        /** Creates the format.
         * @param file the makefile
         * @param target the make target
         */
        public Format (String file, String target) {
            super (new HashMap (4));

            File f = new File (file);
            getMap ().put (TAG_MAKEFILE, f.getName ());
            getMap ().put (TAG_DIRECTORY, f.getParentFile ().getAbsolutePath ());
            getMap ().put (TAG_DIRECTORY_AND_MAKEFILE, f.getAbsolutePath ());
            getMap ().put (TAG_TARGET, target);
        }
    }

}
