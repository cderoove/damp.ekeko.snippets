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

package org.netbeans.modules.web.core.jswdk;

import java.beans.*;
import java.awt.Window;
import java.awt.Frame;
import java.awt.Component;
import java.io.File;
import java.util.Enumeration;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.openide.util.NbBundle;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataFilter;
import org.openide.loaders.DataFolder;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.execution.NbClassPath;
import org.openide.util.UserCancelException;
import org.openide.TopManager;

/** Property editor for server root property of ServletJspExecutor.class
*
* @author Petr Jiricka
* @version 0.11 May 5, 1999
*/
public class ContextRootEditor extends PropertyEditorSupport {

    /** localized local host string*/
    private final static String DEFAULT = NbBundle.getBundle(ContextRootEditor.class).
                                          getString("CTL_DefaultContextRoot");

    /** @return the tag for Default */
    public String[] getTags() {
        Enumeration en = TopManager.getDefault().getRepository().fileSystems();
        ArrayList al = new ArrayList();
        al.add(DEFAULT);
        FileSystem fs;
        for (;en.hasMoreElements();) {
            fs = (FileSystem)en.nextElement();
            if (!fs.isHidden()) {
                File file = NbClassPath.toFile(fs.getRoot());
                if (file != null) {
                    al.add(fs.getDisplayName());
                }
            }
        }
        return (String [])al.toArray(new String[al.size()]);;
    }

    /** @return text for the current value */
    public String getAsText () {
        FileSystem fs = (FileSystem) getValue();
        if (fs == null)
            return DEFAULT;
        else
            return fs.getDisplayName();
    }

    /** @param text A text for the current value. */
    public void setAsText (String text) {
        if (text.equals(DEFAULT)) {
            setValue(null);
            return;
        }
        Enumeration en = TopManager.getDefault().getRepository().fileSystems();
        FileSystem fs;
        for (;en.hasMoreElements();) {
            fs = (FileSystem)en.nextElement();
            if (!fs.isHidden()) {
                if (fs.getDisplayName().equals(text)) {
                    setValue(fs);
                    return;
                }
            }
        }
        throw new IllegalArgumentException ();
    }

    public void setValue(Object value) {
        super.setValue(value);
    }

    public boolean supportsCustomEditor() {
        return false;
    }

    public Component getCustomEditor() {
        return null;
        //return new CustomEditor(this);
    }

    /*  public static class CustomEditor extends Window {
         
        ContextRootEditor edit; 
         
        static final long serialVersionUID =-4983096980843609093L;
        public CustomEditor(ContextRootEditor edit) {
          super(new Frame());
          this.edit = edit;
        }
      
        public void show() {
          selectNode();
          //super.show();
          dispose();
        }
        
        public void selectNode() {  
          DataFilter filter = new DataFilter () {
            public boolean acceptDataObject (DataObject oj) {
              try {
                if (!(oj instanceof DataFolder)) return false;
                if (oj.getPrimaryFile().getFileSystem().getRoot() != oj.getPrimaryFile()) return false;
                File file = NbClassPath.toFile(oj.getPrimaryFile());
                if (file == null) return false;
                if (!file.isDirectory()) return false;
                return true;
              }
              catch (FileStateInvalidException e) {
                return false;
              }          
              catch (SecurityException e) {
                throw new InternalError();
              }
            }
          };
          Node ds = TopManager.getDefault ().getPlaces ().nodes ().repository (filter);
          try {
            DataObject dObj = (DataObject)TopManager.getDefault ().getNodeOperation ().select (
              NbBundle.getBundle(ContextRootEditor.class).getString ("CTL_SelectContext_Title"),
              NbBundle.getBundle(ContextRootEditor.class).getString ("CTL_SelectContext_RootTitle"),
              ds, new NodeAcceptor () {
                public boolean acceptNodes (Node[] nodes) {
                  if (nodes == null || nodes.length != 1) {
                    return false;
                  }
                  Node.Cookie cook = nodes[0].getCookie(DataObject.class);
                  if (!(cook instanceof DataFolder)) return false;
                  FileObject fo = ((DataFolder)cook).getPrimaryFile();
                  File file = NbClassPath.toFile(fo);
                  if (file == null) return false;
                  if (!file.isDirectory()) return false;
                  return true;
                }
              }
            )[0].getCookie(DataObject.class);
            edit.setValue(dObj);
          }
          catch (UserCancelException e) {
          }
        } 
      }*/
}

/*
 * Log
 *  4    Gandalf   1.3         12/21/99 Petr Jiricka    
 *  3    Gandalf   1.2         11/27/99 Patrik Knakal   
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         10/7/99  Petr Jiricka    
 * $
 */
