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

package org.netbeans.modules.multicompile;

import java.beans.*;
import java.io.*;
import java.util.*;

import org.openide.actions.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.text.EditorSupport;
import org.openide.util.NbBundle;
import org.openide.util.WeakListener;
import org.openide.util.actions.SystemAction;

/** A node to represent this object.
 *
 * @author  jglick
 */
public class GenericDataNode extends DataNode {

    public GenericDataNode(GenericDataObject obj) {
        this (obj, new FileChildren (obj));
    }

    public GenericDataNode(GenericDataObject obj,Children ch) {
        super (obj, ch);
        setIconBase ("/org/netbeans/modules/multicompile/GenericDataIcon");
    }

    private GenericDataObject getGenericDataObject () {
        return (GenericDataObject) getDataObject ();
    }

    protected Sheet createSheet () {
        Sheet sheet = super.createSheet ();
        Sheet.Set set = sheet.get (ExecSupport.PROP_EXECUTION);
        if (set == null) {
            set = new Sheet.Set ();
            set.setName (ExecSupport.PROP_EXECUTION);
            set.setDisplayName (NbBundle.getBundle (GenericDataNode.class).getString ("displayNameForGenericDataNodeExecSheet"));
            set.setShortDescription (NbBundle.getBundle (GenericDataNode.class).getString ("hintForGenericDataNodeExecSheet"));
        }
        ((ExecSupport) getCookie (ExecSupport.class)).addProperties (set);
        ((CompilerSupport) getCookie (CompilerSupport.class)).addProperties (set);
        sheet.put (set);
        return sheet;
    }

    public SystemAction getDefaultAction () {
        return SystemAction.get (OpenAction.class);
    }

    public static class FileChildren extends Children.Keys {

        private MultiDataObject obj;
        private PropertyChangeListener listener;

        public FileChildren (MultiDataObject obj) {
            this.obj = obj;
        }

        protected void addNotify () {
            List l = new LinkedList ();
            l.add (obj.getPrimaryEntry ());
            l.addAll (obj.secondaryEntries ());
            setKeys (l);
            if (listener == null) {
                obj.addPropertyChangeListener(listener = new PropertyChangeListener () {
                                                  public void propertyChange (PropertyChangeEvent ev) {
                                                      if (DataObject.PROP_FILES.equals (ev.getPropertyName ())) {
                                                          Children.MUTEX.writeAccess (new Runnable () {
                                                                                          public void run () {
                                                                                              addNotify ();
                                                                                          }
                                                                                      });
                                                      }
                                                  }
                                              });
            }
        }

        protected void removeNotify () {
            setKeys (Collections.EMPTY_SET);
            if (listener != null) {
                obj.removePropertyChangeListener (listener);
                listener = null;
            }
        }

        protected Node[] createNodes (Object key) {
            return new Node[] { new FileNode ((MultiDataObject.Entry) key) };
        }

    }

    public static class FileNode extends AbstractNode {

        private MultiDataObject.Entry file;
        private FileChangeListener listener;

        public FileNode (MultiDataObject.Entry file) {
            super (Children.LEAF);
            this.file = file;
            updateName ();
            FileObject fo = file.getFile ();
            fo.addFileChangeListener (WeakListener.fileChange (listener = new FileChangeAdapter () {
                                          public void fileRenamed (FileRenameEvent ev) {
                                              updateName ();
                                          }
                                      }, fo));
            // [PENDING] an EditorSupport, esp. read-only, would be nice...
            // but see bug #4621
        }

        private void updateName () {
            setName (file.getFile ().getName () + "." + file.getFile ().getExt ());
        }

        public boolean canDestroy () {
            return true;
        }

        public void destroy () throws IOException {
            file.delete ();
        }

    }

}