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

package org.netbeans.modules.url;

import java.util.ResourceBundle;

import org.openide.nodes.*;
import org.openide.actions.*;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;
import org.openide.filesystems.*;
import org.openide.loaders.DataFolder;
import org.openide.util.datatransfer.NewType;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/* TODO:
 - new type for creating new bookmarks
*/

/** The node for the bookmarks folder representation.
* Delegates most of its functionality to the original data folder node.
* Final only for better performance, can be unfinaled.
*
* @author Ian Formanek
*/
public final class BookmarksNode extends DataFolder.FolderNode {

    /** Actions which this node supports */
    static SystemAction[] staticActions;
    /** Actions of this node when it is top level bookmarks node */
    static SystemAction[] topStaticActions;

    private static final Node.PropertySet[] NO_PROPERTIES = new Node.PropertySet[0];

    public static final ResourceBundle bundle = NbBundle.getBundle (BookmarksNode.class);

    public BookmarksNode () {
        this (TopManager.getDefault ().getPlaces ().folders ().bookmarks ());
    }

    /** Constructs this node with given node to filter.
    */
    BookmarksNode (DataFolder folder) {
        folder.super(new BookmarksFolderChildren(folder));
        setShortDescription(bundle.getString("CTL_Bookmarks_hint"));
        super.setIconBase ("/org/netbeans/modules/url/bookmarks"); // NOI18N
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (BookmarksNode.class);
    }

    /** Support for new types that can be created in this node.
    * @return array of new type operations that are allowed
    */
    public NewType[] getNewTypes () {
        return new NewType[] {
                   new NewType () {

                       public String getName () {
                           return bundle.getString ("CTL_NewBookmark");
                       }

                       public void create () throws java.io.IOException {
                           NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine (
                                                                  bundle.getString ("CTL_NewBookmarkName"), bundle.getString ("CTL_NewBookmarkNameTitle"));

                           if (TopManager.getDefault ().notify (input) == NotifyDescriptor.OK_OPTION) {
                               String bookmarkName = input.getInputText ();
                               input = new NotifyDescriptor.InputLine (
                                           bundle.getString ("CTL_NewBookmarkURL"), bundle.getString ("CTL_NewBookmarkURLTitle"));
                               input.setInputText (bundle.getString ("CTL_NewBookmarkValue"));
                               if (TopManager.getDefault ().notify (input) == NotifyDescriptor.OK_OPTION) {
                                   DataFolder dataObj = (DataFolder)getCookieHelper(DataFolder.class);
                                   FileObject folder = dataObj.getPrimaryFile ();
                                   String bookmarkText = input.getInputText ();
                                   String freeName = FileUtil.findFreeFileName (folder, bookmarkName, "url"); // NOI18N
                                   FileObject fo = folder.createData (freeName, "url"); // NOI18N
                                   FileLock lock = null;
                                   java.io.OutputStream os = null;
                                   try {
                                       lock = fo.lock ();
                                       os = fo.getOutputStream (lock);
                                       os.write (bookmarkText.getBytes ());
                                   } catch (java.io.IOException e) {
                                       TopManager.getDefault ().notifyException (e);
                                   } finally {
                                       if (os != null) os.close ();
                                       if (lock != null) lock.releaseLock ();
                                   }
                               }
                           }
                       }
                   },
                   new NewType () {
                       public String getName () {
                           return bundle.getString ("CTL_NewFolder");
                       }
                       public void create () throws java.io.IOException {
                           NotifyDescriptor.InputLine input = new NotifyDescriptor.InputLine (
                                                                  bundle.getString ("CTL_NewFolderName"), bundle.getString ("CTL_NewFolderTitle")
                                                              );
                           input.setInputText (bundle.getString ("CTL_NewFolderValue"));
                           if (TopManager.getDefault ().notify (input) == NotifyDescriptor.OK_OPTION) {
                               DataFolder dataObj = (DataFolder)getCookieHelper(DataFolder.class);
                               FileObject folder = dataObj.getPrimaryFile ();
                               String folderName = input.getInputText ();
                               if (folder.getFileObject (folderName) != null) {
                                   TopManager.getDefault ().notify (
                                       new NotifyDescriptor.Message (
                                           java.text.MessageFormat.format (bundle.getString ("FMT_FolderExists"), new Object[] { folderName }),
                                           NotifyDescriptor.INFORMATION_MESSAGE
                                       )
                                   );
                               } else {
                                   folder.createFolder (folderName);
                               }
                           }
                       }
                   },
               };
    }


    public SystemAction[] getActions () {
        if (staticActions == null) {
            staticActions = new SystemAction[] {
                                SystemAction.get(FileSystemAction.class),
                                null,
                                SystemAction.get(PasteAction.class),
                                null,
                                SystemAction.get(ReorderAction.class),
                                null,
                                SystemAction.get(NewAction.class),
                                null,
                                SystemAction.get(ToolsAction.class),
                                SystemAction.get(PropertiesAction.class),
                            };
        }
        return staticActions;
    }

    /** @return empty property sets. */
    public PropertySet[] getPropertySets () {
        return NO_PROPERTIES;
    }

    public boolean canRemove() {
        return !isTopLevel ();
    }

    /** Special version of getCookie, which supports all cookies incl. the original DataNode ones, which are not provided
    * by default from this Node's getCookie method.
    * @param type the class to look for
    * @return instance of that class or null if this class of cookie
    *    is not supported
    */
    protected Node.Cookie getCookieHelper (Class type) {
        Node.Cookie ck = getCookie (type);
        if (ck == null) return super.getCookie (type);
        else return ck;
    }

    /** Supports index cookie in addition to standard support.
    * Redefined to prevent using DataFolder's cookies, so that common operations on folder like compile, etc. are not used here.
    * @param type the class to look for
    * @return instance of that class or null if this class of cookie
    *    is not supported
    */
    public Node.Cookie getCookie (Class type) {
        // no index for reordering toolbars, just for toolbar items
        if (Index.class.isAssignableFrom(type)) {
            // search for data object
            DataFolder dataObj = (DataFolder)super.getCookie(DataFolder.class);
            if (dataObj != null) {
                return new BookmarksIndex(dataObj, (BookmarksFolderChildren)getChildren());
            }
        }
        return null;
    }

    /** Utility - is this top level toolbar node? */
    boolean isTopLevel () {
        final Node n = getParentNode();
        return (n == null) || !(n instanceof BookmarksNode);
    }

    /** Children for the BookmarksNode. Creates BookmarksNodes or
    * BookmarksItemNodes as filter subnodes...
    */
    static final class BookmarksFolderChildren extends FilterNode.Children {

        /** @param or original node to take children from */
        public BookmarksFolderChildren (DataFolder folder) {
            super(folder.getNodeDelegate ());
        }

        /** Overriden, returns BookmarksNode filters of original nodes.
        *
        * @param node node to create copy of
        * @return BookmarksNode filter of the original node
        */
        protected Node copyNode (Node node) {
            DataFolder df = (DataFolder)node.getCookie(DataFolder.class);
            if (df != null) {
                return new BookmarksNode(df);
            }
            Node parent = node.getParentNode();

            // nodes in the same folder as toolbar folders are not toolbar items!
            if ((parent != null) && (!(parent.getParentNode () instanceof BookmarksNode))) {
                return node.cloneNode ();
            }

            return node.cloneNode (); //new BookmarksItemNode(node);
        }

    }

    /** This class serves as index cookie implementation for the
    * BookmarksNode object. Allows reordering of Toolbar items.
    */
    static final class BookmarksIndex extends DataFolder.Index {

        /** The children we are working with */
        BookmarksFolderChildren children;

        BookmarksIndex (final DataFolder df, final BookmarksFolderChildren children) {
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

    } // end of BookmarksIndex

}

/*
* Log
*  11   Gandalf   1.10        1/5/00   Ian Formanek    NOI18N
*  10   Gandalf   1.9         1/2/00   Ian Formanek    When creating new 
*       bookmark, the user is asked for bookmark name as well.
*  9    Gandalf   1.8         10/29/99 Ian Formanek    BookmarksFolder does not 
*       provide DataNode's cookies to increase usability (Compile, Generate 
*       JavaDoc, etc. items are now not in its popup menu).
*  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    Gandalf   1.6         7/16/99  Ian Formanek    New type for bookmark 
*       finished
*  6    Gandalf   1.5         7/15/99  Ian Formanek    New types
*  5    Gandalf   1.4         7/8/99   Jesse Glick     Context help.
*  4    Gandalf   1.3         7/5/99   Ian Formanek    Changed to folder node
*  3    Gandalf   1.2         6/9/99   Ian Formanek    ToolsAction
*  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         5/8/99   Ian Formanek    
* $
*/
