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

package org.netbeans.beaninfo;


import java.awt.*;
import java.awt.event.*;
import java.beans.*;
import java.io.File;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

import org.openide.TopManager;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.explorer.propertysheet.editors.FileOnlyEditor;

/** Editor and filter for jar and zip files.
*
* @author Jaroslav Tulach
*/
public class JarFileEditor extends FileOnlyEditor {
    /** Adds filter.
    */
    protected JFileChooser createFileChooser () {
        JFileChooser fc = super.createFileChooser ();
        fc.addChoosableFileFilter (new JarAndZipFilter ());
        return fc;
    }

    private static class JarAndZipFilter extends FileFilter {
        public boolean accept(File f) {
            if (f.isDirectory ()) return true;

            String s = f.toString ();
            return s.endsWith (".jar") || s.endsWith (".zip"); // NOI18N
        }

        public String getDescription() {
            java.util.ResourceBundle bundle = NbBundle.getBundle(JarFileEditor.class);


            return bundle.getString("LAB_JarFileFilter");
        }
    }

    protected HelpCtx getHelpCtx () {
        return new HelpCtx (JarFileEditor.class);
    }

}

/*
* Log
*  4    Gandalf   1.3         1/13/00  Jaroslav Tulach I18N
*  3    Gandalf   1.2         11/5/99  Jesse Glick     Context help jumbo patch.
*  2    Gandalf   1.1         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  1    Gandalf   1.0         9/20/99  Jaroslav Tulach 
* $
*/