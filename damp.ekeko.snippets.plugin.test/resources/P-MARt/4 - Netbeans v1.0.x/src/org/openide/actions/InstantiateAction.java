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

package org.openide.actions;

import java.io.IOException;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.util.ResourceBundle;

import javax.swing.*;

import org.openide.*;
import org.openide.filesystems.FileObject;
import org.openide.loaders.*;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.util.actions.SystemAction;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.UserCancelException;
import org.openide.util.NbBundle;

/** Instantiate a template.
* Enabled only when there is one selected node and
* it represents a data object satisfying {@link DataObject#isTemplate}.
*
* @author   Jaroslav Tulach
*/
public class InstantiateAction extends NodeAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 1482795804240508824L;

    protected boolean enable (Node[] activatedNodes) {
        if (activatedNodes.length != 1) return false;
        DataObject obj = (DataObject)activatedNodes[0].getCookie (DataObject.class);
        return obj != null && obj.isTemplate ();
    }

    protected void performAction (Node[] activatedNodes) {
        DataObject obj = (DataObject)activatedNodes[0].getCookie (DataObject.class);
        if (obj != null && obj.isTemplate ()) {
            try {
                instantiateTemplate (obj);
            } catch (UserCancelException ex) {
                // canceled by user
                // do not notify the exception
            } catch (IOException ex) {
                TopManager.getDefault ().notifyException (ex);
            }
        }
    }

    /* @return the name of the action
    */
    public String getName () {
        return ""; // NOI18N
    }

    public HelpCtx getHelpCtx () {
        return new HelpCtx (InstantiateAction.class);
    }

    /** Instantiate a template object.
    * Asks user for the target file's folder and creates the file.
    * Then runs the node delegate's {@link TopManager.NodeOperation#customize customizer} (if there is one).
    * Also the node's {@link Node#getDefaultAction default action}, if any, is run.
    * @param obj the template to use
    * @return set of created objects or null if user canceled the action
    * @exception IOException on I/O error
    * @see DataObject#createFromTemplate
    */
    public static java.util.Set instantiateTemplate (DataObject obj)
    throws IOException {
        // Create component for for file name input
        return NewTemplateAction.getWizard ().instantiate (obj);
    }
}

/*
 * Log
 *  20   Gandalf   1.19        1/12/00  Ian Formanek    NOI18N
 *  19   Gandalf   1.18        11/24/99 Jaroslav Tulach New "New From Template" 
 *       Dialog
 *  18   Gandalf   1.17        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  17   Gandalf   1.16        7/21/99  Ian Formanek    Creating subpackages 
 *       from name only for Folders and SourceCookie providers
 *  16   Gandalf   1.15        7/20/99  Ian Formanek    Creation of new objects 
 *       enhanced with creation of inter-folders
 *  15   Gandalf   1.14        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  14   Gandalf   1.13        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  13   Gandalf   1.12        5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  12   Gandalf   1.11        5/7/99   Jesse Glick     [JavaDoc]
 *  11   Gandalf   1.10        5/6/99   Jaroslav Tulach Run default action code 
 *       from DataObject to InstantiateAction
 *  10   Gandalf   1.9         4/27/99  Jesse Glick     new HelpCtx () -> 
 *       HelpCtx.DEFAULT_HELP.
 *  9    Gandalf   1.8         3/29/99  Jaroslav Tulach Cancel in Ctrl+N dialog 
 *       works.
 *  8    Gandalf   1.7         3/29/99  Jaroslav Tulach places ().nodes 
 *       ().session ()
 *  7    Gandalf   1.6         3/26/99  Jesse Glick     [JavaDoc]
 *  6    Gandalf   1.5         3/26/99  Ian Formanek    Fixed use of obsoleted 
 *       NbBundle.getBundle (this)
 *  5    Gandalf   1.4         3/22/99  Jaroslav Tulach Fixed creation from 
 *       template
 *  4    Gandalf   1.3         3/8/99   Jaroslav Tulach Bundles
 *  3    Gandalf   1.2         3/8/99   Jan Jancura     Bundle moved
 *  2    Gandalf   1.1         1/6/99   Jaroslav Tulach ide.* extended to 
 *       ide.loaders.*
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
