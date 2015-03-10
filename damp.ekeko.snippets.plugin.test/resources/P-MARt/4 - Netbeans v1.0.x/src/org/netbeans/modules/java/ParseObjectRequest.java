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

import java.io.InputStream;

import org.netbeans.modules.java.JavaDataObject;

/**
*
* @author Petr Hamernik
*/
public interface ParseObjectRequest {
    public JavaDataObject getDataObject();

    /** Optionally replaces the input stream with some preprocessing.
    */
    public InputStream modifyInputStream(InputStream is);
    
    /** Sets the number of syntax errors that occured during parsing.
    */
    public void setSyntaxErrors(int errors);
    
    /** Returns the number of syntax errors.
    */
    public int  getSyntaxErrors();
    
    /** Sets number of semantic errors */
    public void setSemanticErrors(int errors);
    
    /** Returns Element implementation creation factory.
    */
    public ElementFactory getFactory();
}

/*
 * <<Log>>
 *  3    Gandalf-post-FCS1.0.1.1     3/9/00   Svatopluk Dedic Line number positions 
 *       are stored here now
 *  2    Gandalf-post-FCS1.0.1.0     3/6/00   Svatopluk Dedic Removed old 
 *       TokenListener related code
 *  1    Gandalf   1.0         12/22/99 Petr Hamernik   
 * $
 */
