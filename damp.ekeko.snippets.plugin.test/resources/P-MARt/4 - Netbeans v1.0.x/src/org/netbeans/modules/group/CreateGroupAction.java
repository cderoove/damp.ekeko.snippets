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

package org.netbeans.modules.group;

import java.util.*;

import java.awt.*;
import javax.swing.*;

import org.openide.*;
import org.openide.filesystems.*;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.CookieAction;

/**
*
* @author Martin Ryzl
*/
public class CreateGroupAction extends CookieAction {

    static final long serialVersionUID =-280394671195477993L;
    protected void performAction(final Node[] nodes) {
        try {
            FileObject fo = selectFile();
            ArrayList list;

            if (fo != null) {
                list = new ArrayList();
                for(int i = 0; i < nodes.length; i++) {
                    Object obj = nodes[i].getCookie(DataObject.class);
                    if (obj != null) {
                        list.add(GroupShadow.getLinkName(((DataObject)obj).getPrimaryFile()));
                    }
                }
                GroupShadow.writeLinks(list, fo);
            }
        } catch (java.io.IOException ex) {
            TopManager.getDefault().notifyException(ex);
        }
    }

    protected Class[] cookieClasses() {
        return new Class[] { DataObject.class };
    }

    protected int mode() {
        return MODE_ALL;
    }

    public String getName() {
        return NbBundle.getBundle(CreateGroupAction.class).getString("CreateGroup");
    }

    public HelpCtx getHelpCtx() {
        return new HelpCtx(CreateGroupAction.class);
    }

    /** Lets the user to select a group shadow.
    * @return FileObject for the filesystem.
    */
    protected static FileObject selectFile() throws java.io.IOException {

        InputPanel jp = new InputPanel();

        try {
            // repository
            Node an = TopManager.getDefault().getPlaces().nodes().repository();

            NodeAcceptor na = new NodeAcceptor() {
                                  public boolean acceptNodes(Node[] nodes) {
                                      if (nodes == null || nodes.length != 1) {
                                          return false;
                                      }
                                      DataFolder cookie = (DataFolder)nodes[0].getCookie (DataFolder.class);
                                      return cookie != null && !cookie.getPrimaryFile ().isReadOnly ();
                                  }
                              };

            // select file system
            Node[] nodes = TopManager.getDefault().getNodeOperation().select(
                               NbBundle.getBundle (CreateGroupAction.class).getString ("PROP_Select_File"),
                               NbBundle.getBundle (CreateGroupAction.class).getString ("PROP_Look_In"),
                               an, na, jp
                           );

            FileObject folder = ((DataFolder)nodes[0].getCookie(DataFolder.class)).getPrimaryFile();
            return folder.createData(jp.getText(), GroupShadow.GS_EXTENSION );
        } catch (UserCancelException ex) {
            return null;
        } catch (NullPointerException ex) {
            // could occur if nodes[0].getCookie returns null, but
            // it should not happen because of the filter
            return null;
        }
    } // select file

    private static class InputPanel extends JPanel {

        JTextField text;

        static final long serialVersionUID =2856913107896554654L;
        public InputPanel () {
            BorderLayout lay = new BorderLayout ();
            lay.setVgap(5);
            lay.setHgap(5);
            setLayout (lay);
            // label and text field with mnemonic
            String labelText = NbBundle.getBundle (CreateGroupAction.class).getString ("CTL_Group_Name");
            JLabel label = new JLabel(labelText.replace('&', ' '));
            text = new JTextField ();
            label.setDisplayedMnemonic(labelText.charAt(labelText.indexOf('&') + 1));
            label.setLabelFor(text);
            add (BorderLayout.WEST, label);
            add (BorderLayout.CENTER, text);
        }

        public void requestFocus () {
            text.requestFocus ();
        }

        public String getText () {
            return text.getText ();
        }

        public void setText (String s) {
            setText (s);
        }
    }
}

/*
 * Log
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         8/19/99  Martin Ryzl     bug corrected
 *  1    Gandalf   1.0         7/29/99  Jaroslav Tulach 
 * $
 */














