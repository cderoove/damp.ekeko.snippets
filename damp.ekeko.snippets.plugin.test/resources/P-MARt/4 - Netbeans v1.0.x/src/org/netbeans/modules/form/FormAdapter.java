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

package org.netbeans.modules.form;

import org.openide.nodes.*;

/**
 *
 * @author  Ian Formanek
 */
public class FormAdapter implements FormListener {

    /** Called when the form is succesfully loaded and fully initialized
    */
    public void formLoaded () {
    }

    /** Called when a change is performed on the form that should lead to regeneration of initializer and variables declaration
    */
    public void formChanged () {
    }

    /** Called when a change is performed on the form that should lead to regeneration of initializer
    */
    public void codeChanged () {
    }

    /** Called when the form is about to be saved
    */
    public void formToBeSaved () {
    }

    /** Called when the order of components within their parent changes
    * @param cont the container on which the components were reordered
    */
    public void componentsReordered (ComponentContainer cont) {
    }

    /** Called when a new component is added to the form
    * @param comps the delta components
    */
    public void componentsAdded (RADComponent[] comps) {
    }

    /** Called when any component is removed from the form
    * @param comps the delta components
    */
    public void componentsRemoved (RADComponent[] comps) {
    }

    /** Called when any synthetic property of a component on the form is changed
    * The synthetic properties include: variableName, serialize, serializeName, generateGlobalVariable
    * @param evt the event object describing the event
    */
    public void componentChanged (FormPropertyEvent evt) {
    }

    /** Called when any bean property of a component on the form is changed
    * @param evt the event object describing the event
    */
    public void propertyChanged (FormPropertyEvent evt) {
    }

    /** Called when any layout property of specified component on given container changes
    * @param container the visual container on which layout the change happened
    * @param component the component which layout property changed or null if layout's own property changed
    * @param propertyName name of changed property
    * @param oldValue old value of changed property
    * @param newValue new value of changed property
    */
    public void layoutChanged (RADVisualContainer container, RADVisualComponent component, String propertyName, Object oldValue, Object newValue) {
    }

    /** Called when an event handler is added to a component on the form
    * @param evt the event object describing the event
    */
    public void eventAdded (FormEventEvent evt) {
    }

    /** Called when an event handler is added to a component on the form
    * @param evt the event object describing the event
    */
    public void eventRemoved (FormEventEvent evt) {
    }

    /** Called when an event handler is renamed on a component on the form
    * @param evt the event object describing the event
    */
    public void eventRenamed (FormEventEvent evt) {
    }
}

/*
 * Log
 *  4    Gandalf   1.3         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  3    Gandalf   1.2         9/29/99  Ian Formanek    codeChanged added to 
 *       FormListener
 *  2    Gandalf   1.1         7/31/99  Ian Formanek    Cleaned up comments
 *  1    Gandalf   1.0         6/30/99  Ian Formanek    
 * $
 */
