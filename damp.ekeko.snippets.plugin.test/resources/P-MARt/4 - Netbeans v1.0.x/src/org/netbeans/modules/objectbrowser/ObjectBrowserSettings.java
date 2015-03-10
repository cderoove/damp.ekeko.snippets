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

package org.netbeans.modules.objectbrowser;

import org.openide.options.SystemOption;
import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;

/**
* Settings for Object browser.
*
* @author Jan Jancura
*/
public class ObjectBrowserSettings extends SystemOption {

    // static .....................................................................................


    static PackagesFilter filter = new PackagesFilter ();


    static final long serialVersionUID =6759955426620832265L;
    /**
    * Returns name of this setings.
    */
    public String displayName () {
        return NbBundle.getBundle (ObjectBrowserSettings.class).getString ("CTL_Object_browser_option");
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (ObjectBrowserSettings.class);
    }

    //is project specific
    public boolean isGlobal() {
        return false;
    }

    // properties .................................................................................

    /**
    * Getter method for bootClassPath property.
    */
    public PackagesFilter getPackageFilter () {
        return filter;
    }

    /**
    * Setter method for bootClassPath property.
    */
    public void setPackageFilter (PackagesFilter filter) {
        PackagesFilter old = this.filter;
        this.filter = filter;
        firePropertyChange ("packageFilter", old, filter); // NOI18N
    }
}

/*
* Log
*  6    Gandalf   1.5         1/14/00  Radko Najman    isGlobal() method added
*  5    Gandalf   1.4         1/13/00  Radko Najman    I18N
*  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  3    Gandalf   1.2         8/9/99   Ian Formanek    Generated Serial Version 
*       UID
*  2    Gandalf   1.1         7/2/99   Jesse Glick     More help IDs.
*  1    Gandalf   1.0         6/10/99  Jan Jancura     
* $
*/
