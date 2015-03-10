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

package org.netbeans.modules.antlr;

import java.util.*;
import org.openide.compiler.*;
import org.openide.compiler.Compiler;

/**
 *
 * @author  jleppanen
 * @version 
 */
public class GCompilerGroup extends CompilerGroup {
    /**
     * @associates Compiler 
     */
    List compilers;

    /** Creates new GCompilerGroup */
    public GCompilerGroup() {
        System.out.println("GCompilerGroup created");
        compilers = new Vector();
    }

    public void add(Compiler c) throws IllegalArgumentException {
        System.out.println("GCompilerGroup::add");
        compilers.add(c);
    }
    public boolean start() {
        System.out.println("GCompilerGroup::start");
        Iterator e = compilers.iterator();
        while (e.hasNext()) {
            GCompiler c = (GCompiler)e.next();
            c.compile();
        }
        return true;
    }
}