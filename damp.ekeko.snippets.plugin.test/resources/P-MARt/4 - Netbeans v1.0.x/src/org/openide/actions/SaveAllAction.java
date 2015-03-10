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

import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.openide.TopManager;
import org.openide.loaders.DataObject;
import org.openide.util.HelpCtx;
import org.openide.util.WeakListener;
import org.openide.util.actions.CallableSystemAction;

/** Save all open objects.
* This class is final only for performance reasons.
* @see DataObject#getRegistry
* @see TopManager#saveAll
*
* @author   Jan Jancura, Ian Formanek
*/
public final class SaveAllAction extends CallableSystemAction {
    /** generated Serialized Version UID */
    static final long serialVersionUID = 7654588299199250931L;

    private static final String MODIFIED_LIST = "ModifiedList"; // NOI18N

    /** Reference to the change listener
    * (we treat it weakly, so we have to to prevent it from
    * being finalized before finalization of this action) */
    private ChangeListener chl;

    /* Creates new HashMap and inserts some properties to it.
    * @return the hash map
    */
    protected void initialize () {
        super.initialize ();
        // false by default
        putProperty (PROP_ENABLED, Boolean.FALSE);
        // listen to the changes
        chl = new ModifiedListL();
        DataObject.getRegistry().addChangeListener(
            (ChangeListener)(WeakListener.change(chl, DataObject.getRegistry ())));
    }

    /* Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return ActionConstants.BUNDLE.getString("SaveAll");
    }

    /* Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx (SaveAllAction.class);
    }

    /* Icon resource.
    * @return name of resource for icon
    */
    protected String iconResource () {
        return "/org/openide/resources/actions/saveAll.gif"; // NOI18N
    }

    /* Saves all. */
    public void performAction() {
        TopManager.getDefault().saveAll();
    }

    /* Listens to the chnages in list of modified data objects
    * and enables / disables this action appropriately */
    final class ModifiedListL implements ChangeListener {
        public void stateChanged(final ChangeEvent evt) {
            setEnabled(((java.util.Set)evt.getSource()).size() > 0);
        }
    } // end of ModifiedListL inner class

}

/*
 * Log
 *  18   Gandalf   1.17        1/12/00  Ian Formanek    NOI18N
 *  17   Gandalf   1.16        11/5/99  Jaroslav Tulach WeakListener has now 
 *       registration methods.
 *  16   Gandalf   1.15        10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  15   Gandalf   1.14        6/24/99  Jesse Glick     Gosh-honest HelpID's.
 *  14   Gandalf   1.13        6/9/99   Ian Formanek    Fixed resources for 
 *       package change
 *  13   Gandalf   1.12        6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  12   Gandalf   1.11        5/28/99  Ian Formanek    Cleaned up source 
 *       (imports, ... - no semantic/english text change)
 *  11   Gandalf   1.10        5/2/99   Ian Formanek    Fixed last change
 *  10   Gandalf   1.9         5/2/99   Ian Formanek    Obsoleted 
 *       help->DEFAULT_HELP
 *  9    Gandalf   1.8         3/26/99  Jesse Glick     [JavaDoc]
 *  8    Gandalf   1.7         2/17/99  Ian Formanek    Updated icons to point 
 *       to the right package (under ide/resources)
 *  7    Gandalf   1.6         2/16/99  David Simonek   
 *  6    Gandalf   1.5         1/15/99  Jaroslav Tulach 
 *  5    Gandalf   1.4         1/15/99  David Simonek   
 *  4    Gandalf   1.3         1/14/99  David Simonek   
 *  3    Gandalf   1.2         1/13/99  David Simonek   
 *  2    Gandalf   1.1         1/7/99   Ian Formanek    fixed resource names
 *  1    Gandalf   1.0         1/5/99   Ian Formanek    
 * $
 */
