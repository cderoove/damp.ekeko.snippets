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

import javax.swing.SwingUtilities;
import javax.swing.event.*;

import org.openide.actions.OpenAction;
import org.openide.loaders.*;
import org.openide.nodes.*;
import org.openide.util.*;
import org.openide.util.actions.SystemAction;

public class ManifestDataNode extends DataNode {

    private ChangeListener list, list2;

    public ManifestDataNode(ManifestDataObject obj) {
        this (obj, new ManifestProvider.CategoryChildren ((ManifestProvider) obj.getCookie (ManifestProvider.class)));
    }

    public ManifestDataNode(ManifestDataObject obj,Children ch) {
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
        if (ManifestProvider.Util.checkForException ((ManifestProvider) getCookie (ManifestProvider.class)) == null) {
            //System.err.println("icon: ok");
            setIconBase ("/org/netbeans/modules/apisupport/resources/ManifestDataIcon");
        } else {
            //System.err.println("icon: bad");
            setIconBase ("/org/netbeans/modules/apisupport/resources/ManifestDataIconError");
        }
    }

    protected Sheet createSheet () {
        Sheet sheet = super.createSheet ();
        Sheet.Set set = sheet.get (ExecSupport.PROP_EXECUTION);
        if (set == null) {
            set = new Sheet.Set ();
            set.setName (ExecSupport.PROP_EXECUTION);
            set.setDisplayName ("Execution");
        }
        ((ExecSupport) getCookie (ExecSupport.class)).addProperties (set);
        set.remove (ExecSupport.PROP_FILE_PARAMS);
        set.remove (ExecSupport.PROP_DEBUGGER_TYPE);
        sheet.put (set);
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

    public SystemAction getDefaultAction () {
        return SystemAction.get (OpenAction.class);
    }

}

/*
 * Log
 *  4    Gandalf-post-FCS1.2.1.0     4/16/00  Jesse Glick     Hopefully avoiding a 
 *       deadlock after reloading a manifest file, and some other 
 *       threading-related stuff.
 *  3    Gandalf   1.2         1/26/00  Jesse Glick     Live manifest parsing.
 *  2    Gandalf   1.1         1/26/00  Jesse Glick     Manifest handling 
 *       changed--now more dynamic, synched properly with open document as for 
 *       real file types.
 *  1    Gandalf   1.0         1/22/00  Jesse Glick     
 * $
 */
