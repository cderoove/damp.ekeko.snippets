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

package org.netbeans.core;

import java.awt.Image;
import java.beans.BeanInfo;
import java.io.*;

import org.openide.*;
import org.openide.actions.*;
import org.openide.cookies.SaveCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.modules.*;
import org.openide.options.*;
import org.openide.nodes.*;
import org.openide.util.actions.*;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;

/** This node represents the root of the hierarchy of the explored
* objects in IDE.
* This class is final only for performance reasons.
* Can be unfinaled if desired.
*
* @author Petr Hamernik, Dafe Simonek
*/
public final class DesktopNode extends AbstractNode implements SaveCookie {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 4457929339850358728L;

    private static java.util.ResourceBundle bundle = NbBundle.getBundle (DesktopNode.class);

    /** Default icons for nodes */
    private static final String DESKTOP_ICON_BASE="/org/netbeans/core/resources/desktop"; // NOI18N
    private static final String PROJECT_SETTINGS_ICON_BASE="/org/netbeans/core/resources/controlPanel"; // NOI18N


    private final static String templatesIconURL=  "/org/netbeans/core/resources/templates.gif"; // NOI18N
    private final static String templatesIcon32URL="/org/netbeans/core/resources/templates32.gif"; // NOI18N
    private final static String startupIconURL=    "/org/netbeans/core/resources/startup.gif"; // NOI18N
    private final static String startupIcon32URL=  "/org/netbeans/core/resources/startup32.gif"; // NOI18N
    private final static String objectTypesIconURL=    "/org/netbeans/core/resources/objectTypes.gif"; // NOI18N
    private final static String objectTypesIcon32URL=  "/org/netbeans/core/resources/objectTypes32.gif"; // NOI18N

    /** empty array of property sets */
    private static final PropertySet[] NO_PROPERTY_SETS = {};

    /** reference to DataSystem - FileSystems */
    private DataSystem dataSystem;

    /** Constructor */
    public DesktopNode () {
        super (new Children.Array());
        initialize();
        getCookieSet ().add (this);
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (DesktopNode.class);
    }

    public void save () throws java.io.IOException {
        NbProjectOperation.saveBasicProject ();
    }

    /** Handle.
    */
    public Handle getHandle () {
        return new DesktopHandle (getName ());
    }

    /** Does all of the initialization */
    private void initialize () {
        setName(bundle.getString("CTL_Desktop_name"));
        setShortDescription(bundle.getString("CTL_Desktop_hint"));
        setIconBase(DESKTOP_ICON_BASE);

        RequestProcessor.postRequest (new Runnable () {
                                          public void run () {
                                              initializeChildren ();
                                          }
                                      }, 5000, Thread.MIN_PRIORITY);
    }

    /** Initializes all children (subnodes) of this node */
    private void initializeChildren () {
        //
        //
        //
        // Please!
        // Please!
        // Please!
        // Please!
        // Please!
        //
        //
        //
        //
        // Do not use anything special that is not accessible
        // from Open API during initialization of nodes.
        //
        // I know that this object is in impl package, but it represents the
        // default project and the specialized projects will be outside the
        // package and that is why they will have no access to classes in
        // this package.
        //
        // That is why, use only Open API.
        // Jst.
        //
        //
        //
        // Thanx!
        // Thanx!
        // Thanx!
        // Thanx!
        // Thanx!
        // Thanx!
        //
        //

        Children desktopChildren = getChildren();
        Places.Nodes ns = TopManager.getDefault ().getPlaces ().nodes ();
        Places.Folders fs = TopManager.getDefault ().getPlaces ().folders ();

        Node repositoryNode = ns.repository ().cloneNode ();
        Node sessionNode = createSessionNode ();
        Node environmentNode = createEnvironmentNode ();

        /* + Project Settings
             + Repository Settings
             + Control Panel
             + Workspaces
        */
        Node projectSettingsNode = createProjectSettingsNode ();

        /* + Desktop
             + Repository
             + Environment
             + Project Settings
             + Session Settings
        */
        desktopChildren.add (new Node[] {
                                 repositoryNode,
                                 environmentNode,
                                 projectSettingsNode,
                                 sessionNode,
                             }
                            );
    }

    /** @return possible actions on this dataobject */
    public SystemAction[] createActions() {
        return new SystemAction[] {
                   SystemAction.get(ToolsAction.class),
                   SystemAction.get(PropertiesAction.class)
               };
    }

    /** Getter for environment node.
    * @return environment node
    */
    static Node createEnvironmentNode () {
        Places.Nodes ns = TopManager.getDefault ().getPlaces ().nodes ();
        Node environmentNode = ns.environment ().cloneNode ();
        environmentNode.setShortDescription (bundle.getString ("CTL_Environment_Hint"));
        return environmentNode;
    }

    /** Getter for session settings node.
    * @return session settings node
    */
    static Node createSessionNode () {
        Places.Nodes ns = TopManager.getDefault ().getPlaces ().nodes ();
        Node sessionNode = ns.session ().cloneNode ();
        sessionNode.setShortDescription (bundle.getString ("CTL_Session_Settings_Hint"));
        return sessionNode;
    }


    static Node getProjectSettingsNode () {
        return ControlPanelNode.getProjectSettings ();
    }

    /** Getter for environment node.
    * @return environment node
    */
    static Node createProjectSettingsNode () {
        return getProjectSettingsNode ().cloneNode ();
    }

    private static class IconSubstituteNode extends FilterNode {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -2098259549820241091L;

        private static SystemAction[] staticActions;

        /** icons for the IconSubstituteNode */
        private String iconURL, icon32URL;
        transient private Image substIcon, substIcon32;

        IconSubstituteNode (Node ref, String iconURL, String icon32URL) {
            super (ref);
            this.iconURL = iconURL;
            this.icon32URL = icon32URL;
        }

        public Image getIcon (int type) {
            if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
                if (substIcon == null)
                    substIcon = java.awt.Toolkit.getDefaultToolkit ().getImage (
                                    getClass ().getResource (iconURL));
                return substIcon;
            }
            else {
                if (substIcon32 == null)
                    substIcon32 = java.awt.Toolkit.getDefaultToolkit ().getImage (
                                      getClass ().getResource (icon32URL));
                return substIcon32;
            }
        }

        public Image getOpenedIcon (int type) {
            return getIcon(type);
        }

        public SystemAction[] getActions () {
            if (staticActions == null) {
                staticActions = new SystemAction[] {
                                    SystemAction.get(ToolsAction.class),
                                    SystemAction.get(PropertiesAction.class)
                                };
            }
            return staticActions;
        }

        /** @return empty property sets. */
        public PropertySet[] getPropertySets () {
            return NO_PROPERTY_SETS;
        }

        public boolean canRemove() {
            return false;
        }
    }

    /** Getter for folders.
    */
    private static Places.Folders fs () {
        return TopManager.getDefault ().getPlaces ().folders ();
    }

    /** Node representing templates folder */
    public static class TemplatesNode extends IconSubstituteNode {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -8202001968004798680L;

        private static SystemAction[] staticActions;

        public TemplatesNode () {
            this (fs ().templates ().getNodeDelegate ());
        }

        public TemplatesNode(Node ref) {
            super(ref, templatesIconURL, templatesIcon32URL);
            super.setDisplayName(bundle.getString("CTL_Templates_name"));
            super.setShortDescription(bundle.getString("CTL_Templates_hint"));
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (TemplatesNode.class);
        }

        public SystemAction[] getActions () {
            if (staticActions == null) {
                staticActions = new SystemAction[] {
                                    SystemAction.get(NewAction.class),
                                    null,
                                    SystemAction.get(ToolsAction.class),
                                    SystemAction.get(PropertiesAction.class)
                                };
            }
            return staticActions;
        }
    }

    /** Node representing startup folder */
    public static class StartupNode extends IconSubstituteNode {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -8202001968004798680L;

        private static SystemAction[] staticActions;

        public StartupNode() {
            super (fs ().startup ().getNodeDelegate (), startupIconURL, startupIcon32URL);
            super.setDisplayName(bundle.getString("CTL_Startup_name"));
            super.setShortDescription(bundle.getString("CTL_Startup_hint"));
        }

        public HelpCtx getHelpCtx () {
            return new HelpCtx (StartupNode.class);
        }

        public SystemAction[] getActions () {
            if (staticActions == null) {
                staticActions = new SystemAction[] {
                                    SystemAction.get(PasteAction.class),
                                    null,
                                    SystemAction.get(ReorderAction.class),
                                    null,
                                    SystemAction.get(ToolsAction.class),
                                    SystemAction.get(PropertiesAction.class)
                                };
            }
            return staticActions;
        }
    }


    /** Node representing object types folder */
    public static class ObjectTypesNode extends IconSubstituteNode {
        /** generated Serialized Version UID */
        static final long serialVersionUID = -8202001968004798680L;

        private static SystemAction[] staticActions;

        public ObjectTypesNode() {
            this (TopManager.getDefault ().getPlaces ().nodes ().loaderPool ());
        }

        public ObjectTypesNode(Node ref) {
            super(ref, objectTypesIconURL, objectTypesIcon32URL);
        }

        public SystemAction[] getActions () {
            if (staticActions == null) {
                staticActions = new SystemAction[] {
                                    SystemAction.get(CustomizeBeanAction.class),
                                    null,
                                    SystemAction.get(ReorderAction.class),
                                    null,
                                    SystemAction.get(ToolsAction.class),
                                    SystemAction.get(PropertiesAction.class)
                                };
            }
            return staticActions;
        }
    }

    /** Handle for this node.
    */
    private static class DesktopHandle extends Object implements Serializable, Handle {
        /** serial version */
        static final long serialVersionUID = 439085320803214565L;

        private String name;

        public DesktopHandle (String name) {
            this.name = name;
        }

        /** Cretes the node
        */
        public Node getNode () {
            DesktopNode n = new DesktopNode ();
            n.setName (name);
            return n;
        }

    }

}

/*
 * Log
 *  42   Gandalf   1.41        1/13/00  Jaroslav Tulach I18N
 *  41   Gandalf   1.40        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  40   Gandalf   1.39        9/30/99  Jaroslav Tulach DataLoader is now 
 *       serializable.
 *  39   Gandalf   1.38        8/3/99   Jaroslav Tulach Project settings node.
 *  38   Gandalf   1.37        8/3/99   Jaroslav Tulach Serialization of 
 *       NbMainExplorer improved again.
 *  37   Gandalf   1.36        8/1/99   Ian Formanek    Fixed to compile
 *  36   Gandalf   1.35        8/1/99   Jaroslav Tulach MainExplorer now listens
 *       to changes in root elements.
 *  35   Gandalf   1.34        7/30/99  David Simonek   again serialization of 
 *       nodes repaired
 *  34   Gandalf   1.33        7/30/99  David Simonek   serialization fixes
 *  33   Gandalf   1.32        7/21/99  Ian Formanek    Fixedc problem with 
 *       NullPointerException from getPathTo in explorer
 *  32   Gandalf   1.31        7/19/99  Ian Formanek    Control Panel content 
 *       moved one level up
 *  31   Gandalf   1.30        7/13/99  Ian Formanek    Cleaned up creation of 
 *       several top-level nodes
 *  30   Gandalf   1.29        7/8/99   Jesse Glick     Context help.
 *  29   Gandalf   1.28        6/28/99  Jaroslav Tulach Debugger types are like 
 *       Executors
 *  28   Gandalf   1.27        6/9/99   Ian Formanek    ToolsAction
 *  27   Gandalf   1.26        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  26   Gandalf   1.25        6/8/99   Ian Formanek    ToolbarsNode moved to 
 *       separate class
 *  25   Gandalf   1.24        6/8/99   Ian Formanek    Cleaned imports
 *  24   Gandalf   1.23        5/30/99  Ian Formanek    DesktopNode provides 
 *       cookies for project operations
 *  23   Gandalf   1.22        5/27/99  Jaroslav Tulach Executors rearanged.
 *  22   Gandalf   1.21        5/16/99  Jaroslav Tulach Serializes the selection
 *       in the explorer panel.
 *  21   Gandalf   1.20        5/9/99   Ian Formanek    Fixed bug 1655 - 
 *       Renaming of top level nodes is not persistent (removed the possibility 
 *       to rename).
 *  20   Gandalf   1.19        5/8/99   Ian Formanek    Removed ProjectsNode - 
 *       it is now provided by Projects module
 *  19   Gandalf   1.18        5/8/99   Ian Formanek    Removed BookmarksNode - 
 *       it is now provided by URL module
 *  18   Gandalf   1.17        4/28/99  Jaroslav Tulach XML storage for modules.
 *  17   Gandalf   1.16        4/19/99  Jaroslav Tulach Updating of modules  
 *  16   Gandalf   1.15        4/8/99   Ian Formanek    Changed Object.class -> 
 *       getClass ()
 *  15   Gandalf   1.14        4/8/99   Ian Formanek    Icons for module nodes
 *  14   Gandalf   1.13        4/7/99   Ian Formanek    Hints for system nodes, 
 *       first version of modules node
 *  13   Gandalf   1.12        4/7/99   Ian Formanek    Rename 
 *       Description->ModuleDescription
 *  12   Gandalf   1.11        3/29/99  Jaroslav Tulach Open API warning.
 *  11   Gandalf   1.10        3/29/99  Jaroslav Tulach places ().nodes 
 *       ().session ()
 *  10   Gandalf   1.9         3/28/99  David Simonek   menu support improved 
 *       (icons, actions...)
 *  9    Gandalf   1.8         3/27/99  Jaroslav Tulach Jikes
 *  8    Gandalf   1.7         3/17/99  Ian Formanek    DesktopNode short 
 *       description
 *  7    Gandalf   1.6         3/17/99  Ian Formanek    Improved locales
 *  6    Gandalf   1.5         3/17/99  Ian Formanek    Added modules, fixed 
 *       icons, and many more...
 *  5    Gandalf   1.4         3/16/99  Ian Formanek    Object Types moved to 
 *       Session Settings
 *  4    Gandalf   1.3         3/13/99  Jaroslav Tulach Places.roots ()
 *  3    Gandalf   1.2         3/11/99  Ian Formanek    
 *  2    Gandalf   1.1         3/11/99  Ian Formanek    Bookmarks & Startup 
 *       added to Session Settings
 *  1    Gandalf   1.0         3/4/99   Ian Formanek    
 * $
 */
