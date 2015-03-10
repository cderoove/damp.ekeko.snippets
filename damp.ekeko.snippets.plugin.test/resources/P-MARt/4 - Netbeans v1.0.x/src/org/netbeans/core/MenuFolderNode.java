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

import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.nodes.*;
import org.openide.util.datatransfer.NewType;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/** The node for the menu folder representation.
* Delegates most of its functionality to the original data folder node.
* Final only for better performance, can be unfinaled.
*
* @author Dafe Simonek
*/
public final class MenuFolderNode extends DataFolder.FolderNode {

    /** Actions which this node supports */
    static SystemAction[] staticActions;
    /** Actions of this node when it is top level menu node */
    static SystemAction[] topStaticActions;

    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];

    private DataFolder folder;
    private static final ResourceBundle bundle = NbBundle.getBundle (MenuFolderNode.class);

    public MenuFolderNode () {
        this (NbTopManager.getDefault ().getPlaces ().folders ().menus ());
    }

    /** Constructs this node with given node to filter.
    */
    MenuFolderNode (DataFolder folder) {
        folder.super(new MenuFolderChildren(folder));
        this.folder = folder;
        //JST: it displays only Menu as name!    super.setDisplayName(NbBundle.getBundle (MenuFolderNode.class).getString("CTL_Menu_name"));
        super.setShortDescription(bundle.getString("CTL_Menu_hint"));

        super.setIconBase ("/org/netbeans/core/resources/menu"); // NOI18N
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (MenuFolderNode.class);
    }

    /** Support for new types that can be created in this node.
    * @return array of new type operations that are allowed
    */
    public NewType[] getNewTypes () {
        return new NewType[] {
                   new NewType () {
                       public String getName () {
                           return bundle.getString ("CTL_newMenuName");
                       }
                       public void create () throws java.io.IOException {
                           newMenu();
                       }
                   }
               };
    }

    void newMenu () {
        NotifyDescriptor.InputLine il = new NotifyDescriptor.InputLine
                                        (bundle.getString ("CTL_newMenuLabel"),
                                         bundle.getString ("CTL_newMenuDialog"));
        il.setInputText (bundle.getString ("CTL_newMenu"));

        Object ok = TopManager.getDefault ().notify (il);
        if (ok == NotifyDescriptor.OK_OPTION) {
            String s = il.getInputText();
            if (!s.equals ("")) { // NOI18N
                FileObject mnFO = folder.getPrimaryFile();
                try {
                    FileSystem mnFS = mnFO.getFileSystem();
                    FileObject newFO = mnFS.find (mnFO.getName(), s, ""); // NOI18N
                    if (newFO == null)
                        newFO = mnFO.createFolder (s);
                } catch (java.io.IOException e) {
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
                                       SystemAction.get(ReorderAction.class),
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
                                    SystemAction.get(MoveUpAction.class),
                                    SystemAction.get(MoveDownAction.class),
                                    SystemAction.get(ReorderAction.class),
                                    null,
                                    SystemAction.get(CutAction.class),
                                    SystemAction.get(CopyAction.class),
                                    SystemAction.get(PasteAction.class),
                                    null,
                                    SystemAction.get(DeleteAction.class),
                                    SystemAction.get(RenameAction.class),
                                    null,
                                    SystemAction.get(NewAction.class),
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
            // default sheet with "properties" property set // NOI18N
            Sheet sheet = Sheet.createDefault();
            sheet.get(Sheet.PROPERTIES).put(
                new PropertySupport.Name(
                    this,
                    bundle.getString("PROP_MenuName"),
                    bundle.getString("HINT_MenuName")
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
        if (Index.class.isAssignableFrom(type)) {
            // search for data object
            DataFolder dataObj = (DataFolder)super.getCookie(DataFolder.class);
            if (dataObj != null) {
                return new DataFolder.Index (dataObj, this);
            }
        }
        return super.getCookie(type);
    }

    /** Utility - is this top level menu node? */
    boolean isTopLevel () {
        final Node n = getParentNode();
        return (n == null) || !(n instanceof MenuFolderNode);
    }

    /** Children for the MenuFolderNode. Creates MenuFolderNodes or
    * MenuItemNodes as filter subnodes...
    */
    static final class MenuFolderChildren extends FilterNode.Children {

        /** @param or original node to take children from */
        public MenuFolderChildren (DataFolder folder) {
            super(folder.getNodeDelegate ());
        }

        /** Overriden, returns MenuFolderNode filters of original nodes.
        *
        * @param node node to create copy of
        * @return MenuFolderNode filter of the original node
        */
        protected Node copyNode (Node node) {
            DataFolder df = (DataFolder)node.getCookie(DataFolder.class);
            if (df != null) {
                return new MenuFolderNode(df);
            }
            return new MenuItemNode(node);
        }

    }

    static final class MenuItemNode extends FilterNode {

        /** Icons for this node */
        static Image itemIcon;
        static Image itemIcon32;

        /** Actions which this node supports */
        static SystemAction[] staticActions;

        /** Constructs new filter node for menu item */
        MenuItemNode (Node filter) {
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
            // default sheet with "properties" property set // NOI18N
            Sheet sheet = Sheet.createDefault();
            sheet.get(Sheet.PROPERTIES).put(
                new PropertySupport.Name(
                    this,
                    bundle.getString("PROP_MenuItemName"),
                    bundle.getString("HINT_MenuItemName")
                )
            );
            // PENDING - enable and help properties missing
            return sheet.toArray();
        }

    } // end of MenuItemNode

}

/*
* Log
*  17   src-jtulach1.16        1/13/00  Jaroslav Tulach I18N
*  16   src-jtulach1.15        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  15   src-jtulach1.14        8/29/99  Ian Formanek    Fixed bug 2405 - 
*       MenuFolderNode and ToolbarFolderNode have inconsistent UIs.
*  14   src-jtulach1.13        7/8/99   Jesse Glick     Context help.
*  13   src-jtulach1.12        6/10/99  Jaroslav Tulach IndexedCustomizer works 
*       on menus
*  12   src-jtulach1.11        6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  11   src-jtulach1.10        6/8/99   Ian Formanek    Cleaned and fixed 
*       properties
*  10   src-jtulach1.9         6/7/99   Ian Formanek    Cleared actions
*  9    src-jtulach1.8         4/20/99  Jaroslav Tulach Does not uses filter 
*       node.
*  8    src-jtulach1.7         4/9/99   Ian Formanek    Removed debug printlns
*  7    src-jtulach1.6         4/8/99   Ian Formanek    Changed Object.class -> 
*       getClass ()
*  6    src-jtulach1.5         4/7/99   Ian Formanek    Hint for menu node
*  5    src-jtulach1.4         3/29/99  Jaroslav Tulach places ().nodes 
*       ().session ()
*  4    src-jtulach1.3         3/28/99  David Simonek   menu support improved 
*       (icons, actions...)
*  3    src-jtulach1.2         3/23/99  Ian Formanek    
*  2    src-jtulach1.1         3/3/99   David Simonek   
*  1    src-jtulach1.0         2/26/99  David Simonek   
* $
*/
