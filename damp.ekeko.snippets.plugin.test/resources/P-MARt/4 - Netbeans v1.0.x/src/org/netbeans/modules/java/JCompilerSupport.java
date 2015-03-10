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

import org.openide.TopManager;
import org.openide.loaders.CompilerSupport;
import org.openide.cookies.CompilerCookie;
import org.openide.loaders.MultiDataObject.Entry;
import org.openide.compiler.CompilerType;

import org.netbeans.modules.java.settings.JavaSettings;

/** Redefines defaultCompiler method
*
* @author Ales Novak
*/
public class JCompilerSupport extends CompilerSupport {

    /** my settings */
    static JavaSettings opts;

    protected JCompilerSupport(Entry entry, Class cookie) {
        super(entry, cookie);
    }

    /** @return default CompilerType for JavaDOs */
    protected CompilerType defaultCompilerType() {
        return getOpts().getCompiler();
        /*
          return (CompilerType) TopManager.getDefault().getServices().find(JavaInternalCompilerType.class);
    } else { // search for an external compiler
          return (CompilerType) TopManager.getDefault().getServices().find(JavaExternalCompilerType.class);
    } */
    }

    /** getter for JavaSettings */
    private static JavaSettings getOpts() {
        if (opts == null) {
            opts = (JavaSettings) JavaSettings.findObject(JavaSettings.class, true);
        }
        return opts;
    }

    /** Compile cookie support.
    * Note that as a special case, when {@link Compiler#DEPTH_ONE} is requested,
    * a {@link CompilerCookie.Build} will actually be sent to the compiler manager,
    * rather than a {@link CompilerCookie.Compile}, on the assumption that the user
    * wished to force (re-)compilation of the single data object.
    */
    public static class Compile extends JCompilerSupport
        implements CompilerCookie.Compile {
        /** New support for given entry. The file is taken from the
        * entry and is updated if the entry moves or renames itself.
        * @param entry entry to create instance from
        */
        public Compile (Entry entry) {
            super (entry, CompilerCookie.Compile.class);
        }
    }

    /** Build cookie support.
    */
    public static class Build extends JCompilerSupport
        implements CompilerCookie.Build {
        /** New support for given entry. The file is taken from the
        * entry and is updated if the entry moves or renames itself.
        * @param entry entry to create instance from
        */
        public Build (Entry entry) {
            super (entry, CompilerCookie.Build.class);
        }
    }

    /** Clean cookie support.
    */
    public static class Clean extends JCompilerSupport
        implements CompilerCookie.Clean {
        /** New support for given entry. The file is taken from the
        * entry and is updated if the entry moves or renames itself.
        * @param entry entry to create instance from
        */
        public Clean (Entry entry) {
            super (entry, CompilerCookie.Clean.class);
        }
    }
}

/*
 * Log
 *  7    Gandalf   1.6         2/7/00   Ales Novak      rolling back last change
 *  6    Gandalf   1.5         1/17/00  Ales Novak      #5394
 *  5    Gandalf   1.4         12/1/99  Ales Novak      property compiler 
 *       changed from boolean to CompilerType
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         10/8/99  Ales Novak      made public
 *  2    Gandalf   1.1         10/8/99  Ales Novak      makeDefaultAction works 
 *       for Java compilers
 *  1    Gandalf   1.0         9/29/99  Ales Novak      
 * $
 */
