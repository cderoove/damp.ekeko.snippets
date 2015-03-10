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

package org.netbeans.modules.form.util;

import java.awt.Image;

/**
* @version 1.00, 17 Jul, 1998
* @author  Ian Formanek
*/
public class GridInfo {
    public Image gridImage = null;
    public int imWidth = -1, imHeight = -1;

    private int gx, gy;

    public GridInfo (int aGx, int aGy) {
        gx = aGx; if (gx <1) gx = 1;
        gy = aGy; if (gy <1) gy = 1;
    }

    public void setGrid(int aGx, int aGy) {
        if (aGx == gx && aGy == gy) return;
        gx = aGx; if (gx <1) gx = 1;
        gy = aGy; if (gy <1) gy = 1;
        invalidate();
    }

public int getGridX() { return gx; }
    public int getGridY() { return gy; }

    public void invalidate() {
        gridImage = null;
        imWidth = -1; imHeight = -1;
    }

    public String toString() {
        return "GridInfo [gridX: "+gx+", gridY: "+gy+", imWidth: "+ // NOI18N
               imWidth+", imHeight: "+imHeight+"Image: "+gridImage+"]"; // NOI18N
    }
}

/*
 * Log
 *  4    Gandalf   1.3         1/5/00   Ian Formanek    NOI18N
 *  3    Gandalf   1.2         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  2    Gandalf   1.1         5/4/99   Ian Formanek    Package change
 *  1    Gandalf   1.0         2/26/99  Ian Formanek    
 * $
 */
