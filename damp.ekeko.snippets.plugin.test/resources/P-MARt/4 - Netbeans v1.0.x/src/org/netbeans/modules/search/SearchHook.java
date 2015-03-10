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

package org.netbeans.modules.search;

import java.beans.*;

import org.openide.actions.*;
import org.openide.util.actions.*;
import org.openide.explorer.*;
import org.openide.windows.*;

/**
 * Hooks search presenter to FindAction.
 *
 * Listens at TopComponent.Registry on CURRENT_NODES property.
 * On change evaluates all registered search types and
 * if some of them is enabled enable the Find action.
 *
 * @author  Petr Kuzel
 * @version 1.0
 */
public class SearchHook implements PropertyChangeListener {

    private SearchPerformer performer;

    private final boolean TRACE = false;

    /** Creates new SearchHook
    * @param performer knows if search type is enabled and so on. 
    * If null is passed hook() does nothing
    */
    public SearchHook(SearchPerformer performer) {
        this.performer = performer;
    }

    /** Hook performer at FindAction.
    * Conditionally hooks performer to FindAction.
    * Condition: active top component is ExplorerManager.Provider AND
    * some criteria is enabled be current nodes
    */
    public void hook() {
        TopComponent.getRegistry().addPropertyChangeListener(this);
    }

    /**
    * Finnish hooking job. After invoking is this class useless.
    */
    public void unhook() {
        performer = null;
        hook();
        TopComponent.getRegistry().removePropertyChangeListener(this);
    }

    /** Determine if actvated topcomponent represent Editor
    * if not call overwriting routine.
    * It prevents battle about FindAction performer.
    */
    private void someoneActivated() {
        TopComponent activated = TopComponent.getRegistry().getActivated();

        if (! (activated instanceof org.openide.text.EditorSupport.Editor) ) {
            overwriteFindPerformer();
        }
    }

    /** Set performer as performer if no performer is set.
    */
    private void overwriteFindPerformer() {
        CallbackSystemAction action = (CallbackSystemAction) SystemAction.get(FindAction.class);

        // hook target does not exist
        if ( action == null ) {
            throw new RuntimeException("Should not happen: Cannot locate FindAction."); // NOI18N
        }

        // handle deinstalation
        if (performer == null) {
            action.setActionPerformer(null);
            return;
        }

        Object currPerformer = action.getActionPerformer();

        /* proposed code
        SearchPerformer newp = null;
        if ( performer.enabled( TopComponent.getRegistry().getCurrentNodes() ) ) 
          newp = performer;

        ...

        action.setActionPerformer(newp);
        */

        if (currPerformer == null || currPerformer.getClass().equals(performer.getClass())) {
            if ( performer.enabled( TopComponent.getRegistry().getCurrentNodes() ) ) {
                action.setActionPerformer(performer);
            } else {
                action.setActionPerformer(null);
            }
        } else {
            // editor why do you have set performer?
            // you should not
            // [TODO] overwrite it unconditionally
            t("Ha, there is a performer: " + action.getActionPerformer()); // NOI18N
        }

    }


    /** Be interested in Current_nodes property change.
    */
    public void propertyChange(final java.beans.PropertyChangeEvent p1) {
        if (p1.getPropertyName() == TopComponent.Registry.PROP_CURRENT_NODES ) {

            // PREVENT deadlock
            // do not depend on locking order of
            // CookieSet[.add/remove()] and TopComponent[.attach()]
            org.openide.util.RequestProcessor.postRequest( new Runnable() {
                        public void run() {
                            someoneActivated();
                        }
                    });

        }
    }

    /**
    * @param args the command line arguments
    */
    public static void main (String args[]) {
        SearchHook me = new SearchHook(new SearchPerformer());
        me.hook();
    }

    /** TRACE code */
    private void t(String msg) {
        if (TRACE)
            System.err.println("ResultV: " + msg);
    }

}


/*
* Log
*  9    Gandalf   1.8         2/28/00  Petr Kuzel      Deadlock on TopComponent 
*       and CookieSet eliminated.
*  8    Gandalf   1.7         1/13/00  Radko Najman    I18N
*  7    Gandalf   1.6         1/5/00   Petr Kuzel      Margins used. Help 
*       contexts.
*  6    Gandalf   1.5         12/23/99 Petr Kuzel      Architecture improved.
*  5    Gandalf   1.4         12/17/99 Petr Kuzel      Bundling.
*  4    Gandalf   1.3         12/16/99 Petr Kuzel      
*  3    Gandalf   1.2         12/15/99 Petr Kuzel      
*  2    Gandalf   1.1         12/14/99 Petr Kuzel      Minor enhancements
*  1    Gandalf   1.0         12/14/99 Petr Kuzel      
* $ 
*/ 

