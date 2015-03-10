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

package org.netbeans.modules.i18n;

import java.util.ResourceBundle;
import java.io.IOException;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.CookieAction;
import org.openide.cookies.SourceCookie;
import org.openide.cookies.EditorCookie;
import org.openide.nodes.Node;
import org.openide.loaders.DataObject;
import org.openide.NotifyDescriptor;
import org.openide.TopManager;

import org.netbeans.editor.BaseDocument;

/**
* Localize action. Dependent on the editor module and the form module.
*
* @author   Petr Jiricka
*/
public class LocalizeAction extends CookieAction {

    static final long serialVersionUID =3322896507302889271L;
    /** Actually performs the SwitchOn action.
    * @param activatedNodes Currently activated nodes.
    */
    public void performAction (final Node[] activatedNodes) {

        final EditorCookie ec = (EditorCookie)(activatedNodes[0]).getCookie(EditorCookie.class);
        if (ec == null)
            return;  // PENDING

        ec.open();
        // wait until the component is selected and focused
        RequestProcessor.postRequest(new Runnable() {
                                         public void run() {
                                             DataObject obj = (DataObject)(activatedNodes[0]).getCookie(DataObject.class);
                                             if (ec.getDocument() instanceof BaseDocument)
                                                 LocalizeSupport.getLocalizeSupport().localize((BaseDocument)ec.getDocument(), obj);
                                         }
                                     });
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
        return NbBundle.getBundle (LocalizeAction.class).getString ("CTL_LocalizeAction");
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (LocalizeAction.class);
    }

    /** The action's icon location.
    * @return the action's icon location
    */
    protected String iconResource () {
        return "/org/netbeans/modules/i18n/localizeAction.gif";
    }
}

/*
 * <<Log>>
 *  4    Gandalf   1.3         11/27/99 Patrik Knakal   
 *  3    Gandalf   1.2         11/5/99  Jesse Glick     Context help jumbo 
 *       patch.
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/2/99   Petr Jiricka    
 * $
 */
