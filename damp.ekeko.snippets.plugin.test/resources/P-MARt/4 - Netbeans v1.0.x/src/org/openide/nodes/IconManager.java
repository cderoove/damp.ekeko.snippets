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

package org.openide.nodes;

import java.awt.Image;
import java.awt.Toolkit;
import java.util.HashMap;
import java.lang.ref.*;

/** Registers all loaded images into the AbstractNode, so nothing is loaded twice.
*
* @author Jaroslav Tulach,
*/
class IconManager extends Object {
    /** default icon to use when none is present */
    private static final String DEFAULT_ICON = "/org/openide/resources/defaultNode.gif"; // NOI18N

    /** a value that indicates that the icon does not exists */
    private static final Object NO_ICON = DEFAULT_ICON;

    /** loaded default icon */
    private static Image defaultIcon;

    /** map of resource name to loaded icon (String, SoftRefrence (Image)) or (String, NO_ICON) */
    private static final HashMap map = new HashMap ();

    /** Loades default icon if not loaded.
    */
    public static Image getDefaultIcon () {
        if (defaultIcon != null) return defaultIcon;

        synchronized (IconManager.class) {
            if (defaultIcon != null) return defaultIcon;
            defaultIcon = Toolkit.getDefaultToolkit ().getImage (
                              IconManager.class.getResource (DEFAULT_ICON)
                          );
            return defaultIcon;
        }
    }

    /** Finds imager for given resource.
    * @param name name of the resource
    * @param loader classloader to use for locating it
    */
    public static Image getIcon (String name, ClassLoader loader) {
        Object img = map.get (name);

        // no icon for this name (already tested)
        if (img == NO_ICON) return null;

        if (img != null) {
            // then it is SoftRefrence
            img = ((Reference)img).get ();
        }

        // icon found
        if (img != null) return (Image)img;

        synchronized (map) {
            // again under the lock
            img = map.get (name);
            if (img != null) {
                // then it is SoftRefrence
                img = ((Reference)img).get ();
            }
            if (img != null) return (Image)img;

            // path for bug in classloader
            String n;
            if (name.startsWith ("/")) { // NOI18N
                n = name.substring (1);
            } else {
                n = name;
            }

            // we have to load it
            java.net.URL url = loader != null ?
                               loader.getResource (n) : IconManager.class.getClassLoader ().getResource (n);

            img = url == null ? null : Toolkit.getDefaultToolkit ().getImage (url);
            if (img != null) {
                Reference r = new SoftReference (img);
                map.put (name, r);
                return (Image)img;
            } else {
                // no icon found
                map.put (name, NO_ICON);
                return null;
            }
        }
    }


}

/*
* Log
*  10   Gandalf   1.9         1/12/00  Jesse Glick     NOI18N
*  9    Gandalf   1.8         11/3/99  Jaroslav Tulach Caching works even for 
*       icons that start with slash.
*  8    Gandalf   1.7         10/22/99 Ian Formanek    NO SEMANTIC CHANGE - Sun 
*       Microsystems Copyright in File Comment
*  7    Gandalf   1.6         8/27/99  Jaroslav Tulach New threading model & 
*       Children.
*  6    Gandalf   1.5         6/9/99   Ian Formanek    Fixed resources for 
*       package change
*  5    Gandalf   1.4         6/8/99   Ian Formanek    ---- Package Change To 
*       org.openide ----
*  4    Gandalf   1.3         2/17/99  Ian Formanek    Updated icons to point to
*       the right package (under ide/resources)
*  3    Gandalf   1.2         1/15/99  Jaroslav Tulach Uses SoftReferences, 
*       resources can start with slash  
*  2    Gandalf   1.1         1/6/99   Jaroslav Tulach 
*  1    Gandalf   1.0         1/5/99   Ian Formanek    
* $
*/
