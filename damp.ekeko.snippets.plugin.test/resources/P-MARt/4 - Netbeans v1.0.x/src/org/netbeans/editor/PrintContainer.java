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

import java.awt.Color;
import java.awt.Font;

/**
* Container for printed text. The parts of text attributed by font,
* fore and back color are added to it for the whole printed area.
*
* @author Miloslav Metelka
* @version 1.00
*/

public interface PrintContainer {

    public void add(char[] chars, Font font, Color foreColor, Color backColor);

    public void eol();

    public boolean initEmptyLines();

}


/*
 * Log
 *  2    Gandalf   1.1         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  1    Gandalf   1.0         6/25/99  Miloslav Metelka 
 * $
 */

