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
import java.beans.*;
import java.text.MessageFormat;
import java.util.Properties;
import java.util.ResourceBundle;

import org.openide.loaders.DataFolder;
import org.openide.explorer.propertysheet.PropertySheet;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.JarFileSystem;
import org.openide.filesystems.Repository;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.Node;

/** Special node for root folder. Takes icon from the
*
*
* @author Jaroslav Tulach
* @version 0.13, November 25, 1998
*/
final class RootFolderNode extends DataFolder.FolderNode {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 3742510847721720990L;
    /** message that gives name to the root */
    private static MessageFormat formatRoot;

    /** file system */
    private transient FileSystem fs;

    /** our data folder */ //is serialized
    DataFolder df;

    /**
    * @param data folder to work on
    */
    public RootFolderNode(DataFolder df, org.openide.nodes.Children ch) {
        df.super(ch);
        this.df = df;
        init();
    }

    /** Name of the node.
    */
    public String getName () {
        return fs == null ? "" : fs.getSystemName (); // NOI18N
    }

    /** initiates node */
    void init() {
        try {
            this.fs = df.getPrimaryFile ().getFileSystem ();
        } catch (org.openide.filesystems.FileStateInvalidException ex) {
            // hopefully should not happen
            throw new InternalError ();
        }

        setIconBase ("/org/netbeans/core/resources/defaultFS"); // NOI18N
        ResourceBundle bundle = org.openide.util.NbBundle.getBundle (RootFolderNode.class);
        formatRoot = new MessageFormat (bundle.getString ("dataFolderRootName"));
        initDisplayName ();
    }

    /** Initializes display name.
    */
    void initDisplayName () {
        String s = formatRoot.format (
                       new Object[] {fs.getDisplayName (), fs.getSystemName ()}
                   );
        setDisplayName (s);
        if (fs instanceof JarFileSystem) {
            try {
                setShortDescription(((JarFileSystem)fs).getJarFile ().getCanonicalPath ());
            } catch (java.io.IOException e) { // ignore error, no hint in such case
            }
        }
    }

    /** Finds an icon for this node. The filesystem's icon is returned.
    * @see java.bean.BeanInfo
    * @see org.openide.filesystems.FileSystem#getIcon
    * @param type constants from <CODE>java.bean.BeanInfo</CODE>
    * @return icon to use to represent the bean
    */
    public Image getIcon (int type) {
        BeanInfo bi = null;
        try {
            bi = org.openide.util.Utilities.getBeanInfo(fs.getClass());
        } catch (IntrospectionException e) {
            return super.getIcon(type);
        }
        Image icon =  bi.getIcon(type);
        return icon==null ? super.getIcon(type) : icon;
    }

    /** The DataFolderRoot's opened icon is the same as the closed one.
    * @return icon to use to represent the bean when opened
    */
    public Image getOpenedIcon (int type) {
        return getIcon(type);
    }

    /** @return the system actions for the root folder */
    public SystemAction[] createActions() {
        return new SystemAction[] {
                   SystemAction.get (org.openide.actions.OpenLocalExplorerAction.class),
                   SystemAction.get (org.openide.actions.FindAction.class),
                   null,
                   SystemAction.get (org.openide.actions.FileSystemAction.class),
                   null,
                   SystemAction.get (org.openide.actions.CompileAction.class),
                   SystemAction.get (org.openide.actions.CompileAllAction.class),
                   null,
                   SystemAction.get (org.openide.actions.BuildAction.class),
                   SystemAction.get (org.openide.actions.BuildAllAction.class),
                   null,
                   //      SystemAction.get (org.openide.actions.CutAction.class),
                   //      SystemAction.get (org.openide.actions.CopyAction.class),
                   SystemAction.get (org.openide.actions.PasteAction.class),
                   null,
                   //      SystemAction.get (org.openide.actions.DeleteAction.class),
                   //      SystemAction.get (org.openide.actions.RenameAction.class),
                   null,
                   SystemAction.get (org.openide.actions.NewAction.class),
                   SystemAction.get (org.openide.actions.NewTemplateAction.class),
                   null,
                   SystemAction.get(org.netbeans.core.actions.UnmountFSAction.class),
                   null,
                   SystemAction.get (org.openide.actions.ToolsAction.class),
                   SystemAction.get (org.openide.actions.PropertiesAction.class)
               };
    }

    /** Cutomizer to customize file system.
    */
    public boolean hasCustomizer () {
        return true;
    }

    /** Property sheet with file system.
    */
    public java.awt.Component getCustomizer () {
        PropertySheet ps = new PropertySheet ();
        try {
            ps.setNodes (new Node[] {
                             new FSPoolNode.FSNode (fs)
                         });
        } catch (java.beans.IntrospectionException ex) {
        }
        return ps;
    }

    /** Returns the cookie (set of behaviour) for this node.
    * The result can then be checked by <CODE>result instanceof MyCookie</CODE>,
    * this is valid even the returned value is <CODE>null</CODE> because
    * <CODE>null instanceof Anything = false</CODE>.
    *
    * @return this implementation returns <CODE>null</CODE>
    */
    public Node.Cookie getCookie (Class cl) {
        Node.Cookie ret = super.getCookie(cl);
        if (ret != null) return ret;
        if (UnmountFSCookie.class == cl && !fs.isDefault ())
            return new UnmountFSCookie() {
                   /** Unmounts the filesystem from the FSPool */
                   public void unmount() {
                       NbTopManager.getDefaultRepository ().removeFileSystem (fs);
                   }
               };
        return null;
    }

    /** deserializes object */
    private void readObject(java.io.ObjectInputStream is)
    throws java.io.IOException, ClassNotFoundException {
        is.defaultReadObject(); // df
        init();
    }
}

/*
 * Log
 *  19   Gandalf   1.18        2/15/00  Jaroslav Tulach #5458 - Added Find 
 *       action into pop-up menu.
 *  18   Gandalf   1.17        1/13/00  Jaroslav Tulach I18N
 *  17   Gandalf   1.16        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  16   Gandalf   1.15        9/13/99  Jaroslav Tulach Has valid getName, so 
 *       deserialization should work better.
 *  15   Gandalf   1.14        9/3/99   Jaroslav Tulach Customize action.
 *  14   Gandalf   1.13        8/29/99  Ian Formanek    Short description 
 *       (tooltip) for root nodes of JAR FS with full path to the JAR
 *  13   Gandalf   1.12        6/9/99   Ian Formanek    ToolsAction
 *  12   Gandalf   1.11        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  11   Gandalf   1.10        3/30/99  Jaroslav Tulach New Package
 *  10   Gandalf   1.9         3/29/99  Jaroslav Tulach Deleted new folder, use 
 *       New From Template -> package
 *  9    Gandalf   1.8         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  8    Gandalf   1.7         3/21/99  Jaroslav Tulach Repository displayed ok.
 *  7    Gandalf   1.6         3/19/99  Jaroslav Tulach TopManager.getDefault 
 *       ().getRegistry ()
 *  6    Gandalf   1.5         3/18/99  Jaroslav Tulach 
 *  5    Gandalf   1.4         2/11/99  Ian Formanek    Renamed FileSystemPool 
 *       -> Repository
 *  4    Gandalf   1.3         2/8/99   Petr Hamernik   OpenLocalExplorer 
 *       renamed to OpenLocalExplorerAction
 *  3    Gandalf   1.2         1/7/99   Ian Formanek    fixed resource names
 *  2    Gandalf   1.1         1/6/99   Ian Formanek    Reflecting change in 
 *       datasystem package
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Ales Novak      serializable
 *  0    Tuborg    0.12        --/--/98 Jaroslav Tulach changes in the static initializer
 */
