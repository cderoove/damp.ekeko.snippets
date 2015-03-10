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
import java.io.IOException;
import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.*;
import org.openide.util.NbBundle;
import org.openide.util.actions.SystemAction;
public class ScoreDataLoader extends MultiFileLoader {
    public static final String MAIN_EXT = "score";
    public static final String SECONDARY_EXT = "au";
    private static final long serialVersionUID =6424491892249774122L;
    public ScoreDataLoader () {
        super (ScoreDataObject.class);
    }
    protected void initialize () {
        super.initialize ();
        setDisplayName (NbBundle.getBundle (ScoreDataLoader.class).getString ("LBL_loaderName"));
        setActions (new SystemAction[] {
                        SystemAction.get (OpenAction.class),
                        SystemAction.get (EditAction.class),
                        SystemAction.get (FileSystemAction.class),
                        null,
                        SystemAction.get (CompileAction.class),
                        null,
                        SystemAction.get (ExecuteAction.class),
                        null,
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
    protected FileObject findPrimaryFile (FileObject fo) {
        if (fo.hasExt (MAIN_EXT))
            return fo;
        else if (fo.hasExt (SECONDARY_EXT))
            return FileUtil.findBrother (fo, MAIN_EXT);
        else
            return null;
    }
    protected MultiDataObject createMultiObject (FileObject primaryFile)
    throws DataObjectExistsException, IOException {
        return new ScoreDataObject (primaryFile, this);
    }
    protected MultiDataObject.Entry createPrimaryEntry (MultiDataObject obj, FileObject primaryFile) {
        return new FileEntry (obj, primaryFile);
    }
    protected MultiDataObject.Entry createSecondaryEntry (MultiDataObject obj, FileObject secondaryFile) {
        secondaryFile.setImportant (false);
        return new FileEntry.Numb (obj, secondaryFile);
    }
}
