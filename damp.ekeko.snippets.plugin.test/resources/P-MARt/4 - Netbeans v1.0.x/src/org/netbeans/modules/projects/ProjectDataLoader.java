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

package org.netbeans.modules.projects;

import java.io.IOException;

import org.openide.actions.*;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.*;

/** Data loader which recognizes project files.
* This class is final only for performance reasons,
* can be unfinaled if desired.
*
* @author Jaroslav Tulach
*/
public final class ProjectDataLoader extends DataLoader {

    static final long serialVersionUID = 2004780723589406680L;

    /** instance of this loader */
    static ProjectDataLoader INSTANCE;

    static Listener listener = null;

    /** Creates new ProjectDataLoader */
    public ProjectDataLoader() {
        super(ProjectDataObject.class);
        INSTANCE = this;

    }

    /** Does initialization. Initializes display name,
    * extension list and the actions. */
    protected void initialize () {
        setDisplayName(NbBundle.getBundle(ProjectDataLoader.class).
                       getString("PROP_ProjectLoader_Name"));
        setActions(new SystemAction[] {
                       SystemAction.get(OpenAction.class),
                       SystemAction.get(FileSystemAction.class),
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
                   });
        if (listener == null) {
            listener = new Listener();
            org.openide.TopManager.getDefault().getLoaderPool().addOperationListener(listener);
        }
    }

    /**
    */
    protected DataObject handleFindDataObject(FileObject fo, DataLoader.RecognizedFiles recognized)
    throws java.io.IOException {
        if (fo.isFolder()) {
            recognized.markRecognized(fo);
            return new ProjectDataObject(fo, this);
        }
        return null;
    }



    /** Listens on creation of new objects.
    */
    static class Listener extends OperationAdapter {

        public volatile boolean enabled = true;

        public void operationCreateFromTemplate(OperationEvent.Copy ev)  {
            if (enabled) {
                RequestProcessor.postRequest(new Performer(ev.getObject()));
            }
        }
    }

    static class Performer implements Runnable {

        DataObject created;

        public Performer(DataObject created) {
            this.created = created;
        }

        public void run() {
            Node p = org.openide.TopManager.getDefault ().getPlaces ().nodes ().projectDesktop ();
            ProjectDataObject pdo = (ProjectDataObject) p.getCookie (ProjectDataObject.class);
            if (pdo != null) {
                try {
                    if (!pdo.isAccessibleFromFolder(created) && MainClassHelper.canAddToProject(created.getName())) {
                        pdo.add(created);
                    }
                } catch (IOException ex) {
                    // problems
                    if (Boolean.getBoolean ("netbeans.debug.exceptions")) ex.printStackTrace (); // NOI18N
                }
            }
        }
    }
}

/*
 * Log
 *  7    Gandalf   1.6         2/4/00   Martin Ryzl     
 *  6    Gandalf   1.5         1/13/00  Martin Ryzl     heavy localization
 *  5    Gandalf   1.4         1/12/00  Martin Ryzl     
 *  4    Gandalf   1.3         1/9/00   Martin Ryzl     
 *  3    Gandalf   1.2         1/3/00   Martin Ryzl     
 *  2    Gandalf   1.1         12/28/99 Martin Ryzl     
 *  1    Gandalf   1.0         12/22/99 Martin Ryzl     
 * $
 */
