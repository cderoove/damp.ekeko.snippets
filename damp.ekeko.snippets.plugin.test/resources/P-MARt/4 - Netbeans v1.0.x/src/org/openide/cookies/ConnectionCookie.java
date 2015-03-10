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

package org.openide.cookies;

import java.util.EventListener;
import java.util.EventObject;
import java.io.IOException;

import org.openide.nodes.Node;

/** Cookie that allows connection between two objects. Also supporting
* persistent connections.
*
* @author Jaroslav Tulach
*/
public interface ConnectionCookie extends Node.Cookie {


    /** Attaches new node to listen to events produced by this
    * event. The type must be one of event types supported by this
    * cookie and the listener should have ConnectionCookie.Listener cookie
    * attached so it can be notified when event of requested type occurs.
    *
    * @param type the type of event, must be supported by the cookie
    * @param listener the node that should be notified
    *
    * @exception InvalidObjectException if the type is not supported by the cookie
    * @exception IOException if the type is persistent and the listener does not
    *    have serializable handle (listener.getHandle () is null or its serialization
    *    throws an exception)
    */
    public void register (Type type, Node listener) throws IOException;

    /** Unregisters an listener.
    * @param type type of event to unregister the listener from listening to
    * @param listener to unregister
    * @exception IOException if there is I/O operation error when the removing
    *   the listener from persistent storage
    */
    public void unregister (Type type, Node listener) throws IOException;

    /** Unmutable set of types supported by this connection source.
    * @return a set of Type objects
    */
    public java.util.Set getTypes ();



    /** Cookie that must be provided by a node that is willing to register
    * itself as a listener to a ConnectionCookie.
    */
    public interface Listener extends Node.Cookie, EventListener {
        /** Notifies that the an event happended.
        * @param ev event that describes the action
        * @exception IllegalArgumentException if the event is not of valid type, then the
        *    caller should call the listener no more
        * @exception ClassCastException if the event is not of valid type, then the
        *    caller should call the listener no more
        */
        public void notify (ConnectionCookie.Event ev) throws IllegalArgumentException, ClassCastException;
    }

    /** Event that is fired to listeners.
    */
    public class Event extends java.util.EventObject {
        private Type type;

        static final long serialVersionUID =7177610435688865839L;
        /** @param n the node that produced the action
        * @param t type of the event
        */
        public Event (Node n, Type t) {
            super (n);
            type = t;
        }

        /** Getter for the node that produced the action.
        * The node can be used to obtain additional information like cookies, etc.
        * @return the node
        */
        public Node getNode () {
            return (Node)getSource ();
        }

        /** Getter for the type of the event.
        * There can be more types of events and the listener can compare
        * if two events are of the same type by using type1.equals (type2)
        *
        * @return type of the event
        */
        public Type getType () {
            return type;
        }
    }

    /** Interface describing cookie type of event a cookie can produce.
    */
    public interface Type extends java.io.Serializable {
        /** The class that is passed into the listener's <CODE>notify</CODE>
        * method when an event of this type is fired.
        *
        * @return event class
        */
        public Class getEventClass ();

        /** Getter whether the registration to this type of event  is persistent
        * or is valid only till the source disappears (the IDE shutdowns).
        */    
        public boolean isPersistent ();

        // Jesse, please improve the comment. [Petr]
        /** Test whether the specified type could be accepted by this type.
        * This method is similar to <CODE>equals(Object)</CODE> method, 
        * so default implementation could be delegated to it.
        * @return <CODE>true</CODE> if type is similar to this type.
        */
        public boolean overlaps(Type type);
    }

}

/*
 * Log
 *  7    Gandalf   1.6         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  6    Gandalf   1.5         8/19/99  Ian Formanek    Removed serial version 
 *       UID from interface
 *  5    Gandalf   1.4         8/18/99  Ian Formanek    Generated serial version
 *       UID
 *  4    Gandalf   1.3         6/8/99   Ian Formanek    ---- Package Change To 
 *       org.openide ----
 *  3    Gandalf   1.2         6/2/99   Petr Hamernik   overlaps instead of 
 *       acceptEvent
 *  2    Gandalf   1.1         5/25/99  Petr Hamernik   acceptEvent added to 
 *       Type
 *  1    Gandalf   1.0         4/23/99  Jaroslav Tulach 
 * $
 */
