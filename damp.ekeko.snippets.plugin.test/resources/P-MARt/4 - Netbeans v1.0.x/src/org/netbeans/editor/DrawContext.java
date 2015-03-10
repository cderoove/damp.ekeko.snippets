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

/** This interface provides methods for
* getting and setting various drawing attributes.
* During painting draw layer receives draw context
* and it is expected to either leave draw parameters
* as they are or change them.
*
* @author Miloslav Metelka
* @version 1.00
*/


public interface DrawContext {

    /** Get current foreground color */
    public Color getForeColor();

    /** Set current foreground color */
    public void setForeColor(Color foreColor);

    /** Get current background color */
    public Color getBackColor();

    /** Set current background color */
    public void setBackColor(Color backColor);

    /** Get current font */
    public Font getFont();

    /** Set current font */
    public void setFont(Font font);

    /** Get current drawing position */
    public int getOffset();

    /** Get start position of the drawing */
    public int getDrawStartPos();

    /** Get end position of the drawing */
    public int getDrawEndPos();

    /** Is current drawing position at the begining of the line? */
    public boolean isBOL();

    /** Is current drawing position at the end of the line */
    public boolean isEOL();

    /** Get draw info for the component that is currently drawn. */
    public ExtUI getExtUI();

    /** Get the buffer with the characters being drawn. No changes can
    * be done in characters in the buffer.
    */
    public char[] getBuffer();

    /** Get token type number according to the appropriate
    * syntax scanner */
    public int getToken();

    /** Get starting offset in the buffer of the token being drawn */
    public int getTokenStart();

    /** Get length of the token text */
    public int getTokenLength();

}

/*
 * Log
 *  6    Gandalf-post-FCS1.4.1.0     3/8/00   Miloslav Metelka 
 *  5    Gandalf   1.4         10/23/99 Ian Formanek    NO SEMANTIC CHANGE - Sun
 *       Microsystems Copyright in File Comment
 *  4    Gandalf   1.3         7/20/99  Miloslav Metelka 
 *  3    Gandalf   1.2         6/25/99  Miloslav Metelka from floats back to ints
 *  2    Gandalf   1.1         5/5/99   Miloslav Metelka 
 *  1    Gandalf   1.0         4/23/99  Miloslav Metelka 
 * $
 */

