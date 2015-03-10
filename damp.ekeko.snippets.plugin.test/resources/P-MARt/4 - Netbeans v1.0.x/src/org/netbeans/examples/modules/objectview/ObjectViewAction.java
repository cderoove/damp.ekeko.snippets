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

package org.netbeans.examples.modules.objectview;

import org.openide.loaders.DataFolder;
import org.openide.util.NbBundle;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;

/** Action that is enabled on any folder and if selected opens
* a new Explorer with a simple version of the Object Browser.
*
* @author Jaroslav Tulach
*/
public class ObjectViewAction extends CookieAction {

    static final long serialVersionUID =-1806677279700987049L;
    /** Create. new ObjectViewAction. */
    public ObjectViewAction () {
    }

    /** Accept folders. */
    protected Class[] cookieClasses () {
        return new Class[] { DataFolder.class };
    }

    /** Activated on only one folder. */
    protected int mode () {
        return MODE_EXACTLY_ONE;
    }

    /** Name of the action. */
    public String getName () {
        return NbBundle.getBundle (ObjectViewAction.class).getString ("CTL_ObjectViewAction");
    }

    /** No help yet. */
    public HelpCtx getHelpCtx () {
        return HelpCtx.DEFAULT_HELP;
    }

    /** Open the view.
    * @param arr this should contain one FolderNode, e.g.
    */
    public void performAction (Node[] arr) {
        DataFolder df = (DataFolder)arr[0].getCookie (DataFolder.class);
        if (df != null) {
            ObjectView.explore (df);
        }
    }
}
