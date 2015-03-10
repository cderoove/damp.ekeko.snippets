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
import java.io.ObjectInputStream;
import java.io.IOException;

import org.openide.loaders.DataObject;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerType;
import org.openide.compiler.CompilerJob;
import org.openide.compiler.ExternalCompiler;
import org.openide.compiler.ExternalCompiler.ErrorExpression;
import org.openide.compiler.ExternalCompilerGroup;
import org.openide.execution.NbProcessDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.filesystems.FileObject;

import org.netbeans.modules.java.settings.ErrorDescriptions;
import org.netbeans.modules.java.settings.JavaSettings;

/**
*
* @author Ales Novak
*/
public class FastJavacCompilerType extends JavaExternalCompilerType {

    /** generated Serialized Version UID */
    private static final long serialVersionUID = -3875747959787225041L;

    private static NbProcessDescriptor FAST_JAVAC;

    static {
        String fileSeparator = java.io.File.separator;

        /*
           The external compiler's CLASSPATH for JDK 1.2 contains:
           - the REPOSITORY
        */

        FAST_JAVAC = new NbProcessDescriptor(
                         "{" + JExternalCompilerGroup.JFormat.TAG_FORTEHOME + "}{" + ExternalCompilerGroup.Format.TAG_SEPARATOR + "}bin{" + // NOI18N
                         ExternalCompilerGroup.Format.TAG_SEPARATOR + "}fastjavac{" + ExternalCompilerGroup.Format.TAG_SEPARATOR + "}fastjavac", // NOI18N
                         //"-msgfile {" + JExternalCompilerGroup.JFormat.TAG_MSGFILE + "} " + // NOI18N
                         "-jdk {" + ExternalCompilerGroup.Format.TAG_JAVAHOME + "}{" + ExternalCompilerGroup.Format.TAG_SEPARATOR + "}.. " + // NOI18N
                         "-classpath {" + ExternalCompilerGroup.Format.TAG_REPOSITORY + "}{" + ExternalCompilerGroup.Format.TAG_PATHSEPARATOR + "}" + // NOI18N
                         "{" + ExternalCompilerGroup.Format.TAG_CLASSPATH + "}{" + ExternalCompilerGroup.Format.TAG_PATHSEPARATOR + "}" + // NOI18N
                         "{" + ExternalCompilerGroup.Format.TAG_LIBRARY + "}{" + ExternalCompilerGroup.Format.TAG_PATHSEPARATOR + "}" + // NOI18N
                         "{" + ExternalCompilerGroup.Format.TAG_BOOTCLASSPATH + "} " + // NOI18N
                         "{" + ExternalCompilerGroup.Format.TAG_FILES + "}", // NOI18N
                         NbBundle.getBundle (ExternalCompilerGroup.class).getString ("MSG_ExternalCompilerHint") +
                         Util.getString("MSG_FastCompilerHint")
                     );
    }

    public FastJavacCompilerType() {
        externalCompiler = FAST_JAVAC;
    }

    /** human presentable name */
    public String displayName() {
        // Default only matters for regular external, which is
        // instantiated using Class.newInstance:
        return getString("CTL_FastCompilerType");
        // In the case of the FastJavac, the .ser is given the token
        // name; when initially loaded from manifest, readObject
        // gives it a localized name. Thereafter the localized name
        // is serialized in the project.
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx(FastJavacCompilerType.class);
    }

    public static boolean isFastJavacPlatform() {
        return /*((Utilities.getOperatingSystem() &
                 (Utilities.OS_WINDOWS_MASK | Utilities.OS_SOLARIS)) != 0) ||
               isX86Linux();*/ isThereFile();
    }

    private static boolean isX86() {
        return (System.getProperty("os.arch").indexOf("i386") >= 0);
    }
    private static boolean isX86Linux() {
        return isX86() && ((Utilities.getOperatingSystem() & Utilities.OS_LINUX) != 0);
    }

    private static boolean isThereFile() {
        String nbdir = System.getProperty("netbeans.home");
        File fileA = new File(nbdir);
        fileA = new File(fileA, "bin");
        fileA = new File(fileA, "fastjavac");
        if (new File(fileA, "fastjavac").exists()) {
            return true;
        } else if (new File(fileA, "fastjavac.exe").exists()) {
            return true;
        }
        nbdir = System.getProperty("netbeans.user");
        File fileB = new File(nbdir);
        fileB = new File(fileB, "bin");
        fileB = new File(fileB, "fastjavac");
        if (new File(fileB, "fastjavac").exists()) {
            return true;
        } else if (new File(fileB, "fastjavac.exe").exists()) {
            return true;
        }
        return false;
    }
    
    /** @return one of fastjavac.sun, fastjavac.sun.intel, fastjavac.exe
     * fastjavac.linux, null
     */
    static String getWiredName() {
        int OS = Utilities.getOperatingSystem();
        if ((OS & Utilities.OS_SOLARIS) != 0) {
            if (System.getProperty("os.arch").indexOf("sparc") >= 0) { // NOI18N
                return "fastjavac.sun"; // NOI18N
            } else {
                return "fastjavac.sun.intel"; // NOI18N
            }
        } else if ((OS & Utilities.OS_WINDOWS_MASK) != 0) {
            return "fastjavac.exe";  // NOI18N
        } else if (isX86Linux()) {
            return "fastjavac.linux";
        }
        
        return null;
    }    
}

/*
* Log
*  1    Gandalf-post-FCS1.0         3/24/00  Ales Novak      
* $
*/