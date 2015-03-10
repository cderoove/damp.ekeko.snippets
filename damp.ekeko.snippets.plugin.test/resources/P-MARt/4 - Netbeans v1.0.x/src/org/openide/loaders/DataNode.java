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

package org.openide.loaders;

import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.beans.*;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ResourceBundle;

import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileStatusListener;
import org.openide.filesystems.FileStatusEvent;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;
import org.openide.util.datatransfer.*;
import org.openide.actions.InstantiateAction;
import org.openide.util.HelpCtx;
import org.openide.util.RequestProcessor;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.actions.SystemAction;
import org.openide.nodes.*;

/** Standard node representing a data object.
*
* @author Jaroslav Tulach
*/
public class DataNode extends AbstractNode {

    /** generated Serialized Version UID */
    static final long serialVersionUID = -7882925922830244768L;

    /** default base for icons for data objects */
    private static final String ICON_BASE = "/org/netbeans/core/resources/x"; // NOI18N

    /** DataObject of this node. */
    private DataObject obj;

    /** property change listener */
    private PropL propL;

    /** Create a data node for a given data object.
    * The provided children object will be used to hold all child nodes.
    * @param obj object to work with
    * @param ch children container for the node
    */
    public DataNode (DataObject obj, Children ch) {
        super (ch);
        this.obj = obj;

        propL = new PropL ();

        obj.addPropertyChangeListener (WeakListener.propertyChange (propL, obj));

        super.setName (obj.getName ());

        setIconBase (ICON_BASE);
    }

    /** Get the represented data object.
     * @return the data object
    */
    public DataObject getDataObject() {
        return obj;
    }

    /* Changes the name of the node and also renames the data object.
    *
    * @param name new name for the object
    * @param rename rename the data object?
    * @exception IllegalArgumentException if the rename failed
    */
    public void setName (String name, boolean rename) {
        try {
            if (rename) {
                obj.rename (name);
            }

            super.setName (name);
        } catch (IOException ex) {
            throw new IllegalArgumentException (ex.getMessage ());
        }
    }

    /* Rename the data object.
    * @param name new name for the object
    * @exception IllegalArgumentException if the rename failed
    */
    public void setName (String name) {
        setName (name, true);
    }


    /** Get the display name for the node.
     * A filesystem may {@link org.openide.filesystems.FileSystem#getStatus specially alter} this.
     * @return the desired name
    */
    public String getDisplayName () {
        String s = super.getDisplayName ();

        try {
            s = obj.getPrimaryFile ().getFileSystem ().getStatus ().annotateName (s, obj.files ());
        } catch (FileStateInvalidException e) {
            // no fs, do nothing
        }

        return s;
    }

    /** Get the displayed icon for this node.
     * A filesystem may {@link org.openide.filesystems.FileSystem#getStatus specially alter} this.
     * @param type the icon type from {@link java.beans.BeanInfo}
     * @return the desired icon
    */
    public java.awt.Image getIcon (int type) {
        java.awt.Image img = super.getIcon (type);

        try {
            img = obj.getPrimaryFile ().getFileSystem ().getStatus ().annotateIcon (img, type, obj.files ());
        } catch (FileStateInvalidException e) {
            // no fs, do nothing
        }

        return img;
    }

    public HelpCtx getHelpCtx () {
        return obj.getHelpCtx ();
    }

    /** Indicate whether the node may be renamed.
    * @return tests {@link DataObject#isRenameAllowed}
    */
    public boolean canRename () {
        return obj.isRenameAllowed ();
    }

    /** Indicate whether the node may be destroyed.
     * @return tests {@link DataObject#isDeleteAllowed}
     */
    public boolean canDestroy () {
        return obj.isDeleteAllowed ();
    }

    /* Destroyes the node
    */
    public void destroy () throws IOException {
        if (obj.isDeleteAllowed ()) {
            obj.delete ();
        }
        super.destroy ();
    }

    /* Returns true if this object allows copying.
    * @returns true if this object allows copying.
    */
    public final boolean canCopy () {
        return obj.isCopyAllowed ();
    }

    /* Returns true if this object allows cutting.
    * @returns true if this object allows cutting.
    */
    public final boolean canCut () {
        return obj.isMoveAllowed ();
    }

    /** This method returns null to signal that actions
    * provide by DataLoader.getActions should be returned from 
    * method getActions. If overriden to provide some actions,
    * then these actions will be preferred to the loader's ones.
    *
    * @return null
    */
    protected SystemAction[] createActions () {
        return null;
    }

    /** Get actions for this data object.
    * @see DataLoader#getActions
    * @return array of actions or <code>null</code>
    */
    public SystemAction[] getActions () {
        if (systemActions == null) {
            systemActions = createActions ();
        }

        if (systemActions != null) {
            return systemActions;
        }

        return obj.getLoader ().getActions ();
    }

    /** Get default action.
     * A data node may have a {@link InstantiateAction default action} if it represents a template.
    * @return an instantiation action if the underlying data object is a template. Otherwise the abstract node's default action is returned, possibly <code>null</code>.
    */
    public SystemAction getDefaultAction () {
        if (obj.isTemplate ()) {
            return SystemAction.get (InstantiateAction.class);
        } else {
            return super.getDefaultAction ();
        }
    }

    /** Get a cookie.
     * First of all {@link DataObject#getCookie} is
    * called. If it produces non-<code>null</code> result, that is returned.
    * Otherwise the superclass is tried.
    *
    * @return the cookie or <code>null</code>
    */
    public Node.Cookie getCookie (Class cl) {
        Node.Cookie c = obj.getCookie (cl);
        if (c != null) {
            return c;
        } else {
            return super.getCookie (cl);
        }
    }

    /* Initializes sheet of properties. Allow subclasses to
    * overwrite it.
    * @return the default sheet to use
    */
    protected Sheet createSheet () {
        Sheet s = Sheet.createDefault ();
        Sheet.Set ss = s.get (Sheet.PROPERTIES);

        Node.Property p;

        p = createNameProperty (obj);
        p.setName (DataObject.PROP_NAME);
        ss.put (p);

        try {
            p = new PropertySupport.Reflection (
                    obj, Boolean.TYPE, "isTemplate", "setTemplate" // NOI18N
                );
            p.setName (DataObject.PROP_TEMPLATE);
            p.setDisplayName (DataObject.getString("PROP_template"));
            p.setShortDescription (DataObject.getString("HINT_template"));
            ss.put (p);
        } catch (Exception ex) {
            throw new InternalError ();
        }
        return s;
    }

    /** Creates a name property for given data object.
    */
    static Node.Property createNameProperty (final DataObject obj) {
        Node.Property p = new PropertySupport.ReadWrite (
                              DataObject.PROP_NAME,
                              String.class,
                              DataObject.getString("PROP_name"),
                              DataObject.getString("HINT_name")
                          ) {
                              public Object getValue () {
                                  return obj.getName();
                              }

                              public void setValue (Object val) throws IllegalAccessException,
                                  IllegalArgumentException, InvocationTargetException {
                                  if (!canWrite())
                                      throw new IllegalAccessException();
                                  if (!(val instanceof String))
                                      throw new IllegalArgumentException();

                                  try {
                                      obj.rename ((String)val);
                                  } catch (IOException ex) {
                                      throw new InvocationTargetException (ex);
                                  }
                              }

                              public boolean canWrite () {
                                  return obj.isRenameAllowed();
                              }
                          };

        return p;
    }

    /** Support for firing property change.
    * @param ev event describing the change
    */
    void fireChange (PropertyChangeEvent ev) {
        firePropertyChange (ev.getPropertyName (), ev.getOldValue (), ev.getNewValue ());
        if (ev.getPropertyName ().equals (DataObject.PROP_NAME)) {
            super.setName (obj.getName ());
            return;
        }
        if (ev.getPropertyName ().equals (DataObject.PROP_COOKIE)) {
            fireCookieChange ();
        }
    }

    /** Handle for location of given data object.
    * @return handle that remembers the data object.
    */
    public Handle getHandle () {
        return new ObjectHandle (obj, this != obj.getNodeDelegate ());
    }

    /** Access method to fire icon change.
    */
    final void fireChangeAccess (boolean icon, boolean name) {
        if (name) {
            fireDisplayNameChange (null, null);
        }
        if (icon) {
            fireIconChange ();
        }
    }

    /** Property listener on data object that delegates all changes of
    * properties to this node.
    */
    private class PropL extends Object
        implements PropertyChangeListener, FileStatusListener {
        /** weak version of this listener */
        private FileStatusListener weakL;
        /** previous file system we were attached to */
        private FileSystem previous;

        public PropL () {
            updateStatusListener ();
        }

        public void propertyChange (PropertyChangeEvent ev) {
            if (DataFolder.PROP_CHILDREN.equals (ev.getPropertyName ())) {
                // the node is not interested in children changes
                return;
            }

            if (DataFolder.PROP_PRIMARY_FILE.equals (ev.getPropertyName ())) {
                // the node is not interested in children changes
                updateStatusListener ();
                setName (obj.getName (), false);
                return;
            }

            fireChange (ev);
        }

        /** Updates listening on a status of file system.
        */
        private void updateStatusListener () {
            if (previous != null) {
                previous.removeFileStatusListener (weakL);
            }
            try {
                previous = obj.getPrimaryFile ().getFileSystem ();

                if (weakL == null) {
                    weakL = WeakListener.fileStatus (this, null);
                }

                previous.addFileStatusListener (weakL);
            } catch (FileStateInvalidException ex) {
                previous = null;
            }
        }

        /** Notifies listener about change in annotataion of a few files.
        * @param ev event describing the change
        */
        public void annotationChanged (FileStatusEvent ev) {
            if (ev.hasChanged (obj.getPrimaryFile ())) {
                fireChangeAccess (ev.isIconChange (), ev.isNameChange ());
            }
        }
    }

    /** Handle for data object nodes */
    private static class ObjectHandle extends Object implements Handle {
        private FileObject obj;
        private boolean clone;

        static final long serialVersionUID =6616060729084681518L;


        public ObjectHandle (DataObject obj, boolean clone) {
            this.obj = obj.getPrimaryFile ();
            this.clone = clone;
        }

        public Node getNode () throws DataObjectNotFoundException {
            Node n = DataObject.find (obj).getNodeDelegate ();
            return clone ? n.cloneNode () : n;
        }
    }
}

/*
 * Log
 *  32   Gandalf   1.31        1/20/00  Jaroslav Tulach Less access to disk.
 *  31   Gandalf   1.30        1/15/00  Jaroslav Tulach DataShadow enhancements
 *  30   Gandalf   1.29        1/12/00  Ian Formanek    NOI18N
 *  29   Gandalf   1.28        11/29/99 Jaroslav Tulach Weaker + setName 
 *       (String, boolean)
 *  28   Gandalf   1.27        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  27   Gandalf   1.26        10/29/99 Jaroslav Tulach MultiFileSystem + 
 *       FileStatusEvent
 *  26   Gandalf   1.25        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  25   Gandalf   1.24        9/25/99  Jaroslav Tulach #2152
 *  24   Gandalf   1.23        9/10/99  Jaroslav Tulach Annotate improvement
 *  23   Gandalf   1.22        9/1/99   Jaroslav Tulach The DataNode reacts to 
 *       changes in FileSystem.getStatus by updating its name and icon.
 *  22   Gandalf   1.21        8/18/99  Ian Formanek    Generated serial version
 *       UID
 *  21   Gandalf   1.20        7/29/99  Jaroslav Tulach createActions can be 
 *       overriden and it does something
 *  20   Gandalf   1.19        7/23/99  Jesse Glick     Removing helpCtx 
 *       property from data nodes.
 *  19   Gandalf   1.18        6/24/99  Jesse Glick     Help context pulled from
 *       data object.
 *  18   Gandalf   1.17        6/9/99   Jaroslav Tulach Executables can be in 
 *       menu & toolbars.
 *  17   Gandalf   1.16        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  16   Gandalf   1.15        5/10/99  Jesse Glick     [JavaDoc]
 *  15   Gandalf   1.14        5/10/99  Jaroslav Tulach DataNode.canRename
 *  14   Gandalf   1.13        5/9/99   Ian Formanek    Fixed bug 1651 - Rename 
 *       action in popup menu of package is always disabled, renaming by "Name" 
 *       field in property sheet works.
 *  13   Gandalf   1.12        4/23/99  Jaroslav Tulach Handle implemented.
 *  12   Gandalf   1.11        4/5/99   Ian Formanek    Removed obsoleted import
 *  11   Gandalf   1.10        3/16/99  Jesse Glick     [JavaDoc]
 *  10   Gandalf   1.9         3/15/99  Jesse Glick     [JavaDoc]
 *  9    Gandalf   1.8         3/13/99  Jaroslav Tulach FileSystem.Status & 
 *       lastModified
 *  8    Gandalf   1.7         3/10/99  Jesse Glick     [JavaDoc]
 *  7    Gandalf   1.6         3/9/99   Jesse Glick     [JavaDoc]
 *  6    Gandalf   1.5         2/5/99   Jaroslav Tulach Changed new from 
 *       template action
 *  5    Gandalf   1.4         1/21/99  Ales Novak      
 *  4    Gandalf   1.3         1/7/99   Jaroslav Tulach 
 *  3    Gandalf   1.2         1/7/99   Ian Formanek    fixed resource names
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach Change of package of 
 *       DataObject
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 * Beta Change History:
 *  0    Tuborg    0.11        --/--/98 Jaroslav Tulach actions are lookuped in the loaders pool
 *  0    Tuborg    0.12        --/--/98 Ales Novak      Serializable
 *  0    Tuborg    0.13        --/--/98 Jaroslav Tulach default action for templates
 */
