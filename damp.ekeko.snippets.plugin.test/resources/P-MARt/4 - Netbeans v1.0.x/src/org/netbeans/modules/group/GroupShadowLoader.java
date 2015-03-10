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

package org.netbeans.modules.group;

import org.openide.loaders.*;
import org.openide.util.actions.SystemAction;
import org.openide.filesystems.*;
import org.openide.util.NbBundle;

/** Loader for GroupShadow.
*
* @author Jaroslav Tulach
*/
public class GroupShadowLoader extends DataLoader {
    /** Default set of actions on the GroupShadow. */
    private static SystemAction[] defaultGSActions = new SystemAction[] {
                SystemAction.get (org.openide.actions.OpenLocalExplorerAction.class),
                SystemAction.get (org.openide.actions.FileSystemAction.class),
                null,
                SystemAction.get (org.openide.actions.CompileAction.class),
                SystemAction.get (org.openide.actions.CompileAllAction.class),
                null,
                SystemAction.get (org.openide.actions.BuildAction.class),
                SystemAction.get (org.openide.actions.BuildAllAction.class),
                null,
                SystemAction.get (org.openide.actions.CutAction.class),
                SystemAction.get (org.openide.actions.CopyAction.class),
                SystemAction.get (org.openide.actions.PasteAction.class),
                null,
                SystemAction.get (org.openide.actions.DeleteAction.class),
                SystemAction.get (org.openide.actions.RenameAction.class),
                null,
                SystemAction.get (org.openide.actions.SaveAsTemplateAction.class),
                null,
                SystemAction.get (org.openide.actions.ToolsAction.class),
                SystemAction.get (org.openide.actions.PropertiesAction.class)
            };

    private ExtensionList extensions;

    static final long serialVersionUID =-2768192459953761627L;
    /** Representation class is DataShadow */
    public GroupShadowLoader () {
        super (GroupShadow.class);
        setActions(defaultGSActions);
        setDisplayName(NbBundle.getBundle(GroupShadowLoader.class).getString("PROP_GroupShadowName"));
        extensions = new ExtensionList();
        extensions.addExtension(GroupShadow.GS_EXTENSION);
    }

    protected DataObject handleFindDataObject (
        FileObject fo, DataLoader.RecognizedFiles recognized
    ) throws java.io.IOException {
        if (getExtensions().isRegistered(fo)) {
            return new GroupShadow(fo, this);
        }
        return null;
    }

    public ExtensionList getExtensions() {
        return extensions;
    }

    public void setExtensions(ExtensionList extensions) {
        this.extensions = extensions;
    }
}

/*
* Log
*  4    Gandalf   1.3         11/27/99 Patrik Knakal   
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         8/17/99  Martin Ryzl     LoaderBeanInfo added, 
*       some bug corrected and some bananas around ..
*  1    Gandalf   1.0         7/29/99  Jaroslav Tulach 
* $
*/