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

package org.netbeans.modules.antlr.nodes;

import org.openide.nodes.*;
import org.openide.loaders.DataNode;
import org.openide.loaders.MultiDataObject;
import org.openide.loaders.DataObject;
import org.openide.util.actions.*;
import org.openide.actions.*;
import org.openide.text.EditorSupport;

/**
 *
 * @author  jleppanen
 * @version 
 */
public class GHtmlNode extends AbstractNode {
    MultiDataObject.Entry entry;

    /** Icon base for the GNode node */
    private static final String GHTML_ICON_BASE =
        "org/netbeans/modules/antlr/gObject";

    /** Creates new GNode */
    public GHtmlNode(MultiDataObject.Entry entry) {
        super(Children.LEAF/*new Children.Array()*/);
        setIconBase(GHTML_ICON_BASE);
        setName("html: "+ entry.getFile().getName());
        this.entry = entry;

        CookieSet cookies = getCookieSet();
        // use editor support
        EditorSupport es = new EditorSupport(entry);
        es.setMIMEType ("text/html");
        cookies.add(es);
        // support compilation
        //cookies.add(new CompilerSupport.Compile(entry));
        //cookies.add(new CompilerSupport.Build(entry));
        //cookies.add(new ViewSupport(entry));
        setCookieSet(cookies);
    }

    public SystemAction[] getActions() {
        return new SystemAction[] {
                   SystemAction.get(OpenAction.class),
                   SystemAction.get(ViewAction.class),
                   SystemAction.get (FileSystemAction.class),
                   null,
                   SystemAction.get (CompileAction.class),
                   SystemAction.get (BuildAction.class),
                   null,
                   SystemAction.get(CutAction.class),
                   SystemAction.get(CopyAction.class),
                   SystemAction.get(PasteAction.class),
                   null,
                   SystemAction.get(DeleteAction.class),
                   SystemAction.get(RenameAction.class),
                   null,
                   SystemAction.get(SaveAsTemplateAction.class),
                   null,
                   SystemAction.get(ToolsAction.class),
                   SystemAction.get(PropertiesAction.class),
               };
    }

    public SystemAction getDefaultAction () {
        SystemAction result = super.getDefaultAction();
        return result == null ? SystemAction.get(ViewAction.class) : result;
    }
}