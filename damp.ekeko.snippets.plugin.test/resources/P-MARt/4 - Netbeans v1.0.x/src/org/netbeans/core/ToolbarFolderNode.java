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
import java.awt.Toolkit;
import java.util.ResourceBundle;
import java.io.IOException;

import org.openide.nodes.*;
import org.openide.actions.*;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.loaders.DataFolder;
import org.openide.util.datatransfer.NewType;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

import org.netbeans.core.windows.toolbars.ToolbarConfiguration;

/** The node for the toolbar folder representation.
* Delegates most of its functionality to the original data folder node.
* Final only for better performance, can be unfinaled.
*
* @author Dafe Simonek
*/
public final class ToolbarFolderNode extends DataFolder.FolderNode {

    /** Actions which this node supports */
    static SystemAction[] staticActions;
    /** Actions of this node when it is top level toolbar node */
    static SystemAction[] topStaticActions;

    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];
    private static final NewType[] NO_NEW_TYPES = new NewType[0];

    public static final ResourceBundle bundle = NbBundle.getBundle (ToolbarFolderNode.class);

    public ToolbarFolderNode () {
        this (NbTopManager.getDefault ().getPlaces ().folders ().toolbars ());
    }

    /** Constructs this node with given node to filter.
    */
    ToolbarFolderNode (DataFolder folder) {
        folder.super(new ToolbarFolderChildren(folder));
        //JST: it displays only Toolbar as name!    super.setDisplayName(NbBundle.getBundle (ToolbarFolderNode.class).getString("CTL_Toolbars_name"));
        super.setShortDescription(NbBundle.getBundle (ToolbarFolderNode.class).getString("CTL_Toolbars_hint"));
        super.setIconBase ("/org/netbeans/core/resources/toolbars"); // NOI18N
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ToolbarFolderNode.class);
    }

    /** Support for new types that can be created in this node.
    * @return array of new type operations that are allowed
    */
    public NewType[] getNewTypes () {
        if (isTopLevel()) {
            return new NewType[] {
                       new NewType () {
                           public String getName () {
                               return bundle.getString ("PROP_newToolbarName");
                           }
                           public void create () throws IOException {
                               newToolbar();
                           }
                       },
                       new NewType () {
                           public String getName () {
                               return bundle.getString ("PROP_newToolbarConfigName");
                           }
                           public void create () throws IOException {
                               newConfiguration();
                           }
                       }
                   };
        } else return NO_NEW_TYPES;
    }

    void newToolbar () {
        NotifyDescriptor.InputLine il = new NotifyDescriptor.InputLine
                                        (bundle.getString ("PROP_newToolbarLabel"),
                                         bundle.getString ("PROP_newToolbarDialog"));
        il.setInputText (bundle.getString ("PROP_newToolbar"));

        Object ok = TopManager.getDefault ().notify (il);
        if (ok == NotifyDescriptor.OK_OPTION) {
            String s = il.getInputText();
            if (!s.equals ("")) { // NOI18N
                FileObject tbFO = TopManager.getDefault().getPlaces().folders().toolbars().getPrimaryFile();
                try {
                    FileSystem tbFS = tbFO.getFileSystem();
                    FileObject newFO = tbFS.find (tbFO.getName(), s, ""); // NOI18N
                    if (newFO == null)
                        newFO = tbFO.createFolder (s);
                } catch (IOException e) {
                    TopManager.getDefault ().notifyException (e);
                }
            }
        }
    }


    void newConfiguration () {
        NotifyDescriptor.InputLine il = new NotifyDescriptor.InputLine
                                        (bundle.getString ("PROP_newToolbarConfigLabel"),
                                         bundle.getString ("PROP_newToolbarConfigDialog"));
        il.setInputText (bundle.getString ("PROP_newToolbarConfig"));

        Object ok = TopManager.getDefault ().notify (il);
        if (ok == NotifyDescriptor.OK_OPTION) {
            String s = il.getInputText();
            if (!s.equals ("")) { // NOI18N
                ToolbarConfiguration tc = new ToolbarConfiguration (s);
                try {
                    tc.writeDocument();
                } catch (IOException e) {
                    TopManager.getDefault ().notifyException (e);
                }
            }
        }
    }


    /** Actions.
    * @return array of actions for this node
    */
    public SystemAction[] getActions () {
        if (isTopLevel()) {
            if (topStaticActions == null)
                topStaticActions = new SystemAction [] {
                                       SystemAction.get (FileSystemAction.class),
                                       null,
                                       SystemAction.get(PasteAction.class),
                                       null,
                                       SystemAction.get(NewAction.class),
                                       null,
                                       SystemAction.get(ToolsAction.class),
                                       SystemAction.get(PropertiesAction.class),
                                   };
            return topStaticActions;
        } else {
            if (staticActions == null)
                staticActions = new SystemAction [] {
                                    SystemAction.get (FileSystemAction.class),
                                    null,
                                    SystemAction.get(ReorderAction.class),
                                    null,
                                    SystemAction.get(CutAction.class),
                                    SystemAction.get(CopyAction.class),
                                    SystemAction.get(PasteAction.class),
                                    null,
                                    SystemAction.get(DeleteAction.class),
                                    SystemAction.get(RenameAction.class),
                                    null,
                                    SystemAction.get(ToolsAction.class),
                                    SystemAction.get(PropertiesAction.class),
                                };
            return staticActions;
        }
    }

    /** Creates properties for this node */
    public Node.PropertySet[] getPropertySets () {
        if (isTopLevel()) {
            return NO_PROPERTIES;
        } else {
            ResourceBundle bundle = NbBundle.getBundle(ToolbarFolderNode.class);
            // default sheet with "properties" property set // NOI18N
            Sheet sheet = Sheet.createDefault();
            sheet.get(Sheet.PROPERTIES).put(
                new PropertySupport.Name(
                    this,
                    bundle.getString("PROP_ToolbarName"),
                    bundle.getString("HINT_ToolbarName")
                )
            );
            return sheet.toArray();
        }
    }

    /** Supports index cookie in addition to standard support.
    *
    * @param type the class to look for
    * @return instance of that class or null if this class of cookie
    *    is not supported
    */
    public Node.Cookie getCookie (Class type) {
        // no index for reordering toolbars, just for toolbar items
        if ((!isTopLevel ()) && Index.class.isAssignableFrom(type)) {
            // search for data object
            DataFolder dataObj = (DataFolder)super.getCookie(DataFolder.class);
            if (dataObj != null) {
                return new ToolbarIndex(dataObj, (ToolbarFolderChildren)getChildren());
            }
        }
        return super.getCookie(type);
    }

    /** Utility - is this top level toolbar node? */
    boolean isTopLevel () {
        final Node n = getParentNode();
        return (n == null) || !(n instanceof ToolbarFolderNode);
    }

    /** Children for the ToolbarFolderNode. Creates ToolbarFolderNodes or
    * ToolbarItemNodes as filter subnodes...
    */
    static final class ToolbarFolderChildren extends FilterNode.Children {

        /** @param or original node to take children from */
        public ToolbarFolderChildren (DataFolder folder) {
            super(folder.getNodeDelegate ());
        }

        /** Overriden, returns ToolbarFolderNode filters of original nodes.
        *
        * @param node node to create copy of
        * @return ToolbarFolderNode filter of the original node
        */
        protected Node copyNode (Node node) {
            DataFolder df = (DataFolder)node.getCookie(DataFolder.class);
            if (df != null) {
                return new ToolbarFolderNode(df);
            }
            Node parent = node.getParentNode();

            // nodes in the same folder as toolbar folders are not toolbar items!
            if (node.getCookie (org.openide.loaders.InstanceDataObject.class) != null) {
                return new ToolbarItemNode(node);
            } else {
                return node.cloneNode ();
            }
        }
    }

    /** This class serves as index cookie implementation for the
    * ToolbarFolderNode object. Allows reordering of Toolbar items.
    */
    static final class ToolbarIndex extends DataFolder.Index {

        /** The children we are working with */
        ToolbarFolderChildren children;

        ToolbarIndex (final DataFolder df, final ToolbarFolderChildren children) {
            super(df);
            this.children = children;
        }

        /** Overrides DataFolder.Index.getNodesCount().
        * Returns count of the nodes from the asociated chidren.
        */
        public int getNodesCount () {
            return children.getNodesCount();
        }

        /** Overrides DataFolder.Index.getNodes().
        * Returns array of subnodes from asociated children.
        * @return array of subnodes
        */
        public Node[] getNodes () {
            return children.getNodes();
        }

    } // end of ToolbarIndex

    static final class ToolbarItemNode extends FilterNode {

        /** Icons for this node */
        static Image itemIcon;
        static Image itemIcon32;

        /** Actions which this node supports */
        static SystemAction[] staticActions;

        /** Constructs new filter node for Toolbar item */
        ToolbarItemNode (Node filter) {
            super(filter, Children.LEAF);
        }

        /** Finds an icon for this node.
        * @see java.bean.BeanInfo
        * @param type constants from <CODE>java.bean.BeanInfo</CODE>
        * @return icon to use to represent the bean
        */
        public Image getIcon (int type) {
            if ((type == java.beans.BeanInfo.ICON_COLOR_16x16) ||
                    (type == java.beans.BeanInfo.ICON_MONO_16x16)) {
                if (itemIcon == null)
                    itemIcon = Toolkit.getDefaultToolkit ().getImage (
                                   getClass ().getResource ("/org/netbeans/core/resources/action.gif")); // NOI18N
                return itemIcon;
            } else {
                if (itemIcon32 == null)
                    itemIcon32 = Toolkit.getDefaultToolkit ().getImage (
                                     getClass ().getResource ("/org/netbeans/core/resources/action32.gif")); // NOI18N
                return itemIcon32;
            }
        }

        /** Finds an open icon for this node.
        *
        * @param type constants from <CODE>java.bean.BeanInfo</CODE>
        * @return icon to use to represent the bean when opened
        */
        public Image getOpenedIcon (int type) {
            return getIcon (type);
        }

        /** Actions.
        * @return array of actions for this node
        */
        public SystemAction[] getActions () {
            if (staticActions == null) {
                staticActions = new SystemAction [] {
                                    SystemAction.get(MoveUpAction.class),
                                    SystemAction.get(MoveDownAction.class),
                                    null,
                                    SystemAction.get(CutAction.class),
                                    SystemAction.get(CopyAction.class),
                                    null,
                                    SystemAction.get(DeleteAction.class),
                                    null,
                                    SystemAction.get(ToolsAction.class),
                                    SystemAction.get(PropertiesAction.class),
                                };
            }
            return staticActions;
        }

        /** Disallows renaming.
        */
        public boolean canRename () {
            return false;
        }

        /** Creates properties for this node */
        public Node.PropertySet[] getPropertySets () {
            ResourceBundle bundle = NbBundle.getBundle(ToolbarFolderNode.class);
            // default sheet with "properties" property set // NOI18N
            Sheet sheet = Sheet.createDefault();
            sheet.get(Sheet.PROPERTIES).put(
                new PropertySupport.Name(
                    this,
                    bundle.getString("PROP_ToolbarItemName"),
                    bundle.getString("HINT_ToolbarItemName")
                )
            );
            // PENDING - enable and help properties missing
            return sheet.toArray();
        }

    } // end of ToolbarItemNode

}

/*
* Log
*  10   Gandalf   1.9         1/16/00  Libor Kramolis  
*  9    Gandalf   1.8         1/13/00  Jaroslav Tulach I18N
*  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    Gandalf   1.6         8/29/99  Ian Formanek    Fixed bug 2404 - 
*       ToolbarFolderNode should not provide new-types on non-root folders.
*  6    Gandalf   1.5         7/29/99  Ian Formanek    Fixed bug 2219 - Toolbars
*       items under Session Settings|Toolbars|<any node> haven't Move Down/Up 
*       actions in popup menu.
*  5    Gandalf   1.4         7/12/99  Jesse Glick     Context help.
*  4    Gandalf   1.3         7/11/99  David Simonek   window system change...
*  3    Gandalf   1.2         6/22/99  Libor Kramolis  
*  2    Gandalf   1.1         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         6/8/99   Ian Formanek    
* $
*/
