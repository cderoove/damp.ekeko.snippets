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
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.HelpCtx;
import org.openide.util.actions.SystemAction;
import org.openide.util.datatransfer.NewType;

/** The PaletteCategoryNode is a Node representing the ComponentPalette content
* in the tree under Environment.
* @author   Ian Formanek
*/
public class PaletteCategoryNode extends DataFolder.FolderNode {
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = -2098259549820241091L;

    // -----------------------------------------------------------------------------
    // Static variables

    /** icons for the PaletteCategoryNode */
    private static String iconURL   = "/org/netbeans/modules/form/resources/paletteCategory.gif"; // NOI18N
    private static String icon32URL = "/org/netbeans/modules/form/resources/paletteCategory32.gif"; // NOI18N
    private static Image substIcon, substIcon32;

    private static final String PALETTE_FOLDER_NAME="Palette"; // NOI18N

    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];

    private static SystemAction[] staticActions;

    // -----------------------------------------------------------------------------
    // Constructors

    /** Creates a new palette node */
    public PaletteCategoryNode (DataFolder folder) {
        folder.super (new PaletteCategoryNodeChildren (folder));
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (PaletteCategoryNode.class);
    }

    // -----------------------------------------------------------------------------
    // Important interface

    public Node[] getCategoryComponents () {
        return getChildren ().getNodes ();
    }

    // -----------------------------------------------------------------------------
    // Other methods

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
                return new PaletteCategoryIndex(dataObj, (PaletteCategoryNodeChildren)getChildren());
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
        return new NewType[0];
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

    /** Creates properties for this node */
    public Node.PropertySet[] getPropertySets () {
        return NO_PROPERTIES;
    }

    // ------------------------------------------------------------------------------------
    // Innerclasses

    /** Children for the PaletteCategoryNode. Creates PaletteCategoryNodes as filter subnodes...
    */
    static final class PaletteCategoryNodeChildren extends FilterNode.Children {

        /** @param original the original node to take children from */
        public PaletteCategoryNodeChildren (DataFolder folder) {
            super(folder.getNodeDelegate ());
        }

        /** Overriden, returns PaletteCategoryNode filters for folders and copies of other nodes.
        * @param node node to create copy of
        * @return PaletteCategoryNode filter of the original node or Node's clone if it is not a DataFolder
        */
        protected Node copyNode (Node node) {
            if ((node.getCookie(DataObject.class) != null) && (node.getCookie(InstanceCookie.class) != null)) {
                return new PaletteItemNode(node);
            }
            return node.cloneNode();
        }
    }

    /** This class serves as index cookie implementation for the
    * PaletteCategoryNode object. Allows reordering of palette items.
    */
    static final class PaletteCategoryIndex extends DataFolder.Index {

        /** The children we are working with */
        PaletteCategoryNodeChildren children;

        PaletteCategoryIndex (final DataFolder df, final PaletteCategoryNodeChildren children) {
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
}

/*
 * Log
 *  13   Gandalf   1.12        1/5/00   Ian Formanek    NOI18N
 *  12   Gandalf   1.11        10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  11   Gandalf   1.10        8/18/99  Ian Formanek    Fixed bug 2608 - There 
 *       is no delete action in popup menu of Global Settings | Component 
 *       Palette but Delete key works.
 *  10   Gandalf   1.9         7/8/99   Jesse Glick     Context help.
 *  9    Gandalf   1.8         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  8    Gandalf   1.7         6/7/99   Ian Formanek    Palette nodes extend 
 *       FolderNode
 *  7    Gandalf   1.6         6/2/99   Ian Formanek    ToolsAction, Reorder
 *  6    Gandalf   1.5         5/11/99  Ian Formanek    Build 318 version
 *  5    Gandalf   1.4         4/26/99  Ian Formanek    
 *  4    Gandalf   1.3         4/7/99   Ian Formanek    Removed debug print
 *  3    Gandalf   1.2         4/1/99   Ian Formanek    fixed obtaining 
 *       resources (Object.class.getResource -> getClass ().getResource)
 *  2    Gandalf   1.1         3/24/99  Ian Formanek    
 *  1    Gandalf   1.0         3/24/99  Ian Formanek    
 * $
 */
