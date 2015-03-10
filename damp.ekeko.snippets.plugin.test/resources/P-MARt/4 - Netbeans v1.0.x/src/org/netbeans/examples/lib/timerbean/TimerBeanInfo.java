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

import java.beans.*;
import java.awt.Image;

/** The BeanInfo for the Timer bean.
*
* @version  1.00, Jul 20, 1998
*/
public class TimerBeanInfo extends SimpleBeanInfo {

    /** Icon for image data objects. */
    private Image icon;

    /** Array of property descriptors. */
    private static PropertyDescriptor[] desc;

    /** Array of event set descriptors. */
    private static EventSetDescriptor[] events;

    // initialization of the array of descriptors
    static {
        try {
            desc = new PropertyDescriptor[2];
            desc [0] = new PropertyDescriptor ("Delay", Timer.class, "getDelay", "setDelay");
            desc [1] = new PropertyDescriptor ("Once Only", Timer.class, "getOnceOnly", "setOnceOnly");

            events = new EventSetDescriptor [1];
            events[0] = new EventSetDescriptor (Timer.class, "timer", TimerListener.class, "onTime");
        } catch (IntrospectionException ex) {
            //throw new InternalError ();
            ex.printStackTrace ();
        }
    }

    public TimerBeanInfo () {
        icon = loadImage ("/org/netbeans/examples/lib/timerbean/timer.gif");
    }

    /** Provides the Timer's icon */
    public Image getIcon(int type) {
        return icon;
    }

    /** Descriptor of valid properties
    * @return array of properties
    */
    public PropertyDescriptor[] getPropertyDescriptors () {
        return desc;
    }

    /** Descriptor of valid events
    * @return array of event sets
    */
    public EventSetDescriptor[] getEventSetDescriptors () {
        return events;
    }

    public int getDefaultEventIndex () {
        return 0;
    }

}

/*
 * Log
 *  6    Gandalf   1.4.2.0     10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  5    Tuborg    1.4         12/29/98 Ian Formanek    Fixed end-of-line 
 *       characters. No semantic change.
 *  4    Tuborg    1.3         10/17/98 Ian Formanek    Modified comments to be 
 *       same as the sources in distribution
 *  3    Tuborg    1.2         7/22/98  Ian Formanek    
 *  2    Tuborg    1.1         6/18/98  Ian Formanek    Added property and 
 *       eventSet descriptors
 *  1    Tuborg    1.0         6/17/98  Ian Formanek    
 * $
 */
