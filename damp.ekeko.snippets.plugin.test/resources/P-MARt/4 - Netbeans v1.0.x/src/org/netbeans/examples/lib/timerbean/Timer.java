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

package org.netbeans.examples.lib.timerbean;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.Vector;
import java.util.Enumeration;

/** The Timer JavaBean is a nonvisual component that sends an ActionEvent
* to the registered TimerListeners every "delay" property milliseconds.
* It can either send that event only once, or it can cycle (according to
* the "onceOnly" property).
*
* @version  1.01, Sep 02, 1998
*/
public class Timer extends Object
    implements java.io.Serializable {

    public static final String PROP_ONCE_ONLY = "onceOnly";
    public static final String PROP_DELAY = "delay";

    public static final long DEFAULT_DELAY = 1000;
    public static final boolean DEFAULT_ONLY_ONCE = false;

    static final long serialVersionUID =-7954930904657028678L;
    /** Creates a new Timer */
    public Timer () {
        delay = DEFAULT_DELAY;
        onceOnly = DEFAULT_ONLY_ONCE;
        propertySupport = new PropertyChangeSupport (this);
        start ();
    }

    public synchronized void start () {
        if (running) return;
        timerThread = new TimerThread ();
        running = true;
        timerThread.start ();
    }

    public synchronized void stop () {
        if (!running) return;
        timerThread.stop ();
        timerThread = null;
        running = false;
    }

    /** Getter method for the delay property.
    * @return Current delay value
    */
    public long getDelay () {
        return delay;
    }

    /** Setter method for the delay property.
    * @param value New delay value
    */
    public void setDelay (long value) {
        if (delay == value) return;
        long oldValue = delay;
        delay = value;
        propertySupport.firePropertyChange (PROP_DELAY,
                                            new Long (oldValue),
                                            new Long (delay));
    }

    /** Getter method for the onceOnly property.
    * @return Current onceOnly value
    */
    public boolean getOnceOnly () {
        return onceOnly;
    }

    /** Setter method for the onceOnly property.
    * @param value New onceOnly value
    */
    public void setOnceOnly (boolean value) {
        if (onceOnly == value) return;
        onceOnly = value;
        propertySupport.firePropertyChange (PROP_ONCE_ONLY,
                                            new Boolean (!onceOnly),
                                            new Boolean (onceOnly));
    }

    public void addPropertyChangeListener (PropertyChangeListener l) {
        propertySupport.addPropertyChangeListener (l);
    }

    public void removePropertyChangeListener (PropertyChangeListener l) {
        propertySupport.removePropertyChangeListener (l);
    }

    public void addTimerListener (TimerListener l) {
        if (listeners == null)
            listeners = new Vector();

        listeners.addElement (l);
    }

    public void removeTimerListener (TimerListener l) {
        if (listeners == null)
            return;
        listeners.removeElement (l);
    }

    private void fireTimerEvent () {
        if (listeners == null) return;
        Vector l;
        synchronized (this) {
            l = (Vector)listeners.clone ();
        }

        for (Enumeration e = l.elements (); e.hasMoreElements ();) {
            TimerListener tl = (TimerListener) e.nextElement ();
            tl.onTime (new ActionEvent (this, ActionEvent.ACTION_PERFORMED, "onTime"));
        }

    }

    class TimerThread extends Thread {
        public void run() {
            while (true) {
                try {
                    sleep(delay);
                } catch (InterruptedException e) {}
                fireTimerEvent();
                if (onceOnly) break;
            }
            running = false;
        }
    }

    transient private TimerThread timerThread;

    /** The timer listeners 
     * @associates TimerListener*/
    transient private Vector listeners;

    /** The support for firing property changes */
    private PropertyChangeSupport propertySupport;

    /** The flag indicating whether the timer is running */
    private boolean running;

    /** If true, the timer stops after firing the first onTime, if false
    * it keeps ticking until stopped */
    private boolean onceOnly;

    /** Delay in milliseconds */
    private long delay;
}


/*
 * Log
 *  9    Gandalf   1.6.2.1     11/27/99 Patrik Knakal   
 *  8    Gandalf   1.6.2.0     10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  7    Tuborg    1.6         12/29/98 Ian Formanek    Fixed end-of-line 
 *       characters. No semantic change.
 *  6    Tuborg    1.5         10/17/98 Ian Formanek    Modified comments to be 
 *       same as the sources in distribution
 *  5    Tuborg    1.4         9/3/98   Ian Formanek    
 *  4    Tuborg    1.3         9/2/98   Ian Formanek    Fixed bug 586 - The 
 *       start () method does not start previously stopped Timer.
 *  3    Tuborg    1.2         7/22/98  Ian Formanek    
 *  2    Tuborg    1.1         7/21/98  Ian Formanek    
 *  1    Tuborg    1.0         6/17/98  Ian Formanek    
 * $
 */
