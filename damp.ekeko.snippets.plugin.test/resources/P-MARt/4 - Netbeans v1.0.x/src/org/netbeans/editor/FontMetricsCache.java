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

package org.netbeans.editor;

import java.awt.Component;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.util.HashMap;

/** Static cache that holds the font metrics for the fonts.
* This can generally speed up drawing if the metrics are not cached
* directly by the system.
*
* @author Miloslav Metelka
* @version 1.00
*/

public class FontMetricsCache {
    /**
     * @associates Object 
     */
    private static HashMap font2FM = new HashMap();

    /** Get the font-metrics for the given font.
    * @param font font for which the metrics is being retrieved.
    * @param c component that is used to retrieve the metrics in case it's
    *   not yet in the cache.
    */
    public static synchronized FontMetrics getFontMetrics(Font f, Component c) {
        Object fm = font2FM.get(f);
        if (fm == null) {
            fm = c.getFontMetrics(f);
            font2FM.put(f, fm);
        }
        return (FontMetrics)fm;
    }

    /** Get the font-metrics for the given font.
    * @param font font for which the metrics is being retrieved.
    * @param g graphics that is used to retrieve the metrics in case it's
    *   not yet in the cache.
    */
    public static synchronized FontMetrics getFontMetrics(Font f, Graphics g) {
        Object fm = font2FM.get(f);
        if (fm == null) {
            fm = g.getFontMetrics(f);
            font2FM.put(f, fm);
        }
        return (FontMetrics)fm;
    }

    /** Clear all the metrics from the cache. It's usually done
    * when any of the editor ui is being garbage collected to
    * ensure there will be no more unused metrics.
    */
    public static synchronized void clear() {
        font2FM.clear();
    }

}

/*
 * Log
 *  1    Gandalf-post-FCS1.0         4/5/00   Miloslav Metelka 
 * $
 */

