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

package org.netbeans.modules.java;

import java.util.EventListener;
import java.util.EventObject;
import java.util.Iterator;
import java.util.List;

import org.openide.src.SourceElement;

/** The public interface for parsing of java sources. This class contains listener
* interface and the event class, also the methods for registration and unregistration
* of the listener.
*
* @author Petr Hamernik
*/
public class Parsing extends Object {

    /** Add the specific listener to the list of global parsing listeners.
    * @param l listener to add
    */
    public static void addParsingListener(Listener l) {
        synchronized (JavaDataLoader.parsingListeners) {
            JavaDataLoader.parsingListeners.add(l);
        }
    }

    /** Remove the specific listener from the list of global parsing listeners.
    * @param l listener to remove
    */
    public static void removeParsingListener(Listener l) {
        synchronized (JavaDataLoader.parsingListeners) {
            JavaDataLoader.parsingListeners.remove(l);
        }
    }

    /** Fire the event for specified JavaDataObject.
    */
    static void fireEvent(JavaDataObject jdo, Object hook) {
        Event evt = new Event(jdo, hook);
        Iterator it = null;
        synchronized (JavaDataLoader.parsingListeners) {
            List list = (List) JavaDataLoader.parsingListeners.clone();
            it = list.iterator();
        }
        while (it.hasNext()) {
            ((Listener) it.next()).objectParsed(evt);
        }
    }

    /** The listener interface for everybody who want to control all
    * parsed JavaDataObjects.
    */
    public static interface Listener extends EventListener {
        /** Method which is called everytime when some object is parsed.
        * @param evt The event with the details.
        */
        public void objectParsed(Event evt);
    }

    /** The event class used in Listener.
    */
    public static class Event extends EventObject {
        static final long serialVersionUID =8512232095851109211L;
        /** Construct the new Event.
        * @param jdo JavaDataObject which is the source of the event.
        * @param hook the object which prevents the garbage collector remove the parsing
        *   information till this event lives.
        */
        Event(JavaDataObject jdo, Object hook) {
            super(jdo);
        }

        /** @return the data object which is the source of the event.
        */
        public JavaDataObject getJavaDataObject() {
            return (JavaDataObject) getSource();
        }

        /** @return the source element which was parsed.
        */
        public SourceElement getSourceElement() {
            return getJavaDataObject().sourceElement;
        }
    }
}


/*
 * Log
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         8/9/99   Ian Formanek    Generated Serial Version
 *       UID
 *  1    Gandalf   1.0         7/23/99  Petr Hamernik   
 * $
 */
