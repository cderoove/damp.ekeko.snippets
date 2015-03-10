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


import org.openide.loaders.DataObject;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.cookies.CompilerCookie;
import org.openide.util.NbBundle;

/** Support for compilation of a servlet generated from a JSP file.
* Does not compile this servlet,
* rather, compiles the original JSP page (if it is found).
*
* @author Petr Jiricka
*/
public class JspServletCompilerSupport extends Object
    implements CompilerCookie {

    /** source JSP page be associated with */
    protected DataObject sourceJspPage;
    protected Class cookieClass;

    /** Create new support for given entry. The file is taken from the
    * entry and is updated if the entry moves or renames itself.
    * @param entry entry to create instance from
    */
    public JspServletCompilerSupport(DataObject sourceJspPage, Class cookieClass) {
        if (!(CompilerCookie.class.isAssignableFrom(cookieClass)))
            throw new IllegalArgumentException();
        this.sourceJspPage = sourceJspPage;
        this.cookieClass = cookieClass;
    }

    public void addToJob(CompilerJob job, Compiler.Depth depth) {
        CompilerCookie cc = (CompilerCookie)sourceJspPage.getCookie(cookieClass);
        if (cc != null) {
            cc.addToJob(job, depth);
        }
        else {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                new Exception(org.openide.util.NbBundle.getBundle(JspServletCompilerSupport.class).
                              getString("CTL_NotCompilable")).printStackTrace();
        }
    }

    public boolean isDepthSupported(Compiler.Depth depth) {
        CompilerCookie cc = (CompilerCookie)sourceJspPage.getCookie(cookieClass);
        if (cc != null) {
            return cc.isDepthSupported(depth);
        }
        else {
            if (Boolean.getBoolean("netbeans.debug.exceptions")) // NOI18N
                new Exception(org.openide.util.NbBundle.getBundle(JspServletCompilerSupport.class).
                              getString("CTL_NotCompilable")).printStackTrace();
            return false;
        }
    }

    /** Inner class for Compile cookie */
    public static class Compile extends JspServletCompilerSupport implements CompilerCookie.Compile {

        public Compile(DataObject sourceJspPage) {
            super(sourceJspPage, CompilerCookie.Compile.class);
        }

    }

    /** Inner class for Build cookie */
    public static class Build extends JspServletCompilerSupport implements CompilerCookie.Build {

        public Build(DataObject sourceJspPage) {
            super(sourceJspPage, CompilerCookie.Build.class);
        }

    }

    /** Inner class for Clean cookie */
    public static class Clean extends JspServletCompilerSupport implements CompilerCookie.Clean {

        public Clean(DataObject sourceJspPage) {
            super(sourceJspPage, CompilerCookie.Clean.class);
        }

    }

}

/*
* Log
*  3    Gandalf   1.2         1/12/00  Petr Jiricka    Fully I18n-ed
*  2    Gandalf   1.1         1/12/00  Petr Jiricka    i18n phase 1
*  1    Gandalf   1.0         12/29/99 Petr Jiricka    
* $
*/
