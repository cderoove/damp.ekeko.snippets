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

package org.netbeans.modules.web.core.jsploader;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Enumeration;

import org.openide.compiler.CompilerType;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.loaders.CompilerSupport;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.cookies.CompilerCookie;

import org.netbeans.modules.java.JCompilerSupport;

/** Support for compilation of JSPDataObject. Subclass of JCompilerSupport to handle
* defaultCompilerType() correctly.
*
* @author Petr Jiricka
*/
public class JspCompilerSupport extends CompilerSupport {

    /** cookie class for the compilation */
    protected Class cookie;

    /** entry to be associated with */
    protected JspDataObject jspdo;

    /** New support for given entry. The file is taken from the
    * entry and is updated if the entry moves or renames itself.
    * @param entry entry to create instance from
    * @param cookie cookie class for the compilation (e.g. {@link CompilerCookie.Build})
    */
    protected JspCompilerSupport(JspDataObject jspdo, Class cookie) {
        super(jspdo.getPrimaryEntry(), cookie);
        this.cookie = cookie;
        this.jspdo = jspdo;
    }


    /* Adds the right compiler to the job.
    */
    public void addToJob (CompilerJob job, Compiler.Depth depth) {
        boolean individual = ((cookie == CompilerCookie.Compile.class) && (Compiler.DEPTH_ONE == depth));
        /*Class xcookie;
        if (cookie == CompilerCookie.Compile.class) {
          xcookie = (Compiler.DEPTH_ONE == depth ? CompilerCookie.Build.class : cookie);
    } else {
          xcookie = cookie;
    }*/
        jspdo.createCompiler(job, cookie, /*depth,*/ individual);
    }

    public CompilerType defaultCompilerType() {
        return (new MyJCompilerSupport(jspdo.getPrimaryEntry(), cookie)).defaultCompilerType();
    }

    /** Compile cookie support.
    * Note that as a special case, when {@link Compiler#DEPTH_ONE} is requested,
    * a {@link CompilerCookie.Build} will actually be sent to the compiler manager,
    * rather than a {@link CompilerCookie.Compile}, on the assumption that the user
    * wished to force (re-)compilation of the single data object.
    */
    public static class Compile extends JspCompilerSupport
        implements CompilerCookie.Compile {
        /** New support for given entry. The file is taken from the
        * entry and is updated if the entry moves or renames itself.
        * @param entry entry to create instance from
        */
        public Compile (JspDataObject jspdo) {
            super (jspdo, CompilerCookie.Compile.class);
        }
    }

    /** Build cookie support.
    */
    public static class Build extends JspCompilerSupport
        implements CompilerCookie.Build {
        /** New support for given entry. The file is taken from the
        * entry and is updated if the entry moves or renames itself.
        * @param entry entry to create instance from
        */
        public Build (JspDataObject jspdo) {
            super (jspdo, CompilerCookie.Build.class);
        }
    }

    /** Clean cookie support.
    */
    public static class Clean extends JspCompilerSupport
        implements CompilerCookie.Clean {
        /** New support for given entry. The file is taken from the
        * entry and is updated if the entry moves or renames itself.
        * @param entry entry to create instance from
        */
        public Clean (JspDataObject jspdo) {
            super (jspdo, CompilerCookie.Clean.class);
        }
    }

    public static class MyJCompilerSupport extends JCompilerSupport {

        public MyJCompilerSupport(MultiDataObject.Entry entry, Class cookie) {
            super(entry, cookie);
        }

        public CompilerType defaultCompilerType() {
            return super.defaultCompilerType();
        }

    }


}

/*
* Log
*  8    Gandalf   1.7         1/17/00  Petr Jiricka    Debug outputs removed
*  7    Gandalf   1.6         1/15/00  Petr Jiricka    Ensuring correct compiler
*       implementation - hashCode and equals
*  6    Gandalf   1.5         1/14/00  Petr Jiricka    Compilation fixes
*  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  4    Gandalf   1.3         10/12/99 Petr Jiricka    defaultCompilerType() 
*       added
*  3    Gandalf   1.2         10/4/99  Petr Jiricka    
*  2    Gandalf   1.1         9/27/99  Petr Jiricka    
*  1    Gandalf   1.0         9/22/99  Petr Jiricka    
* $
*/
