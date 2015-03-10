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

package org.openidex.search;

import org.openide.filesystems.*;
/**
 *
 * @author  pkuzel
 * @version 
 */
public class StructuredDetail extends Object {

    //matching text
    public String text;

    //where is located
    public FileObject fo;
    public int line = 0;
    public int column = 0;

    /** Creates new StructuredDetail */
    public StructuredDetail() {
    }

}