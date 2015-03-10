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

package org.netbeans.examples.modules.minicomposer;
import java.io.IOException;
import org.openide.TopManager;
import org.openide.filesystems.FileUtil;
import org.openide.modules.ModuleInstall;
import org.openide.util.NbBundle;
public class ComposerModule extends ModuleInstall {
    private static final long serialVersionUID =4258506853051142562L;
    public void installed () {
        try {
            FileUtil.extractJar (TopManager.getDefault ().getPlaces ().folders ().templates ().getPrimaryFile (),
                                 NbBundle.getLocalizedFile ("org.netbeans.examples.modules.minicomposer.templates", "jar").openStream ());
        } catch (IOException ioe) {
            TopManager.getDefault ().notifyException (ioe);
        }
        restored ();
    }
}
