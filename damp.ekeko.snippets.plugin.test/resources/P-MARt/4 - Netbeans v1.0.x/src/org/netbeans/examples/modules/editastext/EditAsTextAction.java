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

package org.netbeans.examples.modules.editastext;

import org.openide.cookies.*;
import org.openide.loaders.MultiDataObject;
import org.openide.nodes.Node;
import org.openide.text.EditorSupport;
import org.openide.util.HelpCtx;
import org.openide.util.actions.NodeAction;

public class EditAsTextAction extends NodeAction {

    static final long serialVersionUID =5979965311538971183L;
    public String getName () { return "Edit as Text"; }

    public HelpCtx getHelpCtx () { return HelpCtx.DEFAULT_HELP; }

    public boolean enable (Node[] activated) {
        System.err.println ("eat active chk");
        if (activated.length == 0) {
            System.err.println ("\tno nodes");
            return false;
        }
        for (int i = 0; i < activated.length; i++) {
            Node n = activated[i];
            System.err.println ("\t#" + i + ": " + n.getDisplayName ());
            if (n.getCookie (MultiDataObject.class) == null) {
                System.err.println ("\tnot mdo");
                return false;
            }
            if (n.getCookie (OpenCookie.class) != null ||
                    n.getCookie (ViewCookie.class) != null ||
                    n.getCookie (EditCookie.class) != null ||
                    n.getCookie (EditorCookie.class) != null)
                System.err.println ("\thas some cookie");
            return false;
        }
        System.err.println ("\tactive!");
        return true;
    }

    public void performAction (Node[] activated) {
        for (int i = 0; i < activated.length; i++) {
            MultiDataObject mdo = (MultiDataObject) activated[i].getCookie (MultiDataObject.class);
            if (mdo != null) {
                EditorSupport es = new EditorSupport (mdo.getPrimaryEntry ());
                es.setMIMEType ("text/plain");
                es.open ();
            }
        }
    }

}
