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
import java.util.Enumeration;

import org.openide.TopManager;
import org.openide.execution.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.Repository;
import org.openide.util.*;

/** A testing executor for makefiles.
 * @author Jesse Glick
 */
public class MakefileExecutor extends ProcessExecutor {

    /** Default command template.
     */
    private static final NbProcessDescriptor DEFAULT = new NbProcessDescriptor (
                // PROCESS NAME:
                "make",
                // LIST OF ARGUMENTS INCLUDING OPTIONS:
                "-C {" + MakefileCompilerGroup.Format.TAG_DIRECTORY + "} " +
                "-f {" + MakefileCompilerGroup.Format.TAG_MAKEFILE + "} " +
                "-s {" + MakefileCompilerGroup.Format.TAG_TARGET + "} " +
                "{" + ProcessExecutor.Format.TAG_ARGUMENTS + "}",
                // DESCRIPTION FOR USER OF HOW TO MODIFY THE ARGUMENTS:
                NbBundle.getBundle (MakefileCompilerType.class).getString ("MSG_format_hint_ME")
            );

    /** Current make target.
     */
    private String target = "test";

    /** generated
     */
    private static final long serialVersionUID = -5186756253621423544L;

    /** Create a new executor.
     */
    public MakefileExecutor () {
        setExternalExecutor (DEFAULT);
    }

    /** Get the default display name.
     * Workaround for 1.0 core bug; not needed in 1.1 core.
     * @return the name
     */
    protected String displayName () {
        try {
            return java.beans.Introspector.getBeanInfo (getClass ()).getBeanDescriptor ().getDisplayName ();
        } catch (Exception e) {
            if (Boolean.getBoolean ("netbeans.debug.exceptions"))
                e.printStackTrace ();
            return getClass ().getName ();
        }
    }

    /** Bean getter.
     * @return the make test target
     */
    public String getTarget () {
        return target;
    }

    /** Bean setter.
     * @param nue the make test target
     */
    public synchronized void setTarget (String nue) {
        String old = target;
        target = nue;
        firePropertyChange ("target", old, nue);
    }

    /** Create a process to run the makefile.
     * @param info information about the makefile location and arguments
     * @throws IOException as usual
     * @return the process
     */
    protected Process createProcess (ExecInfo info) throws IOException {
        String base = info.getClassName ().replace ('.', '/');
        Repository repo = TopManager.getDefault ().getRepository ();
        FileObject fo = repo.findResource (base);
        if (fo == null) {
            Enumeration exts = ((MakefileDataLoader) SharedClassObject.findObject (MakefileDataLoader.class, true)).getExtensions ().extensions ();
            while (exts.hasMoreElements ()) {
                fo = repo.findResource (base + '.' + (String) exts.nextElement ());
                if (fo != null) break;
            }
        }
        if (fo == null) throw new IOException ("Cannot find " + base + " in Repository");
        File f = NbClassPath.toFile (fo);
        if (f == null) throw new IOException ("Makefile must live on a local disk");
        return getExternalExecutor ().exec (new Format (f.getAbsolutePath (), getTarget (), info.getArguments ()));
    }

    /** Get context help.
     * @return help on the tester
     */
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.modules.makefile.executor");
    }

    /** A special tag format for the executor.
     */
    public static class Format extends MakefileCompilerGroup.Format {

        /** generated
         */
        private static final long serialVersionUID = -9213361383767640600L;

        /** Create a new format.
         * @param makefile the full path name to the makefile
         * @param target the target to test
         * @param args any command arguments specified in the <B>Execution</B>
         * tab for the makefile
         */
        public Format (String makefile, String target, String[] args) {
            super (makefile, target);
            StringBuffer sb = new StringBuffer ();
            for (int i = 0; i < args.length; i++) {
                if (i > 0) sb.append (' ');
                sb.append ('"');
                sb.append (args[i]);
                sb.append ('"');
            }
            getMap ().put (ProcessExecutor.Format.TAG_ARGUMENTS, sb.toString ());
        }

    }

}
