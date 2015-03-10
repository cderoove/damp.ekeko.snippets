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

import org.openide.actions.*;
import org.openide.cookies.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.text.EditorSupport;
import org.openide.util.HelpCtx;

/** Represents a Generic object in the Repository.
 *
 * @author  jglick
 */
public class GenericDataObject extends MultiDataObject {

    private static final long serialVersionUID =5304808578895851435L;
    public GenericDataObject(FileObject pf,GenericDataLoader loader) throws DataObjectExistsException {
        super (pf, loader);
        init ();
    }

    private void init () {
        CookieSet cookies = getCookieSet ();
        cookies.add (new ExecSupport (getPrimaryEntry ()));
        String mimeType = ((GenericDataLoader) getLoader ()).getMimeType ();
        if (mimeType != null) {
            EditorSupport ed = new EditorSupport (getPrimaryEntry ());
            ed.setMIMEType (mimeType);
            cookies.add (ed);
        }
        cookies.add (new CompilerSupport.Compile (getPrimaryEntry ()));
        cookies.add (new CompilerSupport.Build (getPrimaryEntry ()));
        cookies.add (new CompilerSupport.Clean (getPrimaryEntry ()));
    }

    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
        // If you add context help, change to:
        // return new HelpCtx (GenericDataObject.class);
    }

    protected Node createNodeDelegate () {
        return new GenericDataNode (this);
    }

}