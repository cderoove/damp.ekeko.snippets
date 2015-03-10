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

package org.netbeans.modules.web.core.syntax;

import javax.swing.JEditorPane;

import org.openide.TopManager;
import org.openide.options.SystemOption;
import org.openide.text.PrintSettings;
import org.openide.filesystems.FileUtil;

import org.netbeans.modules.editor.options.AllOptions;
import org.netbeans.modules.editor.NbLocalizer;
import org.netbeans.editor.Settings;
import org.netbeans.editor.LocaleSupport;

import org.netbeans.modules.web.core.jsploader.JspLoader;

/**
 * @author Petr Jiricka
 */
public class RestoreColoring {

    public void addInitializer () {
        Settings.addInitializer (new JspMultiSettings());
        //Settings.addInitializer (new JSPSettings());

        // Registration of the editor kits to JEditorPane
        JEditorPane.registerEditorKitForContentType
        (JspLoader.JSP_MIME_TYPE,
         "org.netbeans.modules.web.core.syntax.JSPKit", // NOI18N
         this.getClass().getClassLoader());

        // add the localizer
        LocaleSupport.addLocalizer(new NbLocalizer(RestoreColoring.class));
    }


    public void installOptions () {
        AllOptions ao = (AllOptions)AllOptions.findObject (AllOptions.class, true);
        ao.addOption ((JSPOptions)SystemOption.findObject(JSPOptions.class, true));
        PrintSettings ps = (PrintSettings)PrintSettings.findObject (PrintSettings.class, true);
        ps.addOption ((JSPPrintOptions)SystemOption.findObject(JSPPrintOptions.class, true));
    }

    public void uninstallOptions () {
        AllOptions ao = (AllOptions)AllOptions.findObject (AllOptions.class, true);
        JSPOptions jspo = (JSPOptions)SystemOption.findObject(JSPOptions.class, false);
        if (jspo != null) ao.removeOption (jspo);
        PrintSettings ps = (PrintSettings)PrintSettings.findObject (PrintSettings.class, true);
        JSPPrintOptions jsppo = (JSPPrintOptions)SystemOption.findObject(JSPPrintOptions.class, false);
        if (jsppo != null) ps.removeOption (jsppo);
    }

} // end of clas RestoreColoring

/*
 * Log
 *  7    Gandalf-post-FCS1.4.2.1     4/6/00   Petr Jiricka    Debug message removed.
 *  6    Gandalf-post-FCS1.4.2.0     4/5/00   Petr Jiricka    Token names and examples
 *       from bundles.
 *  5    Gandalf   1.4         2/10/00  Petr Jiricka    Delegating to the new 
 *       syntax implmentation.
 *  4    Gandalf   1.3         1/17/00  Petr Jiricka    Fixed bug : Coloring 
 *       options not uninstalled when uninstalling the module.
 *  3    Gandalf   1.2         1/12/00  Petr Jiricka    I18N
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         9/13/99  Petr Jiricka    
 * $
 */
