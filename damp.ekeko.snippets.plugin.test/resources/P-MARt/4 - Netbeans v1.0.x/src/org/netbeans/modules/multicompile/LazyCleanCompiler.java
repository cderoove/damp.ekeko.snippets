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

package org.netbeans.modules.multicompile;

import java.util.*;

import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.*;

/** Cleans up after an external compiler.
 *
 * @author jglick
 */
public class LazyCleanCompiler extends Compiler {

    private DataObject obj;
    private String inExt, outExt;

    public LazyCleanCompiler (DataObject obj, String inExt, String outExt) {
        this.obj = obj;
        this.inExt = inExt;
        this.outExt = outExt;
    }

    public Class compilerGroupClass () {
        return LazyCleanCompilerGroup.class;
    }

    public boolean equals (Object o) {
        if (! (o instanceof LazyCleanCompiler)) return false;
        LazyCleanCompiler lcc = (LazyCleanCompiler) o;
        return obj.equals (lcc.obj) && inExt.equals (lcc.inExt) && outExt.equals (lcc.outExt);
    }

    public int hashCode () {
        return obj.hashCode () ^ inExt.hashCode () ^ outExt.hashCode ();
    }

    public boolean isUpToDate () {
        //System.err.println("LazyCleanCompiler.isUpToDate; obj=" + obj.getName () + " inExt=" + inExt + " outExt=" + outExt);
        Iterator it = obj.files ().iterator ();
        // [PENDING] should files also be tested for presence of inExt??
        while (it.hasNext ()) {
            if (((FileObject) it.next ()).hasExt (outExt)) {
                //System.err.println("\tnot up to date");
                return false;
            }
        }
        //System.err.println("\tup to date");
        return true;
    }

    // For use by the compiler group:
    public DataObject getObject () {
        return obj;
    }

    public String getInExt () {
        return inExt;
    }

    public String getOutExt () {
        return outExt;
    }

}
