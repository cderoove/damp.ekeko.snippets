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

package org.netbeans.modules.form.palette;

import java.awt.Image;
import java.awt.Toolkit;
import java.beans.BeanInfo;

import org.openide.*;
import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;

/** The PaletteNode is a Node representing the ComponentPalette content
* in the tree under Environment.
* @author   Ian Formanek
*/
public class PaletteNode extends DataFolder.FolderNode {
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = -2098259549820241091L;

    // -----------------------------------------------------------------------------
    // Static variables

    private static PaletteNode sharedPaletteNode;

    /** icons for the PaletteNode */
    private static String iconURL   = "/org/netbeans/modules/form/resources/palette.gif"; // NOI18N
    private static String icon32URL = "/org/netbeans/modules/form/resources/palette32.gif"; // NOI18N
    private static Image substIcon, substIcon32;

    private static final String PALETTE_FOLDER_NAME="Palette"; // NOI18N
    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];

    private static SystemAction[] staticActions;
    private static DataFolder paletteFolder;

    private static java.util.ResourceBundle bundle = org.openide.util.NbBundle.getBundle (PaletteNode.class);

    // -----------------------------------------------------------------------------
    // Constructors

    /** Creates a new palette node */
    public PaletteNode () {
        this (getPaletteFolder ());
    }

    PaletteNode (DataFolder folder) {
        folder.super (new PaletteNodeChildren (folder));
        setDisplayName(bundle.getString("CTL_Component_palette"));
        if (sharedPaletteNode == null)
            sharedPaletteNode = this;
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (PaletteNode.class);
    }

    // -----------------------------------------------------------------------------
    // Public static methods

    public static PaletteNode getPaletteNode () {
        if (sharedPaletteNode == null) {
            new PaletteNode (); // assigns itself to sharedPaletteNode
        }
        return sharedPaletteNode;
    }

    // -----------------------------------------------------------------------------
    // Important interface

    public PaletteCategoryNode[] getPaletteCategories () {
        Node[] categories = getChildren ().getNodes ();
        java.util.ArrayList al = new java.util.ArrayList (categories.length);
        for (int i = 0; i < categories.length; i++) {
            if (categories[i] instanceof PaletteCategoryNode) {
                al.add (categories[i]);
            }
        }
        return (PaletteCategoryNode[]) al.toArray (new PaletteCategoryNode[al.size ()]);
    }

    // -----------------------------------------------------------------------------
    // Other methods

    static DataFolder getPaletteFolder () {
        if (paletteFolder != null)
            return paletteFolder;

        try {
            FileObject fo = TopManager.getDefault ().getRepository().getDefaultFileSystem ().findResource(PALETTE_FOLDER_NAME);

            if (fo == null) {
                // resource not found, try to create new folder
                fo = TopManager.getDefault ().getRepository ().getDefaultFileSystem ().getRoot ().createFolder (PALETTE_FOLDER_NAME);
            }

            paletteFolder = DataFolder.findFolder(fo);
            return paletteFolder;
        } catch (java.io.IOException ex) {
            throw new InternalError ("Folder not found and cannot be created: " + PALETTE_FOLDER_NAME); // NOI18N
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
                return new PaletteIndex(dataObj, (PaletteNodeChildren)getChildren());
            }
        }
        return super.getCookie(type);
    }

    public Image getIcon (int type) {
        if ((type == BeanInfo.ICON_COLOR_16x16) || (type == BeanInfo.ICON_MONO_16x16)) {
            if (substIcon == null)
                substIcon = Toolkit.getDefaultToolkit ().getImage (
                                getClass ().getResource (iconURL));
            return substIcon;
        }
        else {
            if (substIcon32 == null)
                substIcon32 = Toolkit.getDefaultToolkit ().getImage (
                                  getClass ().getResource (icon32URL));
            return substIcon32;
        }
    }

    public Image getOpenedIcon (int type) {
        return getIcon(type);
    }

    /** Support for new types that can be created in this node.
    * @return array of new type operations that are allowed
    */
    public NewType[] getNewTypes () {
        return new NewType[] { new NewCategory () };
    }

    /** Actions.
    * @return array of actions for this node
    */
    public SystemAction[] getActions () {
        if (staticActions == null)
            staticActions = new SystemAction [] {
                                SystemAction.get (FileSystemAction.class),
                                null,
                                SystemAction.get(MoveUpAction.class),
                                SystemAction.get(MoveDownAction.class),
                                SystemAction.get(ReorderAction.class),
                                null,
                                SystemAction.get(PasteAction.class),
                                null,
                                SystemAction.get(NewAction.class),
                                null,
                                SystemAction.get(ToolsAction.class),
                                SystemAction.get(PropertiesAction.class),
                            };
        return staticActions;
    }

    /** Creates properties for this node */
    public Node.PropertySet[] getPropertySets () {
        return NO_PROPERTIES;
    }

    // ------------------------------------------------------------------------------------
    // Innerclasses

    /** Children for the PaletteNode. Creates PaletteCategoryNodes as filter subnodes...
    */
    static final class PaletteNodeChildren extends FilterNode.Children {

        /** @param original the original node to take children from */
        public PaletteNodeChildren (DataFolder folder) {
            super(folder.getNodeDelegate ());
        }

        /** Overriden, returns PaletteCategoryNode filters for folders and copies of other nodes.
        * @param node node to create copy of
        * @return PaletteNode filter of the original node or Node's clone if it is not a DataFolder
        */
        protected Node copyNode (Node node) {
            DataFolder df = (DataFolder)node.getCookie(DataFolder.class);
            if (df != null) {
                return new PaletteCategoryNode(df);
            }
            return node.cloneNode();
        }
    }

    /** This class serves as index cookie implementation for the
    * PaletteNodeNode object. Allows reordering of palette categories.
    */
    static final class PaletteIndex extends DataFolder.Index {

        /** The children we are working with */
        PaletteNodeChildren children;

        PaletteIndex (final DataFolder df, final PaletteNodeChildren children) {
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
    }

    /** New type for creation of new palette category.
    */
    private final class NewCategory extends NewType {
        /** Display name for the creation action. This should be
        * presented as an item in a menu.
        *
        * @return the name of the action
        */
        public String getName() {
            return bundle.getString ("CTL_NewCategory");
        }

        /** Help context for the creation action.
        * @return the help context
        */
        public org.openide.util.HelpCtx getHelpCtx() {
            return new org.openide.util.HelpCtx (NewCategory.class);
        }

        /** Create the object.
        * @exception IOException if something fails
        */
        public void create () throws java.io.IOException {
            NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine (
                                                   bundle.getString ("CTL_NewCategoryName"), bundle.getString ("CTL_NewCategoryTitle")
                                               );
            input.setInputText (bundle.getString ("CTL_NewCategoryValue"));
            if (TopManager.getDefault ().notify (input) == NotifyDescriptor.OK_OPTION) {
                FileObject folder = getPaletteFolder ().getPrimaryFile ();
                String categoryName = input.getInputText ();
                if (folder.getFileObject (categoryName) != null) {
                    TopManager.getDefault ().notify (
                        new NotifyDescriptor.Message (
                            java.text.MessageFormat.format (bundle.getString ("FMT_CategoryExists"), new Object[] { categoryName }),
                            NotifyDescriptor.INFORMATION_MESSAGE
                        )
                    );
                } else {
                    folder.createFolder (categoryName);
                }
            }
        }
    }

}

/*
 * Log
 *  16   Gandalf   1.15        1/5/00   Ian Formanek    NOI18N
 *  15   Gandalf   1.14        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  14   Gandalf   1.13        7/15/99  Ian Formanek    Implemented suggestion 
 *       2402 - Component Palette node should have new-type for category.
 *  13   Gandalf   1.12        7/8/99   Jesse Glick     Context help.
 *  12   Gandalf   1.11        6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  11   Gandalf   1.10        6/7/99   Ian Formanek    Palette nodes extend 
 *       FolderNode
 *  10   Gandalf   1.9         6/2/99   Ian Formanek    ToolsAction, Reorder
 *  9    Gandalf   1.8         5/24/99  Ian Formanek    Provided static access 
 *       to Palette folder
 *  8    Gandalf   1.7         4/26/99  Ian Formanek    
 *  7    Gandalf   1.6         4/7/99   Ian Formanek    Removed debug print
 *  6    Gandalf   1.5         4/1/99   Jaroslav Tulach 
 *  5    Gandalf   1.4         4/1/99   Ian Formanek    fixed obtaining 
 *       resources (Object.class.getResource -> getClass ().getResource)
 *  4    Gandalf   1.3         3/30/99  Ian Formanek    Removed debug message
 *  3    Gandalf   1.2         3/24/99  Ian Formanek    
 *  2    Gandalf   1.1         3/24/99  Ian Formanek    
 *  1    Gandalf   1.0         3/21/99  Ian Formanek    
 * $
 */
