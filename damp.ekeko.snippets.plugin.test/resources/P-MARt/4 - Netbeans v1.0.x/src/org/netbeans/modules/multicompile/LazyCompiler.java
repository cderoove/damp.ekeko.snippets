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

import java.io.*;
import java.util.*;

import org.openide.compiler.*;
import org.openide.compiler.Compiler;
import org.openide.execution.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.loaders.DataObject;

public class LazyCompiler extends ExternalCompiler {

    private boolean building;
    private DataObject obj;
    private String inExt, outExt;

    public LazyCompiler (DataObject obj, boolean building, NbProcessDescriptor compiler, ExternalCompiler.ErrorExpression errExpr, String inExt, String outExt) throws FileStateInvalidException {
        super (obj.getPrimaryFile ().getFileSystem (), obj.getPrimaryFile ().getPackageName ('/') + "." + inExt, building ? BUILD : COMPILE, compiler, errExpr);
        this.obj = obj;
        this.building = building;
        this.inExt = inExt;
        this.outExt = outExt;
    }

    private static String packageOf (FileObject fo) {
        if (fo.isData ()) fo = fo.getParent ();
        return fo.getPackageName ('.');
    }
    public boolean isUpToDate () {
        //System.err.println("LazyCompiler.isUpToDate; obj=" + obj.getName () + " inExt=" + inExt + " outExt=" + outExt);
        // Always recompile when building.
        if (building) {
            //System.err.println("\tout of date because building");
            return false;
        }
        String basename = obj.getName ();
        String pkg = packageOf (obj.getPrimaryFile ());
        FileObject in = null;
        FileObject out = null;
        Set files = obj.files ();
        Iterator it = files.iterator ();
        while (it.hasNext ()) {
            FileObject fo = (FileObject) it.next ();
            // Only look for files with the same basename (and package):
            if (! basename.equals (fo.getName ()) || ! pkg.equals (packageOf (fo))) continue;
            String ext = fo.getExt ();
            if (inExt.equals (ext))
                in = fo;
            else if (outExt.equals (ext))
                out = fo;
        }
        // Really, it is something else which is not up to date, but pretend:
        if (in == null) {
            //System.err.println("\tout of date because no in");
            return false;
        }
        // No output, definitely out of date:
        if (out == null) {
            //System.err.println("\tout of date because no out");
            return false;
        }
        // Check timestamps:
        int comparison = out.lastModified ().compareTo (in.lastModified ());
        //System.err.println("\tcomparison=" + comparison);
        return comparison > 0;
    }

    // Just specify the kind of compiler group to use. Its default constructor will be called.
    public Class compilerGroupClass () {
        return ExternalCompilerGroup.class;
    }

    // Make sure these compilers are split into separate groups if they do not
    // agree on the value of myOpt.
    public Object compilerGroupKey () {
        List l = new ArrayList (2);
        l.add (super.compilerGroupKey ());
        l.add (LazyCompiler.class);
        return l;
    }

    public boolean equals (Object o) {
        if (! super.equals (o) || ! (o instanceof LazyCompiler)) return false;
        return outExt.equals (((LazyCompiler) o).outExt);
    }

    public int hashCode () {
        return super.hashCode () ^ outExt.hashCode ();
    }

}
