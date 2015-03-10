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

import java.io.Serializable;
import java.io.Externalizable;
import java.text.MessageFormat;
import java.util.Vector;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Enumeration;

import org.openide.*;
import org.openide.util.Utilities;

/** A class that manages the pool of event handlers for one form.
*
* @author   Ian Formanek
*/
final public class EventsManager extends Object {
    // FINALIZE DEBUG METHOD
    public void finalize () throws Throwable {
        super.finalize ();
        if (System.getProperty ("netbeans.debug.form.finalize") != null) {
            System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this); // NOI18N
        }
    } // FINALIZE DEBUG METHOD

    /** Constructs a new EventsManager for specified FormManager2.
    * @param node The FormManager2 that manages the form for which we manage
    *             the event handlers
    */
    EventsManager (FormManager2 formManager) {
        this.formManager = formManager;
    }

    /** Adds a new event handler method. If such event handler already exists,
    * the specified event is attached to it, otherwise a new event handler is created.
    * If attaching to an existing handler, the attached event must be of the same type
    * as the existing handler (i.e. the parameter types must be same).
    * @param event     The event for which to add the event handler
    * @param eventName The name of the event handler method
    */
    void addEventHandler (EventsList.Event event, String eventName) {
        addEventHandler (event, eventName, null);
    }

    /** Adds a new event handler method. If such event handler already exists,
    * the specified event is attached to it, otherwise a new event handler is created.
    * If attaching to an existing handler, the attached event must be of the same type
    * as the existing handler (i.e. the parameter types must be same).
    * @param event     The event for which to add the event handler
    * @param eventName The name of the event handler method
    * @param bodyText  The text of the event handler body or null for default (empty) text
    */
    void addEventHandler (EventsList.Event event, String eventName, String bodyText) {
        EventHandler handler = (EventHandler) nameToHandler.get (eventName);
        if (handler != null) { // if the handler already exist, attach to it
            if (!handler.checkCompatibility (event)) {
                TopManager.getDefault().notify(
                    new NotifyDescriptor.Message(FormEditor.getFormBundle().getString ("MSG_CannotAttach"),
                                                 NotifyDescriptor.WARNING_MESSAGE)
                );
                return;
            }
            handler.attachEvent (event);
        } else {               // otherwise create a new one
            handler = new EventHandler (event, eventName);
            handler.generateHandler (bodyText);
            nameToHandler.put (eventName, handler);
        }
    }

    /** Deattaches the specified event from all of its handlers.
    * @param event the event to deattach from all of its handlers.
    */
    void removeEventHandler (EventsList.Event event) {
        Vector hands = event.getHandlers ();
        for (int i=0, n=hands.size(); i<n; i++) {
            removeEventHandler(event, (EventHandler) hands.get (0));
        }
    }

    /** Deattaches the specified event from its handler.
    * @param event the event to deattach from its handler.
    */
    void removeEventHandler (EventsList.Event event, EventHandler handler) {
        if (handler == null)
            return; // no handler to deattach from
        if (event.preserveEmptyEvent ())
            handler.deattachEvent (event);
        else {
            if (handler.getAttachedEvents ().size () == 1) {
                String message = MessageFormat.format(FormEditor.getFormBundle().getString("FMT_MSG_EventDelete"),
                                                      new Object [] { handler.getName() });
                Object result = TopManager.getDefault().notify(
                                    new NotifyDescriptor.Confirmation(message,
                                                                      NotifyDescriptor.YES_NO_OPTION,
                                                                      NotifyDescriptor.WARNING_MESSAGE)
                                );
                if (NotifyDescriptor.YES_OPTION.equals(result)) {
                    handler.deattachEvent (event);
                    deleteEventHandler (handler);
                } else {
                    handler.deattachEvent (event);
                }
            } else
                handler.deattachEvent (event);
        }
    }

    void deleteEventHandler (EventHandler handler) {
        handler.delete ();
        nameToHandler.remove (handler.getName ());
    }

    /** Renames the specified handler to the given name.
    * @param handler The EventHandler to rename
    * @param newName The new name of the event handler
    */
    void renameEventHandler (EventHandler handler, String newName) {
        if (nameToHandler.get(newName) != null) {
            TopManager.getDefault().notify(
                new NotifyDescriptor.Message(FormEditor.getFormBundle().getString ("MSG_CannotRename"),
                                             NotifyDescriptor.WARNING_MESSAGE)
            );
            return;
        }
        nameToHandler.remove (handler.getName ());
        nameToHandler.put (newName, handler);
        handler.rename (newName);
    }

    /** Allows to acquire an EventHandler of specified name.
    * @return The EventHandler of specified name
    */
    EventHandler findEventHandler (String name) {
        return (EventHandler)nameToHandler.get(name);
    }

    // -----------------------------------------------------------------------------
    // innerclasses

    /** A class that represents one Event handler method.
    * It holds a list of Events that are associated with the handler.
    */
    public class EventHandler extends Object implements Serializable {

        static final long serialVersionUID =-4103904336344477509L;
        EventHandler (EventsList.Event event, String name) {
            handlerName = name;
            Class[] parameters = event.getListenerMethod ().getParameterTypes ();

            parameterTypes = new String[parameters.length];
            for (int i = 0; i < parameters.length; i++)
                parameterTypes [i] = parameters [i].getName ();

            event.addHandler (this);
            getAttachedEvents ().addElement (event);
        }

        /** Called to actually create the handler method in the text
        * @param bodyText The body text of the event or null for default (empty) body
        */
        void generateHandler (String bodyText) {
            if (formManager.isInitialized ()) { // [PENDING - what to do during deserializaion about restoring event handlers?]
                formManager.getCodeGenerator ().generateEventHandler (handlerName, parameterTypes, bodyText);
            }
        }

        /** Called to change the handler text
        * @param bodyText The body text of the event or null for default (empty) body
        */
        void setHandlerText (String bodyText) {
            formManager.getCodeGenerator ().changeEventHandler (handlerName, parameterTypes, bodyText);
        }

        void rename (String newName) {
            // 1. regenerate the event handler method
            formManager.getCodeGenerator ().renameEventHandler (handlerName, newName, parameterTypes);
            handlerName = newName;

            // 2. set the events
            for (Enumeration e = getAttachedEvents ().elements (); e.hasMoreElements ();) {
                EventsList.Event event = (EventsList.Event)e.nextElement ();
                event.nameChanged ();
            }
        }

        void delete () {
            formManager.getCodeGenerator ().deleteEventHandler (handlerName);
        }

        void attachEvent (EventsList.Event event) {
            event.addHandler (this);
            getAttachedEvents ().addElement (event);
        }

        void deattachEvent (EventsList.Event event) {
            event.removeHandler (this);
            getAttachedEvents ().removeElement (event);
        }

        public String getName () {
            return handlerName;
        }

        public java.lang.String toString () {
            return handlerName;
        }

        public Vector getAttachedEvents () {
            if (attachedEvents == null)
                attachedEvents = new Vector ();
            return attachedEvents;
        }

        public boolean isEmpty () {
            return (attachedEvents.size () == 0);
        }

        /** Checks whether the specified event is parameter-compatible with this handler
        * @param event the event to check
        * @return true if the event is parameter-compatible with this handler, false otherwise
        */
        boolean checkCompatibility (EventsList.Event event) {
            Class[] parameters = event.getListenerMethod ().getParameterTypes ();
            if (parameters.length != parameterTypes.length)
                return false;
            for (int i = 0; i < parameterTypes.length; i++)
                if (!parameterTypes [i].equals (parameters [i].getName ()))
                    return false;
            return true;
        }

        transient private Vector attachedEvents;

        private String handlerName;
        private String[] parameterTypes;
    }

    // -----------------------------------------------------------------------------
    // private area

    /** The FormManager2 that manages the form */
    private FormManager2 formManager;

    /** The list of event handlers (mapping name-> eventHandler) 
     * @associates EventHandler*/
    private Hashtable nameToHandler = new Hashtable ();
}

/*
 * Log
 *  12   Gandalf   1.11        1/26/00  Pavel Buzek     #5483 fixed
 *  11   Gandalf   1.10        1/24/00  Pavel Buzek     #5483 fixed - deleting 
 *       event handlers
 *  10   Gandalf   1.9         1/5/00   Ian Formanek    NOI18N
 *  9    Gandalf   1.8         11/27/99 Patrik Knakal   
 *  8    Gandalf   1.7         11/25/99 Pavel Buzek     support for multiple 
 *       handlers for one event
 *  7    Gandalf   1.6         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         6/9/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  5    Gandalf   1.4         5/15/99  Ian Formanek    
 *  4    Gandalf   1.3         5/12/99  Ian Formanek    
 *  3    Gandalf   1.2         5/10/99  Ian Formanek    
 *  2    Gandalf   1.1         5/4/99   Ian Formanek    Package change
 *  1    Gandalf   1.0         4/26/99  Ian Formanek    
 * $
 */
