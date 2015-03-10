/*
 *                 Sun Public License Notice
 * 
 * The contents of this file are subject to the Sun Public License
 * Version 1.0 (the "License"). You may not use this file except in
 * compliance with the License. A copy of the License is available at
 * http://www.sun.com/
 * 
 * The Original Code is NetBeans. The Initial Developer of the Original
 * Code is Sun Microsystems, Inc. Portions Copyright 1997-2000 Sun
 * Microsystems, Inc. All Rights Reserved.
 */

package org.netbeans.modules.properties;

import org.openide.util.WeakListener;

/** WeakListener type for PropertyBundleListener.
*
* @author Petr Jiricka
*/
public class WeakListenerPropertyBundle extends WeakListener
    implements PropertyBundleListener {

    /** Constructor.
    * @param l listener to delegate to
    */
    public WeakListenerPropertyBundle (PropertyBundleListener l) {
        super (PropertyBundleListener.class, l);
    }

    /** Constructor.
    * @param clazz required class
    * @param l listener to delegate to
    */
    WeakListenerPropertyBundle (Class clazz, PropertyBundleListener l) {
        super (clazz, l);
    }

    /** Tests if the object we reference to still exists and
    * if so, delegate to it. Otherwise remove from the source
    * if it has removePropertyBundleListener method.
    */
    public void bundleChanged (PropertyBundleEvent ev) {
        PropertyBundleListener l = (PropertyBundleListener)super.get (ev);
        if (l != null) l.bundleChanged (ev);
    }

    /** Method name to use for removing the listener.
    * @return name of method of the source object that should be used
    *   to remove the listener from listening on source of events
    */
    protected String removeMethodName () {
        return "removePropertyBundleListener";
    }
}
/*
* <<Log>>
*  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  2    Gandalf   1.1         6/9/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  1    Gandalf   1.0         5/13/99  Petr Jiricka    
* $
*/
