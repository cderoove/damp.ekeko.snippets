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

package org.netbeans.modules.properties.syntax;

import javax.swing.JEditorPane;

import org.openide.TopManager;
import org.openide.options.SystemOption;
import org.openide.text.PrintSettings;
import org.openide.filesystems.FileUtil;

import org.netbeans.modules.editor.options.AllOptions;
import org.netbeans.editor.Settings;

import org.netbeans.modules.properties.PropertiesDataObject;

/**
 * @author Petr Jiricka, Libor Kramolis
 */
public class RestoreColoring {

    public void restore () {
        Settings.addInitializer (new PropertiesSettings());

        // Registration of the editor kits to JEditorPane
        JEditorPane.registerEditorKitForContentType
        (PropertiesDataObject.MIME_PROPERTIES,
         "org.netbeans.modules.properties.syntax.PropertiesKit",
         this.getClass().getClassLoader());

        AllOptions ao = (AllOptions)AllOptions.findObject (AllOptions.class, true);
        ao.addOption (new PropertiesOptions());
        PrintSettings ps = (PrintSettings)PrintSettings.findObject (PrintSettings.class, true);
        ps.addOption (new PropertiesPrintOptions());
    }
} // end of clas RestoreColoring

/*
 * <<Log>>
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/13/99  Petr Jiricka    
 * $
 */
