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

import java.beans.*;
import java.lang.reflect.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Vector;
import java.util.StringTokenizer;

/** A class that manages events for one RADComponent.
*
* @author   Ian Formanek
*/
final public class EventsList extends Object {
    /** The handler property name */
    public static final String PROP_HANDLER = "handler"; // NOI18N

    protected static FormLoaderSettings formSettings = new FormLoaderSettings ();

    // FINALIZE DEBUG METHOD
    public void finalize () throws Throwable {
        super.finalize ();
        if (System.getProperty ("netbeans.debug.form.finalize") != null) {
            System.out.println("finalized: "+this.getClass ().getName ()+", instance: "+this); // NOI18N
        }
    } // FINALIZE DEBUG METHOD

    /** Constructs a new EventsList for specified RADComponent.
    * @param component The RADComponent for which this class manages the events
    */ 
    EventsList (RADComponent component) {
        radComponent = component;
        eventsManager = radComponent.getFormManager ().getEventsManager ();
        BeanInfo beanInfo = radComponent.getBeanInfo ();
        EventSetDescriptor[] eventSets = beanInfo.getEventSetDescriptors ();
        ArrayList list = new ArrayList (eventSets.length);
        for (int i = 0; i < eventSets.length; i++) {
            list.add (new EventSet (eventSets[i]));
        }

        if (formSettings.getSortEventSets ()) {

            // if the event sets are to be sorted, sort them out and move
            // the default event set to the beginning
            Collections.sort (list, new Comparator () {
                                  public int compare(Object o1, Object o2) {
                                      String n1 = ((EventSet)o1).getName();
                                      String n2 = ((EventSet)o2).getName();
                                      return n1.compareTo(n2);
                                  }
                              }
                             );
        }

        eventSetHandlers = new EventSet [list.size ()];
        list.toArray (eventSetHandlers);
    }

    /** A setter for preserveEmptyHandlers property.
    * If this property is set to false (default), the user is asked when the last event is 
    * deattached froman event handler, if true, the handlers are preserved even if  oe events 
    * are attached to it.
    */
    public void setPreserveEmptyHandlers (boolean value) {
        preserveEmptyHandlers = value;
    }

    /** A getter for preserveEmptyHandlers property.
    * If this property is set to false (default), the user is asked when the last event is 
    * deattached froman event handler, if true, the handlers are preserved even if  oe events 
    * are attached to it.
    */
    public boolean getPreserveEmptyHandlers () {
        return preserveEmptyHandlers;
    }


    /** @return an array of all EventSets for this component */
    public EventSet[] getEventSets () {
        return eventSetHandlers;
    }

    /** @return the Event that represents the default event or null if there is
    * no default event.
    */
    public Event getDefaultEvent () {
        BeanInfo beanInfo = radComponent.getBeanInfo ();
        int defIndex = beanInfo.getDefaultEventIndex ();
        if (defIndex == -1) {
            for (int i = 0; i < eventSetHandlers.length; i++) {
                if ("action".equals (eventSetHandlers[i].getName ())) { // NOI18N
                    Event[] events = eventSetHandlers[i].getEvents ();
                    for (int j = 0; j < events.length; j++) {
                        if ("actionPerformed".equals (events[j].getName ())) // NOI18N
                            return events[j];
                    }
                }
            }
        } else {
            int counter = 0;
            for (int i = 0; i < eventSetHandlers.length; i++) {
                Event[] events = eventSetHandlers[i].getEvents ();
                for (int j = 0; j < events.length; j++) {
                    if (counter == defIndex)
                        return events[j];
                    counter++;
                }
            }
        }
        return null;
    }

    /** @return an array of all event handler names */
    public int getEventCount () {
        int count = 0;
        for (int i = 0; i < eventSetHandlers.length; i++)
            count += eventSetHandlers[i].getEventCount ();
        return count;
    }

    /** Initializes the names of event handlers for all events
    * @param  eventHandlerNames The mapping <String, String> of event name to event handler name
    */
    void initEvents (Hashtable eventHandlerNames) {
        for (int i = 0; i < eventSetHandlers.length; i++) {
            Event[] events = eventSetHandlers[i].getEvents ();
            for (int j = 0; j < events.length; j++) {
                if (eventHandlerNames.get (events[j].getName ()) != null) {
                    StringTokenizer st = new StringTokenizer ((String)eventHandlerNames.get (events[j].getName ()), ","); // NOI18N
                    while (st.hasMoreTokens()) {
                        String handlerName = st.nextToken();
                        eventsManager.addEventHandler (events[j], handlerName);
                    }
                }
            }
        }
    }

    /** @return the array of all event handler names */
    Hashtable getEventNames () {
        Hashtable names = new Hashtable (20);

        for (int i = 0; i < eventSetHandlers.length; i++) {
            Event[] events = eventSetHandlers[i].getEvents ();
            for (int j = 0; j < events.length; j++) {
                Vector handlers = events[j].getHandlers ();
                String eventName = events[j].getName ();
                String hanlerNames = ""; // NOI18N
                if (handlers.size() > 0) {
                    String handlerNames = ((EventsManager.EventHandler) handlers.get (0)).getName ();
                    for (int k=1, n=handlers.size(); k<n; k++)
                        handlerNames = handlerNames + ","+((EventsManager.EventHandler) handlers.get (k)).getName (); // NOI18N
                    names.put (eventName, handlerNames);
                }
            }
        }
        return names;
    }

    // -----------------------------------------------------------------------------
    // innerclasses

    /** A class that represents one EventSet */
    final public class EventSet {
        EventSet (EventSetDescriptor desc) {
            this.desc = desc;
            Method[] methods = desc.getListenerMethods ();
            events = new Event[methods.length];
            for (int i = 0; i < methods.length; i++)
                events [i] = new Event (methods [i]);
        }

        public Event[] getEvents () {
            return events;
        }

        public int getEventCount () {
            return events.length;
        }

        public String getName () {
            return desc.getName ();
        }

        EventSetDescriptor getEventSetDescriptor () {
            return desc;
        }

        private EventSetDescriptor desc;
        private Event[] events;
    }

    /** A class that represents one event handler.
    * It provides a support for listening to property changes.
    */ 
    final public class Event {
        /** Constructs a new Event for the specified event method.
        * @param m The Event method that is to be represented by this Event
        */ 
        Event (Method m) {
            listenerMethod = m;
        }

        public String getName () {
            return listenerMethod.getName ();
        }

        public Method getListenerMethod () {
            return listenerMethod;
        }

        public void addHandler (EventsManager.EventHandler handler) {
            eventHandlers.add (handler);
            propertySupport.firePropertyChange (PROP_HANDLER, null, handler);
        }

        public void removeHandler (EventsManager.EventHandler handler) {
            eventHandlers.remove (handler);
            propertySupport.firePropertyChange (PROP_HANDLER, handler, null);
        }

        public Vector getHandlers () {
            return eventHandlers;
        }

        public void addPropertyChangeListener (PropertyChangeListener pcl) {
            propertySupport.addPropertyChangeListener (pcl);
        }

        public void removePropertyChangeListener (PropertyChangeListener pcl) {
            propertySupport.removePropertyChangeListener (pcl);
        }


        public void createDefaultEventHandler () {
            eventsManager.addEventHandler (this, FormUtils.getDefaultEventName (radComponent, listenerMethod));
            radComponent.getFormManager ().fireEventAdded (radComponent, (EventsManager.EventHandler) eventHandlers.get (eventHandlers.size() -1));
            String newHandlerName = ((EventsManager.EventHandler) eventHandlers.get (eventHandlers.size() -1)).getName();
            radComponent.getNodeReference ().firePropertyChangeHelper (FormEditor.EVENT_PREFIX + getName(), null, newHandlerName);
        }

        public void gotoEventHandler () {
            if (eventHandlers.size() == 1)
                radComponent.getFormManager ().getCodeGenerator ().gotoEventHandler (((EventsManager.EventHandler) eventHandlers.get (0)).getName ());
            //PBUZEK >1 display
        }

        public void gotoEventHandler (String handlerName) {
            EventsManager.EventHandler handler = null;
            for (int i=0, n=eventHandlers.size(); i<n; i++)
                if (((EventsManager.EventHandler) eventHandlers.get (i)).getName ().equals (handlerName)) {
                    handler = (EventsManager.EventHandler) eventHandlers.get (i);
                    break;
                }
            if (handler != null) {
                radComponent.getFormManager ().getCodeGenerator ().gotoEventHandler (handler.getName ());
            }
        }

        void nameChanged () {
            propertySupport.firePropertyChange (null, null, null);
        }

        boolean preserveEmptyEvent () {
            return getPreserveEmptyHandlers ();
        }

        /** The PropertyChange support */
        private PropertyChangeSupport propertySupport = new PropertyChangeSupport (this);

        /** The event handler methods or null if not specified. Members are EventsManager.EventHandler 
         * @associates EventHandler*/
        private Vector eventHandlers = new Vector ();

        private Method listenerMethod;
        //private Vector handlerNames = new Vector (); // String members
    }

    RADComponent getRADComponent () {
        return radComponent;
    }

    EventsManager getEventsManager () {
        return eventsManager;
    }

    // -----------------------------------------------------------------------------
    // private area
    private RADComponent radComponent;
    private EventsManager eventsManager;
    private EventSet[] eventSetHandlers;
    private boolean preserveEmptyHandlers = false;

}

/*
 * Log
 *  11   Gandalf   1.10        1/14/00  Pavel Buzek     fire property change 
 *       when creating new event handler
 *  10   Gandalf   1.9         1/5/00   Ian Formanek    NOI18N
 *  9    Gandalf   1.8         11/25/99 Pavel Buzek     support for multiple 
 *       handlers for one event
 *  8    Gandalf   1.7         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Gandalf   1.6         8/1/99   Ian Formanek    access modifiers cleaned
 *  6    Gandalf   1.5         7/30/99  Ian Formanek    Fixed bug 2814 - Adding 
 *       events handler failed.
 *  5    Gandalf   1.4         6/28/99  Ian Formanek    Reemoved obsoleted class
 *       EventMapper
 *  4    Gandalf   1.3         5/17/99  Ian Formanek    Events are sorted
 *  3    Gandalf   1.2         5/15/99  Ian Formanek    
 *  2    Gandalf   1.1         5/12/99  Ian Formanek    
 *  1    Gandalf   1.0         5/10/99  Ian Formanek    
 * $
 */
