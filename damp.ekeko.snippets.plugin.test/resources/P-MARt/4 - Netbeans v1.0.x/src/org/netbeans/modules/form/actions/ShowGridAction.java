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

package org.netbeans.modules.form.actions;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

import org.openide.util.HelpCtx;
import org.openide.util.actions.BooleanStateAction;
import org.netbeans.modules.form.FormLoaderSettings;

/** ShowGridAction action.
*
* @author   Ian Formanek
*/
public class ShowGridAction extends BooleanStateAction {
    /** generated Serialized Version UID */
    //  static final long serialVersionUID = 3632673737273451161L;

    /** The help context of this action */
    private static HelpCtx help;
    /** The FormLoaderSettings instance */
    private static FormLoaderSettings formSettings = new FormLoaderSettings ();
    /** Listener listening on the ShowGrid property changes on FormLoaderSettings */
    private static PropertyChangeListener listener;

    static final long serialVersionUID =2323751239523183933L;
    /** Creates a new ShowGridAction */
    public ShowGridAction () {
        internalChange = true;
        setBooleanState (formSettings.getShowGrid ());
        internalChange = false;
        if (listener == null) {
            listener = new PropertyChangeListener () {
                           public void propertyChange (PropertyChangeEvent evt) {
                               if (FormLoaderSettings.PROP_SHOW_GRID.equals (evt.getPropertyName ())) {
                                   if (internalSettingsChange) return;
                                   internalChange = true;
                                   setBooleanState (formSettings.getShowGrid ());
                                   internalChange = false;
                               }
                           }
                       };
            formSettings.addPropertyChangeListener (listener);
        }
    }
    /** Human presentable name of the action. This should be
    * presented as an item in a menu.
    * @return the name of the action
    */
    public String getName() {
        return org.openide.util.NbBundle.getBundle (ShowGridAction.class).getString ("ACT_ShowGrid");
    }

    /** Help context where to find more about the action.
    * @return the help context for this action
    */
    public HelpCtx getHelpCtx() {
        return new HelpCtx(ShowGridAction.class);
    }

    /** @return resource for the action icon */
    protected String iconResource () {
        return "/org/netbeans/modules/form/resources/showGrid.gif"; // NOI18N
    }

    /** Setter for the BooleanState property.
    * The state is propagated to all the action "invokers" so that they
    * can change their visual look according to the new setting and/or
    * perform any actions to reflect the state change
    * @param action The "external" part of the action which invoked th state change
    * @param value the new value of the BooleanState property
    */
    public void setBooleanState (boolean value) {
        super.setBooleanState (value);
        if (!internalChange) {
            internalSettingsChange = true;
            formSettings.setShowGrid (value);
            internalSettingsChange = false;
        }
    }

    /** Flags preventing infinite looping on change listening */
    private boolean internalChange = false;
    private boolean internalSettingsChange = false;

}

/*
 * Log
 *  7    Gandalf   1.6         1/5/00   Ian Formanek    NOI18N
 *  6    Gandalf   1.5         11/27/99 Patrik Knakal   
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         3/17/99  Ian Formanek    
 *  2    Gandalf   1.1         3/17/99  Ian Formanek    Added synchronization 
 *       FormSettings -> Action
 *  1    Gandalf   1.0         3/17/99  Ian Formanek    
 * $
 */
