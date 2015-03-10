/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties;

import java.util.ResourceBundle;
import java.io.IOException;
import java.awt.Dialog;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import javax.swing.JEditorPane;
import javax.swing.text.Document;
import javax.swing.text.BadLocationException;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;
import org.openide.cookies.SourceCookie;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;
import org.openide.NotifyDescriptor;
import org.openide.DialogDescriptor;
import org.openide.TopManager;

/**
* Insert localized string at caret.
*
* @author   Petr Jiricka
*/
public class InsertLocalStringAction extends CookieAction {

    static final long serialVersionUID =-7002111874047983222L;
    /** Actually performs InsertLocalStringAction
    * @param activatedNodes Currently activated nodes.
    */
    public void performAction (final Node[] activatedNodes) {
        /*javax.swing.text.Keymap km= TopManager.getDefault ().getGlobalKeymap ();
        javax.swing.KeyStroke str = org.openide.util.Utilities.stringToKey ("C-I");
        System.out.println("keystroke " + str);
        System.out.println(km.getAction(str));*/


        final EditorCookie ec = (EditorCookie)(activatedNodes[0]).getCookie(EditorCookie.class);
        if (ec == null) return;

        DataObject dobj = (DataObject)(activatedNodes[0]).getCookie(DataObject.class);
        if (dobj == null) return;

        final Dialog[] dial = new Dialog[1];
        final int position = ec.getOpenedPanes()[0].getCaret().getDot();
        final Document doc = ec.getDocument();

        final ResourceBundleStringEditor editor = new ResourceBundleStringEditor();
        editor.setClassName(dobj.getPrimaryFile().getName());
        ResourceBundleString rbs = (ResourceBundleString)editor.getValue();
        final ResourceBundlePanel rbPanel = (ResourceBundlePanel)editor.getCustomEditor();
        rbPanel.setValue(rbs);
        DialogDescriptor dd = new DialogDescriptor(
                                  rbPanel,
                                  NbBundle.getBundle(InsertLocalStringAction.class).getString("CTL_Insert_localized_string"),
                                  true,
                                  DialogDescriptor.OK_CANCEL_OPTION,
                                  DialogDescriptor.OK_OPTION,
                                  new ActionListener() {
                                      public void actionPerformed(ActionEvent ev) {
                                          if (ev.getSource() == DialogDescriptor.OK_OPTION) {
                                              try {
                                                  ResourceBundleString newRbs = (ResourceBundleString)rbPanel.getPropertyValue();
                                                  editor.setValue(newRbs);
                                                  doc.insertString(position, editor.getJavaInitializationString(), null);
                                                  dial[0].setVisible(false);
                                                  dial[0].dispose();
                                              }
                                              catch (IllegalStateException e) {
                                                  NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                                                                                     NbBundle.getBundle(InsertLocalStringAction.class).getString("EXC_BadKey"),
                                                                                     NotifyDescriptor.ERROR_MESSAGE);
                                                  TopManager.getDefault().notify(msg);
                                              }
                                              catch (BadLocationException e) {
                                                  // probably org.netbeans.editor.GuardedException
                                                  NotifyDescriptor.Message msg = new NotifyDescriptor.Message(
                                                                                     NbBundle.getBundle(InsertLocalStringAction.class).getString("EXC_GuardedBlock"),
                                                                                     NotifyDescriptor.ERROR_MESSAGE);
                                                  TopManager.getDefault().notify(msg);
                                                  /*if (Boolean.getBoolean("netbeans.debug.exceptions"))
                                                    e.printStackTrace ();*/
                                              }
                                          } else if (ev.getSource() == DialogDescriptor.CANCEL_OPTION) {
                                              dial[0].setVisible(false);
                                              dial[0].dispose();
                                          }
                                      }
                                  }
                              );
        dial[0] = TopManager.getDefault().createDialog(dd);
        dial[0].show();
    }


    protected boolean enable(Node[] activatedNodes) {
        if (!super.enable(activatedNodes))
            return false;
        // must be a dataobject
        DataObject dobj = (DataObject)(activatedNodes[0]).getCookie(DataObject.class);
        if (dobj == null) return false;
        // must have an open editor pane
        final EditorCookie ec = (EditorCookie)(activatedNodes[0]).getCookie(EditorCookie.class);
        if (ec == null) return false;
        JEditorPane edits[] = ec.getOpenedPanes();
        if (edits == null || edits.length == 0)
            return false;

        // must not be in a guarded block
        int position = ec.getOpenedPanes()[0].getCaret().getDot();
        Document doc = ec.getDocument();
        // PENDING

        return true;
    }

    /**
    * Returns MODE_EXACTLY_ONE.
    */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /**
    * Returns ThreadCookie
    */
    protected Class[] cookieClasses () {
        return new Class [] {
                   SourceCookie.class
               };
    }

    /** @return the action's icon */
    public String getName() {
        return org.openide.util.NbBundle.getBundle(InsertLocalStringAction.class).getString("CTL_Insert_localized_string");
        //return NbBundle.getBundle (InsertLocalStringAction.class).getString ("CTL_InsertLocalStringAction");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (InsertLocalStringAction.class);
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "/org/netbeans/modules/properties/insertLocalStringAction.gif";
    }
}

/*
 * <<Log>>
 *  5    Gandalf   1.4         11/27/99 Patrik Knakal   
 *  4    Gandalf   1.3         11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  3    Gandalf   1.2         11/4/99  Petr Jiricka    Fixed bug #4628
 *  2    Gandalf   1.1         11/4/99  Petr Jiricka    Minor changes with a bug
 *       concerning guarded sections
 *  1    Gandalf   1.0         10/25/99 Petr Jiricka    
 * $
 */
