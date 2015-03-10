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

import org.openide.modules.ModuleInstall;

import org.netbeans.modules.form.FormPropertyEditorManager;

/** Installation class for the properties module
*
* @author Petr Jiricka
*/
public class PropertiesFormModule extends ModuleInstall {

    static final long serialVersionUID =1159160389747335713L;
    /** Creates a new multitab mode for the properties files */
    public void restored() {
        FormPropertyEditorManager.registerEditor (String.class, ResourceBundleStringFormEditor.class);
    }

}

/*
 * <<Log>>
 */
