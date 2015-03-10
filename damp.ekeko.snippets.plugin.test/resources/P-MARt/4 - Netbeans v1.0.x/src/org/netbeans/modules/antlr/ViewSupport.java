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

import org.openide.cookies.*;
import org.openide.loaders.MultiDataObject;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.*;
import org.openide.loaders.*;
/**
 *
 * @author  jleppanen
 * @version 
 */
public class ViewSupport extends Object implements ViewCookie {
    MultiDataObject.Entry entry;

    /** Creates new CookieSupport for given entry */
    public ViewSupport(MultiDataObject.Entry entry) {
        this.entry = entry;
    }

    public void view() {
        System.out.print("ANTLR Module: Viewing ");
        FileObject fo = entry.getFile();
        System.out.println(fo);

        //java.io.File file = org.openide.execution.NbClassPath.toFile(fo);
        DataObject obj = entry.getDataObject();
        // Get the DataObject opened in editor with created editor
        EditorCookie ec=(EditorCookie)(obj.getCookie(EditorCookie.class));
        if (ec==null) { System.out.println("DataObject don't have EC"); return; }
        System.out.println("EditorKit: "+ec);
        ec.open();
    }
}