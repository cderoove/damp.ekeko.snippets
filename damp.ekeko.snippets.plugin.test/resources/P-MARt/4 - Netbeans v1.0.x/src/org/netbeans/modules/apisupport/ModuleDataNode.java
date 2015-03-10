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

package org.netbeans.modules.apisupport;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.jar.*;
import javax.swing.SwingUtilities;
import javax.swing.event.*;

import org.openide.loaders.*;
import org.openide.modules.ModuleDescription;
import org.openide.nodes.*;
import org.openide.util.*;

import org.netbeans.modules.jarpackager.*;

public class ModuleDataNode extends JarDataObject.JarNode {

    private ChangeListener list, list2;

    public ModuleDataNode(ModuleDataObject obj) {
        this (obj, new ManifestProvider.CategoryChildren ((ManifestProvider) obj.getCookie (ManifestProvider.class)));
    }

    public ModuleDataNode(ModuleDataObject obj, Children ch) {
        super (obj, ch);
        ManifestProvider.Util.updateName (this);
        ManifestProvider provider = (ManifestProvider) getCookie (ManifestProvider.class);
        provider.addChangeListener (WeakListener.change (list2 = new ChangeListener () {
                                        public void stateChanged (ChangeEvent ev) {
                                            updateIcon ();
                                        }
                                    }, provider));
        updateIcon ();
    }

    private void updateIcon () {
        if (SwingUtilities.isEventDispatchThread ()) {
            RequestProcessor.postRequest (new Runnable () {
                                              public void run () {
                                                  updateIcon ();
                                              }
                                          });
            return;
        }
        if (ManifestProvider.Util.checkForException ((ManifestProvider) getCookie (ManifestProvider.class)) == null)
            setIconBase ("/org/netbeans/modules/apisupport/resources/ModuleDataIcon");
        else
            setIconBase ("/org/netbeans/modules/apisupport/resources/ModuleDataIconError");
    }

    protected Sheet createSheet () {
        Sheet sheet = super.createSheet ();
        ManifestProvider provider = (ManifestProvider) getCookie (ManifestProvider.class);
        ManifestProvider.Util.addToSheet (sheet, provider);
        provider.addChangeListener (WeakListener.change (list = new ChangeListener () {
                                        public void stateChanged (ChangeEvent ev) {
                                            fPC ();
                                        }
                                    }, provider));
        return sheet;
    }

    private void fPC () {
        firePropertyChange (null, null, null);
    }

}

/*
 * Log
 *  9    Gandalf-post-FCS1.7.1.0     4/16/00  Jesse Glick     Hopefully avoiding a 
 *       deadlock after reloading a manifest file, and some other 
 *       threading-related stuff.
 *  8    Gandalf   1.7         1/26/00  Jesse Glick     Live manifest parsing.
 *  7    Gandalf   1.6         1/26/00  Jesse Glick     Manifest handling 
 *       changed--now more dynamic, synched properly with open document as for 
 *       real file types.
 *  6    Gandalf   1.5         1/22/00  Jesse Glick     Manifest files can now 
 *       be recognized, not just JARs.
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         9/30/99  Jesse Glick     Package rename and misc.
 *  3    Gandalf   1.2         9/20/99  Jesse Glick     CategoryChildren public,
 *       mostly for testing.
 *  2    Gandalf   1.1         9/20/99  Jesse Glick     New resources package.
 *  1    Gandalf   1.0         9/17/99  Jesse Glick     
 * $
 */
