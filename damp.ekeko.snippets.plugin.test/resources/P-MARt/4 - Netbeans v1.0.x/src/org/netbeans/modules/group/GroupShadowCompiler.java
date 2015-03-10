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

package org.netbeans.modules.group;

import org.openide.loaders.DataObject;
import org.openide.compiler.Compiler;
import org.openide.compiler.CompilerJob;
import org.openide.cookies.CompilerCookie;

/** This class is implementation of compiler for GroupShadow. It means that
* folders implements CompilerCookie and returns this compiler. This compiler
* scans the folder and compile files (and folders) which implements
* CompilationCookie too.
*
* @author Jaroslav Tulach, Martin Ryzl
*/
class GroupShadowCompiler extends Object
    implements CompilerCookie.Compile, CompilerCookie.Build, CompilerCookie.Clean {
    /** Folder which is compiled by this compiler */
    private GroupShadow gs;
    /** which cookie the compiler needs */
    private Class cookieClass;

    /* Creates new compiler for the given folder. */
    public GroupShadowCompiler (GroupShadow gs, Class cookieClass) {
        this.gs = gs;
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

        Object[] objs = gs.getLinks();

        // add to the job children
        depth = depth.nextDepth ();

        for(int i = 0; i < objs.length; i++) {
            // anti-loop check - need improvement
            if (gs == objs[i]) continue;

            if (objs[i] instanceof DataObject) {
                DataObject obj = (DataObject) objs[i];
                CompilerCookie c = (CompilerCookie)obj.getCookie (cookieClass);
                if (c != null) {
                    c.addToJob (job, depth);
                }
            }
        }
    }
}

/*
 * Log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         7/29/99  Jaroslav Tulach 
 * $
 * Beta Change History:
 *  0    Tuborg    0.14        --/--/98 Jan Formanek    reflecting changes in cookies
 */
