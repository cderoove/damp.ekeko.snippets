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

package org.netbeans.examples.modules.audioloader;
import java.io.IOException;
import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
public class AudioDataLoader extends UniFileLoader {
    private static final long serialVersionUID =-1642575100744223632L;
    public AudioDataLoader () {
        super (AudioDataObject.class);
    }
    protected void initialize () {
        setDisplayName (NbBundle.getBundle (AudioDataLoader.class).getString ("LBL_loaderName"));
        ExtensionList extensions = new ExtensionList ();
        extensions.addExtension ("au");
        extensions.addExtension ("aiff");
        extensions.addExtension ("wav");
        extensions.addExtension ("mid");
        setExtensions (extensions);
        setActions (new SystemAction[] {
                        SystemAction.get (ViewAction.class),
                        SystemAction.get (FileSystemAction.class),
                        null,
                        /*
                        SystemAction.get (ExecuteAction.class),
                        null,
                        */
                        SystemAction.get (CutAction.class),
                        SystemAction.get (CopyAction.class),
                        SystemAction.get (PasteAction.class),
                        null,
                        SystemAction.get (DeleteAction.class),
                        SystemAction.get (RenameAction.class),
                        null,
                        SystemAction.get (SaveAsTemplateAction.class),
                        null,
                        SystemAction.get (ToolsAction.class),
                        SystemAction.get (PropertiesAction.class),
                    });
    }
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new AudioDataObject (primaryFile, this);
    }
}
