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

package org.netbeans.examples.modules.microed;

import org.openide.modules.ModuleInstall;

/** Installer class for micro-editor.
* @author Jesse Glick
* @version Date
*/
public class Install extends ModuleInstall {
    // Settings handles all the details.
    public void installed () {
        restored ();
    }
    public void restored () {
        Settings.startRunning ();
        Settings.installAll ();
    }
    public void uninstalled () {
        Settings.uninstallAll ();
    }
    public boolean closing () {
        return true;
    }
}
