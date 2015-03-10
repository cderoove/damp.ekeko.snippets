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

import java.awt.*;

/**
* @version 1.00, 17 Jul 1998
* @author  Ian Formanek
*/
public class GridThread extends Thread {
    Component gridComp;
    int iw, ih;
    GridInfo gridIn;

    public GridThread(Component gridComp, GridInfo gridIn) {
        super("GridThread"); // NOI18N
        this.gridComp = gridComp;
        this.gridIn = gridIn;
        Dimension size = gridComp.getSize();
        iw = size.width; ih = size.height;
    }

    public GridThread(Component gridComp, GridInfo gridIn, int w, int h) {
        super("GridThread"); // NOI18N
        this.gridComp = gridComp;
        this.gridIn = gridIn;
        Dimension size = gridComp.getSize();
        iw = w; ih = h;
    }

    public void run() {
        Image im = gridComp.createImage(iw, ih);
        Graphics ig = im.getGraphics();
        ig.setColor(gridComp.getBackground());
        ig.fillRect(0, 0, iw, ih);
        ig.setColor(gridComp.getForeground());
        for (int j=0; j< ih; j+= gridIn.getGridY())
            for (int i=0; i< iw; i+= gridIn.getGridX())
                ig.drawLine(i,j,i,j);
        gridIn.gridImage = im;
        gridIn.imWidth = iw;
        gridIn.imHeight = ih;
        gridComp.repaint();
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
