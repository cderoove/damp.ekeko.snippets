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

import java.awt.Image;
import java.awt.Toolkit;
import java.io.IOException;
import java.beans.BeanInfo;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.loaders.DataFolder;
import org.openide.cookies.*;
import org.openide.nodes.*;
import org.openide.actions.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;
import org.openide.util.NbBundle;


/** Node representing all the projects in directory Project under the
* system file system.
*
* @author Jaroslav Tulach
*/
public final class ProjectsNode extends DataFolder.FolderNode {
    private final static String projectsBase =  "/org/netbeans/modules/projects/resources/projects"; // NOI18N

    public ProjectsNode () {
        TopManager.getDefault().getPlaces().folders().projects().super();

        setDisplayName(ProjectDataObject.getLocalizedString("CTL_Projects_name")); // NOI18N
        setShortDescription(ProjectDataObject.getLocalizedString("CTL_Projects_hint")); // NOI18N
        setIconBase (projectsBase);
    }

    /** Clones the node.
    */
    public Node cloneNode () {
        return new ProjectsNode ();
    }

    /** Default actions on this node */
    protected SystemAction[] createActions () {
        return new SystemAction[] {
                   SystemAction.get (NewAction.class),
                   SystemAction.get (ToolsAction.class),
                   null,
                   SystemAction.get (PropertiesAction.class),
               };
    }

    /** @return empty property sets. */
    public Sheet createSheet () {
        return new Sheet ();
    }

    /** @return cannot be removed */
    public boolean canDestroy () {
        return false;
    }

    /** New type for creation of new project.
    */
    public NewType[] getNewTypes () {
        return new NewType[] { new NewProject () };
    }

    /** New type for creating new project in projects folder.
    */
    private static final class NewProject extends NewType {
        public void create () throws IOException {
            NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine (
                                                   ProjectDataObject.getLocalizedString ("CTL_NewProjectName"), // NOI18N
                                                   ProjectDataObject.getLocalizedString ("CTL_NewProjectTitle") // NOI18N
                                               );

            if (TopManager.getDefault ().notify (input) == NotifyDescriptor.OK_OPTION) {
                String projectName = input.getInputText ();

                ProjectDataObject obj = ProjectDataObject.createProject (
                                            TopManager.getDefault ().getPlaces ().folders ().projects (),
                                            projectName
                                        );
                OpenCookie pc = (OpenCookie)obj.getCookie (ProjectCookie.class);
                if (pc != null) {
                    pc.open ();
                }
            }
        }

        public String getName () {
            return ProjectDataObject.getLocalizedString ("CTL_NewProject"); // NOI18N
        }
    }
}

/*
 * Log
 *  2    Gandalf   1.1         1/15/00  Ian Formanek    NOI18N
 *  1    Gandalf   1.0         1/14/00  Martin Ryzl     
 * $
 */
