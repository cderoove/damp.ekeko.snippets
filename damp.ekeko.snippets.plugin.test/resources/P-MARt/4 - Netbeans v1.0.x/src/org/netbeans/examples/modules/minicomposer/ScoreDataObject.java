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

package org.netbeans.examples.modules.minicomposer;
import org.openide.actions.*;
import org.openide.cookies.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.text.EditorSupport;
import org.openide.util.HelpCtx;
public class ScoreDataObject extends MultiDataObject {
    private static final long serialVersionUID =5776214949118746290L;
    public ScoreDataObject (FileObject pf, ScoreDataLoader loader) throws DataObjectExistsException {
        super (pf, loader);
        CookieSet cookies = getCookieSet ();
        EditorCookie ed = new ScoreEditorSupport (getPrimaryEntry ());
        cookies.add (ed);
        cookies.add (new ScoreSupport (getPrimaryEntry (), ed));
        cookies.add (new ScoreOpenSupport (getPrimaryEntry ()));
        cookies.add (new ScoreCompilerSupport.Compile (this));
        cookies.add (new ScoreCompilerSupport.Build (this));
        cookies.add (new ScoreCompilerSupport.Clean (this));
        cookies.add (new ScoreExecSupport (getPrimaryEntry ()));
    }
    public Node.Cookie getCookie (Class clazz) {
        // Prevent the EditorSupport for being used for the OpenCookie:
        if (clazz.isAssignableFrom (ScoreOpenSupport.class))
            return super.getCookie (ScoreOpenSupport.class);
        else
            return super.getCookie (clazz);
    }
    public HelpCtx getHelpCtx () {
        return new HelpCtx ("org.netbeans.examples.modules.minicomposer");
    }
    protected Node createNodeDelegate () {
        return new ScoreDataNode (this);
    }
}
