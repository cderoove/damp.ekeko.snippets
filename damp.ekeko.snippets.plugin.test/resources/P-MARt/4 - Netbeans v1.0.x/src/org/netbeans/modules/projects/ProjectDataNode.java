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

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.openide.TopManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.actions.*;
import org.openide.cookies.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.*;

import org.openidex.projects.*;

/** Node displaying content of a project desktop.
*
* @author Jaroslav Tulach
*/
public class ProjectDataNode extends DataNode {
    private final static String PROJECT_DESKTOP =  "/org/netbeans/modules/projects/resources/projectDesktop"; // NOI18N

    /** Project to work with */
    private ProjectDataObject project;

    /** indicates whether this node is used as representant of a data object or
    * as a root of project desktop
    */
    private boolean projectDesktop;

    /** Initiliazes the node for given project.
    */
    public ProjectDataNode(ProjectDataObject project) {
        this (project, Children.LEAF);
    }

    /** Initiliazes the node for given project.
    */
    public ProjectDataNode(ProjectDataObject project, Children children) {
        this (project, children, false);
    }

    /** Initiliazes the node for given project.
    * @param project the project object to attach to
    * @param projectDesktop is this node representing DO or is it desktop node
    */
    public ProjectDataNode(ProjectDataObject project, Children children, boolean projectDesktop) {
        super(project, children);

        this.project = project;
        this.projectDesktop = projectDesktop;

        displayFormat = new java.text.MessageFormat (
                            ProjectDataObject.getLocalizedString("CTL_ProjectDesktop_name") // NOI18N
                        );

        setName(getName());

        setShortDescription(ProjectDataObject.getLocalizedString("CTL_ProjectDesktop_hint")); // NOI18N
        setIconBase (PROJECT_DESKTOP);
    }
    
    public boolean canDestroy() {
        if (projectDesktop) return false;
        else return super.canDestroy();
    }

    protected SystemAction[] createActions () {
        if (projectDesktop) {
            return new SystemAction[] {
                       SystemAction.get(AddNewAction.class),
                       SystemAction.get(Add2ProjectAction.class),
                       null,
                       SystemAction.get(SaveAction.class),
                       null,
                       SystemAction.get(CompileAction.class),
                       SystemAction.get(BuildAction.class),
                       null,
                       SystemAction.get(ExecuteAction.class),
                       //        null,
                       //        SystemAction.get(NewAction.class),
                       null,
                       SystemAction.get(PasteAction.class),
                       null,
                       SystemAction.get(RenameAction.class),
                       null,
                       SystemAction.get(ToolsAction.class),
                       SystemAction.get(PropertiesAction.class),
                   };
        } else {
            return new SystemAction[] {
                       SystemAction.get(OpenAction.class),
                       null,
                       SystemAction.get(NewAction.class),
                       null,
                       SystemAction.get(ToolsAction.class),
                       SystemAction.get(PropertiesAction.class),
                   };
        }
    }

    /** Return a handle for serialization.
    */
    public Node.Handle getHandle() {
        if (projectDesktop) {
            return new DesktopHandle();
        }
        return super.getHandle();
    }

    /** Augments the default behaviour to test for {@link NodeTransfer#nodeCutFlavor} and
     * {@link NodeTransfer#nodeCopyFlavor}
     * with the {@link DataObject}. If there is such a flavor then adds
     * the cut and copy flavors. Also, if there is a copy flavor and the
     * data object is a template, adds an instantiate flavor.
     *
     * @param t transferable to use
     * @param s list of {@link PasteType}s
     */
    protected void createPasteTypes (Transferable t, java.util.List s) {
        super.createPasteTypes (t, s);

        DataObject obj = null;

        // try copy flavor
        obj = (DataObject)NodeTransfer.cookie (t, NodeTransfer.CLIPBOARD_COPY , DataObject.class );

        if (obj != null) {
            if (obj.isCopyAllowed ()) {

                // copy and cut
                s.add (new Paste (obj));
            }
        }
    }

    /** Creates sheet.
    */
    protected Sheet createSheet () {
        Sheet s = Sheet.createDefault ();

        //    try {
        Sheet.Set ss = s.get (Sheet.PROPERTIES);
        /*
        PropertySupport.Reflection r = new PropertySupport.Reflection (
          project, String.class, "mainClass"
        );
        r.setName ("mainClass");
        ss.put (r);
        */

        ss.put (new SaveProperty (
                    "controlPanel", // NOI18N
                    ProjectSupport.SAVE_CONTROL_PANEL,
                    getLocalizedString("PROP_SaveControlPanel"), // NOI18N
                    getLocalizedString("HINT_SaveControlPanel") // NOI18N
                ));

        ss.put (new SaveProperty (
                    "loaders",  // NOI18N
                    ProjectSupport.SAVE_LOADERS,
                    getLocalizedString("PROP_SaveLoaders"), // NOI18N
                    getLocalizedString("HINT_SaveLoaders") // NOI18N
                ));
        ss.put (new SaveProperty (
                    "repository",  // NOI18N
                    ProjectSupport.SAVE_REPOSITORY,
                    getLocalizedString("PROP_SaveRepository"), // NOI18N
                    getLocalizedString("HINT_SaveRepository") // NOI18N
                ));
        ss.put (new SaveProperty (
                    "services",  // NOI18N
                    ProjectSupport.SAVE_SERVICES,
                    getLocalizedString("PROP_SaveServices"), // NOI18N
                    getLocalizedString("HINT_SaveServices") // NOI18N
                ));
        ss.put (new SaveProperty (
                    "windowManager",  // NOI18N
                    ProjectSupport.SAVE_WINDOW_MANAGER,
                    getLocalizedString("PROP_SaveWindowManager"), // NOI18N
                    getLocalizedString("HINT_SaveWindowManager") // NOI18N
                ));
        //    } catch (NoSuchMethodException ex) {
        //    }

        return s;
    }

    /** Get localized String.
    */
    private String getLocalizedString(String key) {
        return NbBundle.getBundle(ProjectDataNode.class).getString(key);
    }

    /** Property for setting what to save and what not.
    */
    private final class SaveProperty extends PropertySupport.ReadWrite {
        private int mask;

        public SaveProperty (String name, int mask, String displayName, String hint) {
            super (name, Boolean.TYPE, displayName, hint);

            this.mask = mask;
        }

        public Object getValue () {
            return new Boolean ((project.support.getSave () & mask) != 0);
        }

        public void setValue (Object o) throws InvocationTargetException {
            try {
                if (Boolean.FALSE.equals (o)) {
                    project.support.setSave (project.support.getSave () & ~mask);
                } else {
                    project.support.setSave (project.support.getSave () | mask);
                }
            } catch (java.io.IOException ex) {
                throw new InvocationTargetException (ex);
            }
        }
    }

    /** New type for creation of new folder.
    */

    private final class NewFolder extends NewType {
        /** Display name for the creation action. This should be
        * presented as an item in a menu.
        *
        * @return the name of the action
        */
        public String getName() {
            return ProjectDataObject.getLocalizedString ("CTL_NewFolder"); // NOI18N
        }

        /** Help context for the creation action.
        * @return the help context
        */
        public HelpCtx getHelpCtx() {
            return new HelpCtx (NewFolder.class);
        }

        /** Create the object.
        * @exception IOException if something fails
        */
        public void create () throws java.io.IOException {
            NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine (
                                                   ProjectDataObject.getLocalizedString ("CTL_NewFolderName"), // NOI18N
                                                   ProjectDataObject.getLocalizedString ("CTL_NewFolderTitle") // NOI18N
                                               );

            input.setInputText (ProjectDataObject.getLocalizedString ("CTL_NewFolderValue")); // NOI18N
            if (TopManager.getDefault ().notify (input) == NotifyDescriptor.OK_OPTION) {
                String folderName = input.getInputText ();
                if ("".equals (folderName)) return; // empty name = cancel // NOI18N

                // ProjectDataObject.create (project.getProjectFolder (), folderName);
            }
        }
    }

    private static class DesktopHandle implements Node.Handle {

        static final long serialVersionUID = -8515898905571314270L;

        public Node getNode() throws java.io.IOException {
            return TopManager.getDefault().getPlaces().nodes().projectDesktop();
        }
    }

    /** PasteType for projects. */
    private class Paste extends PasteType {

        private DataObject dobj;

        public Paste(DataObject dobj) {
            this.dobj = dobj;
        }

        public final Transferable paste () throws IOException {
            ProjectDataObject pdo = (ProjectDataObject) getCookie(ProjectDataObject.class);
            if (pdo != null) {
                pdo.add(dobj);
            }
            // clear clipboard or preserve content
            return null;
        }
    }
}

/*
 * Log
 *  9    Gandalf   1.8         2/4/00   Martin Ryzl     serial version uid added
 *       to handle
 *  8    Gandalf   1.7         1/17/00  Martin Ryzl     
 *  7    Gandalf   1.6         1/15/00  Ian Formanek    NOI18N
 *  6    Gandalf   1.5         1/13/00  Martin Ryzl     
 *  5    Gandalf   1.4         1/13/00  Martin Ryzl     heavy localization
 *  4    Gandalf   1.3         1/10/00  Martin Ryzl     
 *  3    Gandalf   1.2         1/9/00   Martin Ryzl     
 *  2    Gandalf   1.1         1/3/00   Martin Ryzl     
 *  1    Gandalf   1.0         12/22/99 Martin Ryzl     
 * $
 */
