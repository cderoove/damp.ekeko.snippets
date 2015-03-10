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

package org.netbeans.modules.antlr.editor;

import java.awt.Color;
import java.awt.Font;
import org.netbeans.editor.Coloring;
import org.netbeans.editor.Settings;
import org.netbeans.editor.BaseColoringManager;

/**
* Mapping of colorings to particular token types
*
* @author Miloslav Metelka
* @version 1.00
*/

public class GColoringManager extends BaseColoringManager {

    /** Create new instance of colorings. This method should be overriden
    * in children to create and return the coloring array.
    */
    protected Coloring[] createColorings(Class kitClass,int coloringSet) {
        if (coloringSet != TOKEN_SET) {
            return super.createColorings(kitClass,coloringSet);
        }
        // not found in settings, use the default
        Coloring superColorings[] = super.createColorings(kitClass,DEFAULT_SET); // plain text coloring
        Font defaultFont = getColorings(kitClass,DEFAULT_SET)[0].getFont();
        //    Font boldFont = defaultFont.deriveFont(Font.BOLD);
        Font italicFont = defaultFont.deriveFont(Font.ITALIC);
        Coloring gColorings[] = new Coloring[] {
                                    //new Coloring(GSyntax.TN_SPECIAL, null, Color.red, null),
                                    new Coloring("foo", null, Color.red, null),
                                    new Coloring("bar", null, Color.red, null),
                                    /*
                                    new Coloring(GSyntax.TN_OPERATOR, null, null, null),
                                    new Coloring(GSyntax.TN_ARG, null, Color.green.darker().darker(), null),
                                    new Coloring(GSyntax.TN_BLOCK_COMMENT, italicFont, Color.gray, null),
                                    new Coloring(GSyntax.TN_STRING, null, Color.magenta, null),
                                    new Coloring(GSyntax.TN_INT, null, Color.red, null),
                                    */
                                };
        return augmentList(superColorings, gColorings);
    }

}