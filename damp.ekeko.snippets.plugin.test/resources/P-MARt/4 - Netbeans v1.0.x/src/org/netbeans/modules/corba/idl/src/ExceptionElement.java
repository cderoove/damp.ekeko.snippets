/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2001 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.corba.idl.src;

public class ExceptionElement extends IDLElement {
    static final long serialVersionUID =-8166176459752773578L;
    public ExceptionElement(int id) {
        super(id);
    }

    public ExceptionElement(IDLParser p, int id) {
        super(p, id);
    }

    public void jjtClose () {
        super.jjtClose ();
        setName (((Identifier)getMember (0)).getName ());
    }
}
