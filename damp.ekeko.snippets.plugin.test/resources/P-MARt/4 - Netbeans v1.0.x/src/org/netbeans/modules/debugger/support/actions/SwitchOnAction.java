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

package org.netbeans.modules.debugger.support.actions;

import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.CookieAction;
import org.openide.nodes.Node;

import org.netbeans.modules.debugger.support.nodes.ThreadNode;


/**
* Switch on action.
* This class is final only for performance reasons, can be
* happily unfinaled if desired.
*
* @author   Jan Jancura, Dafe Simonek
*/
public final class SwitchOnAction extends CookieAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = -7764852450085036874L;

    private String name = NbBundle.getBundle (SwitchOnAction.class).
                          getString ("CTL_SwitchOn");


    /** Actually performs the SwitchOn action.
    * @param activatedNodes Currently activated nodes.
    */
    public void performAction (final Node[] activatedNodes) {
        SwitchOnCookie tc = (SwitchOnCookie) activatedNodes [0].getCookie (SwitchOnCookie.class);
        tc.setCurrent ();
    }

    /** Manages enable - disable logic of this action
    *
    * @param activatedNodes Currently activated nodes.
    */
    protected boolean enable (Node[] activatedNodes) {
        if (!super.enable (activatedNodes)) return false;
        SwitchOnCookie tc = (SwitchOnCookie) activatedNodes [0].getCookie (SwitchOnCookie.class);
        return tc.canSetCurrent ();
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
                   SwitchOnCookie.class
               };
    }

    /** @return the action's name */
    public String getName () {
        return name;
    }

    /** Sets the action's name */
    public void setName (String name) {
        this.name = name;
    }

    /** @return the action's help context */
    public HelpCtx getHelpCtx () {
        return new HelpCtx (SwitchOnAction.class);
    }

    /** Resource name for the icon.
    * @return resource name
    */
    protected String iconResource () {
        return "/org/netbeans/core/resources/actions/switchOn.gif"; // NOI18N
    }

}

/*
 * Log
 */
